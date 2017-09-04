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
/**
 * @ignore(JSON5.*)
 */

qx.Class.define("ms123.shell.views.JsonEditor2", {
	extend: qx.ui.container.Composite,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (config,value) {
		this.base(arguments);
		this.config = config||{};
		var layout = new qx.ui.layout.Dock();
		this.setLayout(layout);
		this.add(this._createEditor(config,value),{edge:"center"});
		this.add(this._createToolbar(),{edge:"south"});

		//var value = JSON.parse(value);
	},

	/******************************************************************************
	 EVENTS
	 ******************************************************************************/
	events: {
		"save": "qx.event.type.Data"
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
		_createEditor:function(config, value){
			console.log("Value:"+value);
			this.msgArea = new ms123.codemirror.CodeMirror(config);
			this.msgArea.set({
				height: null,
				width: null
			});
			this.msgArea.setValue(value);
			return this.msgArea;
		},

		_createToolbar: function (model) {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing(5);
			var buttonSave = new qx.ui.toolbar.Button(this.tr("shell.save"), "icon/22/actions/dialog-ok.png");
			buttonSave.addListener("execute", function () {
				var value =  this.msgArea.getValue();
				try{
				value = JSON5.parse(value);
				}catch(e){
					ms123.form.Dialog.alert("Error:"+e);
					return;
				}
				this.fireDataEvent("save", JSON.stringify(value,null,2));
			}, this);
			toolbar.addSpacer();
			toolbar._add(buttonSave)
			return toolbar;
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
