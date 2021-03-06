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
 * @ignore(Clazz)
 * @ignore(jQuery)
 * @ignore(jsPlumb.*)
 * @ignore(Clazz.extend)
 */

qx.Class.define("ms123.datamapper.plugins.Import", {
	extend: ms123.datamapper.plugins.Preview,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade, config) {
		this.base(arguments,facade,config);
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {},
	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_addButton:function(){
			var upload_msg = this.tr("datamapper.load_import_file");
			var execute_msg = this.tr("datamapper.execute");
			var execute_preview_msg = this.tr("datamapper.execute_preview");
			var group = "2";
			this._facade.offer({
				name: upload_msg,
				description: upload_msg,
				icon: "resource/ms123/upload_icon.gif",
				functionality: this.uploadFile.bind(this),
				group: group,
				index: 1,
				isEnabled: qx.lang.Function.bind(function () {
					return true;
				}, this)
			});
			this._facade.offer({
				name: execute_preview_msg,
				description: execute_preview_msg,
				icon: "resource/ms123/preview.png",
				functionality: this.execute_preview.bind(this),
				group: group,
				index: 2,
				isEnabled: qx.lang.Function.bind(function () {
					return this._uploaded === true;
				}, this)
			});
			this._facade.offer({
				name: execute_msg,
				description: execute_msg,
				icon: "resource/ms123/run_exec.png",
				functionality: this.execute_import.bind(this),
				group: group,
				index: 3,
				isEnabled: qx.lang.Function.bind(function () {
					return this._uploaded === true;
				}, this)
			});
		},
		createUploadWindow:function(id){
			return new ms123.datamapper.edit.ImportUploadWindow(this._facade,{id:id}, this.tr("datamapper.upload_file"));
		},
		_execute: function (withoutSave) {
			var storeId= this._facade.isOrientDB ? this._facade.storeDesc.getNamespace()+"_odata" : this._facade.storeDesc.getStoreId();
			self = this;
			var completed = function (ret) {
				self._hideWait();
				if(!qx.lang.Type.isString(ret)){
					ret = JSON.stringify(ret,null,2);
				}
				this._msgArea.setValue( ret );
			}
			var failed = function (e) {
				self._hideWait();
				var msg = e.message;
				msg = msg.replace(/Application error 500/g, "");
				msg = msg.replace(/ImportingService.doImport:/g, "");
				ms123.form.Dialog.alert("<b>Error</b>"+msg);
			}
			var rpcParams = {
				storeId: storeId,
				withoutSave:withoutSave,
				importingid: this._facade.importingid
			};
			var params = {
				service: "importing",
				method: "doImport",
				parameter: rpcParams,
				failed: failed,
				completed: completed,
				async: true,
				context: this
			}
			this._showWait();
			ms123.util.Remote.rpcAsync(params);
		},
		_hideWait: function () {
			this._waitdia.hide();
		},
		_showWait: function () {
			this._waitdia = new ms123.form.Alert({
				"message": "<h3>"+this.tr("datamapper.please_wait")+"</h3>",
				"noOkButton": true,
				"inWindow": true,
				"hide": false,
				"context": this
			});
			this._waitdia.show();
		},
		execute_import: function (e) {
			this._execute(false);
		},
		execute_preview: function (e) {
			this._execute(true);
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {
	}

});
