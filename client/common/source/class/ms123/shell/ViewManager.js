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

qx.Class.define("ms123.shell.ViewManager", {
	extend: qx.ui.core.Widget,
 include : qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade) {
		this.base(arguments);
		this.facade = facade;
		this._setLayout(new qx.ui.layout.Dock());
		this._mainTabs = new qx.ui.tabview.TabView().set({
			contentPadding: 0
		});
		this._add(this._mainTabs, { edge: "center" });
		this._mainTabs.addListener("changeSelection", function (e) {
			//var pid = e._target.getSelection()[0].getUserData("id");
    }, this);
		this.facade.registerOnEvent(ms123.shell.Config.EVENT_ITEM_SELECTED, this._onItemSelected.bind(this));
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_onItemSelected: function (event) {
			var clazz = event.clazz;
			var model = event.model;
			var title = event.title;
			var tabtitle = event.tabtitle;
			var kind = event.kind;
			var icon = event.icon;
			var param = event.param;
			var facade = event.facade;
			if( kind == "tab"){
				title = tabtitle || title;
				this._createTab(clazz,title,model,param, facade,icon);
			}
			if( kind == "dialog"){
				new clazz( model,param,facade);
			}
			if( kind == "window"){
				new clazz( model,param,facade);
			}
		},
		_createTab: function (clazz,title, model,param, facade,icon) {
			title = title.replace("%n", model.getId());
			this._page = new qx.ui.tabview.Page(title,icon).set({
				showCloseButton: true
			});

			if(Array.isArray(clazz)){ //@@@MS a bit a hack 
				var sdesc = ms123.StoreDesc.getNamespaceDataStoreDesc(model.getPack());
				var isOrientDB = sdesc ? sdesc.isOrientDB() : true;
				clazz = isOrientDB ? clazz[1] : clazz[0];
			}

			var c = new clazz( model,param, facade);
			this._page.setUserData( "component",c);
			this._page.addListener("close", function (e) {
				var page = e._target;
				console.log("ViewManager.close:"+page);
				var comp = page.getUserData("component");
				console.log("ViewManager.close:"+comp);
				var editor=null;
				try{
					editor = comp.getEditor();
				}catch(e){
				}
				console.log("ViewManager.editor:"+editor);
				if( editor && editor._destroy ) editor._destroy();
			}, this);
			this._page.setLayout(new qx.ui.layout.Dock());
			this._page.add( c, { edge:"center" });
			this._mainTabs.add(this._page, {
				edge: 0
			});
			this._mainTabs.setSelection([this._page]);
		},
		_hideTabs: function () {
			this._mainTabs.setEnabled(false);
			this._mainTabs.setVisibility("hidden");
		},
		_showTabs: function () {
			this._mainTabs.setEnabled(true);
			this._mainTabs.setVisibility("visible");
		}
	},
	destruct: function () {
	}
});
