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
qx.Class.define('ms123.desktop.TaskButton', {

	extend: qx.ui.form.ToggleButton,

	properties: {
		realAppName: {
			check: 'String'
		}
	},

	construct: function (label, realAppName) {
		arguments.callee.base.call(this, label);

		this.setLabel('<span style="color:#333333;font-family:Arial;font-weight:bold;font-size:12px">' + label + '</span>');
		this.setRealAppName(realAppName);

		this.getChildControl('label').set({
			rich: true,
			marginLeft: 15
		});

		this.set({
			focusable: false,
			keepFocus: true,
			padding: 3,
			paddingRight: 5,
			height: 29,
			maxHeight: 29,
			alignY: 'middle',
			textColor: '#000000',
			minWidth: 130,
			center: false
		});

		this.setFont(new qx.bom.Font(11, ['Lucida Grande', 'Verdana']));

	},

	members: {
		_miniButton: false,
		_miniButtonStyle: false,
		_miniButtonStyleOver: false,
		_eyeMenu: null,

		_buttonWithFocus: function () {
			this._miniButtonStyle = this._decoratorWhiteNone;
			this._miniButtonStyleOver = this._decoratorWhiteBlue;

			this.set({
				textColor: '#FFFFFF'
			});
		},

		_buttonWithoutFocus: function () {
			this._miniButtonStyle = this._decoratorBlueNone;
			this._miniButtonStyleOver = this._decoratorWhiteLightBlue;

			this.set({
				textColor: '#2a60ac'
			});
		}
	}
});
