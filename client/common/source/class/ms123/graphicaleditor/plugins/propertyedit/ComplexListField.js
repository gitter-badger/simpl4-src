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

qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.ComplexListField", {
	extend: ms123.graphicaleditor.plugins.propertyedit.ComplexListWindow,
	implement: [qx.ui.form.IStringForm,ms123.graphicaleditor.plugins.propertyedit.IUpdate],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (config, title, items, key, facade) {
		this.base(arguments,config,title,items,key,facade);
	},

	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_init:function(){
			this.textField = this._createChildControl("textfield");
			var select = this._createChildControl("select");
			this._createChildControl("clear");
			this.setFocusable(true);
		},
		// interface implementation
		envChanged: function (env) {
			this._env=env;
		},
		setValue: function (value) {
			this.textField.setValue(value);
			this.data = value;
		},
		resetValue: function () {
			this.getChildControl("textfield").setValue(null);
			this.data = null;
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
				control.setFocusable(false);
				control.addListener("execute", function () {
					var oldval = this.data;
					this._internalChange = true;
					this.resetValue();
					this.fireDataEvent("changeValue", null, oldval);
					this._internalChange = false;
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
				this._createWindow();
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
