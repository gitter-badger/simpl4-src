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
 */
qx.Class.define("ms123.datamapper.ImageCellRenderer", {
	extend: qx.ui.table.cellrenderer.Conditional,

	/**
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

	properties: {},


/* 
  *****************************************************************************
     MEMBERS 
  *****************************************************************************
  */

	members: {
		createDataCellHtml: function (cellInfo, htmlArr) {
			htmlArr.push(
			'<div class="', this._getCellClass(cellInfo), '" style="', 'left:', cellInfo.styleLeft, 'px;top:2px;', 
			this._getCellSizeStyle(cellInfo.styleWidth, cellInfo.styleHeight, this._insetX, this._insetY), 
			this._getCellStyle(cellInfo), '" ', 
			this._getCellAttributes(cellInfo), '>' + 
			this._getContentHtml(cellInfo), 
			'</div>');
		},
		_getContentHtml: function (cellInfo) {
			return '<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJ'+
			'bWFnZVJlYWR5ccllPAAAAX1JREFUeNpi/P//PwMlgAWXhMdUEwUgpYAkdGFH9pkP6OoY0V0A1Tif'+
			'h53XQUlEjUGcV4rh5ednDJeengVJTwDiRhSDQAbAsPsUYwMgfr/+wrL/6ODLj0//e/Y0/AfKnwdi'+
			'AZgeZM0CQHx/1/XNYA13X9/8DzJo8amZ/y8+OQM3aMbhHpAh62H6kMMgwVXDV8FVwwfMmXGkF+Zs'+
			'MIgxS2OIMU1jSLcpZjh272AA0KsGQK9cYEIywN9KyR7OCdSPZOgKmMlQ79UD5iMbBlUXgBEL3Gy8'+
			'YPrrz88M6y8uR9GEDJRE1EGUPIhgwqbg0rOzYM1Wig5gV6ADkAVA8AHdBR9efn6O4hKQQV9+fcYw'+
			'4O6bWyDqIboLNu6+sRnM0JM2BgcaKB3A+CAMs/34/QMg5gZs6WA/KK5xAVBaKF2fBorGBmzRCA58'+
			'oCv2A20xQHYBCIBsXXJqFsj5C4DR14AzKUOTM0hBPCgvKAMNgfr5AjQZb8CbF0gFTAwUAoAAAwDH'+
			'8g30F/u66QAAAABJRU5ErkJggg=="/img>';
		},
		_getCellClass: function (cellInfo) {
			return "qooxdoo-table-cell";
		}
	}
});
