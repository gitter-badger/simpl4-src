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
qx.Class.define("ms123.util.RegistryKeyList", {
 extend: qx.core.Object,
 include : [ qx.locale.MTranslation],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (facade,formElement,config) {
		this.base(arguments);
		console.log("config:",config);
		this.attributes = config.attributes;
		var keyList = this._getKeyList(formElement);
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_getKeyList: function (formElement) {
			var completed = (function (ret) {
				var keyList = [];
				keyList.push("-");
				for( var i = 0; i< ret.length;i++){
					keyList.push( ret[i] );
				}
				console.log("keyList.rel:"+JSON.stringify(keyList,null,2));
				for(var i=0; i < keyList.length; i++){
					var keyName = keyList[i];
					var listItem = new qx.ui.form.ListItem(keyName,null,keyName);
					if( formElement.addItem){
						formElement.addItem(listItem);
					}else{
						formElement.add(listItem);
					}
				}
				return keyList;
			}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("namespace.getNamespaces") + ":" + details.message);
			}).bind(this);

			var params = {
				service: "registry",
				method: "getKeys",
				parameter: {
					attributes:this.attributes
				},
				context: this,
				async: true,
				completed: completed,
				failed: failed
			}
			ms123.util.Remote.rpcAsync(params);
		}
	}
});
