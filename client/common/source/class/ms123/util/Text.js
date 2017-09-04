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
qx.Class.define("ms123.util.Text", {

/*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {
		explode: function (text, max) {
			text = text.replace(/  +/g, " ").replace(/^ /, "").replace(/ $/, "");
			if (typeof text === "undefined") return "";
			if (typeof max === "undefined") max = 50;
			if (text.length <= max) return text;
			var exploded = text.substring(0, max);
			text = text.substring(max);
			if (text.charAt(0) !== " ") {
				while (exploded.charAt(exploded.length - 1) !== " " && exploded.length > 0) {
					text = exploded.charAt(exploded.length - 1) + text;
					exploded = exploded.substring(0, exploded.length - 1);
				}
				if (exploded.length == 0) {
					exploded = text.substring(0, max);
					text = text.substring(max);
				} else {
					exploded = exploded.substring(0, exploded.length - 1);
				}
			} else {
				text = text.substring(1);
			}
			return exploded + "<br/>" + ms123.util.Text.explode(text,max);
		}
	}
});
