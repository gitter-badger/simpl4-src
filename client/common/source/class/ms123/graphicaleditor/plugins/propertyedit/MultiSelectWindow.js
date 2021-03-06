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
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/apps/*)
	@asset(qx/icon/${qx.icontheme}/48/actions/*)
	@asset(qx/icon/${qx.icontheme}/48/apps/*)
	@ignore($)
*/
qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.MultiSelectWindow", {
	extend: qx.core.Object,
	include: [qx.locale.MTranslation],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (context, curValue) {
		this.base(arguments);

		this._selectablesDesc = context.selectablesDesc;
		this.__storeDesc = context.storeDesc;
		this._selected_callback = context.selected_callback;
		var title = context.title;

		var selectables = this._getSelectables(context.selectablesDesc);
		var win = this._createWindow(title);
		var centerContent = new qx.ui.container.Composite();
		centerContent.setLayout(new qx.ui.layout.Dock());

		var buttons = this._createButtons(win);
		this._doubleSelectBox = this._createDoubleSelectBox(selectables);
		centerContent.add(this._doubleSelectBox, {
			edge: "center"
		});
		win.add(centerContent, {
			edge: "center"
		});

		win.add(buttons, {
			edge: "south"
		});
		if (curValue && this._checkValue(curValue)) {
			this._value = curValue.split(",");
			this._doubleSelectBox.setSelection(this._value);
		} else {
			this._value = [];
		}
		var app = qx.core.Init.getApplication();
		app.getRoot().add(win);
		win.open();
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	properties: {},

	/**
	 *****************************************************************************
	 EVENTS
	 *****************************************************************************
	 */

	events: {
		/** Whenever the value is changed this event is fired
		 *
		 *  Event data: The new text value of the field.
		 */
		"changeValue": "qx.event.type.Data"
	},


	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_createButtons: function (win, table) {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing(5);
			toolbar.addSpacer();
			toolbar.addSpacer();

			this._buttonSelect = new qx.ui.toolbar.Button(this.tr("Ok"), "icon/16/actions/dialog-ok.png");
			this._buttonSelect.addListener("execute", function () {
				console.log("relpath:" + qx.util.Serializer.toJson(this._value));
				if (this._selected_callback) {
					this._selected_callback(this._value.join());
				}
				win.close();
			}, this);
			toolbar._add(this._buttonSelect)
			var buttonCancel = new qx.ui.toolbar.Button(this.tr("Cancel"), "icon/16/actions/dialog-close.png");
			buttonCancel.addListener("execute", function () {
				win.close();
			}, this);
			toolbar._add(buttonCancel)
			return toolbar;
		},

		_getSelectables: function (selectablesDesc) {
			var list=null;
			if( typeof selectablesDesc == "string" ){
				var p = selectablesDesc.split(":");
				var t = ms123.util.Remote.rpcSync(p[0] + ":" + p[1]);
				if( t.rows != null){
					t = t.rows;
				}
				list = [];
				for (var i = 0; i < t.length; i++) {
					var value = t[i][p[2]];
					var o = {
						value: value,
						label: value
					}
					list.push(o);
				}
				this._selectables = list;
			}else{
				list = selectablesDesc;
				this._selectables = list;
			}
			console.log("list:" + qx.util.Serializer.toJson(list));
			return list;
		},

		_createDoubleSelectBox: function (selectables) {
			var control = new ms123.form.DoubleSelectBox();
			var model = qx.data.marshal.Json.createModel(selectables);
			control.setModel(model);
			control.setSelection([]);
			control.addListener("changeSelection", function (e) {
				this._value = e.getData();
			}, this);
			return control;
		},
		_selectablesContains: function (x) {
			for (var i = 0; i < this._selectables.length; i++) {
				if (x == this._selectables[i].value) return true;
			}
			return false;
		},
		_checkValue: function (v) {
			try {
				var l = v.split(",");
				for (var i = 0; i < l.length; i++) {
					if (!this._selectablesContains(l[i])) {
						return false;
					}
				}
			} catch (e) {
				return false;
			}
			return true;
		},
		_createWindow: function (name) {
			var win = new qx.ui.window.Window(name, "").set({
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			});
			win.setLayout(new qx.ui.layout.Dock);
			win.setWidth(500);
			win.setHeight(400);
			win.setAllowMaximize(false);
			win.setAllowMinimize(false);
			win.setModal(true);
			win.setActive(false);
			win.minimize();
			win.center();
			win.addListener("close", function () {
				win.destroy();
			}, this);
			return win;
		}
	}
});
