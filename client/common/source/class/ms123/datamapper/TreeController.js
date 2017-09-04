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
qx.Class.define("ms123.datamapper.TreeController", {
	extend: qx.data.controller.Tree,


	construct: function (model, target, childPath, labelPath) {
		this.base(arguments,model,target,childPath,labelPath);
		this._eventsEnabled=true;
	},



	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */

	properties: {},



	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {

		/**
		 ---------------------------------------------------------------------------
		 EVENT HANDLER
		 ---------------------------------------------------------------------------
		 */
		__changeModelChildren: function (ev) {
			if( this._eventsEnabled === true ){
				var children = ev.getTarget();
				var treeNode = this.__childrenRef[children.toHashCode()].treeNode;
				var modelNode = this.__childrenRef[children.toHashCode()].modelNode;
				this.__updateTreeChildren(treeNode, modelNode);

				this._updateSelection();
			}
		},
		enableChangeEvents:function(enable){
			this._eventsEnabled=enable;
		}
	},



	/**
	 *****************************************************************************
	 DESTRUCTOR
	 *****************************************************************************
	 */

	destruct: function () {}
});
