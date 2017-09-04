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
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/places/*)
*/


qx.Class.define("ms123.report.FieldSelector", {
	extend: ms123.util.BaseFieldSelector,
	include: [qx.locale.MTranslation],

	events: {
		"treeClicked": "qx.event.type.Data"
	},


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this.base(arguments,context);
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_saveFields: function () {
			this._table.stopEditing();
			var rc = this._tableModel.getRowCount();
			var fields = [];
			for (var i = 0; i < rc; i++) {
				var rd = this._tableModel.getRowDataAsMap(i);
				fields.push(rd);
			}
			this._context.saveFields(fields);
		}
	}
});
