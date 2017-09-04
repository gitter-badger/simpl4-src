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

qx.Class.define("ms123.shell.views.NewNode", {
	extend: qx.core.Object,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (model,facade) {
		this.base(arguments);
		this.model = model;
		this.facade=facade;
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createNode: function (name,what, nodetype,content) {
			console.log("_createNode:" + qx.util.Serializer.toJson(this.model));

			var path=null;
			if( this.model.getPath() == "root" ){
				path = name;
			}else{
				path = this.model.getPath() + "/" + name;
			}
			console.log("path:" + path);
			console.log("name:" + name);
			var completed = (function (data) {
				ms123.form.Dialog.alert(this.tr("shell."+what+"_created"));
				var ret = data;
				var m = {
					"title": name,
					"path": path,
					"value": name,
					"id": name,
					"children": [],
					"type": nodetype 
				};
				if( nodetype == "sw.directory"){
					m.children = [];
				}
				var parentChilds = this.model.getChildren();
				var nodeModel = qx.data.marshal.Json.createModel(m);
				nodeModel.parent = this.model;
				parentChilds.insertAt(0, nodeModel);
			}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("shell."+what+"_create_failed")+":"+details.message);
			}).bind(this);

			var rpcParams = {
				reponame:this.facade.storeDesc.getNamespace(),
				path:path,
				content:content,
				overwrite:false,
				type: nodetype
			};

			var params = {
				method:"createObject",
				service:"git",
				parameter:rpcParams,
				async: false,
				context: this,
				completed: completed,
				failed: failed
			}
			ms123.util.Remote.rpcAsync(params);
		},
		_assetExists:function(name,type){
			return false;
			var rpcParams = {
				reponame:this.facade.storeDesc.getNamespace(),
				name:name,
				type: type
			};
			var params = {
				method:"assetList",
				service:"git",
				parameter:rpcParams,
				async: false,
				context: this
			}
			try{
				var list = ms123.util.Remote.rpcAsync(params);
				if( list.length > 0){
					var text = "<br/>";
					for( var i =0; i < list.length;i++){
						text += list[i] + "<br/>";
					}
					ms123.form.Dialog.alert(this.tr("shell.name")+
									"("+ name + ") "+
									this.tr("shell.for")+
									" '"+ type + "' "+
									this.tr("shell.asset_exists")+
									":"+text);
					return true;
				}
				return false;
			}catch(details){
				ms123.form.Dialog.alert(this.tr("shell.assetExists_failed")+":"+details.message);
			}
			return true;
		}
	}
});
