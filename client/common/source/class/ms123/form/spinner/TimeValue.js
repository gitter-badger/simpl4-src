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
 * A Spinner that allows selection of numbers from a time range, padding them
 * with leading zeros to always display a two-digit number.
 */
qx.Class.define("ms123.form.spinner.TimeValue", {
	extend: ms123.form.spinner.Abstract,

	/**
	 * Instantiate a new spinner for hours, minutes, or seconds.
	 *
	 * @return {Void}
	 */
	construct: function () {
		this.base(arguments);

		// Assume minutes or seconds.  User can set 12 or 24 hour format if needed
		this.setMinimum(0);
		this.setMaximum(59);

		// Allow wrapping by default
		this.setWrap(true);

		// Set an appropriate width for a two-digit time component
		var textField = this.getChildControl("textfield");
		textField.setWidth(26);
	},

	members: {
		__bInOnTextChange: false,


		/**
		 * Transform the value passed to the setter for property 'value'.  In this
		 * case, we prepend a leading zero if necessary.
		 *
		 * param value {Number|String}
		 *   The new value
		 *
		 * @return {String}
		 *   The (possibly) modified value.
		 */
		_transformValue: function (value) {
			// First validate it.  We validate here rather than in the _check method
			if (value > this.getMax()) {
				value = this.getMax();
			}
			else if (value < this.getMin()) {
				value = this.getMin();
			}

			// Get a zero-padded 2-digit value
			var zeropadded = ("0" + value);
			return zeropadded.substring(zeropadded.length - 2);
		},

		/**
		 * Check the value to ensure it's valid... except that in this class we
		 * already did the checking in the {@link #_transformValue} method so
		 * there's nothing to be done here.  This just overrides the superclass
		 * limitations.
		 *
		 * param value {String}
		 *   Value being set
		 *
		 * @return {Boolean}
		 *   <i>true</i> if the value is one of the allowed values;
		 *   <i>false</i> otherwise.
		 */
		_checkValue: function (value) {
			return true;
		},


		// overridden
		_onTextChange: function (e) {
			// Ensure we always have a two-digit representation visible
			if (!this.__bInOnTextChange) {
				this.base(arguments, e);

				// Don't call this method recursively when we modify the value
				this.__bInOnTextChange = true;

				var textField = this.getChildControl("textfield");

				// Get a zero-padded 2-digit value
				var zeropadded = "0" + this.getValue();
				textField.setValue(zeropadded.substring(zeropadded.length - 2));

				this.__bInOnTextChange = false;
			}
		},


		// overridden
		_countUp: function () {
			// Since we save textual inter values, parse them so '+' does
			// integer arithmatic instead of string concatenation
			if (this._pageUpMode) {
				var newValue = parseInt(this.getValue(), 10) + this.getPageStep();
			}
			else {
				var newValue = parseInt(this.getValue(), 10) + this.getSingleStep();
			}

			// handle the case that wraping is enabled
			if (this.getWrap() && newValue > this.getMaximum()) {
				newValue = this.getMinimum();
			}

			this.gotoValue(newValue);
		},


		// overridden
		_countDown: function () {
			// Since we save textual inter values, parse them so '+' does
			// integer arithmatic instead of string concatenation
			if (this._pageDownMode) {
				var newValue = parseInt(this.getValue(), 10) - this.getPageStep();
			}
			else {
				var newValue = parseInt(this.getValue(), 10) - this.getSingleStep();
			}

			// handle the case that wraping is enabled
			if (this.getWrap() && newValue < this.getMinimum()) {
				newValue = this.getMaximum();
			}

			this.gotoValue(newValue);
		}
	}
});
