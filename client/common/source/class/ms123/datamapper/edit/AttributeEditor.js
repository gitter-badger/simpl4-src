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

qx.Class.define("ms123.datamapper.edit.AttributeEditor", {
	extend: ms123.datamapper.edit.AbstractFieldsEditor,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade, context, data) {
		var title = this.tr("datamapper.edit_attribute");
		this._side = context.side;
		this._data  = data;
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
						required: true,
						filter:/[a-zA-Z0-9_]/,
						validator: "/^[A-Za-z]([0-9A-Za-z_]){0,48}$/"
					},
					'value': ""
				},
				"fieldType": {
					'type': "SelectBox",
					'label': this.tr("datamapper.datatype"),
					'value': "string",
					'options': this._getDatatypes()
				},
				"type": {
					'type': "SelectBox",
					'label': this.tr("datamapper.nodetype"),
					'value': "string",
					'options': this._getNodetypes()
				},
				"fieldFormat": {
					'type': "TextField",
					'label': this.tr("datamapper.format"),
					'value': ""
				},
				"fieldWidth" : {
					'type': "NumberField",
					'label': this.tr("datamapper.width"),
					'validation': {
						required: true
					},
					'value': 20 
				}
			};
			if( this._format != ms123.datamapper.Config.FORMAT_FW){
				delete formData.fieldWidth;
			}
			if( this._format == ms123.datamapper.Config.FORMAT_MAP ){
				formData.fieldFormat.exclude= "fieldType!='string'";
			}
			if( this._side != ms123.datamapper.Config.OUTPUT || !this._data.root){
				delete formData.type;
			}
			if( this._format == ms123.datamapper.Config.FORMAT_POJO){
				delete formData.fieldFormat;
			}
			if( this._data.type == ms123.datamapper.Config.NODETYPE_COLLECTION ||
					this._data.type == ms123.datamapper.Config.NODETYPE_ELEMENT
				){
				delete formData.fieldType;
				delete formData.fieldFormat;
			}
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
