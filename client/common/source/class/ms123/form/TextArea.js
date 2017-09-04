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
qx.Class.define("ms123.form.TextArea", {
	extend: ms123.form.AbstractField,


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */
	construct: function (useitCheckboxes) {
		this.base(arguments,useitCheckboxes);
	},

	properties: {
 // overridden
    focusable : {
      refine : true,
      init : true
    }
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		/**
		 ---------------------------------------------------------------------------
		 WIDGET API
		 ---------------------------------------------------------------------------
		 */

		// overridden
		_createChildControlImpl: function (id, hash) {
			var control;

			switch (id) {
			case "textfield":
				control = new qx.ui.form.TextArea();
				control.setFocusable(false);
				control.addState("inner");
				control.addListener("changeValue", this._onTextFieldChangeValue, this);
				this._add(control, { flex:1 });
				break;
			}
			return control || this.base(arguments, id);
		},
		setAutoSize:function( h ){
			this.getChildControl("textfield").setAutoSize(h);
		},

		_setHeight:function( h ){
			this.getChildControl("textfield").setHeight(h);
			this.setHeight(h);
		}
	}
});
