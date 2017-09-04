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
qx.Class.define('ms123.ruleseditor.ColumnModel', {

	extend: qx.ui.table.columnmodel.Resize,

	construct: function (obj) {
		this.base(arguments);
		this._table = obj;
	},

	statics: { /** {int} the default width of a column in pixels. */
		DEFAULT_WIDTH: 100,
		/** {DefaultDataCellRenderer} the default header cell renderer. */
		DEFAULT_HEADER_RENDERER: qx.ui.table.headerrenderer.Default,
		/** {DefaultDataCellRenderer} the default data cell renderer. */
		DEFAULT_DATA_RENDERER: ms123.ruleseditor.CellRenderer,
		/** {TextFieldCellEditorFactory} the default editor factory. */
		DEFAULT_EDITOR_FACTORY: qx.ui.table.celleditor.TextField
	},

	members: {

		init: function (colCount, param) {
			this.base(arguments, colCount, param);
			for (var i = 0; i < colCount; ++i) {
				this.setDataCellRenderer(i, new ms123.ruleseditor.CellRenderer(this._table.getTableModel(), this._table));
				this.setHeaderCellRenderer(i, new ms123.ruleseditor.HeaderRenderer());
				
			}
		},

		moveColumn: function (a, b) {

		}
	}
});
