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
qx.Class.define("ms123.util.FormTest", {
	extend: qx.ui.container.Composite,

	/**
	 * Constructor
	 */
	construct: function (context) {
		this.base(arguments);
		this.setLayout(new qx.ui.layout.Dock());
		context.window.add(this, {});

		this.context = context;
		var id = this.context.data.id;
		console.log("FormTest:" + id);
		if (id != undefined && id) {
			var url = "data/" + this.context.moduleName + "/" + id;
			var map = ms123.util.Remote.sendSync(url + "?what=asRow");
			var json = map.json;
//			console.log("json:" + json);
			var context = {};
			context.formDesc = json.evalJSON();
			console.log("formDesc:" + context.formDesc.resourceId);
			var form = new ms123.widgets.Form(context);
			this.add(form, { edge: "center" });
		}
	},

	events: {
		"changeValue": "qx.event.type.Data"
	},
	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {

	}
});
