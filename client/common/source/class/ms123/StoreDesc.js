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
qx.Class.define("ms123.StoreDesc", {
	extend: qx.core.Object,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		context = context || {};
		var namespace = context.namespace;
		var repository = context.repository;
		if( namespace === undefined || namespace === null){
			namespace = ms123.StoreDesc.getCurrentNamespace();
		}
		if( repository === undefined || repository === null){
			repository = ms123.StoreDesc.getCurrentNamespace();
		}
		this.__namespace = namespace;
		this.__repository = repository;

		var pack = context.pack;
		if( pack === undefined || pack === null){
			pack = ms123.StoreDesc.PACK_DEFAULT;
		}
		this.__pack = pack;
		this.__storeId = context.storeId;
		if( !this.__storeId ){
			this.__storeId = this.__namespace +"_"+this.__pack;
		}
		this.__store = context.store;
	},

	statics: {
		__namespaceDataStoreDesc: {},
		__namespaceMetaStoreDesc: {},
		__namespaceConfigStoreDesc: {},
		__namespacePacks: {},
		__globalMetaStoreDesc: null,
		__globalDataStoreDesc: null,

		setCurrentNamespace:function(ns){
			ms123.StoreDesc.__currentNamespace = ns;
		},
		getCurrentNamespace:function(){
			return ms123.StoreDesc.__currentNamespace;
		},

		setNamespaceDataStoreDesc: function (sdesc) {
			ms123.StoreDesc.__namespaceDataStoreDesc[ms123.StoreDesc.__currentNamespace+"_"+sdesc.getPack()] = sdesc;
		},
		setNamespaceMetaStoreDesc: function (sdesc) {
			ms123.StoreDesc.__namespaceMetaStoreDesc[ms123.StoreDesc.__currentNamespace] = sdesc;
		},
		setNamespaceConfigStoreDesc: function (sdesc) {
			ms123.StoreDesc.__namespaceConfigStoreDesc[ms123.StoreDesc.__currentNamespace] = sdesc;
		},
		setNamespacePacks: function (packs) {
			ms123.StoreDesc.__namespacePacks[ms123.StoreDesc.__currentNamespace] = packs;
		},
		setGlobalMetaStoreDesc: function (sdesc) {
			ms123.StoreDesc.__globalMetaStoreDesc = sdesc;
		},

		setGlobalDataStoreDesc: function (sdesc) {
			ms123.StoreDesc.__globalDataStoreDesc = sdesc;
		},
		getNamespaceDataStoreDescForNS: function (ns,pack) {
			if( pack == null) pack = "data";
			return ms123.StoreDesc.__namespaceDataStoreDesc[ns+"_"+pack];
		},
		isOrientDB: function (ns,pack) {
			if( pack == null) pack = "data";
			return "graph:orientdb" == ms123.StoreDesc.__namespaceDataStoreDesc[ns+"_"+pack].__store;
		},

		getNamespaceDataStoreDesc: function (pack) {
			if( pack == null) pack = "data";
			return ms123.StoreDesc.__namespaceDataStoreDesc[ms123.StoreDesc.__currentNamespace+"_"+pack];
		},
		getNamespaceConfigStoreDesc: function () {
			return ms123.StoreDesc.__namespaceConfigStoreDesc[ms123.StoreDesc.__currentNamespace];
		},
		getNamespaceMetaStoreDesc: function () {
			return ms123.StoreDesc.__namespaceMetaStoreDesc[ms123.StoreDesc.__currentNamespace];
		},
		getNamespacePacks: function () {
			return ms123.StoreDesc.__namespacePacks[ms123.StoreDesc.__currentNamespace];
		},
		getGlobalMetaStoreDesc: function (sdesc) {
			return ms123.StoreDesc.__globalMetaStoreDesc;
		},
		getGlobalDataStoreDesc: function (sdesc) {
			return ms123.StoreDesc.__globalDataStoreDesc;
		},
		NAMESPACE: "namespace",
		STORE_ID: "storeId",
		PACK_DEFAULT: "data"
	},

	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		getNamespaceDataStoreDesc: function (pack) {
			if( pack == null) pack = "data";
			return ms123.StoreDesc.__namespaceDataStoreDesc[this.__namespace+"_"+pack];
		},
		getNamespaceMetaStoreDesc: function () {
			return ms123.StoreDesc.__namespaceMetaStoreDesc[this.__namespace];
		},
		getNamespaceConfigStoreDesc: function () {
			return ms123.StoreDesc.__namespaceConfigStoreDesc[this.__namespace];
		},
		getNamespace: function () {
			return this.__namespace;
		},
		getPack: function () {
			return this.__pack;
		},
		getRepository: function () {
			return this.__repository;
		},
		getStoreId: function () {
			return this.__storeId;
		},
		getStore: function () {
			return this.__store;
		},
		isOrientDB: function () {
			return "graph:orientdb" ==  this.__store;
		},

		toString: function () {
			return "[" + this.getStoreId() + "/" + this.getNamespace() + "," + this.getPack() + "]";
		}

	}
});
