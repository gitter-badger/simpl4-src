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
qx.Class.define('ms123.widgets.DefaultDataAccess', {
 extend: qx.core.Object,
	implement: ms123.widgets.IDataAccess,


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
		insert: function ( p ) {
			var rpcParams ={
				storeId : p.storeDesc.getStoreId(),
				entity:p.entity,
				id:p.id,
				entityChild:p.entityChild,
				data : p.data,
				filter : p.filter,
				state : p.state,
				hints : p.hints,
				pathInfo : p.pathInfo
			}

			var params = {
				service: "data",
				method: "insert",
				parameter: rpcParams,
				async: false
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		/**
		 */
		update: function ( p ) {
			var rpcParams ={
				storeId : p.storeDesc.getStoreId(),
				entity:p.entity,
				id:p.id,
				idChild:p.idChild,
				entityChild:p.entityChild,
				data : p.data,
				filter : p.filter,
				state : p.state,
				hints : p.hints,
				pathInfo : p.pathInfo
			}

			var params = {
				service: "data",
				method: "update",
				parameter: rpcParams,
				async: false
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		/**
		 */
		'delete': function ( p ) {
			var rpcParams ={
				storeId : p.storeDesc.getStoreId(),
				entity:p.entity,
				id:p.id,
				idChild:p.idChild,
				entityChild:p.entityChild,
				data : p.data,
				filter : p.filter,
				hints : p.hints,
				pathInfo : p.pathInfo
			}

			var params = {
				service: "data",
				method: "delete",
				parameter: rpcParams,
				async: false
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		/**
		 */
		query: function ( p ) {
			var rpcParams ={
				storeId : p.storeDesc.getStoreId(),
				entity:p.entity,
				entityChild:p.entityChild,
				id : p.id,
				filter : p.filter,
				fields : p.fields,
				page : p.page,
				pageSize : p.pageSize,
				pathInfo : p.pathInfo,
				where : p.where,
				nosql : p.nosql,
				join : p.join,
				state : p.state,
				sql : p.sql,
				orderby : p.orderby,
				luceneQuery : p.luceneQuery,
				format : p.format,
				options : p.options,
				aliases : p.aliases
			}

			var params = {
				service: "data",
				method: "query",
				parameter: rpcParams,
				async: p.async,
				context: p.context,
				failed: p.failed,
				completed: p.completed
			}
			return ms123.util.Remote.rpcAsync(params);
		},

		/**
		 */
		queryOne: function ( p ) {
			var rpcParams ={
				storeId : p.storeDesc.getStoreId(),
				entity:p.entity,
				id : p.id,
				entityChild:p.entityChild,
				fields : p.fields,
				pathInfo : p.pathInfo,
				getContent : p.getContent
			}

			var params = {
				service: "data",
				method: "queryOne",
				parameter: rpcParams,
				async: p.async,
				context: p.context,
				failed: p.failed,
				completed: p.completed
			}
			return ms123.util.Remote.rpcAsync(params);
		}
	}
});
