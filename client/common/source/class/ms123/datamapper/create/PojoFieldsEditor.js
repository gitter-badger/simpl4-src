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
/*
*/

qx.Class.define("ms123.datamapper.create.PojoFieldsEditor", {
	extend: ms123.datamapper.edit.AbstractFieldsEditor,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade, data) {
		this._facade=facade;
		if( !facade.mainEntity){
			var title = this.tr("datamapper.define") + this.tr("Pojo");
			this.base(arguments,facade,null,title,data);
		}
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createForm:function(){
			var formData = {
				"entity": {
					'type': "ResourceSelector",
					'label': this.tr("datamapper.entity"),
					'validation': {
						required: true
					},
					'config': {
						'type': 'sw.entitytype',
						'showTextField':false,
						'editable':false
					},
					'value': ""
				},
				"type": {
					'type': "SelectBox",
					'label': this.tr("datamapper.type"),
					'value': ms123.datamapper.Config.NODETYPE_COLLECTION,
					'options': [{
						value: ms123.datamapper.Config.NODETYPE_ELEMENT,
						label: "Element"
					},{
						value: ms123.datamapper.Config.NODETYPE_COLLECTION,
						label: "List<Element>"
					}]
				}
			};
			return this.__createForm(formData, "single");
		},
		getValue: function () {
			var data = qx.lang.Json.parse(qx.util.Serializer.toJson(this._form.getData()));
			var model = this.createModel(data.entity,data.type);	
			return model;
		},
		setValue: function (value) {
			if( value != null && value.entity){
				this._form.setData({entity:value.entity,type:value.type});
			}
		},
		createModel:function(entity,type){
			var cm = new ms123.config.ConfigManager();
			var entityTree = cm.getEntityTree(this._facade.storeDesc,entity,4,false);
			var newTree = this._traverse( entityTree,{}, type );
			newTree.entity = entity;
			return newTree;
		},
		_traverse: function (model,visited,type) {
			var newModel = {};
			newModel.name = model.name;
			var entity = model.name;
			
			newModel.type = type;
			if(model.datatype == "list" || model.datatype == "map" ){
				entity = model.entity;
				newModel.type = ms123.datamapper.Config.NODETYPE_COLLECTION;
				newModel.fieldType=entity;
			}
			visited[entity] = true;
			newModel.children = this._getFieldsForEntity(model.entity);
			for (var i = 0; model.children && i < model.children.length; i++) {
				var c = model.children[i];
				if( visited[c.name] !== true && visited[c.entity] !== true){
					visited[c.name] = true;
					var m = this._traverse(c,visited,ms123.datamapper.Config.NODETYPE_ELEMENT);
					newModel.children.push(m);
				}
			}
			return newModel;
		},
		_getFieldsForEntity: function (entity) {
			var cm = new ms123.config.ConfigManager();
			var fields = cm.getFields(this._facade.storeDesc,entity,false,false);
			fields = this._mapToList(fields);
			this._sortFieldList(fields);
			var fieldList = [];
			for (var f = 0; f < fields.length; f++) {
				var name = fields[f].name;
				var datatype = fields[f].datatype;
				if (!name || name.match("^_")) continue;
				if (!datatype) continue;
				if( datatype.match(/^related/)) continue;
				var o = {};
				o[ms123.datamapper.Config.NODENAME] = name;
				o[ms123.datamapper.Config.FIELDTYPE] = datatype;
				o[ms123.datamapper.Config.NODETYPE] = ms123.datamapper.Config.NODETYPE_ATTRIBUTE;
				fieldList.push(o);
			}
			return fieldList;
		},
		_mapToList: function (map) {
			var ret = [];
			for( var key in map ){
				ret.push( map[key] );
			}
			return ret;
		},
		_sortFieldList: function (list) {
			list.sort(function (a, b) {
				a = a.name.toLowerCase();
				b = b.name.toLowerCase();
				if (a < b) return -1;
				if (a > b) return 1;
				return 0;
			});
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
