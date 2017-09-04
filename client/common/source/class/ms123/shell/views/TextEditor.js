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

qx.Class.define("ms123.shell.views.TextEditor", {
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
		this.add(this._createButtons(),{edge:"south"});

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
			config.buttons = [{
				'label': "",
				'tooltip': this.tr("meta.lists.savebutton"),
				'icon': this.__getResourceUrl("save.png"),
				'context': this,
				'callback': function (m) {
					var data = this._textArea.getValue();
					this.fireDataEvent("save", data);
				}
			}];
			var textArea = new ms123.codemirror.CodeMirror(config);
      textArea.set({
        height: null,
        width: null
      });
			textArea.setValue( value);
			this._textArea = textArea;
			return textArea;
		},

		_createButtons: function () {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing(5);

			var buttonSave = new qx.ui.toolbar.Button(this.tr("shell.save"), "icon/22/actions/dialog-ok.png");
			buttonSave.addListener("execute", function () {
				var data = this._textArea.getValue();
				this.fireDataEvent("save", data);
			}, this);
			toolbar._add(buttonSave)

			toolbar.addSpacer();
			toolbar.addSpacer();

			return toolbar;
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
