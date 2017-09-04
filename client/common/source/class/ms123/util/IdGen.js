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
 * Specific data cell renderer for dates.
 */
qx.Class.define("ms123.util.IdGen", {

/*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {
		// private property
		_id: 100,

		// public method for encoding
		nextId: function () {
			var id = ms123.util.IdGen._id++;
			return "xid"+ id;
		},
		id: function (prefix) {
			var res = [],
				hex = '0123456789ABCDEF';

			for (var i = 0; i < 36; i++) res[i] = Math.floor(Math.random() * 0x10);

			res[14] = 4;
			res[19] = (res[19] & 0x3) | 0x8;

			for (var i = 0; i < 36; i++) res[i] = hex[res[i]];

			res[8] = res[13] = res[18] = res[23] = '-';

			return (prefix ? prefix : "ID_") + res.join('');
		}
	}
});
