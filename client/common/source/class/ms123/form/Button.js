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
qx.Class.define("ms123.form.Button", {
	extend: qx.ui.core.Widget,
	implement: [
	qx.ui.form.IStringForm, qx.ui.form.IForm, ms123.form.IConfig],
	include: [
	qx.ui.form.MForm],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (useitCheckboxes) {
		this.base(arguments);
		var layout = new qx.ui.layout.HBox();
		this._setLayout(layout);

		var select = this._createChildControl("select");
		if( useitCheckboxes ){
			this._createChildControl("checkbox");
		}
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	properties: {
		// overridden
		//		appearance: {
		//			refine: true,
		//			init: "combobox"
		//		}
	},

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
    beforeSave : function(context) {
    },
		beforeAdd: function (context) {
			this.__data = context.data;
		},
		beforeEdit: function (context) {
console.log("beforeEdit:"+Object.toJSON(context.data));
			this.__data = context.data;
			this.__moduleName = context.moduleName;
		},
    afterSave : function(context) {
    },
		// overridden
		_createChildControlImpl: function (id, hash) {
			var control;
			switch (id) {
			case "checkbox":
				control = new qx.ui.form.CheckBox();
				control.setFocusable(false);
				control.setKeepActive(true);
				control.setEnabled(true);
				control.addState("inner");
				control.set({
					height:10,
					maxHeight:10,
					minWidth:12,
					maxWidth:12,
					decorator:"checkbox"
				});
				control.setToolTipText(this.tr("usebox.use"));
				this._add(control);
				break;
			case "select":
				var control = new qx.ui.form.Button(null, "resource/ms123/edit2.png").set({
					padding: 0,
					margin: 0,
					maxHeight: 30
				});
				control.addListener("execute", function () {
					var app = qx.core.Init.getApplication();
					var win = this._createWindow(this.tr(this.__clazzName));
					var context = {};
					context.window = win;
					context.data = this.__data;
					context.moduleName = this.__moduleName;
					var instance = new this.__clazz(context);
					instance.addListener("changeValue", function (e) {
						console.log("changeValue in Button");
						this.__value = e.getData();
						this.fireDataEvent("changeValue", e.getData(), null);
					}, this);
					app.getRoot().add(win);
					win.open();
				}, this);
				this._add(control);
				break;
			}
			return control;
		},

		// overridden
		_forwardStates: {
			focused: true
		},
		// overridden
		tabFocus: function () {
			var field = this.getChildControl("select");
			field.getFocusElement().focus();
			field.selectAllText();
		},
		// overridden
		focus: function () {
			this.base(arguments);
			this.getChildControl("select").getFocusElement().focus();
		},
		// interface implementation
		setValue: function (value) {
			if( value != this.__value){
				this.fireDataEvent("changeValue", value, this.__value);
			}
			this.__value = value;
		},
		getValue: function () {
			return this.__value;
		},
		resetValue: function () {},
		setClazzName: function (clazz) {
			this.__clazzName = clazz;
			this.__clazz = this.__getClazz(clazz);
		},
		// useit checkbox
		getCheckBox: function () {
			return this.getChildControl("checkbox");
		},
		__getClazz: function (name) {
			var parts = name.split("\.");
			var obj = window;
			for (var i = 0; i < parts.length; i++) {
				obj = obj[parts[i]];
			}
			return obj;
		},

		_createWindow: function (name) {
			var win = new qx.ui.window.Window(name, "").set({
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			});
			win.setLayout(new qx.ui.layout.Grow);
			win.setWidth(700);
			win.setHeight(500);
			win.setAllowMaximize(false);
			win.setAllowMinimize(false);
			win.setModal(true);
			win.setActive(false);
			win.minimize();
			win.center();
			return win;
		}
	}
});
