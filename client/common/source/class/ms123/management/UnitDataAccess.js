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
qx.Class.define('ms123.management.UnitDataAccess', {
	extend: ms123.widgets.DefaultDataAccess,
	include: [qx.locale.MTranslation],


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
			ms123.form.Dialog.alert(this.tr("management.no_insert_possible"));
		},
		query: function (p) {
			return this.base(arguments, p);	
			var params = {
				service: "management",
				method: "getUnits",
				parameter: {filter:p.filter},
				context: p.context,
				failed: p.failed,
				completed: p.completed,
				async: p.async
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		queryOne: function (p) {
			return this.base(arguments, p);	
			var params = {
				service: "management",
				method: "getUnit",
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
			ms123.form.Dialog.alert(this.tr("management.no_update_possible"));
			return;
		},

		/**
		 */
		'delete': function (p) {
			ms123.form.Dialog.alert(this.tr("management.no_delete_possible"));
		}
	}
});
