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
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/apps/*)
	@asset(qx/icon/${qx.icontheme}/48/actions/*)
	@asset(qx/icon/${qx.icontheme}/48/apps/*)
	@ignore($)
*/
qx.Class.define("ms123.form.GridInputSelector", {
	extend: ms123.form.RelatedTo,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (context,useitCheckboxes) {
		this.base(arguments,context);
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_init:function(){
			var select = this._createChildControl("select");
		},
		setModule: function (mod) {
			this._modulename = ms123.util.Inflector.singularize(mod);
		},
		_setSelectedValues:function(cr,m){
		},
		// interface implementation
		setValue: function (value) {
		},
		// interface implementation
		getValue: function () {
		},
		// interface implementation
		resetValue: function () {
		},
		// useit checkbox
		getCheckBox: function () {
		},

		/**
		 ---------------------------------------------------------------------------
		 TEXTFIELD SELECTION API
		 ---------------------------------------------------------------------------
		 */
		getTextSelection: function () {
		},
		getTextSelectionLength: function () {
		},
		setTextSelection: function (start, end) {
		},
		clearTextSelection: function () {
		},
		selectAllText: function () {
		}
	}
});
