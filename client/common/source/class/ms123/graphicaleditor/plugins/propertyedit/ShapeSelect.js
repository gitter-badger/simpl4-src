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
qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.ShapeSelect", {
	extend: qx.core.Object,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (facade, formElement, config) {
		this.base(arguments);
		this._facade = facade;
		if (config.criteria.fileType) {
			this._remote(formElement, config);
		} else {
			this._local(formElement, config);
		}
	},
	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_remote: function (formElement, config) {
			var self = this;
			var completed = function(shapeList){
console.log("_REMOTE:"+JSON.stringify(shapeList));
				for (var i = 0; i < shapeList.length; i++) {
					var map = shapeList[i];
					var s = "";
					var delim = "";
					var ok = true;
					for (var j = 0; j < config.propertyNames.length; j++) {
						if( self._isEmpty(map[config.propertyNames[j]])) ok = false;
						s += delim + map[config.propertyNames[j]];
						delim = config.delimeter;
					}
					if(ok){
						var item = new qx.ui.form.ListItem(s, null, s);
console.log("item_add:"+item);
						formElement.addItem(item);
					}
				}
			}
			var failed = function(e){
				ms123.form.Dialog.alert("ShapeSelect.construct:" + e);
			}
			var rpcParams = {
				namespace: this._facade.storeDesc.getNamespace(),
				criteria: config.criteria,
				propertyNames: config.propertyNames
			}

			var params = {
				service: "docbook",
				method: "shapePropertiesList",
				parameter: rpcParams,
				async: true,
				completed:completed,
				failed:failed,
				context: this
			}
			ms123.util.Remote.rpcAsync(params);
		},
		_local: function (formElement, config) {
			var json = this._facade.getJSON();
			var shapeList = [];
			var stencilList=[];
			var stencilSets = this._facade.getStencilSets();

			var criteria = config.criteria;
			stencilSets.values().each(function (stencilSet) {
				var nodes = stencilSet.nodes();
				nodes.each(function (stencil) {
					var sRoles = stencil.roles();
					for( var i=0; i< criteria.roles.length;i++){
						if( sRoles.indexOf(stencil.namespace()+criteria.roles[i])!=-1){
							stencilList.push(stencil.idWithoutNs().toLowerCase());
							break;
						}
					}
				});
			});

			this._getShapeList(shapeList, json, null, criteria, config.propertyNames, stencilList);
			for (var i = 0; i < shapeList.length; i++) {
				var map = shapeList[i];
				var s = "";
				var delim = "";
				var ok = true;
				for (var j = 0; j < config.propertyNames.length; j++) {
					if( this._isEmpty(map[config.propertyNames[j]])) ok=false;
					s += delim + map[config.propertyNames[j]];
					delim = config.delimeter;
				}
				if(ok){
					var item = new qx.ui.form.ListItem(s, null, s);
					formElement.add(item);
				}
			}
		},

		_isEmpty:function(s){
			if( s==null || s.length==0) return true;
			return false;
		},
		_isCriteriaOk: function (shape, parentShape, criteria, stencilList) {
			var parentStencil = criteria.parentStencil;
			if (parentShape && parentShape.stencil.id.toLowerCase() != parentStencil.toLowerCase()) {
				return false;
			}
			if (stencilList.indexOf(shape.stencil.id.toLowerCase()) != -1) {
				return true;
			}
			return false;
		},

		_getShapeList: function (shapeList, shape, parentShape, criteria, propertyNames, stencilList) {
			var childShapes = shape.childShapes;
			var properties = shape.properties;
			if (this._isCriteriaOk(shape, parentShape, criteria, stencilList)) {
				var map = {};
				for (var i = 0; i < propertyNames.length; i++) {
					var pname = propertyNames[i];
					map[pname] = properties[pname];
				}
				shapeList.push(map);
			}
			for (var i = 0; i < childShapes.length; i++) {
				var child = childShapes[i];
				this._getShapeList(shapeList, child, shape, criteria, propertyNames, stencilList);
			}
		}
	}
});
