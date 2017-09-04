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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


qx.Class.define('ms123.ruleseditor.DateCellEditor', {
	extend: qx.ui.table.celleditor.TextField,

	construct: function () {
		this.base(arguments);
	},

	members: {
		createCellEditor: function (cellInfo) {
			var cellEditor = new ms123.ruleseditor.DateField;
//			cellEditor.setAppearance("table-editor-textfield");

			cellEditor.originalValue = cellInfo.value;
			if (cellInfo.value === null || cellInfo.value === undefined || cellInfo.value == '') {
				cellInfo.value = new Date().getTime();
			}
console.log("createCellEditor.value:"+cellInfo.value);
			cellEditor.setValue(new Date(cellInfo.value));
			cellEditor.selectAllText();

			return cellEditor;
		},

		getCellEditorValue: function (cellEditor) {
			var value = cellEditor.getValue();
console.log("getCellEditorValue.value:"+cellEditor.getValue().getTime());
			return value.getTime();
		}
	}
});
