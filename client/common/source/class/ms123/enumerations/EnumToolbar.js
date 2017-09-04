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
qx.Class.define('ms123.enumerations.EnumToolbar', {
	extend: qx.core.Object,
	implement: ms123.widgets.IToolbar,
	include: [qx.locale.MTranslation],


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this.base(arguments);
		this._context=context;
		this._toolbar = this._createToolbar();
		this._createArrowButtons();
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		/**
		 */
		getToolbar: function (table) {
			this._widgetTable = table;
			return this._toolbar;
		},
		selectionChanged: function (table) {
			var selModel = table.getSelectionModel();
			var count = selModel.getSelectedCount();
			var index = selModel.getLeadSelectionIndex();
			this._currentTableIndex = index;
			var tableModel = table.getTableModel();
			var map = tableModel.getRowDataAsMap(index);
			this._data = map;
			if (count == 0) {
				this._buttonUp.setEnabled(false);
				this._buttonDown.setEnabled(false);
			} else {
				this._buttonUp.setEnabled(true);
				this._buttonDown.setEnabled(true);
			}
		},
		setDefaultButtons: function (buttonMap) {
			this._menuPart1.add(buttonMap["add"]);
			this._menuPart1.add(buttonMap["copy"]);
			this._menuPart1.add(buttonMap["edit"]);
			this._menuPart1.add(buttonMap["del"]);
		},
		setParentData: function (masterid, parentData) {},
		_createToolbar: function () {
			var tb = new qx.ui.toolbar.ToolBar().set({});
			tb.setSpacing(10);
			this._menuPart1 = new qx.ui.toolbar.Part;
			this._menuPart2 = new qx.ui.toolbar.Part;
			tb.add(this._menuPart1);
			tb.add(this._menuPart2);
			return tb;
		},
		_createArrowButtons: function () {
			this._buttonUp = new qx.ui.toolbar.Button("", "icon/16/actions/go-up.png");
			this._buttonUp.setToolTipText(this.tr("meta.lists.fs.up"));
			this._buttonUp.addListener("execute", function () {
				this._widgetTable.currentRecordUp();
			}, this);
			this._menuPart2.add(this._buttonUp);
			this._buttonUp.setEnabled(false);

			this._buttonDown = new qx.ui.toolbar.Button("", "icon/16/actions/go-down.png");
			this._buttonDown.setToolTipText(this.tr("meta.lists.fs.down"));
			this._buttonDown.addListener("execute", function () {
				this._widgetTable.currentRecordDown();
			}, this);
			this._menuPart2.add(this._buttonDown);
			this._buttonDown.setEnabled(false);

			var buttonSave = new qx.ui.toolbar.Button(this.tr("meta.lists.savebutton"), "icon/16/actions/document-save.png");
			buttonSave.setToolTipText(this.tr("meta.lists.fs.save"));
			buttonSave.addListener("execute", function () {
				this._context.save();
			}, this);
			this._menuPart2.add(buttonSave);



		}
	}
});
