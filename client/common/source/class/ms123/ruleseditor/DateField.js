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
	* @ignore(Hash)
*/
qx.Class.define('ms123.ruleseditor.DateField', {
	extend: qx.ui.form.DateField,

	construct: function () {
		this.base(arguments);
  	var m = qx.locale.Manager.getInstance();
    this.__lang = m.getLanguage();
		if( this.__lang == "de" ){
			var format = new qx.util.format.DateFormat("dd.MM.yyyy"); //@@@MS TODO
			this.setDateFormat(format);
		}
	},

	members: {
   selectAllText : function() {
    }

	}
});
