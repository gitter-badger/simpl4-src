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
qx.Class.define("ms123.processexplorer.plugins.ShowImageRenderer", {
	extend: qx.ui.table.cellrenderer.Conditional,
	include: [qx.locale.MTranslation],

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
			this._msg = this.tr("processexplorer.definition.show_history");
			htmlArr.push(
			'<div class="', this._getCellClass(cellInfo), '" style="', 'left:', cellInfo.styleLeft, 'px;', 
			this._getCellSizeStyle(cellInfo.styleWidth, cellInfo.styleHeight, this._insetX, this._insetY), 
			this._getCellStyle(cellInfo), '" ', 
			this._getCellAttributes(cellInfo), '>' + 
			this._getContentHtml(cellInfo), 
			'</div>');
		},
		_getContentHtml: function (cellInfo) {
			return '<input title="'+this._msg+'" type="image" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlz'+
				'AAABuwAAAbsBOuzj4gAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAGnSURB'+
				'VDiNlZO/a1NRFMc/50Qnxb9ACi7SwQrVwTZVqeBobfUfEPrjHygtOLvo5KCzIOIuguhYS6sgSknt'+
				'UtOlFgONmKYQeeTyXnIccl9yX28WDxy4777z/XXgipkhImeBy/xf/TCzxin/caFe/71hmKoqqoqI'+
				'DBrAn3tH4db09H3gTU6AYQKwvVfrEQSgvMcvnu8RBDb6BKoKwOsP33pAr5TX/N1JtFSK7iOCB3eu'+
				'oaqoSOTg+14NgPHRkSEEHvDq/deBSp4/UF2cm6LkxQoE4hc3PzOBeAehekgyPIIffPnuSzS4dO86'+
				'V0ZHCsDYgQiiysJsGRHp70SGOAlrEOaE7RAMsLV7wNbuQUQURXjx9nM0FC4wyzKSJKHVarWLEfzg'+
				'4txUIUJO0Gj8oV77yY47Zm394061Wl2LdkDgILxfmC1zRtqUb9/k6bPn+6vLyzfMrA0g/jGNJUmy'+
				'rapSqf6K9uCco+SarG9+2n+4ujJpZod9BTMDuPQ3SVLnnKVpammaWpZl1ul0rNvt2lGzaY8eP6kA'+
				'58yMsHMHp4GrFN9JWF2gYmbu5I9/kCKj+JXx9kMAAAAASUVORK5CYII="/input>';
		},
		_getCellClass: function (cellInfo) {
			return "qooxdoo-table-cell";
		}
	}
});
