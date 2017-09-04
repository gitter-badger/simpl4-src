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
	* @ignore($)
*/
qx.Class.define("ms123.form.GridInputButton", {
	extend: qx.ui.form.MenuButton,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (icon) {
		this.base(arguments, "", icon);
		this.addListener("mouseover", this.hover.bind(this));
		this.addListener("mouseout", this.reset.bind(this));
		this.addListener("mouseup", this.hover.bind(this));

		this.setDecorator(null);
		this.setPadding(0, 0, 0, 0);
		this.setWidth(16);
		this.setHeight(16);

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
		reset: function () {
			var node = this._getNode();
			//			node.style.border='0px';
			//			node.style.padding='0px';
			node.style.opacity = '1.0';
		},

		hover: function (evt) {
			var node = this._getNode();
			//			node.style.border='1px solid gray';
			//			node.style.borderRadius='2px';
			node.style.opacity = '0.5';
			//			node.style.padding='1px';
		},

		_getNode: function () {
			var icon = this.getChildControl("icon", false);
			var node = icon.getContentElement().getDomElement();
			return node;
		}

	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
