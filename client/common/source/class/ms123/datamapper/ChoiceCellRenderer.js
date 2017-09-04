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
qx.Class.define("ms123.datamapper.ChoiceCellRenderer", {
	extend: qx.ui.table.cellrenderer.Replace,

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		// overridden
		_getContentHtml: function (cellInfo) {
			var value = cellInfo.value;
			var replaceMap = this.getReplaceMap();
			var replaceFunc = this.getReplaceFunction();
			var label;

			if (replaceMap) {
				label = replaceMap[value];
				if (typeof label != "undefined") {
					cellInfo.value = label;
					return qx.util.StringEscape.escape(this._formatValue(cellInfo), qx.bom.String.FROM_CHARCODE);
				}
			}

			if (replaceFunc) {
				cellInfo.value = replaceFunc(value);
			}
			return qx.util.StringEscape.escape(this._formatValue(cellInfo), qx.bom.String.FROM_CHARCODE);
		}
	}
});
