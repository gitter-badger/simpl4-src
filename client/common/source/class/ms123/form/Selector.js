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
	* @ignore($)
*/
qx.Class.define("ms123.form.Selector", {
	extend: ms123.form.RelatedTo,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (context, useitCheckboxes) {
		this.base(arguments, context);
		var config = context.config;

		this._modulename = ms123.util.Inflector.singularize(config.entity);
		this._fieldList = this._convertField(config.fieldList);
		this._form = context.form;
		this._fieldmap = {};
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		setModule: function () {},
		_setSelectedValues: function (cr, m) {
			console.log("_setSelectedValues:" + qx.util.Serializer.toJson(cr) );
			if (this._fieldList) {
				var formModel = this._form.getModel();
				for (var i = 0; i < this._fieldList.length; i++) {
					var fmap = this._fieldList[i];
					var path = fmap.path + "." + fmap.id;
					var value = cr[path];
					if( value == undefined) value=null;
					console.log("setting(" + path + "):" + fmap.form_fieldname + "=" + value);
					try {
						formModel.set(fmap.form_fieldname, value);
					} catch (e) {
						console.error("Cannot set:" + e);
					}
				}
			}
		},
		resetValue: function () {
			this.getChildControl("textfield").setValue(null);
			this._setSelectedValues({});
		},
		_convertField: function (fieldList) {
			if (fieldList == null) return;
			if (typeof fieldList == "string") {
				if (fieldList.match(/^{/)) {
					return qx.lang.Json.parse(fieldList).items;
				}
				return fieldList.split(",");
			}
			if (typeof fieldList == "object") {
				return fieldList.items;
			}
			return null;
		},
		_displayWindow: function () {
			var tc = this._getTableColumns(this._fieldList);
			if( tc == null) return;
			var win = this._createWindow(this.tr("relatedto.selection") + ": " + this.tr("data."+this._modulename));
			var table = this._createTable(win, tc);
			var self = this;
			var params = {
				modulename: this._modulename,
				onSave: function (data) {
					self._saveSearchFilter(data, self._modulename, null);
				},
				onSelect: function (sf) {
					self._selectSearchFilter(self._modulename, null, self._storeDesc, sf);
				},
				onSearch: function (data) {
					var rpc = {
						namespace: self._storeDesc.getNamespace(),
						entity: self._modulename,
						fields: tc.displayColumns,
						filter: qx.lang.Json.parse(data)
					};
					table.setRpcParams(rpc);
				}
			}

			var sf = this._createSearchFilterWithChilds(this._modulename, params, this._storeDesc);
			var sp = this._doLayout(win, sf, table)
			win.add(sp, {});
			var app = qx.core.Init.getApplication();
			app.getRoot().add(win);
			win.open();
			this._win = win;
		},
		_getTableColumns: function (selFields) {
			var value = qx.lang.Json.stringify(selFields, null, 4); console.log("Fields:" + value);
			var columns = new Array();
			var displayColumns = new Array();
			var aliasColumns = new Array();
			for (var f = 0; f < selFields.length; f++) {
				var selField = selFields[f];

				if (selField.display === true) {
					var fieldDesc = this._getFieldDesc(selField.module, selField.id);
					if( fieldDesc == null){
						ms123.form.Dialog.alert("Selector._getTableColumns:field(\""+selField.id+"\") not found in \"" +selField.module+"\"");
						return null;
					}
					var dt = fieldDesc["datatype"];
					if (dt && dt.match("^array")) {
						continue;
					}
					var fd = qx.lang.Object.mergeWith({}, fieldDesc);
					fd.fqn = selField.path + "." + selField.id;
					fd.label = this.tr("data." + ms123.util.Inflector.getModuleName(selField.module)) + "/" + this.tr("data." + selField.module + "." + selField.id);
					displayColumns.push(fd.fqn);
					aliasColumns.push(selField.mapping);
					fd["id"] = fd.fqn;
					fd["name"] = fd.fqn;
					columns.push(fd);
				}
			}
			return {
				columns: columns,
				displayColumns: displayColumns,
				aliasColumns: aliasColumns
			};
		},
		_createTable: function (win, tc) {
			var self = this;
			var buttons = [{
				'label': "",
				'icon': "icon/16/actions/dialog-ok.png",
				'callback': function (m) {
					var cr = self._table.getCurrentRecord();
					self._setSelectedValues(cr, m);
					win.close();
				},
				'value': "select"
			},
			{
				'label': "",
				'icon': "icon/16/actions/dialog-close.png",
				'callback': function (m) {
					win.close();
				},
				'value': "cancel"
			}];

			var cm = new ms123.config.ConfigManager();
			var cols = cm.buildColModel(tc.columns, "lists", this._storeDesc, "data");

			var gridConfig = {};
			gridConfig.buttons = buttons;
			gridConfig.config = "lists";
			gridConfig.model = this._buildModel(cols, {});
			gridConfig.user = this._user;
			gridConfig.storeDesc = this._storeDesc;
			var table = new ms123.widgets.Table(gridConfig);
			table.addListener("dblclick", this._onCellDblClick, this);
			this._table = table;
			return table;
		},
		_onCellDblClick:function(e){
console.log("_onCellDblClick:"+e);
			var cr = this._table.getCurrentRecord();
			console.log("_onCellClick:"+JSON.stringify(cr));
			this._setSelectedValues(cr);
			this._win.close();
		},
		_buildModel: function (columns, props) {
			var model = {
				attr: function (what) {
					if (what == "gridProps") {
						return props;
					}
					if (what == "colModel") {
						return columns;
					}
				}
			}
			return model;
		},
		_getFieldDesc: function (module, id) {
			var colModel = this._fieldmap[module];
			if (colModel == null) {
				this._getSelectableFields(module);
				colModel = this._fieldmap[module];
			}
			for (var f = 0; f < colModel.length; f++) {
				var field = colModel[f];
				if (field.hidden) continue;
				if (field.id == id) {
					return field;
				}
			}
			return null;
		},
		__maskedEval: function (scr, env) {
			return (new Function("with(this) { return " + scr + "}")).call(env);
		},
		_getSelectableFields: function (entity) {
			var colModel = this._fieldmap[entity];
			if (colModel === undefined) {
				try {
					var cm = new ms123.config.ConfigManager();
					var data = cm.getEntityViewFields(entity,this._storeDesc,"report",false);
					colModel = cm.buildColModel(data, entity, this._storeDesc, "data", "search");
					this._fieldmap[entity] = colModel;
				} catch (e) {
					ms123.form.Dialog.alert("Selector._getSelectableFields:" + e);
					return;
				}
			}
			return colModel;
		}
	}
});
