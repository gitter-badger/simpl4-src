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

qx.Mixin.define("ms123.desktop.MDesktopPersist", {

	/*******************************************************************************
	 MEMBERS
	 ******************************************************************************/

	members: {
		init: function () {
			var cm = ms123.config.ConfigManager;
			this._isSessionRestore = cm.isSessionRestore();
			if (!this._isSessionRestore) return;
			var namespace = this._namespace;
			var store = new qx.data.store.Offline("desktop-" + namespace);
			var model = null;
			if (store.getModel() === null) {
				model = qx.data.marshal.Json.createModel({
					stateList: []
				}, true);
				store.setModel(model);
			} else {
				model = store.getModel();
			}
			this._offlineModel = model;
			var stateList = model.getStateList();
			for (var i = 0; i < stateList.getLength(); i++) {
				var state = qx.lang.Json.parse(stateList.getItem(i));
				if (state.entityName) {
					this._restoreDesktopWindow(namespace, state);
				}
			}
		},
		_getStateList: function () {
			var windowList = this.getWindows();
			var stateList = [];
			for (var i = 0; i < windowList.length; i++) {
				if( !windowList[i].getContext ){
					continue;
				}
				var c = windowList[i].getContext();
				var entityName = null;
				var pack = null;
				if (c.widgets && c.widgets.length > 0) {
					entityName = c.widgets[0].config;
					pack = c.widgets[0].storeDesc.getPack();
				}
				var state = null;
				if (windowList[i].getDesktopUnit() && qx.Class.hasInterface(windowList[i].getDesktopUnit().constructor, ms123.IState)) {
					state = windowList[i].getDesktopUnit().getState();
				}

				var so = {
					clazz: c.config,
					entityName: entityName,
					pack: pack,
					state: state
				}
				stateList.push(qx.util.Serializer.toJson(so));
			}
			return stateList;
		},

		_restoreDesktopWindow: function (namespace, state) {
			var storeDesc = ms123.StoreDesc.getNamespaceDataStoreDescForNS(namespace, state.pack);
			var m = new ms123.config.ConfigManager().getEntity(state.entityName, storeDesc);
			var widgetList = ms123.MainMenu.createWidgetList(m, storeDesc, this);
			widgetList[0].loadSync = true;
			var context = {
				storeDesc: storeDesc,
				unit_id: ms123.util.IdGen.nextId(),
				config: ms123.Crud,
				window_title: this.tr(state.pack+"." + state.entityName),
				widgets: widgetList
			}
			var dw = new ms123.DesktopWindow(context);
			var dunit = dw.getDesktopUnit();
			dunit.setState(state.state);
		},
		updateStatus: function () {
			if (!this._isSessionRestore) return;
			var stateList = this._getStateList();
			this._offlineModel.setStateList(stateList);
		}
	}
});
