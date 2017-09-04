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
qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.ColorPopup", {
	extend: qx.ui.container.Composite,
	implement: [qx.ui.form.IStringForm],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function () {
		this.base(arguments);
		this.setLayout(new qx.ui.layout.HBox());
		this._colorPopup = new qx.ui.control.ColorPopup();
		this._colorPopup.exclude();

		this._colorPopup.addListener("changeValue", function (e) {
 			var value = e.getData();
			if( value == null){
				this._button.setLabel( "------");
				this._button.setTextColor( null);
				this._button.setBackgroundColor( null);
			}else{
				this._button.setLabel( value);
				this._button.setTextColor( value);
				this._button.setBackgroundColor( value);
			}
      this.fireDataEvent("changeValue", value, e.getOldData());
		},this);

		this._button = new qx.ui.form.Button("Choose Color");
		this.add(this._button);

		this._button.addListener("mousedown", function (e) {
			this._colorPopup.placeToPointer(e)
			this._colorPopup.show();
		}, this);

		this.setFocusable(true);
		this._button.setFocusable(false);
		this._colorPopup.setFocusable(false);
	},

	events: {
    "changeValue": "qx.event.type.Data"
  },

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		setValue: function (value) {
			try{
				if( value != null && value!="none"){
					this._button.setLabel( value);
					this._colorPopup.setValue(value);
					this._button.setTextColor( value);
					this._button.setBackgroundColor( value);
				}else{
					this._button.setLabel( "------");
					this._button.setTextColor( null);
					this._button.setBackgroundColor( null);
				}
			}catch(e){
				console.error("ColorPopup.setValue:"+e);
			}
		},

		getValue: function () {
			return this._colorPopup.getValue();
		},

		resetValue: function () {
			this._colorPopup.resetValue();
			this._button.setTextColor( this._colorPopup.getValue());
		}
	}
});
