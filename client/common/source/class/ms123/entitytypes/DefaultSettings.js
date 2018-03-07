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
 */
qx.Class.define("ms123.entitytypes.DefaultSettings", {
	extend: qx.core.Object,
	include: [qx.locale.MTranslation],

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (namespase,pack, language) {
		this._namespace = namespase;
		this._pack = pack;
		this._language = language;
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		createResources:function(_etList,cm,sf,st,ss){
			cm = cm!=null ? cm : true;
			sf = sf!=null ? sf : true;
			st = st!=null ? st : true;
			ss = ss!=null ? ss : true;
			var etList = _etList;
			if( !Array.isArray(_etList)){
				etList = [_etList];
			}
			for( var i=0; i< etList.length;i++){
				var et = etList[i];
				var fieldSettings = {
					fields: []
				}

				var fields = et.fields;
				var keys = Object.keys(fields);
				for (var j = 0; j < keys.length; j++) {
					var key = keys[j];
					var f = fields[key];
					var msgid = this._pack+"." + et.name + "." + f.name;

					var sfield = {
						enabled: true,
						name: f.name,
						tab: "tab1",
						displayname: cm ? this._capitaliseFirstLetter(f.name) : msgid
					}
					fieldSettings.fields.push(sfield);
				}

				var enName =  ms123.settings.Config.getFqEntityName(et.name,this._pack);
				if (sf) this._setResourceSetting("entities." + enName + ".views.main-form.fields", fieldSettings);
				if (st) this._setResourceSetting("entities." + enName + ".views.main-grid.fields", fieldSettings);
				if (ss) this._setResourceSetting("entities." + enName + ".views.search.fields", fieldSettings);
			}
		},
		deleteResources: function (_etList) {
			var etList = _etList;
			if( !Array.isArray(_etList)){
				etList = [_etList];
			}
			var resourceRegex = "";
			var or = "";
			for( var i=0; i< etList.length;i++){
				var et = etList[i];
				resourceRegex += or + "(^entities\\."+et.name+"\\..*)"
				or = "|";
			}

			var failed = (function (details) {
				ms123.form.Dialog.alert("DeleteSettings:" + details.message);
			}).bind(this);

			try {
				 ms123.util.Remote.rpcSync("setting:deleteResourceSetting", {
					namespace: this._namespace,
					settingsid: "global",
					resourceid: resourceRegex
				});
			} catch (e) {
				failed.call(this, e);
				return;
			}
		},
		deleteMessages: function (_etList) {
			var etList = _etList;
			if( !Array.isArray(_etList)){
				etList = [_etList];
			}

			var messages = [];
			for( var i=0; i< etList.length;i++){
				var et = etList[i];
				var fields = et.fields;
				var msgid = this._pack+"." + et.name;
				messages.push(msgid);
				var keys = Object.keys(fields);
				for (var j = 0; j < keys.length; j++) {
					var key = keys[j];
					var f = fields[key];
					msgid = this._pack+"." + et.name + "." + f.name;
					messages.push(msgid);
				}
				this._deleteMessage(messages,  this._pack+"."+et.name+"._team_list");
			}
			var failed = (function (details) {
				ms123.form.Dialog.alert("DeleteMessages:" + details.message);
			}).bind(this);

			try {
				var ret = ms123.util.Remote.rpcSync("message:deleteMessages", {
					namespace: this._namespace,
					lang: this._language,
					msgIds: messages
				});
			} catch (e) {
				failed.call(this, e);
				return;
			}
		},
		createMessages: function (_etList) {
			var etList = _etList;
			if( !Array.isArray(_etList)){
				etList = [_etList];
			}
			var messages = [];
			for( var i=0; i< etList.length;i++){
				var et = etList[i];
				var fields = et.fields;
				var msg = {
					msgid: this._pack+"."+et.name,
					msgstr: this._capitaliseFirstLetter(et.name)
				}
				messages.push(msg);
				msg = {
					msgid: this._pack+"." + et.name + ".id",
					msgstr: "Id"
				}
				messages.push(msg);

				var keys = Object.keys(fields);
				for (var j = 0; j < keys.length; j++) {
					var key = keys[j];
					var f = fields[key];
					var msgid = this._pack+"." + et.name + "." + f.name;

					var msg = {
						msgid: msgid,
						msgstr: this._capitaliseFirstLetter(f.name)
					}
					messages.push(msg);
				}
				this._createMessage(messages,  this._pack+"."+et.name+"._team_list", "Teams");
			}
			var failed = (function (details) {
				ms123.form.Dialog.alert("entitytypes.createMessages:" + details.message);
			}).bind(this);

			try {
				var ret = ms123.util.Remote.rpcSync("message:addMessages", {
					namespace: this._namespace,
					lang: this._language,
					overwrite:false,
					msgs: messages
				});
			} catch (e) {
				failed.call(this, e);
				return;
			}
		},
		_createMessage:function(messages,key,txt){
				var msg = {
					msgid: key,
					msgstr: txt
				}
				messages.push(msg);
		},
		_deleteMessage:function(messages,key){
				messages.push(key);
		},
		_setResourceSetting: function (resourceid, settings) {
			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("entitytypes.addSettings") + ":" + details.message);
			}).bind(this);

			try {
				var curSetting = this._getResourceSetting(resourceid);
				if( curSetting && curSetting.fields){
					var curFields = curSetting.fields;
					var newFields = settings.fields;
					for( var i=0; i< newFields.length;i++){
						var newField=newFields[i];
						if(  !this._isFieldinList( curFields, newField)){
							curFields.push(newField);
						}
					}
				}else{
					curSetting = settings;
				}
				var ret = ms123.util.Remote.rpcSync("setting:setResourceSetting", {
					namespace: this._namespace,
					settingsid: "global",
					resourceid: resourceid,
					overwrite:true,
					settings: curSetting
				});
			} catch (e) {
				failed.call(this, e);
				return;
			}
		},

		_getResourceSetting: function (resourceid, settings) {
			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("entitytypes.getSettings") + ":" + details.message);
			}).bind(this);

			try {
				var ret = ms123.util.Remote.rpcSync("setting:getResourceSetting", {
					namespace: this._namespace,
					settingsid: "global",
					resourceid: resourceid
				});
				return ret;
			} catch (e) {
				failed.call(this, e);
				return;
			}
		},
		_isFieldinList:function(list, field){
			if( list == null) return false;
			for( var i=0; i< list.length;i++){
				var f=list[i];
				if(f.name == field.name){
					return true;
				}
			}
			return false;
		},
		_capitaliseFirstLetter: function (s) {
			return s.charAt(0).toUpperCase() + s.slice(1);
		}
	}
});
