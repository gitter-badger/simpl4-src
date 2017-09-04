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

qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.EnumWindow", {
	extend: ms123.graphicaleditor.plugins.propertyedit.ComplexListWindow,
	include : [ms123.graphicaleditor.plugins.propertyedit.MEnum],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (config, title, items, data, facade,data) {
		this.base(arguments, config, title, items, null, facade,data);
	},
	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		createToolbar: function () {
			var toolbar = this.base(arguments, ["del","add"]);
			var buttonDel = new qx.ui.toolbar.Button("", "icon/16/places/user-trash.png");
			buttonDel.addListener("execute", function () {
				this.enumDisplay.setValue(null);
				this.setValue(null);
				this.setTableData([]);
			}, this);
			toolbar._add(buttonDel);

			this.enumDisplay = this.createSelectedEnumDisplay();
			if (this.enumDescription) this.enumDisplay.setValue(this.enumDescription);
			var container = new qx.ui.container.Composite();
			container.setLayout(new qx.ui.layout.Dock());
			container.add(toolbar, {
				edge: "north"
			});
			container.add(this.enumDisplay, {
				edge: "center"
			});
			return container;
		}
	}

});
