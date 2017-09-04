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
	@asset(qx/decoration/*)
	@asset(qx/decoration/Modern/tree/closed.png)
*/

qx.Class.define("ms123.form.ConfigurableSelectBox", {
	extend: qx.ui.form.SelectBox,

	implement: [qx.ui.form.IStringForm, qx.ui.form.IForm, ms123.form.IConfig],
	include: [qx.ui.form.MForm],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */


	construct: function (config, options) {
		this.base(arguments);
		this._optionsUrl = options;
	},


	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */


	properties: {},

	events: {
		"changeValue": "qx.event.type.Data"
	},


	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */
	members: {
		___onChangeSelection: function (e) {
			var model = e.getData()[0].getModel();
			this.fireDataEvent("changeValue", model.getValue(), null);
		},
		__before: function (context) {
			var app = qx.core.Init.getApplication();
			var params = ms123.util.Clone.merge({}, app.__userData, context.parentData);

			if (context.managedApp) {
				params["app_ns"] = context.managedApp;
			}

			var url = this.__supplant(this._optionsUrl, params);
			var url = this._optionsUrl;
			console.warn("aftersupplant:"+url);

			url = url.substring("delayed_rpc:".length);
			var obj = qx.lang.Json.parse(url); 
			var list = ms123.util.Remote.rpcSync( obj.method, obj.params );

			this.removeListener("changeSelection", this.___onChangeSelection, this);
			this.removeAll();
			var model = qx.data.marshal.Json.createModel(list);
			new qx.data.controller.List(model, this, "label");
			this.addListener("changeSelection", this.___onChangeSelection, this);

			var selectables = this.getSelectables(true);
			if (selectables.length > 0) {
				var model = selectables[0].getModel();
				this.setSelection([selectables[0]]);

				this.fireDataEvent("changeValue", model.getValue(), null);
			}
		},
    beforeSave : function(context) {
    },
		beforeAdd: function (context) {
			this.__before(context);
		},
		beforeEdit: function (context) {
			this.__before(context);
		},
		afterSave: function (context) {
		},
		__createListFromUrl: function (url) {
			return ms123.util.Remote.sendSync(url);
		},
		__supplant: function (s, o) {
			if (!o) return s;
			return s.replace(/@{([^{}]*)}/g, function (a, b) {
				var r = o[b];
				return typeof r === 'string' || typeof r === 'number' ? r : a;
			});
		},

		setValue: function (value) {
			if (value == null || value == "") {
				this.fireDataEvent("changeValue", this.getValue(), null);
			} else {
				var selectables = this.getSelectables(true);
				for (var i = 0; i < selectables.length; i++) {
					var model = selectables[i].getModel();
					console.log("mv:" + model.getValue());
					if (model.getValue() == value) {
						this.setSelection([selectables[i]]);
					}
				}
			}
		},

		getValue: function () {
			var selection = this.getSelection();
			if (selection == null || selection.length == 0) return null;
			var model = selection[0].getModel();
			console.log("getValue.value:" + model.getValue());
			return model.getValue();
		},

		resetValue: function () {}

	}


});
