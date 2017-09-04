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
	* @ignore(Hash)
	* @ignore($A)
*/
qx.Class.define("ms123.ruleseditor.plugins.ContextMenu", {
	extend: qx.core.Object,
	include: [qx.locale.MTranslation],

	/**
	 * Constructor
	 */
	construct: function (facade) {
		this.base(arguments);
		this.facade = facade;

		this.facade.registerOnEvent(ms123.ruleseditor.Config.EVENT_TABLE_CREATED, this.tableCreated.bind(this));
	},

	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {
		tableCreated: function () {
			this.groupIndex = new Hash();
			var pluginsData = this.facade.getPluginsData();
			this.plugs = [];
			var newPlugs = pluginsData.sortBy((function (value) {
				return ((this.groupIndex[value.group] != undefined ? this.groupIndex[value.group] : "") + value.group + "" + value.index).toLowerCase();
			}).bind(this));
			var plugs = $A(newPlugs).findAll(qx.lang.Function.bind(function (value) {
				return !this.plugs.include(value) && (value.target === ms123.ruleseditor.plugins.ContextMenu)
			}, this));
			if (plugs.length < 1) return;

			plugs.each((function (value) {
				if (!value.name) {
					return
				}
				this.plugs.push(value);
			}).bind(this));

			var table = this.facade.getDecisionTable().getTable();
			var cc = table.getTableColumnModel().getVisibleColumnCount();
			for (var c = 0; c < cc; c++) {
				table.setContextMenuHandler(c, this._contextMenuHandler.bind(this));
			}
		},

		_contextMenuHandler : function(col, row, table, dataModel, contextMenu) {
			this.entries = [];
			var currentGroupsName = this.plugs.last() ? this.plugs.last().group : this.plugs[0].group;
			this.plugs.each((function (value) {
				console.log("contextMenu.value:"+value);
				// Add seperator if new group begins
				if (currentGroupsName != value.group) {
					//this._toolbar.add( new qx.ui.toolbar.Separator());
					currentGroupsName = value.group;
				}

				if (value.addFill) {
					//this._toolbar.addSpacer();
				} else { 
					var menuEntry = new qx.ui.menu.Button(value.name,value.icon);
					menuEntry.addListener("execute", value.functionality, this);

					if (value.description) {
						menuEntry.setToolTipText(value.description);
					}

					menuEntry.setUserData("id", value.id);
					value['menuEntryInstance'] = menuEntry;
					contextMenu.add(menuEntry);
				}
				this.entries.push(value);
			}).bind(this));
			this.enableEntries([]);
			return true;
		},

		enableEntries: function (elements) {
			this.entries.each((function (value) {
				value.menuEntryInstance.setEnabled(true);
				if (value.isEnabled && !value.isEnabled(value.menuEntryInstance)) value.menuEntryInstance.setEnabled(false);
			}).bind(this));
		}
	}
});
