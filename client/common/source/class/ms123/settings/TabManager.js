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

qx.Class.define("ms123.settings.TabManager", {
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
			contentPadding: 5
		});
		this.add(this._mainTabs, { edge: "center", left: 0, top:0 });
		this._mainTabs.addListener("changeSelection", function (e) {
			var page = e._target.getSelection()[0];
			if( page != null){
				this._currentWidget = page.getUserData("widget");
			}
    }, this);
		this.facade.rightSpace.add(this,{edge:"center"});
		this.facade.registerOnEvent(ms123.util.BaseResourceSelector.EVENT_RESOURCE_SELECTED, this._onResourceSelected.bind(this));
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_onResourceSelected: function (event) {
			var facade = event.facade;
			var model = event.model;
			console.log("Event:"+ model.getId());
			console.log("modeltype:"+model.getType());
			if( model.getActionClass){
				console.log("modelaction:"+model.getActionClass());
			}
			var icon = event.icon;
			this._createTab(model,facade,icon);
		},
		_createTab: function (model, facade,icon) {
			facade.model = model;
			var clazz = null;
			try{
				clazz = model.getActionClass();
			}catch(e){
				return;
			}
			try{
				this._currentWidget = new clazz( facade );
			}catch(e){
				console.log(e.stack);
				ms123.form.Dialog.alert("TabManager._createTab:" + e);
				return;
			}
			var page = new qx.ui.tabview.Page(model.getTitle(),icon).set({
				showCloseButton: true
			});
			page.setLayout(new qx.ui.layout.Dock());
			page.add( this._currentWidget, { edge:"center", left: 50, top: 50 });
			page.add( new qx.ui.basic.Label(model.getId()), { edge:"north", left: 50 });
			page.setUserData("widget", this._currentWidget );
			this._mainTabs.add(page, {
				edge: 0
			});
			page.addListener("close", function (e) {
				var widget = page.getUserData("widget");
			}, this);
			this._mainTabs.setSelection([page]);
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
