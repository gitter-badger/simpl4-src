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
/** **********************************************************************

   qooxdoo - the new era of web development

   http://qooxdoo.org

   Copyright:
     2008 Derrell Lipman

   License:
     LGPL: http://www.gnu.org/licenses/lgpl.html
     EPL: http://www.eclipse.org/org/documents/epl-v10.php
     See the LICENSE file in the project's top-level directory for details.

   Authors:
     * Derrell Lipman

************************************************************************ */

/**
 * A Spinner that allows selection of "AM" and "PM" (in the appropriate locale)
 */
qx.Class.define("ms123.form.spinner.AmPm", {
	extend: ms123.form.spinner.Abstract,

	/**
	 * Instantiate a new AM/PM spinner.
	 *
	 * param value {String}
	 *   One of the two strings representing AM and PM.  The legal values are
	 *   the translated strings.
	 *
	 * @return {Void}
	 */
	construct: function (value) {
		this.__bInSuperclassConstructor = true;
		this.base(arguments);
		this.__bInSuperclassConstructor = false;

		this.__am = this.tr("AM").toString();
		this.__pm = this.tr("PM").toString();

		this.__toggle[this.__am] = this.__pm;
		this.__toggle[this.__pm] = this.__am;

		// The text field needn't be quite so wide as our superclass'
		var textField = this.getChildControl("textfield");
		textField.setWidth(30);

		// Allow wrapping by default
		this.setWrap(true);

		// Set an initial value to use in case the parameter value is invalid
		this.setValue(this.__am);
		this.setValue(value);
	},

	members: {
		__am: null,
		__pm: null,
		__toggle: {},

		/**
		 * Transform the value passed to the setter for property 'value'.  In this
		 * case, we convert it to upper case.
		 *
		 * param value {String}
		 *   We expect it the value to be "AM" or "PM" (possibly lower or mixed
		 *   case).  We don't actually validate it here, but rather let the
		 *   {@link #_checkValue} method do that for us as the next step.
		 *
		 * @return {String}
		 *   The (possibly) modified value.
		 */
		_transformValue: function (value) {
			// Attempt to convert the value to upper case
			try {
				value = value.toUpperCase();
			}
			catch (e) {
				// don't error; let _checkValue catch it
			}

			return value;
		},

		/**
		 * Check the value to ensure it's valid.  In this spinner, we allow only
		 * the (localized) values "AM" and "PM".
		 *
		 * param value {String}
		 *   "AM" or "PM" to pass this predicate
		 *
		 * @return {Boolean}
		 *   <i>true</i> if the value is one of the allowed values;
		 *   <i>false</i> otherwise.
		 */
		_checkValue: function (value) {
			// Allow superclass to set initial 0 value before imposing restrictions
			if (this.__bInSuperclassConstructor) {
				return true;
			}

			// We allow only two possible values
			return (value == this.__am || value == this.__pm);
		},

		// overridden
		_applyValue: function (value, old) {
			var upButton = this.getChildControl("upbutton");
			var downButton = this.getChildControl("downbutton");
			var textField = this.getChildControl("textfield");

			// up button enabled/disabled
			if (value == this.__am) {
				// Only enable the button if the spinner itself is enabled
				if (this.getEnabled()) {
					upButton.resetEnabled();
				}
			}
			else {
				// Only disable the buttons if wrapping is disabled
				if (!this.getWrap()) {
					upButton.setEnabled(false);
				}
			}

			// down button enabled/disabled
			if (value == this.__pm) {
				// Only enable the button if the spinner itself is enabled
				if (this.getEnabled()) {
					downButton.resetEnabled();
				}
			}
			else {
				// Only disable the buttons if wrapping is disabled
				if (!this.getWrap()) {
					downButton.setEnabled(false);
				}
			}

			// Save the last valid value of the spinner
			this._lastValidValue = value;

			// Write the value of the spinner to the textfield
			textField.setValue(value + "");
		},

		// overridden
		_onTextChange: function (e) {
			// Get the value entered in the text field
			var textField = this.getChildControl("textfield");
			var value = textField.getValue().toUpperCase();

			// Is this a valid value?
			if (!this._checkValue(value)) {
				// Nope.  Revert to previous valid value
				textField.setValue(this._lastValidValue);
			}
			else {
				// Sure, it's valid.  Use it.
				this.setValue(value);
			}
		},

		// overridden
		_countUp: function () {
			var value = this.getValue();
			var wrap = this.getWrap();
			if (value == this.__am || wrap) {
				this.setValue(this.__toggle[value]);
			}
		},

		// overridden
		_countDown: function () {
			var value = this.getValue();
			var wrap = this.getWrap();
			if (value == this.__pm || wrap) {
				this.setValue(this.__toggle[value]);
			}
		}
	}
});
