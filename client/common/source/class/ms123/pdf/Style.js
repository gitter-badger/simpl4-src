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
 */
qx.Class.define("ms123.pdf.Style", {
	extend: qx.core.Object,

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function () {},

	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {
		_prefixes: ['ms', 'Moz', 'Webkit', 'O'],
		_cache: {},
		getProp: function (propName, element) {
			if (arguments.length == 1 && typeof ms123.pdf.Style._cache[propName] == 'string') {
				return ms123.pdf.Style._cache[propName];
			}

			element = element || document.documentElement;
			var style = element.style,
				prefixed, uPropName;

			if (typeof style[propName] == 'string') {
				return (ms123.pdf.Style._cache[propName] = propName);
			}
			uPropName = propName.charAt(0).toUpperCase() + propName.slice(1);
			for (var i = 0, l = ms123.pdf.Style._prefixes.length; i < l; i++) {
				prefixed = ms123.pdf.Style._prefixes[i] + uPropName;
				if (typeof style[prefixed] == 'string') {
					return (ms123.pdf.Style._cache[propName] = prefixed);
				}
			}
			return (ms123.pdf.Style._cache[propName] = 'undefined');
		},
		setProp: function (propName, element, str) {
			var prop = ms123.pdf.Style.getProp(propName);
			if (prop != 'undefined') {
				element.style[prop] = str;
			}
		}
	},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {}
});
