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

qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.ComplexListWindow", {
	extend: qx.ui.core.Widget,
	implement: [
	qx.ui.form.IStringForm, qx.ui.form.IForm],
	include: [
	qx.ui.form.MForm],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (config, title, items, key, facade,data) {
		this.base(arguments);
		this.config = config || {};
		this.title = title;
		this.items = items;
		this.key = key;
		this.facade = facade;
		var layout = new qx.ui.layout.HBox();
		this._setLayout(layout);
		this.data=data;
		this._init();

	},

	/******************************************************************************
	 EVENTS
	 ******************************************************************************/
	events: {
		"changeValue": "qx.event.type.Data"
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_init: function () {
			this._createWindow();
			var value = this.data;
			if (value != undefined && value && value != "") {;
				try{
					value = qx.lang.Json.parse(value);
					if (this.enumDisplay) this.enumDisplay.setValue(value.enumDescription);
					this.enumDescription = value.enumDescription;
				}catch(e){
					console.error("EnumField.setValue:"+value+" wrong value");
				}
			}
		},
		/**
		 * Returns the field key.
		 */
		getFieldKey: function () {
			return this.key;
		},
		resetValue: function () {},

		/**
		 * Returns the actual value of the trigger field.
		 * If the table does not contain any values the empty
		 * string will be returned.
		 */
		getValue: function () {
			return this.data;
		},

		/**
		 * Sets the value of the trigger field.
		 * In this case this sets the data that will be shown in
		 * the grid of the dialog.
		 * 
		 * param {Object} value The value to be set (JSON format or empty string)
		 */
		setValue: function (value) {
			this.data = value;
		},


		_createWindow:function(){
			var container = new qx.ui.container.Composite();
			container.setLayout(new qx.ui.layout.Dock());
			var win = this.createWindow(this.title);
			var table = this.createTable(win);
			var toolbar = this.createToolbar(["add","del"]);
			var buttons = this.createButtons();
			container.add(table, {
				edge: "center"
			});
			container.add(toolbar, {
				edge: "north"
			});
			container.add(buttons, {
				edge: "south"
			});
			if (this.config.helperTree) {
				var rh = new ms123.graphicaleditor.plugins.propertyedit.ResourceDetailTree(this.config, this.facade);
				rh.addListener("nodeSelected", function (e) {
					this.handleNodeSelected(e);
				}, this);
				rh.setWidth(300);
				this._resourceHelper = rh;
				var sp = this._splitPane(container, rh);
				win.add(sp, {
					edge: "center"
				});
			} else {
				win.add(container, {
					edge: "center"
				});
			}
			this.editWindow = win;
			win.open();
		},
		handleNodeSelected:function(e){
			var model = e.getData().model;
			var type = e.getData().type;
			console.log("type:" + type);
			console.log("model:" + model.getValue() + "/" + type);
		},
		createTable: function (win) {
			var dialogWidth = 0;

			var colIds = new Array();
			var colHds = new Array();
			var recordType = [];
			for (var i = 0; i < this.items.length; i++) {
				var id = this.items[i].id();
				var header = this.items[i].name();
				var type = this.items[i].type();
				var config = this.items[i].config();
				colIds.push(id);
				colHds.push(header);

				if (type == ms123.oryx.Config.TYPE_CHOICE) {
					type = ms123.oryx.Config.TYPE_STRING;
				}
				if (type == ms123.oryx.Config.TYPE_COMBO) {
					type = ms123.oryx.Config.TYPE_STRING;
				}
				recordType[i] = {
					name: id,
					config:config,
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
			table.addListener("cellTap", this.onCellClick, this, false);
			var selModel = table.getSelectionModel();
			selModel.setSelectionMode(qx.ui.table.selection.Model.NO_SELECTION);
			selModel.addListener("changeSelection", function (e) {
				var index = selModel.getLeadSelectionIndex();
				this.table.stopEditing();
				if( this.delButton){
					this.delButton.setEnabled((index > -1) ? true : false);
				}
			}, this);

			var tcm = table.getTableColumnModel();
			table.setStatusBarVisible(false);
			var numberCellRendererFactory = new qx.ui.table.cellrenderer.Dynamic(this.numberCellRendererFactoryFunc);
			var numberCellEditorFactory = new qx.ui.table.celleditor.Dynamic(this.numberCellEditorFactoryFunc);
			var booleanCellRendererFactory = new qx.ui.table.cellrenderer.Dynamic(this.booleanCellRendererFactoryFunc);
			var booleanCellEditorFactory = new qx.ui.table.celleditor.Dynamic(this.booleanCellEditorFactoryFunc);
			for (var i = 0; i < this.items.length; i++) {
				var width = this.items[i].width();
				var type = this.items[i].type();

				var readonly = this.items[i].readonly() ? this.__maskedEval(this.items[i].readonly(),this._env) : false;
				this.items[i]._readonly = readonly;
				if (type == ms123.oryx.Config.TYPE_STRING) {
					var f = null;
					if( this.items[i].filter ){
						f = new ms123.graphicaleditor.plugins.propertyedit.RegexCellEditor(this.items[i].filter());
					}else{
						f = new qx.ui.table.celleditor.TextField();
					}
					tcm.setCellEditorFactory(i, f);
					table.getTableModel().setColumnEditable(i, !readonly);
				} else if (type == ms123.oryx.Config.TYPE_CHOICE) {
					var r = new qx.ui.table.cellrenderer.Replace();
					tcm.setDataCellRenderer(i, r);

					var listData = [];
					var items = this.items[i].items();
					var self = this;
					items.each(function (value) {
						var b = value.enabled ? self.__maskedEval(value.enabled(),self._env) : true;
						if( b){
							var option = [value.title(), null, value.value()];
							listData.push(option);
						}
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
					table.getTableModel().setColumnEditable(i, !readonly);
				} else if (type == ms123.oryx.Config.TYPE_COMBO) {
					var r = new qx.ui.table.cellrenderer.Replace();
					tcm.setDataCellRenderer(i, r);

					var listData = [];
					var items = this.items[i].items();
					items.each(function (value) {
						var option = [value.title(), null, value.value()];
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

					var f = new qx.ui.table.celleditor.ComboBox();
					f.setListData(listData);
					tcm.setCellEditorFactory(i, f);
					table.getTableModel().setColumnEditable(i, !readonly);
				} else if (type == ms123.oryx.Config.TYPE_BOOLEAN) {
					tcm.setDataCellRenderer(i, booleanCellRendererFactory);
					tcm.setCellEditorFactory(i, booleanCellEditorFactory);
					table.getTableModel().setColumnEditable(i, !readonly);
				} else if (type == ms123.oryx.Config.TYPE_INTEGER) {
					tcm.setDataCellRenderer(i, numberCellRendererFactory);
					tcm.setCellEditorFactory(i, numberCellEditorFactory);
					table.getTableModel().setColumnEditable(i, !readonly);
				} else if (type == ms123.oryx.Config.TYPE_CONSTRAINTS) {
					tcm.setDataCellRenderer(i, new ms123.graphicaleditor.plugins.propertyedit.ImageRenderer());
					table.getTableModel().setColumnEditable(i, false);
				} else if (type == "label") {
					table.getTableModel().setColumnEditable(i, false);
				}else{
					table.getTableModel().setColumnEditable(i, true);
				}

				var resizeBehavior = tcm.getBehavior();
				resizeBehavior.setWidth(i, width, 1);

				dialogWidth += width;
			}
			if (this.config.helperTree) {
				dialogWidth += 400;
			}

			if (dialogWidth > 900) {
				dialogWidth = 900;
			}
			dialogWidth += 32;

			tableModel.setColumns(colHds, colIds);
			this.tableModel = tableModel;
			this.table = table;


			var data = this.data;
			if (data == undefined || !data || data == "") {;
			} else {
				try{
					data = qx.lang.Json.parse(data);
					if( this.config.asArray){
						for (var i = 0; i < data.length; i++) {
							for (var j = 0; j < this.items.length; j++) {
								var item = this.items[j];
								if( item._readonly){
									data[i][item.id()] = item.value();
								}
							}
						}
						this.setTableData(data);
					}else{
						this.setTableData(data.items);
					}
				}catch(e){
					console.error("ComplexListWindow.createTable:"+data+" wrong value");
				}
			}

			win.setWidth(dialogWidth);


			//table.setDraggable(true);
			table.setDroppable(true);
			table.setFocusCellOnPointerMove(true);

			table.addListener("drop", this._handleDrop, this);

			var selModel = table.getSelectionModel();
			selModel.setSelectionMode(qx.ui.table.selection.Model.SINGLE_SELECTION);

			return table;
		},
		_handleDrop: function (e) {
			console.log("_handleDrop:" + e);
			if (this.table.isEditing()) {
				this.table.stopEditing();
			}
			var col = this.table.getFocusedColumn();
			var row = this.table.getFocusedRow();
			if (col === undefined || row == undefined) return;
			var target = e.getRelatedTarget();
			var value = null;


			if (qx.Class.implementsInterface(target, qx.ui.form.IStringForm)) {
				value = target.getValue();
			} else {
				value = target.getSelection().getItem(0).getValue();
			}
			console.log("cell:" + row + "/" + col);
			console.log("_handleDrop:" + value);
			this.table.getTableModel().setValue(col, row, value);
		},
		booleanCellRendererFactoryFunc: function (cellInfo) {
			return new qx.ui.table.cellrenderer.Boolean;
		},
		booleanCellEditorFactoryFunc: function (cellInfo) {
			return new qx.ui.table.celleditor.CheckBox;
		},
		numberCellRendererFactoryFunc: function (cellInfo) {
			return new qx.ui.table.cellrenderer.Number;
		},
		numberCellEditorFactoryFunc: function (cellInfo) {
			var tf = new ms123.util.NumberField;
			return tf;
		},
		createToolbar: function (buttonList) {
			var toolbar = new qx.ui.toolbar.ToolBar();
			if( buttonList.indexOf("add") != -1){
				var badd = new qx.ui.toolbar.Button("", "icon/16/actions/list-add.png");
				badd.setToolTipText(this.tr("graphicaleditor.add_record"));
				badd.addListener("execute", function () {
					var initial = this.buildInitial(this.recordType, this.items);
					this.addRecord(initial);
				}, this);
				this.addButton = badd;
				toolbar._add(badd);
			}
			if( buttonList.indexOf("del") != -1){
				var bdel = new qx.ui.toolbar.Button("", "icon/16/actions/list-remove.png");
				bdel.setToolTipText(this.tr("graphicaleditor.delete_record"));
				bdel.setEnabled(false);
				bdel.addListener("execute", function () {
					this.deleteCurrentRecord();
				}, this);
				toolbar._add(bdel);
				toolbar.add(new qx.ui.core.Spacer(), {
					flex: 1
				});
				this.delButton = bdel;
			}
			return toolbar;
		},
		handleOkButton:function(e){
			this.table.stopEditing();
			var value = this.getTableData();
			if( this.config.asArray){
				data = value;
			}else{
				var data = {
					totalCount: value.length,
					items: value
				};
			}

			data = qx.util.Serializer.toJson(data);
			console.log("data:" + data);
			var oldVal = this.data;
			this.data = data;
			this.fireDataEvent("changeValue", data, oldVal);
			this.editWindow.close();
		},
		createButtons: function () {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing(5);
			toolbar.addSpacer();
			toolbar.addSpacer();

			var buttonSave = new qx.ui.toolbar.Button(this.tr("Ok"), "icon/16/actions/dialog-ok.png");
			buttonSave.addListener("execute", function (e) {
				this.handleOkButton(e);
			}, this);
			toolbar._add(buttonSave)

			var buttonCancel = new qx.ui.toolbar.Button(this.tr("Cancel"), "icon/16/actions/dialog-close.png");
			buttonCancel.addListener("execute", function () {
				this.editWindow.close();
			}, this);
			toolbar._add(buttonCancel)

			return toolbar;
		},
		buildInitial: function (recordType, items) {
			var initial = {};
			for (var i = 0; i < items.length; i++) {
				var id = items[i].id();
				initial[id] = items[i].value();
			}
			var data = qx.util.Serializer.toJson(initial);
			console.log("initial:" + data);
			return initial;
		},
		setTableData: function (data) {
			this.tableModel.setDataAsMapArray(data, true);
		},

		getTableData: function () {
			var arr = this.tableModel.getDataAsMapArray();
			return arr;
		},
		deleteCurrentRecord: function () {
			this.table.stopEditing();
			var selModel = this.table.getSelectionModel();
			var index = selModel.getLeadSelectionIndex();
			if (index > -1) {
				this.tableModel.removeRows(index, 1);
			}
		},
		addRecord: function (map) {
			this.table.stopEditing();
			var selModel = this.table.getSelectionModel();
			var index = selModel.getLeadSelectionIndex();
			if (index > -1) {
				this.tableModel.addRowsAsMapArray([map], index+1, true);
			}else{
				this.tableModel.addRowsAsMapArray([map], null, true);
			}
		},

		onCellClick: function (e) {
			var rownum = e.getRow();
			var colnum = e.getColumn();
			this.table.stopEditing();
			this.table.setFocusedCell(colnum, rownum);
			console.log("colnum:" + colnum + "/" + rownum+"/"+this.recordType[colnum].type);
			if (this.recordType[colnum].type == ms123.oryx.Config.TYPE_CONSTRAINTS) {
				var valueType=this.tableModel.getValue(colnum-1,rownum); //Muss direkt links neben der constraint-col sein!!!
				var valueData=this.tableModel.getValue(colnum,rownum);
				console.log("contraints clicked:"+qx.util.Serializer.toJson(this.recordType[colnum].config[valueType]));
				console.log("valueType:"+valueType);
				console.log("valueData:"+valueData);
				var _valueData = valueData;
				if( !this._lastValueType){
					this._lastValueType={};
				}else  if( valueType != this._lastValueType[rownum]){
					valueData=undefined;	
					this.tableModel.setValue(colnum, rownum, valueData);
				}
				if( valueType == 'search'){
					var context = {};
					context.resourceType = "sw.entitytype";
					context.resultPrefix = "";
					context.storeDesc = this.facade.storeDesc;
					context.title = this.tr("graphicaleditor.select_entity");
					context.selected_callback = (function(data){
						console.log("selected_callback:"+qx.util.Serializer.toJson(data));
						this.tableModel.setValue(colnum, rownum, {entity:data.value});
					}).bind(this);
					if(!_valueData)_valueData={};
					new ms123.form.ResourceSelectorWindow(context, _valueData.entity);
				}else{
					console.log("valueData2:"+valueData);
					this._lastValueType[rownum] = valueType;
					var cw = new ms123.graphicaleditor.plugins.propertyedit.ConstraintsWindow(this.recordType[colnum].config[valueType],this.tr("graphicaleditor.constraints"),valueData);
					cw.addListener('changeValue', function (e) {
						console.log("valueData:"+e.getData());
						this.tableModel.setValue(colnum, rownum, e.getData());
					},this);
				}




				return;
			}
			if (this.recordType[colnum].type != ms123.oryx.Config.TYPE_BOOLEAN) {
				this.table.startEditing();
				if (this.config.helperTree) {
					var value = this.tableModel.getValue(0, rownum);
					this._resourceHelper.selectNode(value);
				}
				return;
			}
			if (this.tableModel.getValue(colnum, rownum) === true) {
				this.tableModel.setValue(colnum, rownum, false);
			} else {
				this.tableModel.setValue(colnum, rownum, true);
			}
		},
		_splitPane: function (left, right) {
			var splitPane = new qx.ui.splitpane.Pane("horizontal").set({
				decorator: null
			});

			splitPane.add(left, 4);
			splitPane.add(right, 4);
			return splitPane;
		},
		__maskedEval: function (scr, env) {
			if( scr === false) return false;
			if( scr === true) return true;
			return (new Function("with(this) { return " + scr + "}")).call(env);
		},
		createWindow: function (name) {
			var win = new qx.ui.window.Window(name, "").set({
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			});
			win.setLayout(new qx.ui.layout.Dock);
			win.setWidth(500);
			win.setHeight(400);
			win.setAllowMaximize(false);
			win.setAllowMinimize(false);
			win.setModal(true);
			win.setActive(false);
			win.minimize();
			win.center();
			return win;
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
