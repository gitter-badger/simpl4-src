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
	@asset(qx/icon/${qx.icontheme}/16/apps/*)
	@asset(ms123/icons/*)
	@asset(ms123/*)
*/

qx.Class.define("ms123.shell.views.DelNode", {
	extend: qx.core.Object,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (model,param,facade) {
		this.base(arguments);
		this.model = model;
		this.facade=facade;
		this._delDialog();
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_delDialog: function () {
			console.log("_delNode:" + qx.util.Serializer.toJson(this.model));
			ms123.form.Dialog.confirm(this.tr("shell.confirm_delete")+":"+this.model.getPath(), function (e) {
				if (e) {
					this._delNode.call(this);
				}
			}, this);
		},
		_delNode: function () {
			console.log("_delNode:" + qx.util.Serializer.toJson(this.model));

			var	path = this.model.getPath();
			console.log("path:" + path);
			var completed = (function (data) {
				ms123.form.Dialog.alert(this.tr("shell.node_deleted")+":"+this.model.getPath());
				var name = this.model.getValue();
				console.log("name:"+name);
				var children = this.model.parent.getChildren();
				var len = children.getLength();
				console.log("len:"+len);
				for(var i=0; i < len; i++){
					var child = children.getItem(i);
					console.log("\tname:"+child.getValue());
					if( child.getValue() == name){
						children.remove(child);
						break;
					}
				}
			}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("shell.node_delete_failed")+":"+details.message);
			}).bind(this);

			var rpcParams = {
				reponame:this.facade.storeDesc.getNamespace(),
				path:path
			};

			var params = {
				method:"deleteObject",
				service:"git",
				parameter:rpcParams,
				async: false,
				context: this,
				completed: completed,
				failed: failed
			}
			ms123.util.Remote.rpcAsync(params);


		}
	}
});
