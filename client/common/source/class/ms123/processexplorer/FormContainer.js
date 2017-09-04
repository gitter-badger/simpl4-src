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
	* @ignore(Clazz)
*/
qx.Class.define("ms123.processexplorer.FormContainer", {
  extend: ms123.processexplorer.FormWindow,
	include: [qx.locale.MTranslation],

	/**
	 * Constructor
	 */
	construct: function (context) {
		this.base(arguments,null,null);
		this._parenContainer = context.parentContainer;
		if( context.window){
			this._parenContainer = new qx.ui.container.Composite(new qx.ui.layout.Dock());
			context.window.add(this._parenContainer );
			this._parenContainer.setLayout(new qx.ui.layout.Dock());
		}
	},
	statics: {
		__formCache: {}
	},

	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {
		_init: function () {
		},
		close: function () {
		},
		destroy: function () {
			this._parenContainer._removeAll();
		},
		open: function (params) {
			if( params.taskName ){
				//this._window.setCaption(params.processName+"/"+params.taskName);
			}else{
				//this._window.setCaption(params.processName);
			}
			var form = this.createForm(params);
			if (this._parenContainer._hasChildren()) {
				this._parenContainer._removeAll();
			}
			this._parenContainer._add(form, {
				edge: "center"
			});
			this._form = form;
		},
		_createFormWindow: function (name) {
		}
	}
});
