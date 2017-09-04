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
 */
qx.Class.define('ms123.enumerations.EnumPlugin', {
	extend: qx.core.Object,
	implement: ms123.shell.IShellPlugin,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade) {
		this.base(arguments);
		this._facade = facade;
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		/**
		 */
		getContextMenuActions: function () {
			var contextMenu = [{
				nodetypes: ["sw.enums"],
				clazz: ms123.enumerations.NewEnum,
				menuicon: "icon/16/actions/list-add.png",
				title: this.tr("enumerations.new_enum"),
				kind: "dialog"
			}];
			return contextMenu;
		},

		/**
		 */
		getOnClickActions: function () {
			var onclick = [{
				nodetypes: ["sw.enum"],
				clazz: ms123.enumerations.EnumEditor,
				menuicon: "icon/16/actions/list-add.png",
				tabicon: "resource/ms123/enum.png",
				title: "%n",
				kind: "tab"
			}];
			return onclick;
		},

		/**
		 */
		prepareNode: function (model) {
			if (model.id == "enumerations") {
				model.title = this.tr("enumerations.enumeration_dir");
				model.type = "sw.enums";
			}
		},
		onOpenNode: function (e) {},

		getExcludePaths: function () {return null},
		/**
		 */
		getIconMapping: function () {
			var iconMap = {};
			iconMap["sw.enums"] = "sw.directory";
			iconMap["sw.enum"] = "resource/ms123/enum.png";
			return iconMap;
		}
	}
});
