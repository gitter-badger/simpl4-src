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
	@ignore($)
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/places/*)
*/
qx.Class.define("ms123.permissions.RoleEdit", {
	extend: ms123.util.TableEdit,

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade) {
		this.base(arguments, facade);
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createColumnModel: function () {
			var columnmodel = [{
				name: "permission",
				width: 300,
				type: "TextField",
				header: "%permissions.permission"
			},
			{
				name: "actions",
				header: "%permissions.actions",
				type: "SelectBox",
				options: [{
					value: 'read',
					label: 'read'
				},
				{
					value: 'write',
					label: 'write'
				},
				{
					value: 'read,write',
					label: 'read,write'
				}]
			},
			{
				name: "enabled",
				type: "CheckBox",
				width: 60,
				header: "%permissions.enabled"
			}];
			return this._translate(columnmodel);
		},
		_load: function () {
			var permissions = null;
			try {
				var name = this._facade.event.name;
				var ns = name.split(".")[0];
				var ret  = ms123.util.Remote.rpcSync("permission:getRole", {
					namespace: ns,//this._facade.storeDesc.getNamespace(),
					name: name
				});
				permissions = ret.permissions;
			} catch (e) {
				ms123.form.Dialog.alert("RoleEdit._initRecords:" + e);
				return null;
			}
			return permissions;
		},
		_save: function () {
			var permissions = this._getRecords();
			console.log("_save:"+qx.util.Serializer.toJson(permissions));
			try {
				var name = this._facade.event.name;
				var ns = name.split(".")[0];
				ms123.util.Remote.rpcSync("permission:saveRole", {
					name: name,
					namespace: ns,
					data: { permissions: permissions }
				});
			} catch (e) {
				ms123.form.Dialog.alert("RoleEdit.saveRecords:" + e);
				return;
			}
			ms123.form.Dialog.alert(this.tr("permissions.role_permission_saved"));
		}
	}
});
