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

qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.TextAreaField", {
	extend: qx.ui.core.Widget,
	implement: [
	ms123.graphicaleditor.plugins.propertyedit.IUpdate,
	qx.ui.form.IStringForm, qx.ui.form.IForm],
	include: [
	qx.ui.form.MForm],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (key,config,facade) {
		this.base(arguments);
		this.key = key;
		this.facade = facade;
		this.config = config||{};
		var layout = new qx.ui.layout.HBox();
		this._setLayout(layout);

		this.scriptField = key.match(".*activiti.script");
		var textField = this._createChildControl("textfield");
		var select = this._createChildControl("select");
		this.setFocusable(true);

	},

	/******************************************************************************
	 EVENTS
	 ******************************************************************************/
	events: {
		"changeValue": "qx.event.type.Data"
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
		// interface implementation
		envChanged: function (env) {
			this._env = env;
		},
		/**
		 * Returns the field key.
		 */
		getFieldKey: function () {
			return this.key;
		},
		resetValue: function () {},

		/**
		 * Returns the actual value of the trigger field.
		 * If the table does not contain any values the empty
		 * string will be returned.
		 */
		getValue: function () {
			if( this.data && this.data.length> 32){
				//console.log("TextAreaField.getValue1:" + this.data.substring(0,31));
			}else{
				//console.log("TextAreaField.getValue2:" + this.data);
			}
			return this.data;
		},

		/**
		 * Sets the value of the trigger field.
		 * In this case this sets the data that will be shown in
		 * the grid of the dialog.
		 * 
		 * param {Object} value The value to be set (JSON format or empty string)
		 */
		setValue: function (value) {
			//console.log("TextAreaField.setValue:" + value);
			//if (value != undefined && value && value.length > 0) {
			//	if (this.data == undefined) {
					this.data = value;
					this.getChildControl("textfield").setValue(value);
			//	}
		//	}
		},

		// overridden
		_createChildControlImpl: function (id, hash) {
			var control;
			switch (id) {
			case "textfield":
				control = new qx.ui.form.TextField();
				control.setLiveUpdate(true);
				control.setFocusable(false);
				control.setReadOnly(true);
				control.setEnabled(false);
				control.addState("inner");
				//control.addListener("changeValue", this._onTextFieldChangeValue, this);
				this._add(control, {
					flex: 1
				});
				break;
			case "select":
				var control = new qx.ui.form.Button(null, "resource/ms123/edit2.png").set({
					padding: 0,
					margin: 0,
					maxHeight: 30
				});
				control.setFocusable(false);
				control.addListener("execute", function (e) {
					if( false){
						var context = {}
						context.insertBar = this.scriptField;
						context.facade = this.facade;
						context.helper = this.config.helper;
						context.toolbarAddon = this.config.toolbarAddon;
						context.mode = (this.config && this.config.mode) ? this.config.mode : "text/x-groovy";
						this.textArea = this.createTextArea(context);
						if( !this.data ) this.data = "";
						this.textArea.setValue( this.data);
						var buttons = this.createButtons();
						this._editContainer = new qx.ui.container.Composite();
						this._editContainer.setLayout(new qx.ui.layout.Dock);
						this._editContainer.add(this.textArea, {
							edge: "center"
						});
						this._editContainer.add(buttons, {
							edge: "south"
						});
						var stack = this.facade.mainStack;
						stack.add(this._editContainer);
						stack.setSelection([this._editContainer]);
					}else{
						var app = qx.core.Init.getApplication();
						var win = this.createWindow(this._getWinName());
						win.addListener("close", function (e) {
							win.destroy();
							this._height = win.getHeight();
							this._width = win.getWidth();
							this._lp = win.getLayoutProperties();
						}, this);
						var context = {}
						context.insertBar = this.scriptField;
						context.facade = this.facade;
						context.helper = this.config.helper;
						context.toolbarAddon = this.config.toolbarAddon;
						context.mode = (this.config && this.config.mode) ? this.getMode(this.config.mode) : "text/x-groovy";
						this.textArea = this.createTextArea(context);
						if( !this.data ) this.data = "";
						this.textArea.setValue( this.data);
						var buttons = this.createButtons();
						win.add(this.textArea, {
							edge: "center"
						});
						win.add(buttons, {
							edge: "south"
						});
						app.getDesktop(ms123.StoreDesc.getCurrentNamespace()).add(win);
						win.open();
						if( this._lp){ 
							win.moveTo( this._lp.left, this._lp.top);
						}
						this.win = win;
					}
				}, this);
				this._add(control);
				break;
			case "clear":
				var control = new qx.ui.form.Button(null, "resource/ms123/clear.png").set({
					padding: 0,
					margin: 0
				});
				control.addListener("execute", function () {
					this.resetValue();
				}, this);
				this._add(control);
				break;
			}
			return control;
		},
		getMode: function (type) {
			if( type == "njs"){
				return "application/x-javascript"
			}else if( type == "java"){
				return "text/x-java"
			}else if( type == "groovy"){
				return "text/x-groovy"
			}else{
				return type;
			}
		},
		createTextArea: function (context) {
			var textAreaMax = new ms123.codemirror.CodeMirror(context);//new qx.ui.form.TextArea();
      textAreaMax.set({
        height: null,
        width: null
      });
			return textAreaMax;
		},
		createButtons: function () {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing(5);

			var buttonSave = new qx.ui.toolbar.Button(this.tr("save"), this.__getResourceUrl("save.png"));
			buttonSave.addListener("execute", function () {
				var value = this.textArea.getValue();
				var data = value;
				var oldVal = this.data;
				this.data = data;
				this.fireDataEvent("changeValue", data, oldVal);
				this.getChildControl("textfield").setValue(data);
				this.facade.save.save();
			}, this);
			toolbar._add(buttonSave)

			if(this.facade.editorType == "sw.process"){
				var buttonSaveDeploy = new qx.ui.toolbar.Button(this.tr("savedeploy"), this.__getResourceUrl("savedeploy.png"));
				buttonSaveDeploy.addListener("execute", function () {
					var value = this.textArea.getValue();
					var data = value;
					var oldVal = this.data;
					this.data = data;
					this.fireDataEvent("changeValue", data, oldVal);
					this.getChildControl("textfield").setValue(data);
					this.facade.save.savedeploy();
				}, this);
				toolbar._add(buttonSaveDeploy)

				var buttonDeploy = new qx.ui.toolbar.Button(this.tr("deploy"), this.__getResourceUrl("deploy.png"));
				buttonDeploy.addListener("execute", function () {
					var value = this.textArea.getValue();
					var data = value;
					var oldVal = this.data;
					this.data = data;
					this.fireDataEvent("changeValue", data, oldVal);
					this.getChildControl("textfield").setValue(data);
					this.facade.save.deploy();
				}, this);
				toolbar._add(buttonDeploy)
			}

			toolbar.addSpacer();
			toolbar.addSpacer();

			var buttonOk = new qx.ui.toolbar.Button(this.tr("Ok"), "icon/16/actions/dialog-ok.png");
			buttonOk.addListener("execute", function () {
				var value = this.textArea.getValue();
				var data = value;
				var oldVal = this.data;
				this.data = data;
				this.fireDataEvent("changeValue", data, oldVal);
				if( this.win){
					this._height = this.win.getHeight();
					this._width = this.win.getWidth();
					this._lp = this.win.getLayoutProperties();
			 		this.win.close();
				}else{
					var stack = this.facade.mainStack;
					stack.setSelection([stack.getChildren()[0]]);
					stack.remove(this._editContainer);
				}
				this.getChildControl("textfield").setValue(data);
			}, this);
			toolbar._add(buttonOk)

			var buttonCancel = new qx.ui.toolbar.Button(this.tr("Cancel"), "icon/16/actions/dialog-close.png");
			buttonCancel.addListener("execute", function () {
				if( this.win){
					this._height = this.win.getHeight();
					this._width = this.win.getWidth();
					this._lp = this.win.getLayoutProperties();
					this.win.close();
				}else{
					var stack = this.facade.mainStack;
					stack.setSelection([stack.getChildren()[0]]);
					stack.remove(this._editContainer);
				}
			}, this);
			toolbar._add(buttonCancel)

			return toolbar;
		},
		createWindow: function (name) {
			var win = new qx.ui.window.Window(name).set({
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			});
			var root = qx.core.Init.getApplication().getRoot();
			var w = root.getInnerSize().width;
			var h = root.getInnerSize().height;
		
			win.setLayout(new qx.ui.layout.Dock);
			if( this.config && this.config.width){
				win.setWidth(this.config.width);
			}else if(this.config && this.config.helper){
				win.setWidth(w*.8);
			}else {
				win.setWidth(w*.7);
			}
			if( this.config && this.config.height){
				win.setHeight(this.config.height);
			}else if(this.config && this.config.helper){
				win.setHeight(h*.7);
			}else {
				win.setHeight(h*.6);
			}
			win.setAllowMaximize(true);
			win.setAllowMinimize(true);
			win.setModal(true);
			win.setActive(false);
			if( this._height){
				win.setHeight(this._height);
				win.setWidth(this._width);
			}else{
				win.center();
			}
			win.minimize();
			return win;
		},
		_getWinName:function(n){
			var n = this._env.caption;
			if( n == null || n.length==0){
				n = "Editor";
			}
			return n;
		},
		__getResourceUrl: function (name) {
			var am = qx.util.AliasManager.getInstance();
			return am.resolve("resource/ms123/" + name);
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
