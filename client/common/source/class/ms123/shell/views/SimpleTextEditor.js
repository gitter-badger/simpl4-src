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

qx.Class.define("ms123.shell.views.SimpleTextEditor", {
	extend: qx.ui.core.Widget,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (model,param,facade) {
		this.base(arguments);
		this.facade=facade;
		this._setLayout(new qx.ui.layout.Dock());
		var type = model.getType();
		console.log("type:" , type);

		var config ={}
		config.mode = type;
		console.log("config:" , config);
		if( type == "text/html"){
			config.htmlMode=true;
		}
		if( type == "text/x-asciidoc"){
			config.helper = "DocumentAsciidoctor";
		}
		
		config.buttons = [{
			'label': "",
			'tooltip': this.tr("meta.lists.savebutton"),
			'icon': this.__getResourceUrl("save.png"),
			'context': this,
			'callback': function (m) {
				var value =  this.msgArea.getValue();
				this._saveContent(model, model.getType(), {json: value});
			}
		}];

		this.msgArea = new ms123.codemirror.CodeMirror(config);
		this.msgArea.set({
			height: null,
			width: null
		});

		this._add( this.msgArea, {edge:"center"});
		this._toolbar = this._createToolbar(model);
		this._add(this._toolbar, {
			edge: "south"
		});
		this._show(model);
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_show:function(model){
			var value=null;
			try{
				value = ms123.util.Remote.rpcSync( "git:getContent",{
												reponame:this.facade.storeDesc.getNamespace(),
												path:model.getPath()
											});
			}catch(e){
				ms123.form.Dialog.alert("TextEditor._show:"+e.message);
				return;
			}


			if( value ){
				this.msgArea.setValue(value);
			}
		},
		_createToolbar: function (model) {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing(5);
			var buttonSave = new qx.ui.toolbar.Button(this.tr("shell.save"), "icon/22/actions/dialog-ok.png");
			buttonSave.addListener("execute", function () {
				var value =  this.msgArea.getValue();
				this._saveContent(model, model.getType(), {json: value});
			}, this);
			toolbar._add(buttonSave)
			toolbar.addSpacer();
			toolbar.addSpacer();
			return toolbar;
		},
		_saveContent: function (model, type, content) {
			var path = model.getPath();
			console.log("path:" + path);
			var completed = (function (e) {
				ms123.form.Dialog.alert(this.tr("shell.saved"));
			}).bind(this);

			var failed = (function (e) {
				ms123.form.Dialog.alert(this.tr("shell.save_failed")+":"+e.message);
			}).bind(this);

			var rpcParams = {
				reponame:this.facade.storeDesc.getNamespace(),
				path:path,
				type:type,
				content: content.json
			};

			var params = {
				method:"putContent",
				service:"git",
				parameter:rpcParams,
				async: false,
				context: this,
				completed: completed,
				failed: failed
			}
			ms123.util.Remote.rpcAsync(params);
		},
		__getResourceUrl: function (name) {
			var am = qx.util.AliasManager.getInstance();
			return am.resolve("resource/ms123/" + name);
		}
	}
});
