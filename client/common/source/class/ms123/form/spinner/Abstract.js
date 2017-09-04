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
 * Abstract spinner for the time chooser components
 */
qx.Class.define("ms123.form.spinner.Abstract", {
	extend: qx.ui.form.Spinner,
	type: "abstract",

	construct: function (value) {
		this.base(arguments);

		// Set the initial layout format
		this.initLayoutFormat();
	},

	events: {
		/**
		 * Fired when the spinner's value changes
		 */
		changeValue: "qx.event.type.Event"
	},

	properties: {
		/**
		 * Specify the spinner's layout format.  Valid values are 'below/vertical'
		 * and 'below/horizontal'.
		 */
		layoutFormat: {
			init: "below/vertical",
			check: function (value) {
				return this._checkLayoutFormat(value);
			},
			apply: "_applyLayoutFormat"
		}
	},

	members: {
		// overridden
		_onTextChange: function (e) {
			this.base(arguments);
			this.fireEvent("changeValue");
		},


		/**
		 * Check function for layoutFormat property.  We ensure that the value
		 * being set is one of the allowed values.
		 *
		 * param value {String}
		 *   Value being set
		 *
		 * @return {Boolean}
		 *   <i>true</i> if the value is allowed;
		 *   <i>false</i> otherwise.
		 */
		_checkLayoutFormat: function (value) {
			return (value == 'below/vertical' || value == 'below/horizontal');
		},


		/**
		 * Apply function for layoutFormat property.
		 * Re-layout the buttons according to the specified value.
		 *
		 * param value {String}
		 *   New value
		 *
		 * param old {String}
		 *   Previous value
		 *
		 * @return {Void}
		 */
		_applyLayoutFormat: function (value, old) {
			//
			// Put the up/down buttons below the text field instead of to the right
			//
			// Get the various controls in the spinner
			var upButton = this.getChildControl("upbutton");
			var downButton = this.getChildControl("downbutton");
			var textField = this.getChildControl("textfield");

			// Cause the buttons' labels to center properly
			upButton.setCenter(true);
			downButton.setCenter(true);

			// Remove all of them from the grid layout
			this._removeAll();

			// Re-insert them in the preferred locations.
			// Which layout format was requested?
			switch (value) {
			case 'below/vertical':
				var layout = new qx.ui.layout.Grid();
				layout.setColumnFlex(0, 1);
				this._setLayout(layout);
				this._add(textField, {
					column: 0,
					row: 0,
					colSpan: 2,
					rowSpan: 1
				});
				this._add(upButton, {
					column: 0,
					row: 1,
					colSpan: 1,
					rowSpan: 1
				});
				this._add(downButton, {
					column: 0,
					row: 2,
					colSpan: 1,
					rowSpan: 1
				});
				break;

			case 'below/horizontal':
				var layout = new qx.ui.layout.Grid();
				layout.setColumnFlex(0, 1);
				layout.setRowFlex(0, 1);
				layout.setRowFlex(1, 1);
				this._setLayout(layout);
				this._add(textField, {
					column: 0,
					row: 0,
					colSpan: 2,
					rowSpan: 1
				});
				this._add(downButton, {
					column: 0,
					row: 1,
					colSpan: 1,
					rowSpan: 1
				});
				this._add(upButton, {
					column: 1,
					row: 1,
					colSpan: 1,
					rowSpan: 1
				});
				break;

			case 'right/horizontal':
				var layout = new qx.ui.layout.HBox();
				this._setLayout(layout);
				this._add(textField);

				var cont = new qx.ui.container.Composite(new qx.ui.layout.VBox());
				cont.add(upButton);
				cont.add(downButton);
				this._add(cont);

				break;
			}
		},

		/**
		 * We add a 'transform' key in the "defer" section, below.  Ensure that
		 * there's a valid transform in case our subclasses don't implement it.
		 *
		 * param value {Any}
		 *   The value to be transformed
		 *
		 * @return {Any}
		 *   The input value, unaltered
		 */
		_transformValue: function (value) {
			return value;
		}
	},

	defer: function () {
		// Ensure that Spinner "value" property is modified to contain a
		// 'transform' function.
		qx.Class.patch(qx.ui.form.Spinner, ms123.form.spinner.MAddTransform);
	}
});
