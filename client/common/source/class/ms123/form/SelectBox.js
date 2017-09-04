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
 @asset(qx/decoration/*)
 @asset(qx/decoration/Modern/tree/closed.png)
 */

qx.Class.define("ms123.form.SelectBox", {
	extend: qx.ui.form.SelectBox,

	implement: [ms123.form.IConfig],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (selectable_items,useitCheckboxes) {
		this.base(arguments);
		this._selectable_items = selectable_items;
		if( this._selectable_items && useitCheckboxes){
			this._missingParamList = this._selectable_items.getMissingParamList();
		}
    this.getChildControl("list").setQuickSelection(false);
	},


	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */


	properties: {},

	events: {
		"changeValue": "qx.event.type.Data"
	},


	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */
	members: {
    _onTap : function(e) {
			if( e.getTarget() instanceof qx.ui.form.CheckBox){
				return;
			}
      this.toggle();
    },
		createList: function (options) {
			if (typeof options == 'string') {
				return;
			}
			var model = qx.data.marshal.Json.createModel(this._correctOptions(options));
			var lc = new qx.data.controller.List(model, this, "label");
			this._listController = lc;

			var delegate = {
				createItem: function () {
					return new ms123.form.TooltipListItem();
				},

				bindItem: function (controller, item, index) {
					controller.bindProperty("tooltip", "tooltip", null, item, index);
					controller.bindProperty("", "model", null, item, index);
					controller.bindProperty(controller.getLabelPath(), "label", controller.getLabelOptions(), item, index);
					if (controller.getIconPath() != null) {
						controller.bindProperty(controller.getIconPath(), "icon", this.getIconOptions(), item, index);
					}
				}
			};
			lc.setDelegate(delegate);
		},
		_correctOptions: function (options) {
			if( options == null){
				return null;
			}
			for (var i = 0; i < options.length; i++) {
				var o = options[i];
				if (!o.value === undefined) {
					o.value = null;
				}
				if (o.label === undefined) {
					o.label = null;
				}
				if (o.tooltip === undefined) {
					o.tooltip = null;
				}
			}
			return options;
		},
		_refreshItems: function (vars) {
			console.log("_refreshItems.Vars:" + JSON.stringify(vars, null, 2));
			this.resetSelection();
			this.setModelSelection([]);
			this._selectable_items.setVarMap(vars);
			var items = this._selectable_items.getItems();
	    if(typeof items == "string" || items.length == null)return;
			var listModel = this._listController.getModel();
			listModel.splice(0, listModel.getLength());
			var newElements = qx.data.marshal.Json.createModel(this._correctOptions(items), true);
			listModel.append(newElements);
		},
		beforeSave: function (context) {},
		updateEvent: function (eventData) {
			if (this._selectable_items == null) return;
			var name = eventData.name;
			var value = eventData.value;
			console.log("updateEvent.in:" + this.getUserData("key") + "/field:" + name + "=" + value+"/missingParamList:"+this._missingParamList);
			if (this._missingParamList && this._missingParamList.indexOf(name) >= 0) {
				var vars = {};
				vars[name] = value;
				this._refreshItems(vars);
			}
		},
		beforeAdd: function (context) {
			if (this._selectable_items == null) return;
			this._missingParamList = this._selectable_items.getMissingParamList();
			if (this._missingParamList) {
				var vars = {};
				for (var i = 0; i < this._missingParamList.length; i++) {
					var mp = this._missingParamList[i];
					vars[mp] = context.data[mp];
				}
				this._refreshItems(vars);
			}
		},
		beforeEdit: function (context) {
			if (this._selectable_items == null) return;
			console.log("Missing:" + JSON.stringify(this._selectable_items.getMissingParamList(), null, 2));
			var missingParamList = this._selectable_items.getMissingParamList();
			this._missingParamList = missingParamList;
			if (missingParamList) {
				var vars = {};
				for (var i = 0; i < missingParamList.length; i++) {
					var mp = missingParamList[i];
					vars[mp] = context.data[mp];
				}
				this._refreshItems(vars);
			}
		},
		afterSave: function (context) {}
	}


});
