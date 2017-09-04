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
/*
*/

qx.Class.define("ms123.datamapper.create.CSVInlineFieldsEditor", {
	extend: ms123.datamapper.create.CSVFieldsEditor,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade, side) {
		this.base(arguments,facade,side);
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_init:function(){
			this._form = this._createForm(this._facade);
		},
		_createForm:function(){
			var formData = {
				"quote": {
					'type': "ComboBox",
					'label': this.tr("export.csv.quote"),
					'value': '\"',
					'options': [{
						'label': "\""
					},
					{
						'label': "'"
					}]
				},
				"columnDelim": {
					'type': "ComboBox",
					'label': this.tr("export.csv.col_delimeter"),
					'value': ',',
					'options': [{
						'label': ","
					},
					{
						'label': "TAB"
					},
					{
						'label': ";"
					}]
				},
				"header": {
					'type': "CheckBox",
					'label': this.tr("export.csv.include_column_header"),
					'value': true
				}
			};
			return this.__createForm(formData, "single");
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
