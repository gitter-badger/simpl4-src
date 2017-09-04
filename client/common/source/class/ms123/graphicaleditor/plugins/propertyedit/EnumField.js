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

qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.EnumField", {
	extend: ms123.graphicaleditor.plugins.propertyedit.ComplexListField,
	include : [ms123.graphicaleditor.plugins.propertyedit.MEnum],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (config, title, items, key, facade) {
		this.base(arguments, config, title, items, key, facade);
	},
	members: {
		setValue: function (value) {
			this.base(arguments, value);
			var data = value;
			if (value != undefined && value && value != "") {;
				try{
					value = qx.lang.Json.parse(value);
					console.log("EnumField.setValue:" + value.enumDescription);
					if (this.enumDisplay) this.enumDisplay.setValue(value.enumDescription);
					this.enumDescription = value.enumDescription;
				}catch(e){
					console.error("EnumField.setValue:"+value+" wrong value");
				}
			}
		},
		createToolbar: function () {
			var toolbar = this.base(arguments, ["add", "del"]);
			this.addButton.setEnabled(true);
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
