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
	@ignore($)
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/places/*)
*/

/**
 */
qx.Class.define("ms123.entitytypes.RDBMSEntitytypeCreate", {
	extend: ms123.util.TableEdit,

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (model, param, facade) {
    console.log("RDBMSEntitytypeCreate");
		this._facade = facade;
		this._model = model;
		this._isNew = param.isNew;
		this._etdata = null;
		var pack = model.getPack();
		this._pack = model.getPack();
		this.storeDesc = ms123.StoreDesc.getNamespaceDataStoreDesc(pack);
		if( this.storeDesc == null){
			this.storeDesc = new ms123.StoreDesc({
				namespace:"global",
				pack:"odata"
			});
		}
		if (!this._isNew) {
			this._etdata = this._getEntitytype(this._model.getId());
			var value = qx.lang.Json.stringify(this._etdata, null, 4);
			console.log(value);
			if (!this._etdata) return;
		}
		this._createDatatypeList();
		this._createEdittypeList();
		this.base(arguments, facade);
		if (this._etdata) {
			this.__setRecords(this._etdata.fields);
		}
		this._fieldEditAllowed = true;
		if( this._isNew){
			this._fieldEditAllowed = false;
		}
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createColumnModel: function () {
			var columnmodel = [{
				name: "name",
				type: "TextField",
				label: this.tr("data.field.name"),
				//readonly: !this._isNew,
				width: 120,
				validation: {
					required: true,
					filter: /[a-z0-9_]/,
					validator: "/^[a-zA-Z][0-9a-z_A-Z]{2,64}$/"
				},
				'value': ""
			},
			{
				name: "description",
				type: "TextField",
				label: this.tr("aid.field.description"),
				width: 120,
				'value': ""
			},
			{
				name: "datatype",
				type: "SelectBox",
				width: 60,
				value: "string",
				readonly:false,//!this._isNew,
				options: this._dataTypeList,
				label: this.tr("aid.field.datatype")
			},
			{
				name: "edittype",
				type: "SelectBox",
				width: 60,
				value: "text",
				options: this._editTypeList,
				label: this.tr("aid.field.edittype")
			},
			/*{
				name: "selectable_items",
				type: "TextField",
				width: 120,
				value: "text",
				readonly:true,
				label: this.tr("data.field.selectable_items")
			},*/
			{
				name: "primary_key",
				type: "CheckBox",
				value: false,
				width: 30,
				label: this.tr("aid.field.primary_key")
			},
			{
				name: "enabled",
				type: "CheckBox",
				value: true,
				width: 30,
				label: this.tr("aid.field.enabled")
			}];
			this._columnModel = this._translate(columnmodel);
			return this._columnModel;
		},
		_doLayout: function (table, columnmodel) {
			this.base(arguments, table, columnmodel);
			var sp = this._createClassForm();
			this.add(sp, {
				edge: "north"
			});
		},
		_createClassForm: function () {
			var formData = {
				"name": {
					'type': "TextField",
					'label': this.tr("data.entity.name"),
					'readonly': !this._isNew,
					'validation': {
						required: true,
						filter: /[a-z0-9]/,
						validator: "/^[a-z][0-9a-z_]{2,64}$/"
					},
					'value': ""
				},
				"description": {
					'type': "TextField",
					'label': this.tr("data.entity.description"),
					'validation': {
						required: false
					},
					'value': ""
				},
				"tableName": {
					type: "TextField",
					label: this.tr("data.field.tablename"),
					'value': ""
				},
				"default_fields":{
					'type': "CheckBox",
					'label': this.tr("data.entity.default_fields"),
					'value': false
				},
				"state_fields":{
					'type': "CheckBox",
					'label': this.tr("data.entity.state_fields"),
					'value': false
				},
				"team_security":{
					'type': "CheckBox",
					'label': this.tr("data.entity.team_security"),
					'value': false
				}
			};

			var self = this;
			var form = new ms123.form.Form({
				"tabs": [{
					id: "tab1",
					layout: "single"
				}],
				"useScroll": false,
				"formData": formData,
				"buttons": [],
				"inWindow": false,
				"context": self
			});
			this._classForm = form;
			if (this._etdata) {
				this._classForm.setData(this._etdata);
			}
			return form;
		},

		_createFieldEdit: function () {},
		_createToolbar: function () {
			var toolbar = new qx.ui.toolbar.ToolBar();

			var buttonUpdateDb = new qx.ui.toolbar.Button(this.tr("entitytypes.update_db"), "icon/16/actions/object-rotate-right.png");
			buttonUpdateDb.addListener("execute", function () {
				this._createClasses(true);
			}, this);
			toolbar._add(buttonUpdateDb);
			toolbar.addSpacer();

			this._buttonAdd = new qx.ui.toolbar.Button("", "icon/16/actions/list-add.png");
			this._buttonAdd.addListener("execute", function () {

				this._isEditMode = false;
				this._table.stopEditing();
				if (this._currentForm) {
					this._propertyEditWindow.remove(this._currentForm);
				}
				this._currentForm = this._createAddForm();
//				this._currentForm.fillForm({});
				this._currentForm.fillForm(this._getDefaultValues());
				this._propertyEditWindow.add(this._currentForm);
				this._propertyEditWindow.setActive(true);
				this._propertyEditWindow.open();

			}, this);
			this._buttonAdd.setEnabled(true);
			toolbar._add(this._buttonAdd);

			this._buttonDel = new qx.ui.toolbar.Button("", "icon/16/actions/list-remove.png");
			this._buttonDel.addListener("execute", function () {
				this._deleteRecordAtPos(this._currentTableIndex);
			}, this);
			toolbar._add(this._buttonDel);
			this._buttonDel.setEnabled(false);

			toolbar.setSpacing(5);
			toolbar.addSpacer();

			this._buttonUp = new qx.ui.toolbar.Button("", "icon/16/actions/go-up.png");
			this._buttonUp.setToolTipText(this.tr("meta.lists.fs.up"));
			this._buttonUp.addListener("execute", function () {
				if (this._currentTableIndex == 0) return;
				var curRecord = this._getRecordAtPos(this._currentTableIndex);
				this._deleteRecordAtPos(this._currentTableIndex);
				this._insertRecordAtPos(curRecord, this._currentTableIndex - 1);
				var selModel = this._table.getSelectionModel();
				selModel.setSelectionInterval(this._currentTableIndex - 1, this._currentTableIndex - 1);
			}, this);
			toolbar._add(this._buttonUp);
			this._buttonUp.setEnabled(false);

			this._buttonDown = new qx.ui.toolbar.Button("", "icon/16/actions/go-down.png");
			this._buttonDown.setToolTipText(this.tr("meta.lists.fs.down"));
			this._buttonDown.addListener("execute", function () {
				var rc = this._tableModel.getRowCount();
				if (this._currentTableIndex >= (rc - 1)) return;
				var curRecord = this._getRecordAtPos(this._currentTableIndex);
				this._deleteRecordAtPos(this._currentTableIndex);
				this._insertRecordAtPos(curRecord, this._currentTableIndex + 1);
				var selModel = this._table.getSelectionModel();
				selModel.setSelectionInterval(this._currentTableIndex + 1, this._currentTableIndex + 1);
			}, this);
			toolbar._add(this._buttonDown);
			this._buttonDown.setEnabled(false);

			toolbar.setSpacing(5);
			toolbar.addSpacer();
			var buttonDelEntityType = new qx.ui.toolbar.Button(this.tr("entitytypes.delete"), "icon/16/places/user-trash.png");
			buttonDelEntityType.addListener("execute", function () {
				var x = this._deleteIsOk(this._classForm.getData());
				if (x) {
					ms123.form.Dialog.alert(this.tr("entitytypes.delete_not_possible") + ":" + x);
					return
				}
				this._confirmDelete();
			}, this);
			toolbar._add(buttonDelEntityType);
			if (this._isNew) {
				buttonDelEntityType.setEnabled(false);
			}

			toolbar.add(new qx.ui.core.Spacer(), {
				flex: 1
			});
			this._buttonSave = new qx.ui.toolbar.Button(this.tr("meta.lists.savebutton"), "icon/16/actions/document-save.png");
			this._buttonSave.addListener("execute", function () {
				this._save();
			}, this);
			this._buttonSave.setEnabled(true);
			toolbar._add(this._buttonSave);
			this._toolbar = toolbar;
			return toolbar;
		},
		_createPropertyEdit: function (tableColumns) {
			this._propertyEditWindow = this._createPropertyEditWindow();
		},
		_createTableListener: function (table) {
			this._tableModel = table.getTableModel();
			table.addListener("dblclick", this._onDblClick, this);
			var selModel = table.getSelectionModel();
			selModel.setSelectionMode(qx.ui.table.selection.Model.SINGLE_SELECTION);
			selModel.addListener("changeSelection", function (e) {
				var index = selModel.getLeadSelectionIndex();
				var map = this._tableModel.getRowDataAsMap(index);
				var count = selModel.getSelectedCount();
				if (count == 0) {
					if (this._buttonUp) this._buttonUp.setEnabled(false);
					if (this._buttonDown) this._buttonDown.setEnabled(false);
					if (this._buttonEdit) this._buttonEdit.setEnabled(false);
					//					if (this._buttonSave) this._buttonSave.setEnabled(false);
					if (this._buttonDel) this._buttonDel.setEnabled(false);
					return;
				}
				this._currentTableIndex = index;
				if (this._buttonUp) this._buttonUp.setEnabled(true);
				if (this._buttonDown) this._buttonDown.setEnabled(true);
				if (this._buttonEdit) this._buttonEdit.setEnabled(true);
				if (this._buttonSave) this._buttonSave.setEnabled(true);
				if (this._buttonDel) this._buttonDel.setEnabled(true);
			}, this);
		},
		_onDblClick: function (e) {
			if( !this._fieldEditAllowed){
				ms123.form.Dialog.alert(this.tr("entitytypes.class_not_created"));
				return;
			}
			var selModel = this._table.getSelectionModel();
			var index = selModel.getLeadSelectionIndex();
			if (index < 0) return;
			var data = this._tableModel.getRowDataAsMap(index);
			console.log("_onDblClick:" + data.name);
			var f = {};
			f.id = data.name;
			f.value = data.name;
			f.entitytype = this._etdata.name;
			f.title = this._etdata.name;
			f.type = "sw.field";
			f.pack = this.storeDesc.getPack();
			f.children = [];
			var fmodel = qx.data.marshal.Json.createModel(f, true);
			var param = {
				isNew:false,
				mode:"field"
			}
			var efe = new ms123.entitytypes.EntitytypeFieldEdit(fmodel,param,this._facade,data);
			efe.addListener("changeValue", function (e) {
				this._etdata = this._getEntitytype(this._model.getId());
				var value = qx.lang.Json.stringify(this._etdata, null, 4);
				console.log(value);
				if (this._etdata) {
					this._clearTable();
					this.__setRecords(this._etdata.fields);
				}
			}, this);
			var win = this._createFieldEditWindow(data.name);
			win.add(efe, {
				edge: "center"
			});
			win.open();
		},

		_deleteEntitytype: function (name) {
			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("entitytypes.deleteEntitytype_failed")+":"+details.message);
			}).bind(this);

			try {
				var ret = ms123.util.Remote.rpcSync("entity:deleteEntitytype", {
					storeId: this.storeDesc.getStoreId(),
					name: name
				});
			} catch (e) {
				failed.call(this,e);
			}
		},
		_getRelations: function () {
			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("entitytypes.getRelations_failed") + ":" + details.message);
			}).bind(this);

			try {
				var ret = ms123.util.Remote.rpcSync("entity:getRelations", {
					storeId: this.storeDesc.getStoreId()
				});
				return ret;
			} catch (e) {
				return [];
			}
		},
		_deleteIsOk:function(data){
			var relations = this._getRelations();
			if( relations==null){
				return null;
			}
			var etname = this.storeDesc.getPack()+"."+data.name;
			for( var i=0; i< relations.length;i++){
				var rel = relations[i];
				if( rel.leftmodule == etname || rel.rightmodule == etname){
					return this.tr("entitytypes.entitytype_exists_in_relations");
				}
			}
			return null;
		},

		_confirmDelete: function () {
			var buttons = [{
				'label': this.tr("entitytypes.delete_class"),
				'icon': "icon/22/actions/dialog-ok.png",
				'value': 1
			},
			{
				'label': this.tr("composite.select_dialog.cancel"),
				'icon': "icon/22/actions/dialog-cancel.png",
				'value': 2
			}];
			var formData = {
				delete_messages: {
					name: "delete_messages",
					type: "CheckBox",
					value: true,
					label: this.tr("entitytypes.delete_messages")
				},
				delete_settings: {
					name: "delete_settings",
					type: "CheckBox",
					value: true,
					label: this.tr("entitytypes.delete_settings")
				}
			};

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
				"inWindow": true,
				"callback": function (m, v) {
					if (m !== undefined) {
						form.hide();
						if (v == 1) {
							self._delete(self._classForm.getData(),m);
							self.setEnabled(false);
							self._buttonSave.setEnabled(false);
							self._buttonAdd.setEnabled(false);
						} else if (v == 2) {}
					}
				},
				"context": self
			});
			form.show();
		},
		_delete:function(data,flags){
			console.log("delete.data:"+qx.util.Serializer.toJson(data));
			var children = this._model.parent.getChildren();
			var len = children.getLength();
			console.log("len:"+len);
			for(var i=0; i < len; i++){
				var child = children.getItem(i);
				console.log("\tname:"+child.getId());
				if( child.getId() == data.name){
					children.remove(child);

					var dm = flags.get("delete_messages");
					var ds = flags.get("delete_settings");

					var 	namespace= this.storeDesc.getNamespace();
					var lang= ms123.config.ConfigManager.getLanguage();
					var ds = new ms123.entitytypes.DefaultSettings(namespace,this._pack, lang);
					
					if (dm) ds.deleteMessages({name:data.name,fields:this._fields});
					if (ds) ds.deleteResources(data);	

					this._deleteEntitytype(data.name);
					break;
				}
			}
		},


		_createAddForm: function () {
			var formData = {};
			for (var i = 0; i < this._columnModel.length; i++) {
				var col = this._columnModel[i];
				formData[col.name] = col;
			}
			var self = this;
			var buttons = [{
				'label': this.tr("entitytypes.attribute_takeit"),
				'icon': "icon/22/actions/dialog-ok.png",
				'callback': function (m) {
					var validate = self._addForm.validate();
					console.error("validate:" + validate);
					if (!validate) {
						var vm = self._addForm.getValidationManager();
						var items = vm.getInvalidFormItems();
						for (var i = 0; i < items.length; i++) {
							items[i].setValid(false);
						}
						ms123.form.Dialog.alert(self.tr("widgets.table.form_incomplete"));
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
			return this._addForm;
		},
		_getDefaultValues:function(){
			var fdata = {};
			for (var i = 0; i < this._columnModel.length; i++) {
				var col = this._columnModel[i];
				fdata[col.name] = col.value;
			}
			return fdata;
		},
		_createDatatypeList: function () {
			this._dataTypeList = [{
				"value": "string",
				"label": "String"
			},
			{
				"value": "text",
				"label": "BigString"
			},
			{
				"value": "number",
				"label": "Number"
			},
			{
				"value": "decimal",
				"label": "Decimal"
			},
			{
				"value": "boolean",
				"label": "Boolean"
			},
			{
				"value": "date",
				"label": "Date"
			},
			{
				"value": "array/string",
				"label": "StringArray"
			}];
		},
		_createEdittypeList: function () {
			this._editTypeList = [{
				"value": "text",
				"label": "Textfield"
			},
			{
				"value": "select",
				"label": "SelectBox"
			},
			{
				"value": "checkbox",
				"label": "Checkbox"
			},
			{
				"value": "date",
				"label": "Date"
			},
			{
				"value": "datetime",
				"label": "DateTime"
			},
			{
				"value": "textarea",
				"label": "Textarea"
			},
			{
				"value": "multiselect",
				"label": "DoubleSelectBox"
			},
			{
				"value": "auto",
				"label": "Autoincrement"
			},
			{
				"value": "functional",
				"label": "Computed"
			}];
		},
		_capitaliseFirstLetter: function (s) {
			return s.charAt(0).toUpperCase() + s.slice(1);
		},
		_saveAll: function (classData, fieldData, flags) {
			//var value = qx.lang.Json.stringify(fieldData, null, 4);
			//console.log("fieldData:"+value);
			var cm = flags.get("create_messages");
			var sf = flags.get("create_settings_form");
			var st = flags.get("create_settings_table");
			var ss = flags.get("create_settings_search");
			var cc = flags.get("create_classes");

			console.log("ss:" + ss + "/" + st + "/" + sf + "/" + cm);
			var oldData = this._etdata || {};
			var data = ms123.util.Clone.merge({}, oldData, classData);
			data.fields={};
			if (this._isNew) {
				data.enabled = true;
				data.sort = 100;
				//data.default_fields = false;
				//data.team_security = false;
				data.filter = "";
				data.fields= {};
			}
			for (var i = 0; i < fieldData.length; i++) {
				var f = fieldData[i];
				var field = qx.lang.Object.mergeWith({}, f);
				if (this._isNew) {
					field.enabled = true;
					field.form_enabled_expr = null;
					field.formula_in = null;
					field.constraints = "[]";
					field.formula_client = null;
					field.editoptions_cols = null;
					field.selectable_items = null;
					field.formula_out = null;
					field.editoptions_rows = null;
					field.default_value = null;
					field.searchable_items = null;
					field.search_options = null;
				}
				field.sort = i;

				data.fields[f.name] = field;
			}

			if (this._saveEntitytype(data)) {
				var emodel = this._model;
				var e = {}
				if( this._isNew){
					e.id = data.name;
					e.value = data.name;
					e.title = data.name;
					e.pack = this._model.getPack();
					e.type = "sw.entitytype";
					e.children = [];
					emodel = qx.data.marshal.Json.createModel(e, true);
					var echildren = this._model.getChildren();
					emodel.parent = this._model;
					echildren.insertAt(0, emodel);
				}

				if(!this._isNew){
					emodel.getChildren().removeAll();
				}
				for (var i = 0; i < fieldData.length; i++) {
					var fd = fieldData[i];
					var f = {}
					f.id = fd.name;
					f.value = fd.name;
					f.entitytype = data.name;
					f.title = fd.name;
					f.type = "sw.field";
					f.pack = this._model.getPack();
					f.children = [];
					var fmodel = qx.data.marshal.Json.createModel(f, true);
					var children = emodel.getChildren();
					fmodel.parent = emodel;
					children.insertAt(0, fmodel);
				}
			}
			var 	namespace= this.storeDesc.getNamespace();
			var lang= ms123.config.ConfigManager.getLanguage();
			var ds = new ms123.entitytypes.DefaultSettings(namespace,this._pack, lang);
			if (cm) ds.createMessages(data);
			ds.createResources(data,cm,sf,st,ss);	

			if (cc) this._createClasses(false);
			this._setEntityProperties(data.name);
			ms123.config.ConfigManager.clearCache();
			if( this._isNew){
				this.setEnabled(false);
				this._buttonSave.setEnabled(false);
				this._buttonAdd.setEnabled(false);
			}
		},
		_getEntitytype: function (name) {
			try {
				var ret = ms123.util.Remote.rpcSync("entity:getEntitytype", {
					storeId: this.storeDesc.getStoreId(),
					name: name
				});
				return ret;
			} catch (e) {
				ms123.form.Dialog.alert(this.tr("entitytypes.getEntitytype_failed") + ":" + e.message);
				return null;
			}
		},
		_saveEntitytype: function (data) {
			try {
				var ret = ms123.util.Remote.rpcSync("entity:saveEntitytype", {
					storeId: this.storeDesc.getStoreId(),
					name: data.name,
					data: data
				});
				ms123.form.Dialog.alert(this.tr("entitytypes.entitytype_saved"));
				return true;
			} catch (e) {
				ms123.form.Dialog.alert(this.tr("entitytypes.saveEntitytype_failed") + ":" + e.message);
				return false;
			}
		},
		_createClasses:function(mess){
			try {
				ms123.util.Remote.rpcSync("domainobjects:createClasses", {
					storeId: this.storeDesc.getStoreId()
				});
				if( mess ) ms123.form.Dialog.alert(this.tr("entitytypes.update_db_successfull"));
			} catch (e) {
				ms123.form.Dialog.alert("RelationsEdit.updateDb:" + e);
				return;
			}
		},
		_setEntityProperties: function (entity) {
			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("entitytypes.addSettings") + ":" + details.message);
			}).bind(this);

			try {
				entity =  ms123.settings.Config.getFqEntityName(entity,this.storeDesc);
				var ret = ms123.util.Remote.rpcSync("setting:setResourceSetting", {
					namespace: this.storeDesc.getNamespace(),
					settingsid: "global",
					resourceid: "entities."+entity+".properties",
					overwrite:false,
					settings: {
						"add_self_to_subpanel": false,
						"multi_add": false,
						"multiple_tabs": false,
						"teams_in_subpanel": false,
						"sidebar": false,
						"state_select": false,
						"exclusion_list": true
					}
				});
			} catch (e) {
				failed.call(this, e);
				return;
			}
		},
		_assetExists: function (name, type) {
			var rpcParams = {
				reponame: this.storeDesc.getNamespace(),
				name: name,
				type: type
			};
			var params = {
				method: "assetList",
				service: "git",
				parameter: rpcParams,
				async: false,
				context: this
			}
			try {
				var list = ms123.util.Remote.rpcAsync(params);
				if (list.length > 0) {
					var text = "<br/>";
					for (var i = 0; i < list.length; i++) {
						text += list[i] + "<br/>";
					}
					ms123.form.Dialog.alert(this.tr("shell.name") + "(" + name + ") " + this.tr("shell.for") + " '" + type + "' " + this.tr("shell.asset_exists") + ":" + text);
					return true;
				}
				return false;
			} catch (details) {
				ms123.form.Dialog.alert(this.tr("shell.assetExists_failed") + ":" + details.message);
			}
			return true;
		},
		_createOptionForm: function () {
			var buttons = [{
				'label': this.tr("entitytypes.generate_class"),
				'icon': "icon/22/actions/dialog-ok.png",
				'value': 1
			},
			{
				'label': this.tr("composite.select_dialog.cancel"),
				'icon': "icon/22/actions/dialog-cancel.png",
				'value': 2
			}];
			var formData = {
				create_messages: {
					name: "create_messages",
					type: "CheckBox",
					value: false,
					label: this.tr("entitytypes.create_messages")
				},
				create_settings_form: {
					name: "create_settings_form",
					type: "CheckBox",
					value: false,
					label: this.tr("entitytypes.create_form_settings")
				},
				create_settings_table: {
					name: "create_settings_table",
					type: "CheckBox",
					value: false,
					label: this.tr("entitytypes.create_table_settings")
				},
				create_settings_search: {
					name: "create_settings_search",
					type: "CheckBox",
					value: false,
					label: this.tr("entitytypes.create_search_settings")
				},
				create_classes: {
					name: "create_classes",
					type: "CheckBox",
					value: true,
					label: this.tr("entitytypes.update_db")
				}
			};

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
				"inWindow": true,
				"callback": function (m, v) {
					if (m !== undefined) {
						form.hide();
						if (v == 1) {
							self._saveAll(self._classForm.getData(), self._getRecords(), m);
						} else if (v == 2) {}
					}
				},
				"context": self
			});
			form.show();
		},
		_save: function () {
			var validate = this._classForm.validate();
			console.error("validate:" + validate);
			if (!validate) {
				var vm = this._classForm.getValidationManager();
				var items = vm.getInvalidFormItems();
				for (var i = 0; i < items.length; i++) {
					items[i].setValid(false);
				}
				ms123.form.Dialog.alert(this.tr("widgets.table.form_incomplete"));
				return;
			}
			var entityName = this._classForm.getData()["name"];
			if( this._isNew){
				if (this._assetExists(entityName, "sw.entitytype")) return;
			}

			this._createOptionForm();
			if(this._etdata==null){
				this._fieldEditAllowed=true;
				var fe = this._classForm.getFormElementByKey("name");
				fe.setReadOnly(true);
				this._etdata = {
					name: entityName,
					description: this._classForm.getData()["description"]
				}
			}
		},
		_load: function () {
			return []; 
		},
		__setRecords: function (map) {
			var keys = Object.keys(map);
			var arr = [];
			for (var i = 0; i < keys.length; i++) {
				arr.push(map[keys[i]]);
			}
			arr.sort( function(a,b){
				return a.sort-b.sort;
			});
			this._setRecords(arr);
		},
		_createFieldEditWindow: function (name) {
			var win = new qx.ui.window.Window(name, "").set({
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			});
			win.addListener("close", function (e) {
				win.destroy();
			}, this);
			win.setLayout(new qx.ui.layout.Dock);
			win.setWidth(700);
			win.setHeight(550);
			win.setAllowMaximize(false);
			win.setAllowMinimize(false);
			win.setModal(true);
			win.setActive(false);
			win.minimize();
			win.center();
			return win;
		}
	}
});
