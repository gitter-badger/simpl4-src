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
qx.Class.define('ms123.shell.InternalPlugin', {
 extend: qx.core.Object,
	implement: ms123.shell.IShellPlugin,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this.base(arguments);
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
			var contextMenu = [
			{
				nodetypes: ["sw.directory", "sw.project"],
				clazz: ms123.shell.views.NewDirectory,
				menuicon: "icon/16/actions/list-add.png",
				title: this.tr("shell.new_directory"),
				kind: "dialog"
			},
/*			{
				nodetypes: ["sw.directory"],
				clazz: ms123.shell.views.Details,
				icon: "icon/16/actions/view-restore.png",
				title: this.tr("shell.details"),
				defaultEntry:true,
				kind: "tab"
			},*/
			{
				nodetypes: ["sw.project","sw.directory"],
				clazz: ms123.shell.views.NewFile,
				menuicon: "icon/16/actions/list-add.png",
				title: this.tr("shell.new_file"),
				kind: "dialog"
			},
			{
				nodetypes: ms123.shell.FileType.getAllEditables().concat(["sw.directory"]).concat(ms123.shell.FileType.getAllForeigns()),
				clazz: ms123.shell.views.DelNode,
				menuicon: "icon/16/actions/list-remove.png",
				title: this.tr("shell.del_node"),
				kind: "dialog"
			},
			{
				nodetypes: ms123.shell.FileType.getAllEditables().concat(["sw.directory"]).concat(ms123.shell.FileType.getAllForeigns()),
				clazz: ms123.shell.views.MoveNode,
				menuicon: "icon/16/actions/edit-paste.png",
				title: this.tr("shell.rename_node"),
				kind: "dialog"
			},
			{
				nodetypes: ms123.shell.FileType.getAllEditables().concat(ms123.shell.FileType.getAllForeigns()),
				clazz: ms123.shell.views.CopyNode,
				menuicon: "icon/16/actions/edit-copy.png",
				title: this.tr("shell.copy_node"),
				kind: "dialog"
			},
	/*		{
				nodetypes: ms123.shell.FileType.getAllEditables(),
				clazz: ms123.shell.views.Editor,
				menuicon: "resource/ms123/edit2.png",
				title: this.tr("shell.editor"),
				defaultEntry:true,
				kind: "tab"
			},*/
			{
				nodetypes: ms123.shell.FileType.getAllJsonEditables(),
				clazz: ms123.shell.views.JsonEditor,
				menuicon: "resource/ms123/edit2.png",
				title: this.tr("shell.jsoneditor"),
				kind: "tab"
			},
			{
				nodetypes: ms123.shell.FileType.getAllTextEditables(),
				clazz: ms123.shell.views.SimpleTextEditor,
				menuicon: "resource/ms123/edit2.png",
				title: this.tr("shell.texteditor"),
				tabtitle: "%n",
				kind: "tab"
			},
			{
				nodetypes: ["sw.module"],
				clazz: ms123.shell.views.Editor,
				menuicon: "resource/ms123/edit2.png",
				title: this.tr("shell.editor"),
				tabtitle: "%n",
				kind: "tab"
			}];
			return contextMenu;
		},

		/**
		 */
		getOnClickActions: function () {
			var actions = [ 
			{
				nodetypes: ms123.shell.FileType.getAllEditables(),
				clazz: ms123.shell.views.Editor,
				title: "%n",
				kind: "tab"
			}, {
				nodetypes: ms123.shell.FileType.getAllTextEditables(),
				clazz: ms123.shell.views.SimpleTextEditor,
				title: "%n",
				kind: "tab"
			}
			];
			return actions;
		},

		/**
		 */
		prepareNode: function (model,level) {
			if (model.id == "filter" && level == 1) {
				model.title = this.tr("shell.filter_dir");
			}
			if (model.id == "forms" && level == 1) {
				model.title = this.tr("shell.forms_dir");
			}
			if (model.id == "rules" && level == 1) {
				model.title = this.tr("shell.rules_dir");
			}
			if (model.id == "services" && level == 1) {
				model.title = this.tr("shell.services_dir");
			}
			if (model.id == "processes" && level == 1) {
				model.title = this.tr("shell.processes_dir");
			}
		},
		onOpenNode: function (e) {},

		getExcludePaths: function () {return null},
		/**
		 */
		getIconMapping: function () {
			return ms123.shell.FileType.getIconMapping();
		}
	}
});
