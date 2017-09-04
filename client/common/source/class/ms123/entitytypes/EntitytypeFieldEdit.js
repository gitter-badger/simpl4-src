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
/*
*/
qx.Class.define("ms123.entitytypes.EntitytypeFieldEdit", {
	extend: ms123.entitytypes.FormEdit,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (model,param,facade,data) {
		var pack = model.getPack();
		this.storeDesc = ms123.StoreDesc.getNamespaceDataStoreDesc(pack);
		if( data ){
			this.__noLoad = true;
		}
		this.base(arguments,model,param,facade);
		if( data ){
			this._setData(data);
			this._buttonDel.setEnabled(false);
		}
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_saveEntitytypeField: function (entitytype, name, data) {
			var completed = (function (data) {
				ms123.form.Dialog.alert(this.tr("entitytypes.field_saved"));
			}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("entitytypeFields.saveEntitytypeField_failed")+":"+details.message);
			}).bind(this);

			try {
				var ret = ms123.util.Remote.rpcSync("entity:saveEntitytypeField", {
					storeId: this.storeDesc.getStoreId(),
					data:data,
					entitytype:entitytype,
					name: name
				});
				completed.call(this,ret);
				ms123.config.ConfigManager.clearCache();
			} catch (e) {
				failed.call(this,e);
				return;
			}
		},
		_getEntitytypeField: function (entitytype,name) {
			var completed = (function (data) {
			}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("entitytypeFields.getEntitytypeField_failed")+":"+details.message);
			}).bind(this);

			try {
				var ret = ms123.util.Remote.rpcSync("entity:getEntitytypeField", {
					storeId: this.storeDesc.getStoreId(),
					entitytype: entitytype,
					name: name
				});
				completed.call(this,ret);
				return ret;
			} catch (e) {
				failed.call(this,e);
				return;
			}
		},
		_deleteEntitytypeField: function (entitytype,name) {
			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("entitytypeFields.deleteEntitytypeField_failed")+":"+details.message);
			}).bind(this);
			try {
				var ret = ms123.util.Remote.rpcSync("entity:deleteEntitytypeField", {
					storeId: this.storeDesc.getStoreId(),
					entitytype:entitytype,
					name: name
				});
			} catch (e) {
				failed.call(this,e);
			}
		},
		_load: function () {
			if( !this.__noLoad){
				var data = this._getEntitytypeField( this._model.getEntitytype(), this._model.getId() );
				this._form._mode = this._isNew ? "add" : "edit";
				this._form.setData(data);
			}
		},
		_setData: function (data) {
			this._form._mode = this._isNew ? "add" : "edit";
			this._form.setData(data);
		},
		_delete:function(data){
			console.log("delete.data:"+qx.util.Serializer.toJson(data));
			var entityType = this._model.parent.getId();
			var children = this._model.parent.getChildren();
			var len = children.getLength();
			console.log("len:"+len);
			for(var i=0; i < len; i++){
				var child = children.getItem(i);
				console.log("\tname:"+child.getId());
				if( child.getId() == data.name){
					children.remove(child);
					this._deleteEntitytypeField(entityType,data.name);
					break;
				}
			}
		},
		_save:function(data){
			var entitytype = this._isNew ? this._model.getId() : this._model.getEntitytype();
			this._saveEntitytypeField( entitytype, data.name, data );
			if( this._isNew ){
				var f = {}
				f.id = data.name;
				f.value = data.name;
				f.entitytype = entitytype;
				f.title = data.name;
				f.type = "sw.field";
				f.pack = this._model.getPack();
				f.children = [];
				var fmodel = qx.data.marshal.Json.createModel(f, true);
				var children = this._model.getChildren();
				fmodel.parent = this._model;
				children.insertAt(0,fmodel);
			}
			this.fireDataEvent("changeValue", data, null);
		}
	}
});
