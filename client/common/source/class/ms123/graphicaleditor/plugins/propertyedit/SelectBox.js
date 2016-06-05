/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
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
