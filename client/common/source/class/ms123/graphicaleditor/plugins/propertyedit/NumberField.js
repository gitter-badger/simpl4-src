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
 * The TextField is a single-line text input field.
 */
qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.NumberField", {
	extend: qx.ui.form.AbstractField,


/*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

	properties: {
		// overridden
		appearance: {
			refine: true,
			init: "textfield"
		},

		// overridden
		allowGrowY: {
			refine: true,
			init: false
		},

		// overridden
		allowShrinkY: {
			refine: true,
			init: false
		}
	},

	members: {
		// interface implementation
		setValue: function (value) {
			var oldValue = this.getValue();
			if ((typeof oldValue) == (typeof value) &&  oldValue == value) {
				return;
			}

			var newValue;
			if( value != undefined && value != null && !isNaN(value)){
				newValue = new String(value);
			}else{
				newValue = new String("");
			}
			this.base(arguments,newValue);
		},
		// interface implementation
		getValue: function () {
			var value = this.base(arguments);
			try{
				var v = parseInt(value);
				if( isNaN(v)) return null;
				return v;
			}catch(e){
				return null;
			}
		},

    _onChangeContent : function(e) {
      var value = e.getData();
			var v;
			try{ v = parseInt(value); }catch(e){
				console.log("NumberField._onChangeContent:"+e);
			}
      this.__nullValue = e.getData() === null;
      this.__fireChangeValueEvent(e.getData());
    },

		// overridden
		_renderContentElement: function (innerHeight, element) {
			if ((qx.core.Environment.get("engine.name") == "mshtml") && (parseInt(qx.core.Environment.get("engine.version"), 10) < 9 || qx.core.Environment.get("browser.documentmode") < 9)) {
				element.setStyles({
					"line-height": innerHeight + 'px'
				});
			}
		}
	}
});
