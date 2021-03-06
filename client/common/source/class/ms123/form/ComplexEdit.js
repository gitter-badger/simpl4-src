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
qx.Class.define('ms123.form.ComplexEdit', {
	extend: qx.ui.container.Composite,
	implement: [qx.ui.form.IStringForm, qx.ui.form.IModel, qx.ui.form.IForm],
	include: [qx.ui.form.MForm,qx.ui.form.MModelProperty],

	construct: function (context, key) {
		this.base(arguments);
		this.setLayout(new qx.ui.layout.Dock());
		this._currentTableIndex = -1;
		this._currentFieldCounter = 1;
		this._config = context.config;
		this._hasToolbar = context.toolbar !== false;
		this._key = key;

		var gb = new qx.ui.groupbox.GroupBox(context.caption);
		gb.getChildrenContainer().setDecorator(null);
		gb.setLayout(new qx.ui.layout.Dock());

		var table = this._createTable();
		if( context.toolbar !== false ){
			var toolbar = this._createToolbar();
			gb.add(toolbar, {
				edge: "north"
			});
		}
		gb.add(table, {
			edge: "center"
		});
		this.add(gb, {
			edge: "center"
		});
		this.addListener("changeValid", this._changeValid, this);
		this._borderNormal = new ms123.util.RoundSingleBorder(1, "solid", "#C2C2C2", 5);
		this._borderError = new ms123.util.RoundSingleBorder(2, "solid", "red", 5);
	},

	properties: {},

	events: {
		"changeValue": "qx.event.type.Data"
	},
	members: {
		// overridden
		/**
		 * @lint ignoreReferenceField(_forwardStates)
		 */
		_forwardStates: {
			focused: true
		},
		_changeValid: function (e) {
			console.log("ComplexEdit._changeValid:" + this.getValid());
			if (this.getValid()) {
				this.setDecorator(this._borderNormal);
			} else {
				this.setDecorator(this._borderError);
			}
		},
		getModel: function () {
			this._table.stopEditing();
			var data = this._getTableData();
			return data;
		},
		setModel: function (model) {
			try {
				if (model == null || model == "") {
					this._setTableData([]);
				} else {
					this._setTableData(model);
				}
			} catch (e) {}
		},
		getValue: function () {
			this._table.stopEditing();
			var data = this._getTableData();
			return data;
		},
		setValue: function (value) {
			try {
				if (value == null || value == "") {
					this._setTableData([]);
				} else {
					var data = value;
					if( typeof value === 'string'){
						var data = qx.lang.Json.parse(value);
					}
					this._setTableData(data);
				}
			} catch (e) {}
		},
		_dataEditListener:function(e){
			console.log("_dataEditListener");
			this._fireDataEvent();
		},
		_fireDataEvent:function(){
			console.log("_fireDataEvent");
			if( this._inFireDataEvent === true) return;
			this._inFireDataEvent = true;
			var newData = this._getTableData();
			this.fireDataEvent("changeValue", newData, this._oldData);
			this._oldData = newData;
			this._inFireDataEvent = false;
		},
		_createTable: function () {
			var dialogWidth = 0;

			var colIds = new Array();
			var colHds = new Array();
			var recordType = [];
			var items = this._config;
			for (var i = 0; i < items.length; i++) {
				var id = items[i].id;
				var header = items[i].name;
				var type = items[i].type;
				colIds.push(id);
				if( header.match(/^%/)){
					header = this.tr(header.substring(1));
				}
				colHds.push(header);

				if (type.toLowerCase() == "checkbox") {
					type = ms123.oryx.Config.TYPE_BOOLEAN;
					items[i].type = type;
				}

				if (type == ms123.oryx.Config.TYPE_CHOICE) {
					type = ms123.oryx.Config.TYPE_STRING;
				}
				recordType[i] = {
					name: id,
					type: type
				};
			}
			this.recordType = recordType;

			var tableModel = new qx.ui.table.model.Simple();
			tableModel.setColumns(colHds, colIds);
			var customMap = {
				tableColumnModel: function (obj) {
					return new qx.ui.table.columnmodel.Resize(obj);
				}
			};
			var table = new qx.ui.table.Table(tableModel, customMap);
			table.addListener("dataEdited", this._dataEditListener,this);
			table.addListener("cellTap", this._onCellClick, this, false);
			var selModel = table.getSelectionModel();
			selModel.setSelectionMode(qx.ui.table.selection.Model.SINGLE_SELECTION);
			selModel.addListener("changeSelection", function (e) {
				if( !this._hasToolbar ) return;
				var index = selModel.getLeadSelectionIndex();
				var count = selModel.getSelectedCount();
				if (count == 0) {
					this._delButton.setEnabled(false);
					this._upButton.setEnabled(false);
					this._downButton.setEnabled(false);
					return;
				}
				this._currentTableIndex = index;
				console.log("selected:" + index);
				this._table.stopEditing();
				this._delButton.setEnabled((index > -1) ? true : false);
				this._upButton.setEnabled((index > 0) ? true : false);
				var rc = this._tableModel.getRowCount();
				this._downButton.setEnabled((index > -1 && index < (rc - 1)) ? true : false);
			}, this);

			var tcm = table.getTableColumnModel();
			table.setStatusBarVisible(false);
			var booleanCellRendererFactory = new qx.ui.table.cellrenderer.Dynamic(this._booleanCellRendererFactoryFunc);
			var booleanCellEditorFactory = new qx.ui.table.celleditor.Dynamic(this._booleanCellEditorFactoryFunc);
			for (var i = 0; i < items.length; i++) {
				var width = items[i].width;
				var type = items[i].type;

				//table.getTableModel().setColumnEditable(1, true);
				if (type == ms123.oryx.Config.TYPE_STRING) {
					var f = new qx.ui.table.celleditor.TextField();
					f.setValidationFunction(items[i].validationFunction);
					tcm.setCellEditorFactory(i, f);
					table.getTableModel().setColumnEditable(i, true);
				} else if (type == "icon") {
					tcm.setDataCellRenderer(i, new ms123.datamapper.ImageCellRenderer());
					table.getTableModel().setColumnEditable(i, false);
				} else if (type == ms123.oryx.Config.TYPE_CHOICE) {
					var r = new ms123.datamapper.ChoiceCellRenderer();
					tcm.setDataCellRenderer(i, r);

					var listData = [];
					var _items = items[i].items;
					_items.each(function (value) {
						var option = [value.title, null, value.value];
						listData.push(option);
					});

					var replaceMap = {};
					listData.each(function (row) {
						if (row instanceof Array) {
							replaceMap[row[0]] = row[2];
						}
					});
					r.setReplaceMap(replaceMap);
					r.addReversedReplaceMap();

					var f = new qx.ui.table.celleditor.SelectBox();
					f.setListData(listData);
					tcm.setCellEditorFactory(i, f);
					table.getTableModel().setColumnEditable(i, true);

				} else if (type == ms123.oryx.Config.TYPE_BOOLEAN) {
					tcm.setDataCellRenderer(i, booleanCellRendererFactory);
					tcm.setCellEditorFactory(i, booleanCellEditorFactory);
					table.getTableModel().setColumnEditable(i, true);
				}

				var resizeBehavior = tcm.getBehavior();
				resizeBehavior.setWidth(i, width, 1);

				dialogWidth += width;
			}
			if (dialogWidth > 900) {
				dialogWidth = 900;
			}
			dialogWidth += 32;

			tableModel.setColumns(colHds, colIds);
			this._tableModel = tableModel;
			this._table = table;


			var data = this._data;
			if (data instanceof String && data !== "") {;
				try {
					data = qx.lang.Json.parse(data);
					this._setTableData(data.items);
				} catch (e) {
					console.error("FieldEditor._createTable:" + data + " wrong value");
				}
			}
			table.setFocusCellOnPointerMove(true);

			var selModel = table.getSelectionModel();
			selModel.setSelectionMode(qx.ui.table.selection.Model.SINGLE_SELECTION);
			return table;
		},
		_booleanCellRendererFactoryFunc: function (cellInfo) {
			return new qx.ui.table.cellrenderer.Boolean;
		},
		_booleanCellEditorFactoryFunc: function (cellInfo) {
			return new qx.ui.table.celleditor.CheckBox;
		},
		_createToolbar: function () {
			var toolbar = new qx.ui.toolbar.ToolBar();
			var badd = new qx.ui.toolbar.Button("", "icon/16/actions/list-add.png");
			badd.setToolTipText(this.tr("graphicaleditor.add_record"));
			badd.addListener("execute", function () {
				var initial = this._buildInitial(this._items);
				this._table.stopEditing();
				this._addRecord(initial);
			}, this);
			toolbar._add(badd);

			var bdel = new qx.ui.toolbar.Button("", "icon/16/actions/list-remove.png");
			bdel.setToolTipText(this.tr("graphicaleditor.delete_record"));
			bdel.setEnabled(false);
			bdel.addListener("execute", function () {
				this._deleteCurrentRecord();
			}, this);
			toolbar._add(bdel);
			this._delButton = bdel;

			toolbar.addSpacer();
			this._upButton = new qx.ui.toolbar.Button("", "icon/16/actions/go-up.png");
			this._upButton.addListener("execute", function () {
				console.log("_upButton:" + this._currentTableIndex);
				if (this._currentTableIndex == 0) return;
				this._table.stopEditing();
				var curRecord = this._getRecordAtPos(this._currentTableIndex);
				this._deleteRecordAtPos(this._currentTableIndex);
				this._insertRecordAtPos(curRecord, this._currentTableIndex - 1);
				var selModel = this._table.getSelectionModel();
				selModel.setSelectionInterval(this._currentTableIndex - 1, this._currentTableIndex - 1);
			}, this);
			toolbar._add(this._upButton);
			this._upButton.setEnabled(false);

			this._downButton = new qx.ui.toolbar.Button("", "icon/16/actions/go-down.png");
			this._downButton.addListener("execute", function () {
				var rc = this._tableModel.getRowCount();
				console.log("_downButton:" + this._currentTableIndex + "/" + rc);
				if (this._currentTableIndex >= (rc - 1)) return;
				this._table.stopEditing();
				var curRecord = this._getRecordAtPos(this._currentTableIndex);
				this._deleteRecordAtPos(this._currentTableIndex);
				this._insertRecordAtPos(curRecord, this._currentTableIndex + 1);
				var selModel = this._table.getSelectionModel();
				selModel.setSelectionInterval(this._currentTableIndex + 1, this._currentTableIndex + 1);
			}, this);
			toolbar._add(this._downButton);
			this._downButton.setEnabled(false);
			toolbar.add(new qx.ui.core.Spacer(), {
				flex: 1
			});
			return toolbar;
		},
		_buildInitial: function (items) {
			var initial = {};
			var items = this._config;
			for (var i = 0; i < items.length; i++) {
				var id = items[i].id;
				var ini = items[i].initialFunction;
				if (ini) {
					initial[id] = ini.call(this, {
						counter: this._currentFieldCounter++
					});
				} else {
					initial[id] = items[i].value;
				}
			}
			return initial;
		},
		_setTableData: function (data) {
			this._tableModel.setDataAsMapArray(data, true);
		},

		_getTableData: function () {
			var arr = this._tableModel.getDataAsMapArray();
			return arr;
		},
		_deleteCurrentRecord: function () {
			this._table.stopEditing();
			var oldData = this._oldData;
			var selModel = this._table.getSelectionModel();
			var index = selModel.getLeadSelectionIndex();
			if (index > -1) {
				this._tableModel.removeRows(index, 1);
			}
			var newData = this._getTableData();
			this.fireDataEvent("changeValue", newData, oldData);
			this._oldData = newData;
		},
		_addRecord: function (map) {
			var oldData = this._oldData;
			this._tableModel.addRowsAsMapArray([map], null, true);
			var newData = this._getTableData();
			this.fireDataEvent("changeValue", newData, oldData);
			this._oldData = newData;
		},

		_insertRecordAtPos: function (map, pos) {
			this._tableModel.addRowsAsMapArray([map], pos, true);
		},
		_deleteRecordAtPos: function (pos) {
			this._tableModel.removeRows(pos, 1);
		},
		_getRecordAtPos: function (pos) {
			return this._tableModel.getRowDataAsMap(pos);
		},
		_onCellClick: function (e) {
			var rownum = e.getRow();
			var colnum = e.getColumn();
			this._table.stopEditing();
			this._table.setFocusedCell(colnum, rownum);
			if (this.recordType[colnum].type != ms123.oryx.Config.TYPE_BOOLEAN) {
				this._table.startEditing();
				return;
			}
			if (this._tableModel.getValue(colnum, rownum) === true) {
				this._tableModel.setValue(colnum, rownum, false);
			} else {
				this._tableModel.setValue(colnum, rownum, true);
			}
			this._fireDataEvent();
		},
		// useit checkbox
		getCheckBox: function () {
			return this.getChildControl("checkbox");
		},

		// overridden
		_createChildControlImpl: function (id, hash) {
			var control;
			switch (id) {
			case "checkbox":
				control = new qx.ui.form.CheckBox();
				control.setFocusable(false);
				control.setKeepActive(true);
				control.addState("inner");
				control.set({
					decorator: "main"
				});
				this.add(control, {
					edge: "west"
				});
				break;
			case "select":
				break;
			}
			return control;
		},
		resetValue: function () {}
	}
});
