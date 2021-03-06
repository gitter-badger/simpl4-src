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

qx.Class.define("ms123.settings.views.FieldsEdit", {
	extend: qx.ui.container.Composite,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade) {
		this.base(arguments);
		this.facade = facade;
		this.setLayout(new qx.ui.layout.Dock());

		this._model = facade.model;
		var entity = this._model.parent.parent.parent.getId();
		var id = this._model.getId();
		var namespace = facade.storeDesc.getNamespace();
		var p = id.split(".");
		var entity = ms123.settings.Config.getEntityName(p[1]);
		var pack = ms123.settings.Config.getPackName(p[1]);
		this.pack = pack;
		var view = p[3];
		this.storeDesc = ms123.StoreDesc.getNamespaceDataStoreDesc(pack);
		console.log("entity:" + entity);
		console.log("view:" + view);

		this.facade.namespace = namespace;
		this.facade.entity = entity;
		this.facade.view = view;

		this.facade.selectables = this._getSelectableFields(namespace, entity, view);
		var selected = this._getSelectedFields(namespace, entity, view);
		this.facade.selected = this._removeUnusedFields(selected, this.facade.selectables);

		var viewSelectedItems = new ms123.settings.views.SelectedItems(facade);
		var viewSelectableItems = new ms123.settings.views.SelectableItems(facade,pack);
		viewSelectableItems.addListener("change", function (event) {
			console.log("change:" + qx.util.Serializer.toJson(event.getData()));
			var data = event.getData();
			data.enabled = true;
			if (data.value) {
				viewSelectedItems.addRecordByNamedField(data, "name", data.name);
			} else {
				viewSelectedItems.removeRecordByNamedField("name", data.name);
			}
		}, this);

		var sp = this._splitPane(viewSelectableItems, viewSelectedItems);
		this.add(sp, {
			edge: "center",
			left: 0,
			top: 0
		});
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_getSelectableFields: function (namespace, entity, view) {
			var filter = null;
			if (view == "export" || view == "main-grid" || view=='duplicate-check') {
				filter = "filter=!datatype.startsWith('array/team')";
			}
			console.error("_getSelectableFields.Path:" + this._model.getId());
			var cm = new ms123.config.ConfigManager();
			return cm.getFields(this.storeDesc,entity, true, false,filter);
		},
		_getSelectedFields: function (namespace, entity, view) {
			var filter = null;
			if (view == "export" || view == "main-grid") {
				filter = "filter=!datatype.startsWith('array/team')";
			}
			var f = null;
			try {
				var resourceid = this._model.getId();
				f = ms123.util.Remote.rpcSync("setting:getResourceSetting", {
					namespace: namespace,
					storeId: this.storeDesc.getStoreId(),
					resourceid: resourceid,
					settingsid: this.facade.settingsid,
					entity: entity,
					filter: filter,
					view: view
				});
				if (f) {;
					f = f.fields;
				} else {
					f = [];
				}
			} catch (e) {
				ms123.form.Dialog.alert("settings.views.Edit._getSelectedFields:" + e);
			}
			return this._setDisplayName(entity, f);
		},
		_removeUnusedFields: function (selected, selectables) {
			var ret = [];
			if( selected== null || selectables == null) return ret;
			for (var i = 0; i < selected.length; i++) {
				if (this._contains(selectables, selected[i])) {
					ret.push(selected[i]);
				}
			}
			return ret;
		},
		_contains: function (selectables, f) {
			for (var i = 0; i < selectables.length; i++) {
				if (selectables[i].name == f.name) return true;
			}
			return false;
		},
		_setDisplayName: function (entity, selected) {
			if (selected == null) return null;
			for (var i = 0; i < selected.length; i++) {
				selected[i].displayname = this.tr(this.pack+"." + entity + "." + selected[i].name).toString();
			}
			return selected;
		},
		_splitPane: function (top, bottom) {
			var splitPane = new qx.ui.splitpane.Pane("vertical").set({
				decorator: null
			});

			splitPane.add(top, 4);
			splitPane.add(bottom, 4);
			return splitPane;
		}
	}
});
