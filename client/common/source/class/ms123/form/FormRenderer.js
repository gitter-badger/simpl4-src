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
 qooxdoo dialog library
 
 http://qooxdoo.org/contrib/project#dialog
 
 Copyright:
 2007-2010 Christian Boulanger
 
 License:
 LGPL: http://www.gnu.org/licenses/lgpl.html
 EPL: http://www.eclipse.org/org/documents/epl-v10.php
 See the LICENSE file in the project's top-level directory for details.
 
 Authors:
 *  Christian Boulanger (cboulanger)
 ************************************************************************ */

qx.Class.define("ms123.form.FormRenderer", {
	extend: qx.ui.form.renderer.Single,
	implement: qx.ui.form.renderer.IFormRenderer,

	members: {
		_row: 0,
		_buttonRow: null,

		addItems: function (items, names, title) {
			if (title != null) {
				this._add(
				this._createHeader(title), {
					row: this._row,
					column: 0,
					colSpan: 2
				});
				this._row++;
			}

			for (var i = 0; i < items.length; i++) {
				var item = items[i];
				if (item instanceof qx.ui.form.RadioGroup) {
					if (item.getUserData("orientation") == "horizontal") {
						var widget = this._createHBoxForRadioGroup(item);
					}
					else {
						var widget = this._createWidgetForRadioGroup(item);
					}
				} else {
					var widget = item;
				}

				if (names[i] && item.getUserData("excluded")) {
					var label = new qx.ui.basic.Label(names[i]);
					label.setRich(true);
					this._add(label, {
						row: this._row,
						column: 0,
						colSpan: 2
					});
				} else if (!names[i]) {
					this._add(widget, {
						row: this._row,
						column: 0,
						colSpan: 2
					});
				} else {
					var label = this._createLabel(names[i], item);
					label.setRich(true);
					this._add(label, {
						row: this._row,
						column: 0
					});
					this._add(widget, {
						row: this._row,
						column: 1
					});
				}
				this._row++;

			}
		},

		_createWidgetForRadioGroup: function (group) {
			var widget = new qx.ui.container.Composite(new qx.ui.layout.VBox(5));
			var items = group.getItems();
			for (var i = 0; i < items.length; i++) {
				widget.add(items[i]);
			}
			return widget;
		},

		_createHBoxForRadioGroup: function (group) {
			var widget = new qx.ui.container.Composite(new qx.ui.layout.HBox(5));
			var items = group.getItems();
			for (var i = 0; i < items.length; i++) {
				widget.add(items[i]);
			}
			return widget;
		}
	}
});
