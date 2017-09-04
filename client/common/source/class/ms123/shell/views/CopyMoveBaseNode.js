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

qx.Class.define("ms123.shell.views.CopyMoveBaseNode", {
	extend: ms123.shell.views.NewNode,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (model, param, facade) {
		this.base(arguments, model, facade);
		this._model = model;
		console.log("model:" + qx.util.Serializer.toJson(model));
		this._createResourceWindow();
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_getFunctionString:function(){
		},
		_getDestModel: function (model,path) {
			if( model.getPath && model.getPath() == path) return model;
			var children = model.getChildren();
			for (var i = 0; i < children.getLength(); i++) {
				var c = children.getItem(i);
				var model = this._getDestModel(c,path);
				if( model != null) return model;
			}
			return null;
		},
		_createResourceWindow:function(){
			var context = {};
			context.storeDesc = this.facade.storeDesc;
			var path = this.model.getPath();
			context.title=path +" " + this.tr("shell."+this._getFunctionString()+"_to");
			var currDir = this._getParent(path);

			var destName = this._getBase(path);
			context.ok_callback = (function(data){
				var path = data.path;
				var name = data.name;
				var destModel = this._getDestModel(this.facade.treeModel, path);;
				var nt = this.model.getType();
				if (this._getFunctionString() == "move" || !this._assetExists(name, nt)) {
					var newPath = path + "/"+ name;
					if( path == "root"){
						newPath = name;
					}
					this._doIt(this.model.getPath(), newPath, name, destModel, nt);
				}
			}).bind(this);
			new ms123.shell.ResourceSelectorWindow(context,currDir,destName);
		},
		_getParent: function (path) {
			var lastIndex = path.lastIndexOf("/");
			if (lastIndex == -1) return "";
			return path.substring(0, lastIndex);
		},
		_getBase: function (path) {
			var lastIndex = path.lastIndexOf("/");
			if (lastIndex == -1) return path;
			return path.substring(lastIndex+1);
		},
		_doIt: function (origPath, newPath, name,destModel, nodetype) {
			console.log("_doIt:origPath:"+origPath+"|newPath:"+newPath+"|name:"+name);
			var completed = (function (data) {
				ms123.form.Dialog.alert(this.tr("shell."+this._getFunctionString()+"_ready"));
				var ret = data;
				var m = {
					"title": name,
					"path": newPath,
					"value": name,
					"id": name,
					"children": [],
					"type": nodetype
				};
				var parentChilds = destModel.getChildren();
				var nodeModel = qx.data.marshal.Json.createModel(m);
				nodeModel.parent = destModel;
				parentChilds.insertAt(0, nodeModel);

				if( this._getFunctionString() == "move"){
					this.model.parent.getChildren().remove(this.model);
				}
			}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("shell."+this._getFunctionString()+"_failed") + ":" + details.message);
			}).bind(this);

			var rpcParams = {
				reponame: this.facade.storeDesc.getNamespace(),
				oldPath: origPath,
				origPath: origPath,
				newPath: newPath
			};

			var params = {
				method: this._getFunctionString()+"Object",
				service: "git",
				parameter: rpcParams,
				async: false,
				context: this,
				completed: completed,
				failed: failed
			}
			ms123.util.Remote.rpcAsync(params);
		}
	}
});
