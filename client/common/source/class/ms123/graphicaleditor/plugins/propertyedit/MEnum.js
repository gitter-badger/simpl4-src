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
qx.Mixin.define("ms123.graphicaleditor.plugins.propertyedit.MEnum", {
  members: {
		createSelectedEnumDisplay: function () {
			var control = new qx.ui.form.TextField();
			control.setFocusable(false);
			control.setReadOnly(true);
			control.setEnabled(false);
			control.addState("inner");
			return control;
		},
		handleNodeSelected: function (e) {
			var model = e.getData().model;
			var type = e.getData().type;
			console.log("button:" + this.delButton);
			console.log("xtype:" + type);
			console.log("xmodel:" + model.getValue() + "/" + type);
			this.addButton.setEnabled(false);
			if (type == "sw.enum" || type == "sw.filter" || type == "camelparam_route") {
				this.enumDisplay.setValue(type + ":" + model.getValue());
				var childs = model.getChildren();
				this.setTableData([]);
				if( type != "camelparam_route"){
					for (var i = 0; i < childs.getLength(); i++) {
						var field = model.getChildren().getItem(i);
						var map = {};
						map.colname = field.getValue();
						this.addRecord(map);
					}
				}else{
					this.addButton.setEnabled(true);
				}
			}
		},
		handleOkButton: function (e) {
			this.table.stopEditing();
			var value = this.getTableData();
			var data = {
				totalCount: value.length,
				enumDescription: this.enumDisplay.getValue(),
				items: value
			};

			data = qx.util.Serializer.toJson(data);
			console.log("data:" + data);
			var oldVal = this.data;
			this.data = data;
			this.fireDataEvent("changeValue", data, oldVal);
			this.editWindow.close();
		}
	}
});
