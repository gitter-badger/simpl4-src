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
qx.Class.define("ms123.entitytypes.DatabaseAdmin", {
	extend: qx.ui.container.Composite,
	include: [qx.locale.MTranslation],

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (model, param, facade) {
		this.base(arguments);
		this._facade = facade;
		this.setLayout(new qx.ui.layout.VBox(2));
		this.setPadding(10);
		 var spacer = new qx.ui.core.Spacer(30, 40);
		this.add(spacer, { });
		var control = new qx.ui.form.Button(this.tr("entitytypes.databasescheme_clean"), "icon/16/actions/go-previous.png");
		control.setCenter(false);
		control.setBackgroundColor("red");
		control.addListener("execute", this._cleanDatabase, this);
		this.add(control, { });
		 var spacer = new qx.ui.core.Spacer(30, 40);
		this.add(spacer, { });
		this._cbMap = {};
		for(var i=0; i < model.getChildren().getLength(); i++){
			var child = model.getChildren().getItem(i);
			var cb = new qx.ui.form.CheckBox(child.getValue());
			this.add(cb, { });
			this._cbMap[child.getValue()] = cb;
		}
		var control = new qx.ui.form.Button(this.tr("entitytypes.databasescheme_table_clean"), "icon/16/actions/go-previous.png");
		control.setCenter(false);
		control.addListener("execute", this._cleanTable, this);
		this.add(control, { alignX:"left" });
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
		_cleanDatabase: function (name, data) {
			ms123.form.Dialog.confirm(this.tr("entitytypes.databasescheme_clean.confirm"), function (e) {
				if (e) {
					try {
						var result = ms123.util.Remote.rpcSync("nucleus:schemaTool", {
							storeId: ms123.StoreDesc.getNamespaceDataStoreDesc().getStoreId(),
							dry:false,
							op: "delete"
						});
					} catch (e) {
						ms123.form.Dialog.alert("DatabaseAdmin._cleanDatabase:" + e);
						return;
					}
				}
			}, this);
		},
		_cleanTable: function (name, data) {
			var keys = Object.keys( this._cbMap);
			var kList = [];
			for (var i = 0; i < keys.length; i++) {
				var key = keys[i];
				var cb = this._cbMap[key];
				if( cb.getValue() ){
					kList.push(key);
				}
			}
			console.log("kList:"+JSON.stringify(kList));
			ms123.form.Dialog.confirm(this.tr("entitytypes.databasescheme_clean.klass_confirm"), function (e) {
				if (e) {
					try {
						var result = ms123.util.Remote.rpcSync("nucleus:schemaTool", {
							storeId: ms123.StoreDesc.getNamespaceDataStoreDesc().getStoreId(),
							dry:false,
							classes:kList,
							op: "delete"
						});
					} catch (e) {
						ms123.form.Dialog.alert("DatabaseAdmin._cleanTable:" + e);
						return;
					}
				}
			}, this);
		}
	}
});
