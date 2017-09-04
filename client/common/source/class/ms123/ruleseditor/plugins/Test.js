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
	* @ignore(Clazz)
*/

qx.Class.define("ms123.ruleseditor.plugins.Test", {
	extend: qx.core.Object,
	include: [qx.locale.MTranslation],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade, metadata) {
		this.base(arguments);
		this.facade = facade;
		this.metadata = metadata;

		var test_msg = this.tr("ruleseditor.test");
		var group = "4";
		this.facade.offer({
			name: test_msg,
			description: test_msg,
			icon: "icon/16/actions/media-playback-start.png",
			functionality: this.testRules.bind(this),
			group: group,
			index: 1,
			isEnabled: qx.lang.Function.bind(function () {
				return this.facade.getConditionColumns().length > 0 && this.facade.getActionColumns().length > 0 && this.facade.getCountRules() > 0;
			}, this)
		});
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {},
	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		testRules: function (e) {
			var callback = (function (values) {
				try {
					var ns = this.facade.storeDesc.getNamespace();
					var ret = ms123.util.Remote.rpcSync("workflow:testRules", {
						namespace: ns,
						values:values,
						name: this.facade.name
					});
					this.msgArea.setValue(qx.lang.Json.stringify(ret, null, 2));
				} catch (e) {
					ms123.form.Dialog.alert("RulesEdit.testRules failed:"+e);
					return;
				}
			}).bind(this);
			this._createForm(callback, null);
		},
		_createForm: function (callback, data) {
			var variables = this.facade.getConditionVariables();
			var formData = {}
			for (var i = 0; i < variables.length; i++) {
				var varname = variables[i].variable;
				var vartype = variables[i].vartype;
				var fieldtype = this._getFieldType(vartype);
				formData[varname] = {
					'type': fieldtype,
					'label': varname
				};
			}
			var f = qx.util.Serializer.toJson(formData);
			console.log("formData:" + f);
			var w = this._createWindow();

			var buttons = [{
				'label': this.tr("ruleseditor.test"),
				'icon': "icon/16/actions/dialog-ok.png",
				'callback': function (m) {
					var f = qx.util.Serializer.toJson(m);
					console.log("apply_changes:" + f);
					callback(m);
				},
				'value': 1
			},
			{
				'label': this.tr("ruleseditor.test_close"),
				'icon': "icon/16/actions/dialog-cancel.png",
				'callback': function (m) {
					w.destroy();
				},
				'value': 2
			}];

			var context = {};
			context.formData = formData;
			context.buttons = buttons;
			context.formLayout = [{
				id: "tab1"
			}];
			var form = new ms123.widgets.Form(context);
			if (data) form.fillForm(data);

			var container = new qx.ui.container.Composite(new qx.ui.layout.VBox());
			container.add(new qx.ui.basic.Label(this.tr("ruleseditor.table_must_first_be_saved")));
			container.add(form, {
				flex: 1
			});

			this.msgArea = new qx.ui.form.TextArea();
			container.add(this.msgArea);

			w.add(container);
			w.setActive(true);
			w.open();
		},
		_getFieldType: function (vartype) {
			var ret = "TextField";
			if (vartype == "integer") ret = "NumberField";
			if (vartype == "decimal") ret = "DecimalField";
			if (vartype == "date") ret = "DateField";
			if (vartype == "boolean") ret = "CheckBox";
			return ret;
		},
		_createWindow: function () {
			var win = new qx.ui.window.Window(this.tr("ruleseditor.rules_test")).set({
				resizable: false,
				useMoveFrame: false,
				useResizeFrame: false
			});
			win.setLayout(new qx.ui.layout.Grow);
			win.setWidth(400);
			win.setHeight(300);
			win.setAllowMaximize(false);
			win.setAllowMinimize(false);
			win.setModal(true);
			win.setActive(false);
			win.minimize();
			win.center();
			var root = qx.core.Init.getApplication().getRoot();

			root.add(win);
			win.addListener("close", function (e) {
				win.destroy();
			}, this);
			return win;
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
