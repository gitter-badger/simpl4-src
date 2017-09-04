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
qx.Class.define('ms123.widgets.UserDataAccess', {
	extend: ms123.widgets.DefaultDataAccess,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this.base(arguments);
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		/**
		 */
		insert: function (p) {
			var rpcParams = {
				userid: p.id,
				data: p.data
			}

			var params = {
				service: "auth",
				method: "createUser",
				parameter: rpcParams,
				async: false
			}
			return ms123.util.Remote.rpcAsync(params);
		},
		query: function (p) {
			var params = {
				service: "auth",
				method: "getUsers",
				parameter: {filter:p.filter},
				context: p.context,
				failed: p.failed,
				completed: p.completed,
				async: p.async
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		queryOne: function (p) {
			var params = {
				service: "auth",
				method: "getUser",
				parameter: {userid:p.id},
				context: p.context,
				failed: p.failed,
				completed: p.completed,
				async: p.async
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		/**
		 */
		update: function (p) {
			return this.insert(p);
		},

		/**
		 */
		'delete': function (p) {
			var params = {
				service: "auth",
				method: "deleteUser",
				parameter: {userid:p.id},
				async: false
			}
			return ms123.util.Remote.rpcAsync(params);
		}
	}
});
