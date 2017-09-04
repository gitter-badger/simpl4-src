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
qx.Class.define("ms123.form.TextField", {
	extend: ms123.form.AbstractField,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (useitCheckboxes) {
		this.base(arguments,useitCheckboxes);
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */

	properties: {
		placeholder: {
			check: "String",
			nullable: true,
			apply: "_applyPlaceholder"
		},
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
		// property apply
		_applyPlaceholder: function (value, old) {
			this.getChildControl("textfield").setPlaceholder(value);
		},

		setReadOnly:function(flag){
			this.getChildControl("textfield").setReadOnly(flag);
			this.getChildControl("textfield").setBackgroundColor(flag ? "#cfcfcf" : null);
		},
		setFilter:function(flag){
			this.getChildControl("textfield").setFilter(flag);
		},
		setValid:function(flag){
			this.getChildControl("textfield").setValid(flag);
		},
		setMaxLength:function(flag){
			this.getChildControl("textfield").setMaxLength(flag);
		},
		setLiveUpdate:function(flag){
			this.getChildControl("textfield").setLiveUpdate(flag);
		},
		setInvalidMessage:function(msg){
			this.getChildControl("textfield").setInvalidMessage(msg);
		},

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
				control = new qx.ui.form.TextField();
				control.setLiveUpdate(true);
				control.setFocusable(false);
				control.addState("inner");
				control.addListener("changeValue", this._onTextFieldChangeValue, this);
				this._add(control, { flex:1 });
				break;
			}
			return control || this.base(arguments, id);
		}

	}
});
