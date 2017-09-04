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

qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.ModuleSelectorField", {
	extend: qx.ui.core.Widget,
	implement: [
	qx.ui.form.IStringForm, qx.ui.form.IForm],
	include: [
	qx.ui.form.MForm],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (config, key, facade) {
		this.base(arguments);
		var p  = config.from.split(":");
		this.module = p[0];
		this.field = p[1];
		this.key = key;
		this.config = config;
		this.facade = facade;
		var layout = new qx.ui.layout.HBox();
		this._setLayout(layout);

console.error("ModuleSelectorField:"+config.from+"/"+key);
		var textField = this._createChildControl("textfield");
		var select = this._createChildControl("select");
		var clear = this._createChildControl("clear");
		this.setFocusable(true);

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
		/**
		 * Returns the field key.
		 */
		getFieldKey: function () {
			return this.key;
		},

		/**
		 * Returns the actual value of the trigger field.
		 * If the table does not contain any values the empty
		 * string will be returned.
		 */
		getValue: function () {
			console.log("ModuleSelectorField.getValue:" + this.data);
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
			console.log("ModuleSelectorField.setValue:" + value);
			if (value != undefined && value && value.length > 0) {
				if (this.data == undefined) {
					this.data = value;
					this.getChildControl("textfield").setValue(value);
				}
			}
		},
		resetValue: function () {
			this.getChildControl("textfield").setValue(null);
		},

		// overridden
		_createChildControlImpl: function (id, hash) {
			var control;
			switch (id) {
			case "textfield":
				control = new qx.ui.form.TextField();
				control.setLiveUpdate(true);
				control.setFocusable(false);
				if( this.config.readonly){
					control.setReadOnly(true);
					control.setEnabled(false);
				}
				control.addState("inner");
				control.addListener("changeValue", this._onTextFieldChangeValue, this);
				this._add(control, {
					flex: 1
				});
				break;
			case "select":
				var control = new qx.ui.form.Button(null, "resource/ms123/edit2.png").set({
					padding: 0,
					margin: 0,
					maxHeight: 20
				});
				control.setFocusable(false);
				control.addListener("execute", function (e) {
					var context = {};
					context.modulename = this.module;
					context.title = this.tr("graphicaleditor.bpmn.select_form");
					context.selected_callback = (function(value){
						var data = value[this.field];
						var oldVal = this.data;
						this.data = data;
						this.getChildControl("textfield").setValue(data);
						this.fireDataEvent("changeValue", data, oldVal);
					}).bind(this);
					context.storeDesc = this.facade.storeDesc;
					new ms123.util.RecordSelector(context);
				}, this);
				this._add(control);
				break;
			case "clear":
				var control = new qx.ui.form.Button(null, "resource/ms123/clear.png").set({
					padding: 0,
					maxHeight: 20,
					margin: 0
				});
				control.addListener("execute", function () {
					var oldval = this.data;
					this.data = "";
					this.resetValue();
					this.fireDataEvent("changeValue", null, oldval);
				}, this);
				this._add(control);
				break;
			}
			return control;
		},
		_onTextFieldChangeValue:function(){
			var oldVal = this.data;
			this.data = this.getChildControl("textfield").getValue();
			this.fireDataEvent("changeValue", this.data, oldVal);
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
