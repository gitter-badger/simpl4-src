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

qx.Class.define("ms123.datamapper.edit.StructureNameEditor", {
	extend: ms123.datamapper.edit.AbstractFieldsEditor,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade, context, data) {
		var title = this.tr("datamapper.edit_structure_name");
		this.base(arguments,facade,context, title,data);
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
						required: true
					},
					'value': ""
				},
				"map2parent": {
					'type': "Checkbox",
					'label': this.tr("datamapper.map2parent"),
					'value': true
				}
			};
			return this.__createForm(formData, "single");
		},
		__createForm:function(formData, layout){
			var context = {};
			context.formData = formData;
			context.buttons = [];
			context.formLayout = [{
				id: "tab1", lineheight:-1
			}];
			var form = new ms123.widgets.Form(context);
			return form;
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
