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
qx.Class.define("ms123.util.Clone", {

/*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {
		clone: function (item) {
			if (!item) {
				return item;
			}
			var types = [Number, String, Boolean];
			var result;

			types.forEach(function (type) {
				if (item instanceof type) {
					result = type(item);
				}
			});

			if (typeof result == "undefined") {
				if (Object.prototype.toString.call(item) === "[object Array]") {
					result = [];
					item.forEach(function (child, index, array) {
						result[index] = ms123.util.Clone.clone(child);
					});
				} else if (ms123.util.Clone.isRegExp(item)) {
					result = ms123.util.Clone.regexpClone(item);
				} else if (typeof item == "object") {
					if (!item.prototype) {
						// it is an object literal
						result = {};
						for (var i in item) {
							result[i] = ms123.util.Clone.clone(item[i]);
						}
					} else {
						result = item;
					}
				} else {
					result = item;
				}
			}
			return result;
		},
		isRegExp: function (o) {
			return 'object' == typeof o && '[object RegExp]' == Object.prototype.toString.call(o);
		},
		regexpClone: function (regexp) {
			var flags = [];
			if (regexp.global) flags.push('g');
			if (regexp.multiline) flags.push('m');
			if (regexp.ignoreCase) flags.push('i');
			return new RegExp(regexp.source, flags.join(''));
		},
		merge: function (target, varargs) {
			var len = arguments.length;

			for (var i = 1; i < len; i++) {
				qx.lang.Object.mergeWith(target, arguments[i]);
			}
			return target;
		}
	}
});
