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
qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.SelectBox", {
	extend: qx.ui.form.SelectBox,
	implement: [qx.ui.form.IStringForm,ms123.graphicaleditor.plugins.propertyedit.IUpdate],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (items) {
		this.base(arguments);
		this.items = items;
console.log("items:",this.items);
	},
	/******************************************************************************
	 EVENTS
	 ******************************************************************************/
	events: {
		"changeValue": "qx.event.type.Data"
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		__maskedEval: function (scr, env) {
			if( scr === false) return false;
			if( scr === true) return true;
			return (new Function("with(this) { return " + scr + "}")).call(env);
		},
		_getItem:function(model){
			for( var i=0; i < this.items.length;i++){
				var value = this.items[i].value();
				if( value == model){
					return this.items[i];
				}
			}
		},
		// interface implementation
		envChanged: function (env) {
			var selectables = this.getSelectables(true);
			for( var i=0; i < selectables.length;i++){
				var model = selectables[i].getModel();
				var item = this._getItem(model);
				if( item != null){
					var enabled = item.enabled();
					var b = this.__maskedEval(enabled,env);
					selectables[i].setEnabled( b );
				}
			}
		},
		resetValue: function () {},
		setValue: function (value) {
			this.setModelSelection([value]);
			this._value = value;
		},


		// interface implementation
		getValue: function () {
			var value = this.getModelSelection();
			//console.log("SelectBox.getValue:" + value);
			if (value && value.length > 0) {
				//console.log("\tgetValue:" + value.getItem(0));
				return value.getItem(0);
			}
		},
		addItem:function(item){ //@@@MS Async added
			this.add(item);
			if( this._value){
				this.setModelSelection([this._value]);
			}
		}
	}
});
