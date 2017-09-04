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
qx.Class.define("ms123.searchfilter.Connector", {
	extend: qx.ui.core.Widget,

	construct: function (params) {
		this.params = params;
		this.base(arguments);

		this._setLayout(new qx.ui.layout.HBox());

		this._add(this.getChildControl("connector"), {
		});
		this._add(this.getChildControl("connector_label"), {
		});
	},

	properties: {
		value: {
			check: "String",
			init: "",
			apply: "_applyValue",
			event: "changeValue"
		}
	},
	events: {
		"changeValue": "qx.event.type.Data"
	},

	members: {
		// overridden
		_createChildControlImpl: function (id) {
			var control;

			switch (id) {
			case "connector":
				control = new qx.ui.form.SelectBox();
				var tempItem = this.getChildControl("connector_and");
				control.add(tempItem);
				tempItem = this.getChildControl("connector_or");
				control.add(tempItem);
				tempItem = this.getChildControl("connector_not");
				control.add(tempItem);
				tempItem = this.getChildControl("connector_and_not");
				control.add(tempItem);
				control.addListener("changeSelection", function (e) {
					if (e.getData().length > 0) {
						this.fireDataEvent("changeValue", e.getData()[0].getModel(), null);
						this.getChildControl("connector_label").setValue(this.tr("filter.text."+e.getData()[0].getModel()+"_help"));
					}
				}, this);
				break;
			case "connector_and":
				control = new qx.ui.form.ListItem(this.tr("filter.text.and"), null, "and");
				break
			case "connector_or":
				control = new qx.ui.form.ListItem(this.tr("filter.text.or"), null, "or");
				break
			case "connector_not":
				control = new qx.ui.form.ListItem(this.tr("filter.text.not"), null, "not");
				break
			case "connector_and_not":
				control = new qx.ui.form.ListItem(this.tr("filter.text.and_not"), null, "and_not");
				break;
			case "connector_label":
				control = new qx.ui.basic.Label(this.tr("filter.text.and_help"));
				control.setRich( true );
				break

			}

			return control || this.base(arguments, id);
		},

		_applyValue: function (value, old) {
			this.getChildControl("connector").setModelSelection([value]);
		}
	}
});
