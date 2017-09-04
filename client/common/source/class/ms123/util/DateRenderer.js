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
 @ignore(moment)
 */
/**
 * Specific data cell renderer for dates.
 */
qx.Class.define("ms123.util.DateRenderer", {
	extend: qx.ui.table.cellrenderer.Conditional,

/*
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
		_getContentHtml: function (cellInfo) {
			var m = qx.locale.Manager.getInstance();
			var lang = m.getLanguage();
			var format = "MM-DD-YYYY";
			if (lang == "de") {
				format = "DD.MM.YYYY";
			}

			try {
				var t = parseInt(cellInfo.value);
				if (!isNaN(t)) {
					return moment(t).format(format);
				}
				return "";
			} catch (e) {
				return "";
			}

		},

		// overridden
		_getCellClass: function (cellInfo) {
			return "qooxdoo-table-cell";
		}
	}
});
