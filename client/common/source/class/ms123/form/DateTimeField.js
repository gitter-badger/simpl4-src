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
qx.Class.define("ms123.form.DateTimeField", {
	extend: qx.ui.core.Widget,
	include: [
	qx.ui.core.MContentPadding, qx.ui.core.MRemoteChildrenHandling, qx.ui.form.MForm],
	implement: [
	qx.ui.form.IForm, qx.ui.form.IDateForm],

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	/**
	 *  value is an integer representing a time, or a time string in 24 hour format
	 */
	construct: function (value) {
		this.base(arguments);

		var layout = new qx.ui.layout.HBox(2);
		this._setLayout(layout);

		this.__datefield = this.__createChildControl("date");
		this.__timefield = this.__createChildControl("time");
		this.setValue(value);
	},



	/**
	 *****************************************************************************
	 STATICS
	 *****************************************************************************
	 */

	statics: {},

	/**
	 *****************************************************************************
	 EVENTS
	 *****************************************************************************
	 */
	events: {
		"changeValue": "qx.event.type.Data"
	},



	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */

	properties: {
		// overridden
		focusable: {
			refine: true,
			init: true
		}
	},



	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		/**
		 ---------------------------------------------------------------------------
		 WIDGET INTERNALS
		 ---------------------------------------------------------------------------
		 */

		// overridden
		__createChildControl: function (id) {
			var control;

			switch (id) {
			case "date":
				control = new qx.ui.form.DateField();
				control.addState("inner");
				control.setFocusable(false);
				control.addListener("changeValue", this._onDateChange, this);

				this._add(control, {
					flex: 1
				});
				break;

			case "time":
				//control = new ms123.form.TimeSpinner();
				control = new ms123.form.TimeChooser();
				control.setTimeFormat("24");
				control.setLayoutFormat("right/horizontal");

				control.setFocusable(false);
				control.addListener("changeValue", this._onTimeChange, this);

				this._add(control, {
					flex: 0
				});
				break;
			}
			return control || this.base(arguments, id);
		},


		// overridden
		_forwardStates: {
			focused: true
		},
		resetValue: function () {
			alert("resetValue");
		},


		// overridden
		tabFocus: function () {},
		setDateFormat: function (value) {
			this.__datefield.setDateFormat(value);
		},
		setTimeFormat: function (value) {
			this.__timefield.setTimeFormat(value);
		},

		getValue: function () {
			if (this.__datefield.getValue() == undefined || this.__timefield.getValue() == undefined) return null;
			var ret = new Date();
			var time = this.__timefield.getValue();
			ret.setTime(this.__datefield.getValue().getTime() + (1000 * this.__timefield.getValue()));
			return ret;
		},

		setValue: function (value) {
			if (value === undefined || value == null) {
				value = new Date();
			}
			if (typeof(value) == "number") {
				var val = value;
				value = new Date();
				value.setTime(val);
			}
			if (value instanceof Date) {
				var date = new Date(value.getTime());
				date.setHours(0);
				date.setMinutes(0);
				date.setSeconds(0);
				var datevalue = date;
				var minutes = value.getMinutes();
				var hour = value.getHours();
				var seconds = value.getSeconds();
				var timevalue = hour * 3600 + minutes * 60 + seconds;
				this.__timefield.setValue(timevalue);
				this.__datefield.setValue(datevalue);
			}
		},

		_onDateChange: function (e) {
			var newValue = this.getValue();
			this.fireDataEvent("changeValue", newValue, e.oldValue);
		},
		_onTimeChange: function (e) {
			var newValue = this.getValue();
			this.fireDataEvent("changeValue", newValue, e.oldValue);
		}
	}
});
