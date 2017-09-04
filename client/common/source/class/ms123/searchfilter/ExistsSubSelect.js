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
qx.Class.define("ms123.searchfilter.ExistsSubSelect", {
	extend: qx.ui.core.Widget,

	construct: function (params) {
		this.params = params;
		this.base(arguments);

		var hboxLayout = new qx.ui.layout.HBox().set({
			spacing: 4,
			alignX: "right"
		});

		this._setLayout(hboxLayout);

		this._add(this.getChildControl("exists_subselect"), { });
		this._add(this.getChildControl("filtername"), { });
		this._add(this.getChildControl("params"), { });
	},

	properties: {
		field: {
			check: "String",
			init: "",
			event: "changeField"
		},
		op: {
			check: "String",
			init: "",
			event: "changeOp"
		},
		data: {
			check: "String",
			init: "",
			event: "changeData"
		}
	},
	events: {
		"change": "qx.event.type.Data"
	},

	members: {
		// overridden
		_createChildControlImpl: function (id) {
			var control;

			switch (id) {
			case "exists_subselect":
				control = new qx.ui.form.SelectBox();
				control.setWidth(170);
				var tempItem = this.getChildControl("exists_subselect_item");
				control.add(tempItem);
				tempItem = this.getChildControl("exists_not_subselect_item");
				control.add(tempItem);
				control.addListener("changeSelection", function (e) {
					if (e.getData().length > 0) {
						this.fireDataEvent("changeField", e.getData()[0].getModel(), null);
					}
				}, this);
				break;
			case "exists_subselect_item":
				control = new qx.ui.form.ListItem(this.tr("filter.exists_subselect"), null, "_exists_subselect");
				break
			case "exists_not_subselect_item":
				control = new qx.ui.form.ListItem(this.tr("filter.exists_not_subselect"), null, "_exists_not_subselect");
				break
			case "connector_label":
				control = new qx.ui.basic.Label(this.tr("filter.text.and_help"));
				control.setRich( true );
				break
			case "filtername":
				control = new qx.ui.form.TextField();
				control.setWidth(150);
				control.setFilter("[0-9a-z_]");
				control.addListener("changeValue", function (e) {
					console.log("changeValue:" + e.getData());
					if (e.getData().length > 0) {
						this.fireDataEvent("changeOp", e.getData(), null);
					} else {
						this.fireDataEvent("changeOp", "", null);
					}
				}, this);
				break
			case "params":
				control = new qx.ui.form.TextField();
				control.setWidth(250);
				control.setFilter("[0-9a-z_ . =]");
				control.addListener("changeValue", function (e) {
					console.log("changeValue:" + e.getData());
					if (e.getData().length > 0) {
						this.fireDataEvent("changeData", e.getData(), null);
					} else {
						this.fireDataEvent("changeData", "", null);
					}
				}, this);
				break
			}

			return control || this.base(arguments, id);
		},

		getField: function () {
			var x = this.getChildControl("exists_subselect").getModelSelection();
			return x.getItem(0);
		},
		setField: function (value) {
		  this.fireDataEvent("change", value, null);
			this.getChildControl("exists_subselect").setModelSelection([value]);
		},
		getOp: function () {
			return this.getChildControl("filtername").getValue();
		},
		setOp: function (value) {
		  this.fireDataEvent("change", value, null);
		  this.getChildControl("filtername").setValue(value);
		},
		getData: function () {
			return this.getChildControl("params").getValue();
		},
		setData: function (value) {
		  this.fireDataEvent("change", value, null);
		  this.getChildControl("params").setValue(value);
		}
	}
});
