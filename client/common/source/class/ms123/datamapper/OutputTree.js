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
/*
*/

qx.Class.define("ms123.datamapper.OutputTree", {
	extend: ms123.datamapper.BaseTree,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade,data) {
		this.base(arguments,data);
	},

	/******************************************************************************
	 EVENTS
	 ******************************************************************************/
	events: {
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},
	statics: {
  },

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_customize:function(config){
			var l = new qx.ui.basic.Label().set({
				rich:true
			});
			this._label = l;
			this.setFormat(config.format);
			this.add( l, {edge:"north"});
		},
		setFormat:function(format){
			this._label.setValue("<div style='border-bottom:1px solid gray;padding:3px;'><b>"+this.tr("datamapper.output") + ":"+ format+"</b></div>");
		},
		getSide:function(){
			return ms123.datamapper.Config.OUTPUT;
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
