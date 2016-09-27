/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
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
			value = JSON5.parse(value);
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
