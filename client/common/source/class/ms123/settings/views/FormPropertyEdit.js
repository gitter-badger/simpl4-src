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
qx.Class.define("ms123.settings.views.FormPropertyEdit", {
	extend: ms123.settings.PropertyEdit,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (facade) {
		this.base(arguments, facade);
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_createEditForm: function () {
			var formData = {
				"formlayout": {
					'type': "TextField",
					'label': this.tr("settings.views.propertyedit.formlayout"),
					'validation': {
						required: false,
						validator: "/^[A-Za-z]([0-9A-Za-z_.:;,]){0,60}$/"
					},
					'value': "tab1"
				},
				"customForm": {
					'type': "TextField",
					'label': this.tr("settings.views.propertyedit.customForm"),
					'validation': {
						required: false,
						validator: "/^[A-Za-z]([0-9A-Za-z._]){0,60}$/"
					},
					'value': null
				},
				"loadBeforeEdit":{
					type: "CheckBox",
					defaultValue:false,
					label: this.tr("settings.loadBeforeEdit")
				},
				"modal":{
					type: "CheckBox",
					defaultValue:true,
					label: this.tr("settings.modal")
				}
			}
			this._form = new ms123.form.Form({
				"tabs": [{
					id: "tab1",
					layout: "single",
					lineheight: 20
				}],
				"formData": formData,
				"allowCancel": true,
				"inWindow": false,
				"buttons": [],
				"callback": function (m, v) {},
				"context": null
			});
			return this._form;
		}
	}
});
