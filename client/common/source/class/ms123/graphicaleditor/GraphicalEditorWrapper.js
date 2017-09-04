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
*/
qx.Class.define("ms123.graphicaleditor.GraphicalEditorWrapper", {
	extend: ms123.graphicaleditor.GraphicalEditor,

	/**
	 * Constructor
	 */
	construct: function (context) {
		this.base(arguments);
		this.setLayout(new qx.ui.layout.Dock());
		context.window.add(this, {});
		var editorType=null;
		var diagramName = null;

		if( !context.editorType ){
			if( context.moduleName == "activitiprocess" ){
				editorType = "sw.process";
				context.window.setCaption("GraphicalEditor("+context.data.pid+")");
				diagramName = context.data.pid;
			}
			if( context.moduleName == "form" ){
				editorType = "sw.form";
				context.window.setCaption("GraphicalEditor("+context.data.fid+")");
				diagramName = context.data.fid;
			}
		}
		this.addListener("save",this._save,this);
		this.addListener("deploy",this._deploy,this);
		this.addListener("undeploy",this._undeploy,this);

		this._moduleName = context.moduleName;
		this._id = context.data.id;
		this._pid = context.data.pid;
		var json = this._loadFile();
		this.init(editorType,diagramName,json);
	},

	events: {
		"changeValue": "qx.event.type.Data"
	},
	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {
		_loadFile:function(){
			var id = this._id;
			console.log("import:" + id);
			if (id != undefined && id) {
				var url = "data/"+this._moduleName+"/" + id;
				var map = ms123.util.Remote.sendSync(url + "?what=asRow");
				return map.json;
			}
			return "";
		},
		__deploy:function(prefix){
			var okMessage = this.tr("data.process."+prefix+"deployed");
			var failMessage = this.tr("data.process."+prefix+"deploy_failed");
			var url = "xconfig/"+prefix+"deployprocess/" + this._pid;

			var completed = function (e) {
				ms123.form.Dialog.alert(okMessage);
				var eventBus = qx.event.message.Bus;
				eventBus.getInstance().dispatchByName("processdiagram.deployed", {});
			};

			var failed = function (e) {
				var txt = e.getContent().replace(/\\n/g, "<br />");
				ms123.form.Dialog.alert(failMessage + ":" + e.getStatusCode() + "/" + txt);
			};
			var params = {
				url: url,
				method: "GET",
				async: true,
				context: this,
				failed: failed,
				completed: completed
			}
			ms123.util.Remote.send(params);
		},
		_undeploy:function(e){
			this.__deploy("un");
		},
		_deploy:function(e){
			this.__deploy("");
		},
		_save:function(e){
			var jsonProcessModel = e.getData();
			var url = "data/" + this._moduleName + "/" + this._id + "?what=asRow";
			var completed = function (e) {
				ms123.form.Dialog.alert(this.tr("data." + this._moduleName + ".saved"));
			};
			var failed = function (e) {
				ms123.form.Dialog.alert(this.tr("data." + this._moduleName + ".savefailed:" + e));
			};
			var data = "json=" + encodeURIComponent(jsonProcessModel);
			this.fireChanged(jsonProcessModel);

			var params = {
				url: url,
				method: "PUT",
				data: data,
				async: true,
				context: this,
				completed: completed,
				failed: failed
			}
			ms123.util.Remote.send(params);
		},
		fireChanged: function (data) {
			console.log("graphicaleditor.fireDataEvent");
			this.fireDataEvent("changeValue", data, null);
		}
	}
});
