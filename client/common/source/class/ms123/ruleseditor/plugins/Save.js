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
*/
/**
	* @ignore(Hash)
	* @ignore(Clazz)
*/

qx.Class.define("ms123.ruleseditor.plugins.Save", {
	extend: qx.core.Object,
 include : [ qx.locale.MTranslation],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade,main) {
		this.base(arguments);
		this.facade = facade;
		this.main = main;

		var save_msg = this.tr("ruleseditor.save");
		this.facade.offer({
			'name': save_msg,
			'functionality': this.save.bind(this, false),
			'group': "0",
			'icon': "icon/16/actions/document-save.png",
			'description': save_msg,
			'index': 1,
			'isEnabled': qx.lang.Function.bind(function () {
				return this.facade.getConditionColumns().length > 0 && this.facade.getActionColumns().length> 0 && this.facade.getCountRules() >0;
			}, this)
		});

	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {},
	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		/**
		 * Saves the current process to the server.
		 */
		save: function (forceNew, event) {
			var json = this.facade.getJSON();
			var jsonRulesModel = qx.lang.Json.stringify(json,null,2); 
			console.log("SAVE:"+jsonRulesModel);
			this.main.fireDataEvent("save", jsonRulesModel, null);
			return true;
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
