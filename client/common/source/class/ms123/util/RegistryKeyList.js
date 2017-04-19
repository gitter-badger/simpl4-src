/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
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
