/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
	@ignore($)
	@ignore(jQuery.trim)
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/places/*)
*/

/**
 */
qx.Class.define( "ms123.entitytypes.OrientDBEntitytypeCreate", {
	extend: ms123.entitytypes.RDBMSEntitytypeCreate,

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function( model, param, facade ) {
		this.base( arguments, model, param, facade );
		console.log( "OrientDBEntitytypeCreate" );
		var pack = model.getPack();
		this._pack = model.getPack();
//		this.storeDesc = ms123.StoreDesc.getNamespaceDataStoreDesc(pack);
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createColumnModel: function() {
			this._createEntitytypeList();
			this._createVertexTypeList();
			var columnmodel = [ {
					name: "name",
					type: "TextField",
					label: this.tr( "data.field.name" ),
					//readonly: !this._isNew,
					readonly: true, 
					width: 120,
					validation: {
						required: true,
						filter: /[a-zA-Z0-9_]/,
						validator: "/^[a-zA-Z_][0-9a-z_A-Z]{2,64}$/"
					},
					'value': ""
				}, {
					name: "description",
					type: "TextField",
					readonly: true, 
					label: this.tr( "aid.field.description" ),
					width: 120,
					'value': ""
				}, {
					name: "datatype",
					type: "SelectBox",
					value: "7",
					readonly: true, 
					enabled: '!edgeconn',
					options: this._dataTypeList,
					label: this.tr( "Type" )
				}, {
					name: "linkedtype",
					type: "SelectBox",
					value: "",
					readonly: true, 
					enabled: '!(edgeconn || linkedclass!="" || (datatype!="10" && datatype!="12" && datatype!="11"))',
					options: this._linkedTypeList,
					label: this.tr( "Linked Type" )
				}, {
					name: "linkedclass",
					type: "SelectBox",
					value: "",
					readonly: true, 
					enabled: '!(edgeconn || linkedtype!="" || (datatype!="9" && datatype!="10" && datatype!="12" && datatype!="11" && datatype!="13" && datatype!="14" && datatype!="15" && datatype!="16"))',
					options: this._entitytypeList,
					label: this.tr( "Linked Class" )
				}, {
					name: "edittype",
					readonly: true, 
					type: "SelectBox",
					value: "",
					enabled: '!(edgeconn || linkedtype!="" || linkedclass!="")',
					options: this._editTypeList,
					label: this.tr( "aid.field.edittype" )
				}, {
					name: "primary_key",
					type: "CheckBox",
					value: false,
					enabled: '!edgeconn',
					label: this.tr( "aid.field.primary_key" )
				}, {
					name: "edgeconn",
					type: "CheckBox",
					value: false,
					label: this.tr( "Edge Connection" )
				}, {
					name: "vertexclass",
					type: "SelectBox",
					value: "",
					readonly: true, 
					enabled: 'edgeconn',
					options: this._vertexList,
					label: this.tr( "Vertex Class" )
				}, {
					name: "vertextype",
					type: "SelectBox",
					value: "single",
					readonly: true, 
					enabled: 'edgeconn',
					options: this._vertexTypeList,
					label: this.tr( "Vertex Type" )
				}, {
					name: "edgeclass",
					type: "SelectBox",
					value: "",
					readonly: true, 
					enabled: 'edgeconn',
					options: this._edgeList,
					label: this.tr( "Edge Class" )
				}, {
					name: "enabled",
					type: "CheckBox",
					value: true,
					width: 30,
					label: this.tr( "aid.field.enabled" )
				}
			];
			this._columnModel = this._translate( columnmodel );
			return this._columnModel;
		},
		_createClassForm: function() {
			this._createSuperClassList();
			var formData = {
				"name": {
					'type': "TextField",
					'label': this.tr( "data.entity.name" ),
					'readonly': !this._isNew,
					'validation': {
						required: true,
						filter: /[a-zA-Z0-9]/,
						validator: "/^[a-z][0-9a-zA-Z_]{2,64}$/"
					},
					'value': ""
				},
				"description": {
					'type': "TextField",
					'label': this.tr( "data.entity.description" ),
					'validation': {
						required: false
					},
					'value': ""
				},
				"superclass": {
					'type': "SelectBox",
					'label': this.tr( "Superclass" ),
					'value': "vertex",
					'options': this._superclassList
				},
				"from": {
					'type': "SelectBox",
					'label': this.tr( "From" ),
					'exclude': 'superclass != "edge"',
					'value': "",
					'options': this._vertexList
				},
				"to": {
					'type': "SelectBox",
					'label': this.tr( "To" ),
					'exclude': 'superclass != "edge"',
					'value': "",
					'options': this._vertexList
				}, 
				"restricted": {
					'type': "CheckBox",
					'value': false,
					'label': this.tr( "Restricted" )
				}
			};

			var self = this;
			var context = {};
			context.formData = formData;
			context.buttons = [];
			context.formLayout = [{
				id: "tab1"
			}];
			var form = new ms123.widgets.Form(context);
			this._classForm = form;
			if ( this._etdata ) {
				this._classForm.setData( this._etdata );
			}
			return form;
		},

		_createFieldEdit: function() {},
		_getRelations: function() {},
		_createDatatypeList: function() {
			this._dataTypeList = [ {
				'value': "",
				'label': ""
			}, {
				'value': "0",
				'label': "BOOLEAN"
			}, {
				'value': "1",
				'label': "INTEGER"
			}, {
				'value': "2",
				'label': "SHORT"
			}, {
				'value': "3",
				'label': "LONG"
			}, {
				'value': "4",
				'label': "FLOAT"
			}, {
				'value': "5",
				'label': "DOUBLE"
			}, {
				'value': "6",
				'label': "DATETIME"
			}, {
				'value': "7",
				'label': "STRING"
			}, {
				'value': "9",
				'label': "EMBEDDED"
			}, {
				'value': "10",
				'label': "EMBEDDEDLIST"
			}, {
				'value': "11",
				'label': "EMBEDDEDSET"
			}, {
				'value': "12",
				'label': "EMBEDDEDMAP"
			}, {
				'value': "13",
				'label': "LINK"
			}, {
				'value': "14",
				'label': "LINKLIST"
			}, {
				'value': "15",
				'label': "LINKSET"
			}, {
				'value': "16",
				'label': "LINKMAP"
			}, {
				'value': "17",
				'label': "BYTE"
			}, {
				'value': "19",
				'label': "DATE"
			}, {
				'value': "21",
				'label': "DECIMAL"
			} ]
			this._linkedTypeList = ms123.util.Clone.clone(this._dataTypeList);
			this._linkedTypeList.unshift( { 'value': '', 'label': '' } );
		},
		_createEdittypeList: function() {
			this._editTypeList = [ {
				"value": "",
				"label": ""
			}, {
				"value": "text",
				"label": "Textfield"
			}, {
				"value": "select",
				"label": "SelectBox"
			}, {
				"value": "checkbox",
				"label": "Checkbox"
			}, {
				"value": "date",
				"label": "Date"
			}, {
				"value": "datetime",
				"label": "DateTime"
			}, {
				"value": "textarea",
				"label": "Textarea"
			}, {
				"value": "multiselect",
				"label": "DoubleSelectBox"
			}, {
				"value": "auto",
				"label": "Autoincrement"
			}, {
				"value": "functional",
				"label": "Computed"
			} ];
		},
		_createVertexTypeList: function() {
			this._vertexTypeList = [ {
				"value": "single",
				"label": "Single"
			}, {
				"value": "list",
				"label": "List"
			}, {
				"value": "set",
				"label": "Set"
			}, {
				"value": "map",
				"label": "Map"
			} ];
		},
		_createSuperClassList: function() {
			this._superclassList = [ {
				"value": "vertex",
				"label": "Vertex"
			}, {
				"value": "edge",
				"label": "Edge"
			}, {
				"value": "orientDocument",
				"label": "Document"
			} ];
		},
		_setEntityProperties: function( entity ) {
			var failed = ( function( details ) {
				ms123.form.Dialog.alert( this.tr( "entitytypes.addSettings" ) + ":" + details.message );
			} ).bind( this );

			try {
				entity = ms123.settings.Config.getFqEntityName( entity, this.storeDesc );
				var ret = ms123.util.Remote.rpcSync( "setting:setResourceSetting", {
					namespace: this.storeDesc.getNamespace(),
					settingsid: "global",
					resourceid: "entities." + entity + ".properties",
					overwrite: false,
					settings: {
						"add_self_to_subpanel": false,
						"multi_add": false,
						"multiple_tabs": false,
						"teams_in_subpanel": false,
						"sidebar": false,
						"state_select": false,
						"exclusion_list": true
					}
				} );
			} catch ( e ) {
				failed.call( this, e );
				return;
			}
		},
		_createEntitytypeList: function () {
			this._entitytypeList = this._getEntitytypes();
			this._vertexList = this._getEdgeVertexList("vertex");
			this._edgeList = this._getEdgeVertexList("edge");
		},
		_getEdgeVertexList: function (edgeOrVertex) {
			var ret = [{value:"",label:""}];
			for( var i=0; i < this._rawEntityList.length;i++){
				var e = this._rawEntityList[i];
				if( e.superclass == edgeOrVertex){
					var o = {};
					o.value = e.name;
					o.label = this._capitaliseFirstLetter(e.name);
					ret.push(o );
				}
			}
			return ret;
		},
		_getEntitytypes: function () {
			var completed = (function (data) {}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("entitytypes.getEntitytypes_failed") + ":" + details.message);
			}).bind(this);

			try {
				var ret = ms123.util.Remote.rpcSync("entity:getEntitytypes", {
					storeId: this.storeDesc.getStoreId()
				});
				completed.call(this, ret);
				this._rawEntityList = ret;
				var retList = [{value:"",label:""},{value:"Object",label:"Object"}];
				for (var i = 0; i < ret.length; i++) {
					var o = {};
					o.value = ret[i].name;
					o.label = this._capitaliseFirstLetter(ret[i].name);
					retList.push(o);
				}

				var value = qx.lang.Json.stringify(retList, null, 4);
				console.log("retList:" + value);
				return retList;
			} catch (e) {
				failed.call(this, e);
				return;
			}
		},
		isEmpty: function (s) {
			if (!s || jQuery.trim(s) == '') return true;
			return false;
		},
		_createAddForm: function () {
			var formData = {};
			for (var i = 0; i < this._columnModel.length; i++) {
				var col = this._columnModel[i];
				col = ms123.util.Clone.clone(col);
				col.readonly= false;
				formData[col.name] = col;
			}
			var self = this;
			var buttons = [{
				'label': this.tr("entitytypes.attribute_takeit"),
				'icon': "icon/22/actions/dialog-ok.png",
				'callback': function (m) {
					var validate = self._addForm.validate();
					console.error("validate:" + validate);
					console.error("m:" , m);
					if (!validate) {
						var vm = self._addForm.getValidationManager();
						var items = vm.getInvalidFormItems();
						for (var i = 0; i < items.length; i++) {
							items[i].setValid(false);
						}
						ms123.form.Dialog.alert(self.tr("widgets.table.form_incomplete"));
						return;
					}
					if( m.edgeconn === true && (self.isEmpty(m.edgeclass) || self.isEmpty(m.vertexclass))){
						ms123.form.Dialog.alert(self.tr("Edge or Vertex is missing"));
						return;
					}
					var map = {};
					qx.lang.Object.mergeWith(map, m);
					if (self._isEditMode) {
						self._tableModel.setRowsAsMapArray([m], self._currentTableIndex, true);
						self._propertyEditWindow.close();
					} else {
						if (m["primary_key"] === "") m["primary_key"] = false;
						var value = qx.lang.Json.stringify(m, null, 4);
						console.log("m:" + value);
						self._tableModel.addRowsAsMapArray([m], null, true);
						self._currentForm.fillForm(self._getDefaultValues());
					}
				},
				'value': "save"
			}];

			var context = {};
			context.formData = formData;
			context.buttons = buttons;
			context.formLayout = [{
				id: "tab1"
			}];
			this._addForm = new ms123.widgets.Form(context);
			this.__changeListener();
			this.__addChangeListener();
			return this._addForm;
		},
		__changeListener: function (e) {
			var form = this._addForm.form;
			var m = form.getModel();
			var props = qx.Class.getProperties(m.constructor);
			var datatype = m.get("datatype");
			var edgeconn = m.get("edgeconn");
			var linkedtype = m.get("linkedtype");
			var linkedclass = m.get("linkedclass");
			var edittype = m.get("edittype");
			if( linkedclass!="" || (datatype!="10" && datatype!="12" && datatype!="11")){
				m.set("linkedtype", "");			
			}else{
				m.set("edittype", "");			
			}
			if( (linkedtype!="" || (datatype!="9" && datatype!="10" && datatype!="12" && datatype!="11" && datatype!="13" && datatype!="14" && datatype!="15" && datatype!="16"))){
				m.set("linkedclass", "");			
			}else{
				m.set("edittype", "");			
			}
			if( edgeconn ){
				m.set("datatype", "");			
			}
		},
		__removeChangeListener: function () {
			var m = this._addForm.form.getModel();
			m.removeListener("changeBubble", this.__changeListener, this);
		},
		__addChangeListener: function () {
			var m = this._addForm.form.getModel();
			m.addListener("changeBubble", this.__changeListener, this);
		},
		_onDblClick: function (e) {
				this._isEditMode = true;
				this._table.stopEditing();
				if (this._currentForm) {
					this._propertyEditWindow.remove(this._currentForm);
				}
				this._currentForm = this._createAddForm();
				var selModel = this._table.getSelectionModel();
				var index = selModel.getLeadSelectionIndex();
				var map = this._tableModel.getRowDataAsMap(index);

				this.__removeChangeListener();
				this._currentForm.fillForm(map);
				this.__addChangeListener();
				this._propertyEditWindow.add(this._currentForm);
				this._propertyEditWindow.setActive(true);
				this._propertyEditWindow.open();
		},
		_createPropertyEdit: function (tableColumns) {
			this._propertyEditWindow = this._createPropertyEditWindow(400);
		},
		_createClasses:function(mess){
			try {
				ms123.util.Remote.rpcSync("domainobjects:createClasses", {
					storeId: this.storeDesc.getStoreId()
				});
				if( mess ) ms123.form.Dialog.alert(this.tr("entitytypes.update_db_successfull"));
			} catch (e) {
				ms123.form.Dialog.alert(e);
				return;
			}
		},
		_createOptionForm: function() {
			var buttons = [ {
				'label': this.tr( "entitytypes.generate_class" ),
				'icon': "icon/22/actions/dialog-ok.png",
				'value': 1
			}, {
				'label': this.tr( "composite.select_dialog.cancel" ),
				'icon': "icon/22/actions/dialog-cancel.png",
				'value': 2
			} ];
			var formData = {
				create_messages: {
					name: "create_messages",
					type: "CheckBox",
					value: false,
					label: this.tr( "entitytypes.create_messages" )
				},
				create_settings_form: {
					name: "create_settings_form",
					type: "CheckBox",
					value: false,
					label: this.tr( "entitytypes.create_form_settings" )
				},
				create_settings_table: {
					name: "create_settings_table",
					type: "CheckBox",
					value: false,
					label: this.tr( "entitytypes.create_table_settings" )
				},
				create_settings_search: {
					name: "create_settings_search",
					type: "CheckBox",
					value: false,
					label: this.tr( "entitytypes.create_search_settings" )
				},
				create_classes: {
					name: "create_classes",
					type: "CheckBox",
					value: true,
					label: this.tr( "entitytypes.update_db" )
				}
			};

			var self = this;
			var form = new ms123.form.Form( {
				"buttons": buttons,
				"tabs": [ {
					id: "tab1",
					layout: "single"
				} ],
				"useScroll": false,
				"formData": formData,
				"hide": false,
				"inWindow": true,
				"callback": function( m, v ) {
					if ( m !== undefined ) {
						form.hide();
						if ( v == 1 ) {
							self._saveAll( self._classForm.getData(), self._getRecords(), m );
						} else if ( v == 2 ) {}
					}
				},
				"context": self
			} );
			form.show();
		}
	}
} );
