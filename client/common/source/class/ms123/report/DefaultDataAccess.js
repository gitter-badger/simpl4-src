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
qx.Class.define('ms123.report.DefaultDataAccess', {
 extend: qx.core.Object,
	implement: ms123.report.IDataAccess,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this.base(arguments);
		this.__storeDesc = context.storeDesc;
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
		getReports: function ( p ) {
			var rpcParams ={
				namespace : this.__storeDesc.getNamespace()
			}

			var params = {
				service: "report",
				method: "getReports",
				parameter: rpcParams,
				async: false
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		/**
		 */
		createReport: function ( p ) {
			var rpcParams ={
				namespace : this.__storeDesc.getNamespace(),
				name : p.name,
				report : p.report
			}

			var params = {
				service: "report",
				method: "saveReport",
				parameter: rpcParams,
				async: false
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		/**
		 */
		updateReport: function ( p ) {
			var rpcParams ={
				namespace : this.__storeDesc.getNamespace(),
				name:p.id,
				report : p.report
			}

			var params = {
				service: "report",
				method: "saveReport",
				parameter: rpcParams,
				async: false
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		/**
		 */
		deleteReport: function ( p ) {
			var rpcParams ={
				namespace : this.__storeDesc.getNamespace(),
				name:p.id
			}

			var params = {
				service: "report",
				method: "deleteReport",
				parameter: rpcParams,
				async: false
			}
			return ms123.util.Remote.rpcAsync(params);
		}
	}
});
