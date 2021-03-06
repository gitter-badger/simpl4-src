/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @ignore(Hash)
 */

qx.Class.define("ms123.datamapper.plugins.EntityCreate", {
	extend: qx.core.Object,
	include: [qx.locale.MTranslation],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade,context,dest) {
		this.base(arguments);
		this._facade = facade;
		this._side = context.side;

		var ec_msg = null;
		this.isOrientDB = false;
		if( "orient" == dest ){
			ec_msg = this.tr("datamapper.entitytypes_create_orient");
			this.isOrientDB = true;
		}else{
			ec_msg = this.tr("datamapper.entitytypes_create_nucleus");
		}
		this._pack = this.isOrientDB ? "odata" : this._facade.storeDesc.getPack();;
		var group = "4";
		this._facade.offer({
			name: ec_msg,
			description: ec_msg,
			icon: "resource/ms123/table.png",
			functionality: this.create.bind(this),
			group: group,
			isEnabled: qx.lang.Function.bind(function () {
				return true;
			}, this),
			index: 0
		});


	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {},
	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		create: function () {
			var allList = this._toStringList(this._getEntitytypes());
			var ret = this._createEntitytypes(true,null);
			var newList = this._toStringList(ret.entityList);
			this._existsList = this._getExistsList(allList, newList);

			var message=null;
			if( this.isOrientDB){
				message = "<b>" + this.tr("datamapper.entitytypes_create_orient") + "</b><br/><br/>";
			}else{
				message = "<b>" + this.tr("datamapper.entitytypes_create_nucleus") + "</b><br/><br/>";
			}
			message += this.tr("datamapper.classes_exists") + ":<br/>";
			if( this._existsList.length >0){
				message += "<ul>";
				for (var i = 0; i < this._existsList.length; i++) {
					message += "<li>" + this._existsList[i] + "</li>";
				}
				message += "</ul>";
			}else{
				message += "<div>-----</div>";
			}
			this._createForm(message,newList);
		},
		_getExistsList: function (allList, newList) {
			var retList = [];
			for (var i = 0; i < newList.length; i++) {
				var entity = newList[i];
				if (allList.indexOf(entity) != -1 ) {
					retList.push(entity);
				}
			}
			return retList;
		},
		_toStringList: function (mapList) {
			var stringList = [];
			for (var i = 0; i < mapList.length; i++) {
				stringList.push(mapList[i].name);	
			}
			return stringList;
		},
		_getEntitytypes: function () {
			var storeId = this._facade.storeDesc.getStoreId();
			if( this.isOrientDB){
				storeId = this._facade.storeDesc.getNamespace()+"_"+ this._pack;
			}
			try {
				var ret = ms123.util.Remote.rpcSync("entity:getEntitytypes", {
					storeId: storeId
				});
				return ret;
			} catch (e) {
				ms123.form.Dialog.alert("Datamapper.getEntitytypes failed:" + e.message);
				return null;
			}
		},
		_getEntitytypeInfo: function (nameList) {
			try {
				var ret = ms123.util.Remote.rpcSync("entity:getEntitytypeInfo", {
					storeId: this._facade.storeDesc.getStoreId(),
					names:nameList
				});
				return ret;
			} catch (e) {
				ms123.form.Dialog.alert("Datamapper.getEntitytypeInfo failed:" + e.message);
				return null;
			}
		},
		_createEntitytypes: function (infoOnly,strategy) {
			var storeId = this._facade.storeDesc.getStoreId();
			if( this.isOrientDB){
				storeId = this._facade.storeDesc.getNamespace()+"_"+ this._pack;
			}
			try {
				var ret = ms123.util.Remote.rpcSync("entity:createEntitytypes", {
					storeId: storeId,
					datamapperConfig: this._facade.getConfig(),
					side: this._side,
					strategy: strategy,
					isOrientDB: this.isOrientDB,
					infoOnly: infoOnly
				});
				return ret;
			} catch (e) {
				ms123.form.Dialog.alert("Datamapper.createEntitytypes failed:" + e.message);
				return null;
			}
		},
		_removeSettings: function () {
			var etList = this._getEntitytypeInfo(this._existsList);
			var namespace= this._facade.storeDesc.getNamespace();
			var lang= ms123.config.ConfigManager.getLanguage();
			var ds = new ms123.entitytypes.DefaultSettings(namespace,this._pack, lang);
			ds.deleteMessages(etList);
			ds.deleteResources(etList);	
		},
		_createSettings: function (etList) {
			var namespace= this._facade.storeDesc.getNamespace();
			var lang= ms123.config.ConfigManager.getLanguage();
			var ds = new ms123.entitytypes.DefaultSettings(namespace,this._pack, lang);
			ds.createMessages(etList);
			ds.createResources(etList);	
		},
		_createClasses:function(){
			try {

			var namespace= this._facade.storeDesc.getNamespace();
				ms123.util.Remote.rpcSync("domainobjects:createClasses", {
					storeId: namespace+ "_"+ this._pack 
				});
			} catch (e) {
				ms123.form.Dialog.alert("EntityCreate.createClasses:" + e);
				return;
			}
		},
		_dropVertices: function (onlyDelete) {
			if( this._existsList.length==0) return;
			try {
				var namespace= this._facade.storeDesc.getNamespace();
				var result = ms123.util.Remote.rpcSync("orientdb:dropVertices", {
					onlyDelete:onlyDelete,
					databaseName: namespace,
					vertexList: this._existsList
				});
			} catch (e) {
				ms123.form.Dialog.alert("EntityCreate.dropVertices:" + e);
				return;
			}
		},
		_cleanTables: function () {
			if( this._existsList.length==0) return;
			try {
				var namespace= this._facade.storeDesc.getNamespace();
				var result = ms123.util.Remote.rpcSync("nucleus:schemaTool", {
					storeId: namespace +"_data",//this._facade.storeDesc.getStoreId()
					dry: false,
					classes: this._existsList,
					op: "delete"
				});
			} catch (e) {
				ms123.form.Dialog.alert("DatabaseAdmin._cleanTable:" + e);
				return;
			}
		},
		_doAll: function (options) {
			var kind = options.get("kind");
			var createSettings = options.get("createSettings");
			if (kind == "overwrite") {
				if( this.isOrientDB){
					this._dropVertices(false);
				}else{
					this._cleanTables();
				}
			}
			if (kind == "onlyDelete") {
				if( this.isOrientDB){
					this._dropVertices(true);
				}
			}
			var ret = this._createEntitytypes(false,options.get("entities"));
			if (createSettings) {
				this._createSettings(ret.entityList);
			}
			this._createClasses(false);
			ms123.config.ConfigManager.clearCache();
		},
		_getEntityEditConfig: function () {
			var items = [
				{
				id: "entityname",
				width: 300,
				type: "TextField",
				name: "%datamapper.entity"
			}, {
				id: "create",
				type: "CheckBox",
				width: 60,
				name: "%datamapper.entity_create"
			}, {
				id: "depend",
				type: "CheckBox",
				width: 60,
				name: "%datamapper.entity_depend"
			}];
			return items;
		},
		_createForm: function (message,entityList) {
			var evalue = [];
			for( var i=0; i < entityList.length;i++){
				evalue.push({entityname:entityList[i], create:true,depend:true});
			}
			var svalue = JSON.stringify(evalue);
			var buttons = [{
				'label': this.tr("datamapper.generate_classes"),
				'icon': "icon/22/actions/dialog-ok.png",
				'value': 1
			},
			{
				'label': this.tr("composite.select_dialog.cancel"),
				'icon': "icon/22/actions/dialog-cancel.png",
				'value': 2
			}];
			var formData = {
				"entities": {
          'rowflex':1,
					'height': 200,
          'type': "ComplexEdit",
          'caption': this.tr("datamapper.new_classes"),
					'toolbar':false,
          'config':this._getEntityEditConfig(),
          'value':svalue
        },
				"createSettings": {
					'type': "CheckBox",
					'label': this.tr("datamapper.with_msgs_and_settings"),
					'value': true
				},
				"kind": {
					'height': 52,
					'type': "RadioGroup",
					'label': this.tr("datamapper.exist_classes"),
					'value': "overwrite",
					'options': [{
						value: "overwrite",
						label: this.tr("datamapper.overwrite_classes")
					},
					{
						value: "change",
						label: this.tr("datamapper.change_classes")
					}]
				}
			};

			if( this.isOrientDB){
				formData.createSettings.value=false;
				formData.kind.value="onlyDelete";
				formData.kind.options.push({
					value: "onlyDelete",
					label: this.tr("datamapper.only_delete_data")
				});
			}

			var self = this;
			var form = new ms123.form.Form({
				"buttons": buttons,
				"tabs": [{
					id: "tab1",
					layout: "single"
				}],
				"useScroll": false,
				"formData": formData,
				"hide": false,
				"width": 650,
				"message": message,
				"inWindow": true,
				"callback": function (m, v) {
					if (m !== undefined) {
						form.hide();
						if (v == 1) {
							self._doAll(m);
						} else if (v == 2) {}
					}
				},
				"context": self
			});
			form.show();
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
