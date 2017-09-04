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

qx.Class.define("ms123.datamapper.edit.ImportUploadWindow", {
	extend: ms123.datamapper.edit.UploadWindow,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade, config, title) {
		this._facade = facade;
		this.base(arguments,facade,config,title);
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_getRpcHeader: function (id) {
			var rpc = {
				"service": "importing",
				"method": "upload",
				"id": 31,
				"params": {
					"importingid": this._facade.importingid,
					"withoutImport": true,
					"storeId": this._facade.storeDesc.getStoreId()
				}
			};
			return rpc;
		},
		_saveFile: function (params) {
			try {
				ms123.util.Remote.rpcSync("importing:upload", params);
			} catch (e) {
				ms123.form.Dialog.alert("ImportUploadWindow._saveFile:" + e);
				return null;
			}
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
