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
qx.Class.define("ms123.form.NumberField", {
	extend: ms123.form.AbstractField,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (useitCheckboxes) {
		this.base(arguments,useitCheckboxes);
		this.setFocusable(true);
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
			if( this._bgColor==null){
				this._bgColor = this.getChildControl("textfield").getBackgroundColor();
			}
			var f = this.getChildControl("textfield").getFocusable();
console.log("setReadOnly:"+flag+"/"+this._bgColor+"/"+(flag===true)+"/"+f);
			this.getChildControl("textfield").setReadOnly(flag);
			if( flag === true){
				this.getChildControl("textfield").setBackgroundColor("#cfcfcf");
			}else{
				this.getChildControl("textfield").setBackgroundColor(this._bgColor);
			}
			this.getChildControl("textfield").setFocusable(false);
		},
		setFilter:function(flag){
			this.getChildControl("textfield").setFilter(flag);
		},
		setValid:function(flag){
			this.getChildControl("textfield").setValid(flag);
		},
		setInvalidMessage:function(msg){
			this.getChildControl("textfield").setInvalidMessage(msg);
		},
		// interface implementation
		setValue: function (value) {
			var textfield = this.getChildControl("textfield");
			if ((typeof textfield.getValue()) == (typeof value) &&  textfield.getValue() == value) {
				return;
			}

			// Apply to text field
			if( value != undefined && value != null && !isNaN(value)){
				textfield.setValue(new String(value));
			}else{
				textfield.setValue(new String(""));
			}
		},
		// interface implementation
		getValue: function () {
			try{
				var v = parseInt(this.getChildControl("textfield").getValue());
				if( isNaN(v)) return null;
				return v;
			}catch(e){
				return null;
			}
		},

    _onTextFieldChangeValue : function(e) {
      var value = e.getData();
			var v;
			try{ v = parseInt(value); }catch(e){
				console.log("NumberField._onTextFieldChangeValue:"+e);
			}
      this.fireDataEvent("changeValue", v, e.getOldData());
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
				control.setFocusable(false);
				break;
			}
			return control || this.base(arguments, id);
		}

	}
});
