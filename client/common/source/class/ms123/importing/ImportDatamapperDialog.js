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

qx.Class.define("ms123.importing.ImportDatamapperDialog", {
	extend: qx.core.Object,
	include: qx.locale.MTranslation,

	statics: {
	},

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this.base(arguments);

		this.__storeDesc = context.storeDesc;
		this.__id = context.id;
		this.__prefix = context.prefix;
		this.__mainEntity = context.mainEntity;
		this.__fileType = context.fileType;
		this.__configManager = new ms123.config.ConfigManager();

		var win = context.parentWidget;
		if (win.hasChildren()) {
			win.removeAll();
		}
		this.__user = ms123.config.ConfigManager.getUserProperties();
		win.add(this._createDatamapper(), {
			edge: "center"
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
		_createDatamapper:function(){
			var context = {};
			context.storeDesc = this.__storeDesc;
			context.use=ms123.datamapper.Config.USE_IMPORT;
			context.importingid= this.__prefix + "/" + this.__id;
			context.mainEntity= this.__mainEntity;
			var dm = new ms123.datamapper.Datamapper(context);
			dm.addListener("save2", function(e){
				var settings = e.getData();
				this._saveSettings(settings);
			}, this);
			var setting = this._loadSettings();
			dm.init(setting );
			return dm;
		},
		_saveSettings: function (settings) {
			settings.database=this._database;
			var ret = null;
			try {
				ret = ms123.util.Remote.rpcSync("importing:updateImporting", {
					namespace: this.__storeDesc.getNamespace(),
					settings: settings,
					importingid: this.__prefix + "/" + this.__id
				});
				ms123.form.Dialog.alert(this.tr("import.import_saved"));
			} catch (e) {
				ms123.form.Dialog.alert("ImportDatamapperDialog._saveSettings:" + e);
				return null;
			}
			return ret;
		},
		_loadSettings: function () {
			var settings = null;
			try {
				settings = ms123.util.Remote.rpcSync("importing:getSettings", {
					namespace: this.__storeDesc.getNamespace(),
					importingid: this.__prefix + "/" + this.__id
				});
			} catch (e) {
				ms123.form.Dialog.alert("ImportDatamapperDialog._loadSettings:" + e);
				return null;
			}
			this._database = settings.database;
			return settings;
		}
	}
});
