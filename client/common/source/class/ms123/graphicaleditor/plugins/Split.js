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

qx.Class.define("ms123.graphicaleditor.plugins.Split", {
	extend: qx.core.Object,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade) {
		this.base(arguments);
		this.facade = facade;

		this._propertyPanelVisible = true;

		this.facade.offer({
			name: "graphicaleditor.togglePropertyPanel",
			description: "",
			icon: this.__getResourceUrl("view.png"),
			keyCodes: [{
				metaKeys: [ms123.oryx.Config.META_KEY_META_CTRL],
				keyCode: 65,
				keyAction: ms123.oryx.Config.KEY_ACTION_DOWN
			}],
			functionality: this.togglePropertyPanel.bind(this),
			group: "zzGroup",
			isEnabled: qx.lang.Function.bind(function () {
				return true
			}, this),
			index: 2
		});

		/*this.facade.offer({
			name: this.tr("ge.Split.toggleShapeMenu"),
			description: this.tr("ge.Split.toggleShapeMenuDesc"),
			icon: this.__getResourceUrl("arrow_redo.png"),
			keyCodes: [{
				metaKeys: [ms123.oryx.Config.META_KEY_META_CTRL],
				keyCode: 83,
				keyAction: ms123.oryx.Config.KEY_ACTION_DOWN
			}],
			functionality: this.toggleShapeMenu.bind(this),
			group: "zzGroup",
			isEnabled: qx.lang.Function.bind(function () {
				return true
			}, this),
			index: 0
		});*/
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
		/**
		 * 
		 */
		togglePropertyPanel: function () {
			if( this._propertyPanelVisible ){
				this.facade.container.dialogPanel.exclude();
				this._propertyPanelVisible = false;
			}else{
				this.facade.container.dialogPanel.show();
				this._propertyPanelVisible = true;
			}
		},
		/**

		 * 
		 */
		toggleShapeMenu: function () {
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
