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
	* @ignore(Hash)
*/
qx.Class.define('ms123.ruleseditor.HeaderRenderer', {
	extend: qx.ui.table.headerrenderer.Default,

	construct: function () {
		this.base(arguments);
	},

	members: {
		// overridden
		createHeaderCell: function (cellInfo) {
			var widget = new ms123.ruleseditor.HeaderCell();
			widget.setFont(qx.bom.Font.fromString("9px sans-serif")),
console.log("cellInfo.col:"+cellInfo.col);
			var model = cellInfo.table.getTableModel();
			var colid = model.getColumnId(cellInfo.col);
			if( colid.match("^C")){
				widget.setBackgroundColor("#e1dbb1");
			}else{
				widget.setBackgroundColor("#b6b5ca");
			}
			this.updateHeaderCell(cellInfo, widget);
			return widget;
		}
	}
});
