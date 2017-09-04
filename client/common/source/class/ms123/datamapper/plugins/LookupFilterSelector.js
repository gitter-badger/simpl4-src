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

qx.Class.define("ms123.datamapper.plugins.LookupFilterSelector", {
	extend: ms123.graphicaleditor.plugins.propertyedit.ComplexListField,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (config, title, items, key, facade) {
		this.base(arguments, config, title, items, key, facade);
	},
	members: {
		setValue: function (value) {
			this.base(arguments, value);
			if (value != undefined && value && value != "") {;
				try{
					value = qx.lang.Json.parse(value);
					this._value = value;
					//console.log("FilterParamField.setValue:" + JSON.stringify(value,null,2));
					if (this._filterForm) this._filterForm.setData(value);
				}catch(e){
					console.error("LookupFilterSelector.setValue:"+value+" wrong value");
				}
			}else{
				this._value = {};
			}
		},
		handleNodeSelected: function (e) {
			var model = e.getData().model;
			var type = e.getData().type;
			if (type == "sw.filter") {
				this._filterForm.setData({name:model.getValue()});
				var childs = model.getChildren().getItem(0).getChildren();
				this.setTableData([]);
				for (var i = 0; i < childs.getLength(); i++) {
					var field = childs.getItem(i);
					var map = {};
					map.param = field.getValue();
					this.addRecord(map);
				}
			}
		},
		_createForm: function () {
			var formData = {
				"name": {
					'type': "TextField",
					'label': this.tr("filtercheck.name"),
					'readonly': true,
					'value': ""
				}
			};

			var self = this;
			var form = new ms123.form.Form({
				"tabs": [{
					id: "tab1",
					layout: "single"
				}],
				"useScroll": false,
				"formData": formData,
				"buttons": [],
				"inWindow": false,
				"context": self
			});
			this._filterForm = form;
			return form;
		},
		handleOkButton: function (e) {
			this.table.stopEditing();
			var value = this.getTableData();
			var formData = this._filterForm.getData();
			var data = null;
			if( value.length > 0){
				data = {
					totalCount: value.length,
					name: formData.name,
					items: value
				};
				data = qx.util.Serializer.toJson(data);
			}

			console.log("data:" + data);
			var oldVal = this.data;
			this.data = data;
			this.fireDataEvent("changeValue", data, oldVal);
			this.editWindow.close();
		},
		createToolbar: function () {
			var toolbar = this.base(arguments, ["del"]);
			var form = this._createForm();
			if (this._value) form.setData(this._value);
			var container = new qx.ui.container.Composite();
			container.setLayout(new qx.ui.layout.Dock());
			container.add(toolbar, {
				edge: "north"
			});
			container.add(form, {
				edge: "center"
			});
			return container;
		}
	}
});
