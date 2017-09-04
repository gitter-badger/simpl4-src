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
 * The default data cell renderer.
 */
qx.Class.define("ms123.util.TooltipRenderer", {
	extend: qx.ui.table.cellrenderer.Default,

/*

*****************************************************************************
     MEMBERS

*****************************************************************************
  */

	members: {
		// interface implementation
		createDataCellHtml: function (cellInfo, htmlArr) {
        htmlArr.push(
        '<div class="',
        this._getCellClass(cellInfo),
        '" style="',
        'left:', cellInfo.styleLeft, 'px;',
        this._getCellSizeStyle(cellInfo.styleWidth, cellInfo.styleHeight, this._insetX, this._insetY),
        this._getCellStyle(cellInfo), '" ',
        this._getCellAttributes(cellInfo),
        ' title="', this._getContentHtml(cellInfo),
        '">' +
        this._getContentHtml(cellInfo),
        '</div>'
         );
		}

	}
});
