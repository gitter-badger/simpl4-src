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
qx.Class.define('ms123.ruleseditor.CellRenderer', {

	extend: qx.ui.table.cellrenderer.Html,

	construct: function (model, table) {
		this.base(arguments);
		this._model = model;
	},


	members: {

		_model: null,

		_getCellAttributes: function (cellInfo) {
			return cellInfo.attributes || "";
		},

		_getCellStyle: function (cellInfo) {

			if (cellInfo.style) {
				return cellInfo.style;
			}

			var style = {
				"text-align": 'left',
				"color": '#606060',
				"font-style": 'Lucida Grande, Verdana, Arial',
				"font-weight": 'normal'
			};

			var value = cellInfo.value;

			if (value == null) {
				return "";
			}

			if (typeof value == "string") {
				if (value.substr(0, 1) == "=") {
					style['text-align'] = 'right';
				}
			} else if (typeof value == "number") {
				style['text-align'] = 'right';
			}
		},

		createDataCellHtml: function (cellInfo, htmlArr) {
			var extra = '';
			var extra2 = '';

			if (cellInfo.inSelection) {
				extra += 'background-color: #E0E0E0';
			}

			cellInfo.style = '';
			cellInfo.attributes = '';

			var value = cellInfo.value;
			if ((!isNaN(value) && value != '') || (typeof(value) == 'string' && value.substr(0, 1) == "=")) {
				extra2 = ' qooxdoo-table-cell-right';
			}

			htmlArr.push('<div class="', this._getCellClass(cellInfo), extra2, '" style="', 'left:', cellInfo.styleLeft, 'px;', this._getCellSizeStyle(cellInfo.styleWidth, cellInfo.styleHeight, this._insetX, this._insetY), extra, this._getCellStyle(cellInfo), '" ', this._getCellAttributes(cellInfo), '>' + this._formatValue(cellInfo), '</div>');

		},

		_formatValue: function (cellInfo) {

			var value = cellInfo.value;

			if (value == null || value == undefined || value.toString() == 'NaN') {
				return "";
			}

			if (typeof value == "string") {
					return value;
			}
			else if (typeof value == "number") {
				if (!qx.ui.table.cellrenderer.Default._numberFormat) {
					qx.ui.table.cellrenderer.Default._numberFormat = new qx.util.format.NumberFormat();
					qx.ui.table.cellrenderer.Default._numberFormat.setMaximumFractionDigits(10);
				}
				var res = qx.ui.table.cellrenderer.Default._numberFormat.format(value);
			}
			else if (value instanceof Date) {
				res = qx.util.format.DateFormat.getDateInstance().format(value);
			}
			else {
				res = value;
			}

			return res;
		},

		getModel: function () {
			return this._model;
		},

		setModel: function (model) {
			this._model = model;
		}
	}
});
