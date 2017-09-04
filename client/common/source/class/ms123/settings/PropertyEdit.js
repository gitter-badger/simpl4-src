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
/*
*/
qx.Class.define("ms123.settings.PropertyEdit", {
	extend: qx.ui.container.Composite,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (facade) {
		this.base(arguments);
		this._facade = facade;
		this._model = this._facade.model;
		this.__init();
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		__init:function(){
			this.setLayout(new qx.ui.layout.Dock());
			var form =  this._createEditForm();
			this.add( form, {edge:"center"});
			var toolbar =  this._createToolbar();
			this.add( toolbar, {edge:"south"});
			this._load();
		},
		_createEditForm: function () {
		},
		_getModelPath: function (model) {
			var path = [];
			path.push( model.getId() );
			while(model.parent){
				model = model.parent;
				path.push( model.getId() );
			}
			path= path.reverse();
			path.splice(0,1);
			return path.join("/");
		},
		_load: function () {
			var resourceid = this._model.getId();
			try {
				var data = ms123.util.Remote.rpcSync("setting:getResourceSetting", {
					namespace: this._facade.storeDesc.getNamespace(),
					settingsid: this._facade.settingsid,
					resourceid: resourceid
				});
				if (data) {
console.log("data:"+JSON.stringify(data,null,2));
					this._form.setData(data);
				}
			} catch (e) {
				ms123.form.Dialog.alert("settings.views.PropertyEdit._load:" + e);
			}
		},
		_save: function () {
			var resourceid = this._model.getId();
			var data = this._form.getData();
			try {
				ms123.util.Remote.rpcSync("setting:setResourceSetting", {
					namespace: this._facade.storeDesc.getNamespace(),
					settingsid: this._facade.settingsid,
					resourceid: resourceid,
					settings: data
				});
				ms123.form.Dialog.alert(this.tr("settings.properties_saved"));
				ms123.config.ConfigManager.clearCache();
			} catch (e) {
				ms123.form.Dialog.alert("settings.views.PropertyEdit._save:" + e);
			}
		},
		_createToolbar: function () {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing(5);
			toolbar.addSpacer();
			var buttonSave = new qx.ui.toolbar.Button(this.tr("meta.lists.savebutton"), "icon/16/actions/document-save.png");
			buttonSave.setToolTipText(this.tr("meta.lists.fs.save"));
			buttonSave.addListener("execute", function () {
				this._save();
			}, this);
			toolbar._add(buttonSave);
			return toolbar;
		}
	}
});
