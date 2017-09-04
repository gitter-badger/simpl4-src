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
qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.DefinitionSelect", {
	extend: qx.core.Object,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (facade, formElement, config) {
		this.base(arguments);
		this._facade = facade;
		this._init(formElement, config);
	},
	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_init: function (formElement, config) {
			var json = this._facade.getJSON();

			var item = new qx.ui.form.ListItem("-", null, null);
			formElement.add(item);

			var defs = json.properties[config.definitions];
			console.log("defs:",defs);
			for (var i = 0; i < defs.length; i++) {
				var d = defs[i];
				var item = new qx.ui.form.ListItem(d.name, null, d.id);
				formElement.add(item);
			}
		}
	}
});
