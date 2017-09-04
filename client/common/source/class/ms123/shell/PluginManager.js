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
qx.Class.define('ms123.shell.PluginManager', {
 extend: qx.core.Object,
	implement: ms123.shell.IShellPlugin,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (plugins) {
		this.base(arguments);
		this._contextMenuActions = [];
		this._onClickActions = [];
		this._iconMapping = {};
		this._excludePaths = [];
		for( var i=0; i< plugins.length;i++){
			this._contextMenuActions = this._contextMenuActions.concat(plugins[i].getContextMenuActions());
			this._onClickActions = this._onClickActions.concat(plugins[i].getOnClickActions());
			if( plugins[i].getExcludePaths && plugins[i].getExcludePaths()!=null ){
				this._excludePaths = this._excludePaths.concat(plugins[i].getExcludePaths());
			}
			qx.lang.Object.mergeWith(this._iconMapping, plugins[i].getIconMapping());
		}
		this._plugins = plugins;
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
			return this._contextMenuActions;
		},

		/**
		 */
		getOnClickActions: function () {
			return this._onClickActions;
		},

		/**
		 */
		getExcludePaths: function () {
			return this._excludePaths;
		},

		/**
		 */
		prepareNode: function (model,level) {
			for( var i=0; i< this._plugins.length;i++){
				this._plugins[i].prepareNode(model,level);
			}
		},
		onOpenNode: function (e) {
			for( var i=0; i< this._plugins.length;i++){
				this._plugins[i].onOpenNode(e);
			}
		},

		/**
		 */
		getIconMapping: function () {},
		getNodeTypeIcon: function (type) {
			var icon = this._iconMapping[type];
			if( icon ) return icon;
			return "resource/ms123/file.png";
		}
	}
});
