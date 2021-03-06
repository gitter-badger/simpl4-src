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
qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.ImageRenderer", {
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
			'<div class="', this._getCellClass(cellInfo), '" style="', 'left:', cellInfo.styleLeft, 'px;', 
			this._getCellSizeStyle(cellInfo.styleWidth, cellInfo.styleHeight, this._insetX, this._insetY), 
			this._getCellStyle(cellInfo), '" ', 
			this._getCellAttributes(cellInfo), '>' + 
			this._getContentHtml(cellInfo), 
			'</div>');
		},
		_getContentHtml: function (cellInfo) {
			return '<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAACXBIWXMAAAsTAAALEwEAmpwYAAAA'+
			'B3RJTUUH3QYXDAkVlf+ZEQAAAwlJREFUKM8FwW1ME2ccAPD/PXdPr9eXK1coraAiTiQaRWUhJE5G'+
			'fPlixGAYZlnMssg+mjizLOMLmzHGGDUZHxTRhA2WbQ6TiYrRjMwQYzB2OuYkgrEjKKWF0pcr7bV3'+
			'3PXu/v5+jIUrxPSBDmCC6QMFCjIkXOAMwNoCgA6qF9BVIlASgEAJgTMsiiVdcPF5UAuQH/jzqslq'+
			'z56H+TLdNMUDzUc7mj8NEZF3QiwV9QUDDNqYtzWD1R7FxoZGr21t2uTxOkxDi5N/dM0feWXIs+a5'+
			'k2cag3XroMIJDmY1hWaFGlYf9o1e+LitycHjo/Ex0ed6m5+3bbdJxYWUbGSt3uM/tJp7Q1oVgxom'+
			'6FzXlU9aju2K/P/SSZnVUl7RswlBzE7JFVb54fa2a78NWknX5MU5f1EgBWXl8dT4tj1b8mp2XWj9'+
			'4lyKYXmH35tzYJDdMPjZ0Bdi18g3dylx9U30RfgUYQL0dniUBoVIdOH532/qaptllX0y+5afdPd3'+
			'DWx0Nonumon4TMarjIz/HuAIWYJMEleK1Ppv+s3OHS3/vpiNyUUhVHmv80691mCxzI/zt07cPSlL'+
			'KS0jS5pE/FCuYDbxeumnA8NfMT39R28EZuqHdv+1hn6gCMuXls+euPMlOC2fVslBKCHoBCwq4Zp4'+
			'JnF2/Pvs5sUKj+tBz81dhZqMe+6XqeE/ntz3VEuAQm4quVYKicATqtJTR7rzBX268lXPxLc5M6VG'+
			'0jxxDyV/7X85OBl+UVABlgHQ097a5kJCvB5mi1BvvCMJ3RiZfXz+fq++GW/OX+6+fTrCzsNWNxhe'+
			'WKJNtR/t39kKusEY9gpF39PVZ/uu76NVnJQOABpRNgocEIfXXrTgnV1T/uHpzq8/bzzMqTYxDB0Y'+
			'qNarr3cMuKf90YV4jE+DAHxStMMKROj2subvOk51Nh7igAGeZdBCuZiWvGUMcDGID8/c6P25r4wG'+
			'raxdV1Xbtufg3oaWjVINB4yczfglP6CKsp0roYJpRAVzGCuigjJm0CyihTaiYaJhoVlCNHRcfQ+7'+
			'No7XtCvvgQAAAABJRU5ErkJggg=="/img>';
		},
		_getCellClass: function (cellInfo) {
			return "qooxdoo-table-cell";
		}
	}
});
