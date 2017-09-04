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
	* @lint ignoreDeprecated(alert,eval) 
*/

qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.ConstraintsField", {
	extend: ms123.graphicaleditor.plugins.propertyedit.ConstraintsWindow,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (config, title, items, key) {
		this.base(arguments,config,title);
		this.key = key;
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
	 STATICS
	 ******************************************************************************/
	statics: {},
	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		getFieldKey: function () {
			return this.key;
		},
		_init:function(){
			var layout = new qx.ui.layout.HBox();
			this._setLayout(layout);

			this.textField = this._createChildControl("textfield");
			var select = this._createChildControl("select");
			this.setFocusable(true);
		},
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
			this.textField.setValue(value);
			this.data = value;
		},


		// overridden
		_createChildControlImpl: function (id, hash) {
			var control;
			switch (id) {
			case "textfield":
				control = new qx.ui.form.TextField();
				control.setLiveUpdate(true);
				control.setFocusable(false);
				control.setReadOnly(true);
				control.setEnabled(false);
				control.addState("inner");
				this._add(control, {
					flex: 1
				});
				break;
			case "select":
				control = this.createActionButton();
				break;
			case "clear":
				var control = new qx.ui.form.Button(null, "resource/ms123/clear.png").set({
					padding: 0,
					margin: 0
				});
				control.addListener("execute", function () {
					alert("clear");
					this.resetValue();
				}, this);
				this._add(control);
				break;
			}
			return control;
		},
		createActionButton: function () {
			var control = new qx.ui.form.Button(null, "resource/ms123/edit2.png").set({
				padding: 0,
				margin: 0,
				maxHeight: 30
			});
			control.setFocusable(false);
			control.addListener("execute", function (e) {
				var selectionPane = this.createSelectionPane(selectionPane);
				var buttons = this.createButtons();
				var win = this.createWindow(this.title);
				win.add(selectionPane, {
					edge: "center"
				});
				win.add(buttons, {
					edge: "south"
				});
				this.editWindow = win;
				win.open();
			}, this);
			this._add(control);
			return control;
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
