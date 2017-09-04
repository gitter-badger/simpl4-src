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
	@asset(qx/icon/${qx.icontheme}/22/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/places/*)
*/
qx.Class.define("ms123.form.TriggerConditionEdit", {
	extend: qx.ui.container.Composite,
	implement: [qx.ui.form.IStringForm, qx.ui.form.IForm, ms123.form.IConfig],
	include: [
	qx.ui.form.MForm, ms123.searchfilter.MSearchFilter],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (config) {
		this.base(arguments);
		var layout = new qx.ui.layout.Grow();
		this.setLayout(layout);

		this._config = config;
		this._mainContainer = new qx.ui.container.Composite(new qx.ui.layout.Dock()).set({
			decorator: "main",
			backgroundColor: "gray",
			minHeight: 200,
			allowGrowX: true,
			allowGrowY: true
		});
		this._add(this._mainContainer, {});
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */

	properties: {
		// overridden
		appearance: {
			refine: true,
			init: "combobox"
		}
	},

	/**
	 *****************************************************************************
	 EVENTS
	 *****************************************************************************
	 */

	events: {
		/** Whenever the value is changed this event is fired
		 *
		 *  Event data: The new text value of the table.
		 */
		"changeValue": "qx.event.type.Data"
	},


	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		// overridden
		/**
		 * @lint ignoreReferenceField(_forwardStates)
		 */
		_forwardStates: {
			focused: true
		},
    beforeSave : function(context) {
    },
		beforeAdd: function (context) {
			this.__storeDesc = context.storeDesc;
			this._installSearchFilter(context);
		},
		beforeEdit: function (context) {
			this.__storeDesc = context.storeDesc;
			this._installSearchFilter(context);
		},
		afterSave: function (context) {},
		_installSearchFilter: function (context) {
			if (this._sf) {
				this._mainContainer.remove(this._sf);
			}
			var parentData = context.parentData;
			var modulename = parentData.targetmodule.toLowerCase();
			if( modulename.indexOf(".") != -1){
				var dot = modulename.lastIndexOf(".");
				modulename = modulename.substring(dot+1);
			}
			modulename = ms123.util.Inflector.getModuleName(modulename);
			this._sf = this._createConditionEdit(modulename);
			this._sf.addListener("change", function () {
				var filter = this._sf.getFilter();
				console.log("-->>> changeValue:" + filter);
				this._ignoreSetValue = true;
				this.fireDataEvent("changeValue", filter, null);
				this._ignoreSetValue = false;
			}, this);
			this._mainContainer.add(this._sf, {
				edge: "center"
			});
			this.fireDataEvent("changeValue", null, null);
		},

		setValue: function (value) {
			console.log("TriggerConditionEdit.setValue:" + value);
			if (this._ignoreSetValue || !this._sf) {
				return;
			}
			try {
				if (value == null || value == "") {} else {
					var filter = qx.lang.Json.parse(value);
					this._sf.setFilter(filter);
				}
			} catch (e) {}
		},

		getValue: function () {
			var data = {}; //this._sf.getData();
			data = qx.util.Serializer.toJson(data);
			return data;
		},


		resetValue: function () {
			console.error("TriggerConditionEdit.resetValue");
		},


		// useit checkbox
		getCheckBox: function () {
			return this.getChildControl("checkbox");
		},

		__createSearchFilter: function (params,storeDesc) {
			var f1 = this._getSearchFilterFieldSets(params.modulename,storeDesc);
			var f2 = this._getSearchFilterFields(params.modulename,storeDesc);
			var fields = f1.concat(f2);

			var root = {};
			root.id="root";
			root.title="root";
			root.children = [];
			for (var i = 0; i < fields.length; i++) {
				var f = fields[i];
				f.module = "";
				var node = {};
				node.id = f.itemval;
				if( f.itemval == "_team_list"){
					f.ops.push({
						op: "team_changed",
						text: this.tr("meta.lists.team_changed")
					});
					f.ops.push({
						op: "team_hierarchy_changed",
						text: this.tr("meta.lists.team_hierarchy_changed")
					});
				}else{
					f.ops.push({
						op: "changed",
						text: this.tr("meta.lists.field_changed")
					});
				}
				node.title = f.text;
				node.module = ""; 
				node.moduleTitle = ""; 
				node.children = [];
				root.children.push(node);
			}
			var sf = new ms123.searchfilter.SearchFilter(root,fields, params);
			return sf;
		},

		/**
		 */
		_createConditionEdit: function (modulename) {
			var cm = new ms123.config.ConfigManager();
			var params = {
				showToolbar: false,
				modulename: modulename
			}
			var sf = this.__createSearchFilter(params, this.__storeDesc.getNamespaceDataStoreDesc());
			return sf;
		}
	}
});
