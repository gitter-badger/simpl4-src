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
qx.Class.define('ms123.desktop.Window', {
	extend: qx.ui.window.Window,

	construct: function (application, caption, icon) {
		this.base(arguments, caption, icon);

		this.addListener('beforeMinimize', function (e) {
			this.setLastMode(this.getMode());
		}, this);
	},

	/*******************************************************************************
	 PROPERTIES
	 ***************************************************************************** */
	properties: {
	},

	members: {
		_lastMode: null,

		setLastMode: function (mode) {
			this._lastMode = mode;
		},
		getLastMode: function () {
			return this._lastMode;
		},

		open: function (left, top) {
			if (typeof left !== 'undefined' && typeof top !== 'undefined') {
				this.moveTo(left, top)
			} else {
				this.center();
			}
			this.base(arguments);
		}
	}
});
