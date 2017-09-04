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

qx.Class.define("ms123.shell.views.NewFile", {
	extend: ms123.shell.views.NewNode,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (model,param,facade) {
		this.base(arguments,model,facade);
		this.model = model;
		this._createDialog();
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createDialog: function () {
			var formData = {
				"name": {
					'type': "TextField",
					'label': this.tr("shell.file_name"),
					'validation': {
						required: true,
						filter:/[a-zA-Z0-9_.]/,
						validator: "/^[A-Za-z]([0-9A-Za-z_.]){1,48}$/"
					},
					'value': ""
				},
				"nodetype" : {
					'type'  : "SelectBox",
					'label' : this.tr("shell.node_type"),
					'value' : 1,
					'options' : ms123.shell.FileType.getFileOptions()
				}
			};

			var self = this;
			var form = new ms123.form.Form({
				"formData": formData,
				"allowCancel": true,
				"inWindow": true,
				"callback": function (m) {
					if (m !== undefined) {
						var val = m.get("name");
						var nt = m.get("nodetype");
						var res = self._handleTypeSpecific(val,nt);
						if( !this._assetExists(res.name,res.nt)){
							console.log("_createNode:"+JSON.stringify(res,null,2));
							self._createNode(res.name, "file", res.nt,res.content);
						}
					}
				},
				"context": self
			});
			form.show();
		},
		_handleTypeSpecific:function(name, nt){
			if( nt.match("^"+ms123.shell.Config.GROOVY_FT)){
				return this._handleGroovy(name,nt);
			}
			if( nt.match("^"+ms123.shell.Config.NJS_FT)){
				return this._handleNJS(name,nt);
			}
			if( nt.match("^"+ms123.shell.Config.JAVA_FT)){
				return this._handleGroovy(name,nt);
			}
			return {
				name:name,
				nt:nt,
				content:null
			}
		},
		_addExtension:function(name,nt){
			var ext = nt.substring(2);
			if( this._endsWith(name, ext)){
				return name;
			}
			return name+ext;
		},
		_getClassName:function(name,nt){
			var ext = nt.substring(2);
			if( this._endsWith(name, ext)){
				name = name.substring(0, name.length-ext.length);
			}
			return name[0].toUpperCase() + name.slice(1);
		},
		_endsWith:function(str, suffix) {
				return str.indexOf(suffix, str.length - suffix.length) !== -1;
		},
		//NJS specific ------------------------------------
		_handleNJS:function(name, nt){
			var maintype = nt.split("/")[0].toLowerCase();
			return  {
				name:this._addExtension(name,maintype),
				nt:maintype,
				content:null
			}
		},
		//Groovy specific ------------------------------------
		_handleGroovy:function(name, nt){
			var content = null;
			var maintype = nt.split("/")[0].toLowerCase();
			var subtype = null;
			try{
				subtype = nt.split("/")[1].toLowerCase();
			}catch(e){
			}
			var className = this._getClassName(name,maintype);
			if( subtype == "bean"){
				content = this._getBean(className);
			}
			if( subtype == "processor"){
				content = this._getProcessor(className);
			}
			return  {
				name:this._addExtension(name,maintype),
				nt:maintype,
				content:content
			}
		},
		_getBean:function(name){
			var code  = "import org.apache.camel.*;\n";
					code += "public class "+ name  +"{\n";
					code += "\t@Handler\n";
					code += "\tpublic String  doSomething(String body, Exchange exchange){\n";
					code += "\t}\n";
					code += "}\n";
			return code;
		},
		_getProcessor:function(name){
			var code  = "import org.apache.camel.*;\n";
					code += "public class "+ name  +" implements Processor{\n";
					code += "\tpublic void  process(Exchange exchange){\n";
					code += "\t}\n";
					code += "}\n";
			return code;
		}
		//Groovy specific end --------------------------------
	}
});
