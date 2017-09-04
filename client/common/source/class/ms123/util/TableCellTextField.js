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
qx.Class.define("ms123.util.TableCellTextField", {
	extend: qx.ui.table.celleditor.AbstractField,
	construct: function (options) {
		this.options = options;
	},

	members: {
		// overridden
		getCellEditorValue: function (cellEditor) {
			var value = cellEditor.getValue();

			// validation function will be called with new and old value
			var validationFunc = this.getValidationFunction();
			if (validationFunc) {
				value = validationFunc(value, cellEditor.originalValue);
			}

			if (typeof cellEditor.originalValue == "number") {
				if (value != null) {
					value = parseFloat(value);
				}
			}
			return value;
		},


		_createEditor: function () {
			var cellEditor = new qx.ui.form.TextField();
			if( this.options && this.options.filter){
				cellEditor.setFilter(this.options.filter);
			}
			cellEditor.setAppearance("table-editor-textfield");
			return cellEditor;
		}
	}
});
