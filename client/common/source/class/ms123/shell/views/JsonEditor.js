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

qx.Class.define("ms123.shell.views.JsonEditor", {
	extend: ms123.shell.views.SimpleTextEditor,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (model,param,facade) {
		this.base(arguments,model,param,facade);
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
			var item=null;
			try{
				item = ms123.util.Remote.rpcSync( "git:getContent",{
												reponame:this.facade.storeDesc.getNamespace(),
												path:model.getPath()
											});
				console.log("JsonEditor:"+qx.util.Serializer.toJson(item));
			}catch(e){
				ms123.form.Dialog.alert("JsonEditor._show:"+e.message);
				return;
			}


			if( item ){
				var value = qx.lang.Json.stringify(  qx.lang.Json.parse(item), null, 4 );
				this.msgArea.setValue(value);
			}
		},
		_createToolbar: function (model) {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing(5);
			var buttonSave = new qx.ui.toolbar.Button(this.tr("shell.save"), "icon/22/actions/dialog-ok.png");
			buttonSave.addListener("execute", function () {
				var value =  this.msgArea.getValue();
				try{
				value = qx.lang.Json.stringify(qx.lang.Json.parse(value),null,2);
				}catch(e){
					ms123.form.Dialog.alert("Error:"+e);
					return;
				}
				this._saveContent(model, null, {json: value});
			}, this);
			toolbar.addSpacer();
			toolbar._add(buttonSave)
			return toolbar;
		}
	}
});
