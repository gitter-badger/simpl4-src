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
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/places/*)
	@asset(ms123/icons/*)
	@asset(ms123/*)
*/

qx.Class.define("ms123.permissions.RoleEditLauncher", {
	extend: qx.ui.container.Composite,
 include : qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade) {
		this.base(arguments);
		this.facade = facade;
		this.setLayout(new qx.ui.layout.Dock());
		this._mainTabs = new qx.ui.tabview.TabView().set({
			contentPadding: 0
		});
		this.add(this._mainTabs, { edge: "center", left: 0, top:0 });
		this._mainTabs.addListener("changeSelection", function (e) {
			var page = e._target.getSelection()[0];
			this._currentRoleEdit = page.getUserData("roleEdit");
    }, this);
		this.facade.rightSpace.add(this,{edge:"center"});
		this.facade.registerOnEvent(ms123.permissions.Config.EVENT_ROLE_SELECTED, this._onRoleSelected.bind(this));
		this.facade.registerOnEvent(ms123.permissions.Config.EVENT_RESOURCE_SELECTED, this._onResourceSelected.bind(this));
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_onRoleSelected: function (event) {
			this._createTab(event);
		},
		_createTab: function (event) {
			var page = new qx.ui.tabview.Page(event.name,event.icon).set({
				showCloseButton: true
			});
			var facade = event.facade;
			facade.event = event;
			this._currentRoleEdit = new ms123.permissions.RoleEdit( facade);
			page.setLayout(new qx.ui.layout.Dock());
			page.add( this._currentRoleEdit, { edge:"center", left: 50, top: 50 });
			page.setUserData("roleEdit", this._currentRoleEdit );
			this._mainTabs.add(page, {
				edge: 0
			});
			this._mainTabs.setSelection([page]);
		},
		_onResourceSelected: function (event) {
			var model = event.model;
			if( !this._currentRoleEdit  ) return;
			var map = {};
			console.log("id:"+model.getId());

			map.permission = model.getId();
			map.actions = "read";
			map.enabled = true;
			this._currentRoleEdit.addRecord(map);
		},
		_hideTabs: function () {
			this._mainTabs.setEnabled(false);
			this._mainTabs.setVisibility("hidden");
		},
		_showTabs: function () {
			this._mainTabs.setEnabled(true);
			this._mainTabs.setVisibility("visible");
		}
	}
});
