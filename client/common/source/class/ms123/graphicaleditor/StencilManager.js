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
	* @ignore($)
*/
qx.Class.define("ms123.graphicaleditor.StencilManager", {
	extend: qx.core.Object,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function () {
		var m = qx.locale.Manager.getInstance();
		var locale = m.getLocale();

		this.__lang = locale;
		this.__groups = {};
		this.__groupNameList = [];
		this.__stencilsMap = {};
		this.__stencilList = [];
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {
		__stencilsetcache: {}
	},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		loadStencilSet: function (name) {
			var cm = new ms123.config.ConfigManager();
			var stencilset = cm.getStencilSet(name);
			var stencils = stencilset["stencils"];
			console.log("stencils:" + stencils.length + "," + this.__lang);
			for (var i = 1; i < stencils.length; i++) {
				var stencilMap = stencils[i];
				var stencil = new ms123.graphicaleditor.Stencil();
				stencil.setType(stencilMap.type);
				stencil.setView(stencilMap.view);
				var desc = this.__getLangString(stencilMap, "description");
				stencil.setDescription(desc);
				var title = this.__getLangString(stencilMap, "title");
				stencil.setTitle(title);
				stencil.setId(stencilMap.id);
				stencil.setIcon(this.__getResourceUrl(name,stencilMap.icon));
				var groups = this.__getLangGroups(stencilMap);
				for (var j = 0; groups && j < groups.length; j++) {
					var groupName = groups[j];
					var groupList = this.__groups[groupName];
					if (groupList == undefined) {
						groupList = [];
						this.__groups[groupName] = groupList;
					}
					groupList.push(stencil);
					if (!this.__contains(this.__groupNameList, groupName)) {
						this.__groupNameList.push(groupName);
					}
				}
				this.__stencilList.push(stencil);
				this.__stencilsMap[stencilMap.id] = stencil;
			}
			return this.__stencilList;
		},

		getGroupListByName: function (name) {
			return this.__groups[name];
		},
		getGroupNameList: function () {
			return this.__groupNameList;
		},
		getStencilById: function (id) {
			return this.__stencilsMap[id];
		},

		__contains:function(a, obj) {
			for (var i = 0; i < a.length; i++) {
				if (a[i] === obj) {
					return true;
				}
			}
			return false;
		},

		__getResourceUrl: function (name, i) {
			var am = qx.util.AliasManager.getInstance(name);
			return am.resolve("resource/ms123/stencilsets/"+name+"/" + i );
		},

		__getLangString: function (map, key) {
			var val = map[key + "_" + this.__lang];
			if (val && val.length > 0) return val;
			return map[key];
		},

		__getLangGroups: function (map) {
			var key = "groups";
			var val = map[key + "_" + this.__lang];
			if (val && val.length > 0) return val;
			return map[key];
		}
	}
});
