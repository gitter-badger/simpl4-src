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

qx.Class.define("ms123.datamapper.create.MapFieldsEditor", {
	extend: ms123.datamapper.edit.AbstractFieldsEditor,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade, data,isPojo) {
		var title = this.tr("datamapper.define") + (isPojo ? this.tr("POJO") : this.tr("Map"));
		this.base(arguments,facade,null,title,data);
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createForm:function(){
			var formData = {
				"name": {
					'type': "TextField",
					'label': this.tr("datamapper.name"),
					'validation': {
						required: true,
						filter:/[a-zA-Z0-9_]/,
						validator: "/^[A-Za-z]([0-9A-Za-z_]){0,48}$/"
					},
					'value': ""
				},
				"type": {
					'type': "SelectBox",
					'label': this.tr("datamapper.type"),
					'value': ms123.datamapper.Config.NODETYPE_ELEMENT,
					'options': [ {
						value: ms123.datamapper.Config.NODETYPE_ELEMENT,
						label: "Element"
					},{
						value: ms123.datamapper.Config.NODETYPE_COLLECTION,
						label: "List<Element>"
					}]
				},
				"children": this._fieldsFormData()
			};
			return this.__createForm(formData, "double");
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
