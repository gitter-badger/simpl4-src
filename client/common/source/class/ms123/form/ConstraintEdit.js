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
qx.Class.define("ms123.form.ConstraintEdit", {
	extend: ms123.form.TableEdit,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (config) {
		this.base(arguments,config,ms123.StoreDesc.getGlobalMetaStoreDesc());
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
		configGridContext:function(context){
			var _this = this;
			var buttons = [ {
				'label': "",
				'icon': "icon/16/actions/list-add.png",
				'callback': function(m){
					_this._table.addRecord( { annotation:"AssertFalse", parameter1:"", parameter2:"", message:"" } );
				},
				'value': "add"
			}, 
			{
				'label': "",
				'icon': "icon/16/places/user-trash.png",
				'callback': function(m){
					_this._table.deleteCurrentRecord();
				},
				'value': "del"
			} ,
			{
				'label': "",
				'icon': "icon/16/actions/go-up.png",
				'callback': function(m){
					_this._table.currentRecordUp();
				},
				'value': "up"
			} ,
			{
				'label': "",
				'icon': "icon/16/actions/go-down.png",
				'callback': function(m){
					_this._table.currentRecordDown();
				},
				'value': "down"
			} 
			];


			context.buttons=buttons;
			var cols = context.model.attr("colModel");
			this._replaceMap = {};
			for (var i = 0; i < cols.length; i++) {
				if(cols[i].name == "annotation" || cols[i].name == "parameter1" || cols[i].name == "parameter2" || cols[i].name == "message"){
					cols[i].tableedit=true;
				}
				if(cols[i].name == "annotation"){
					this._colNumAnno = i;
				}
			}
		},
		propagateTable:function(table){
			this._table = table;

//			var colModel = table.getTable().getTableColumnModel();
//			var colRenderer = new qx.ui.table.cellrenderer.Replace();
//			var _this = this;
//			colRenderer.setReplaceFunction( function(x){
//				return _this._replaceMap[x];
//			});
//			colModel.setDataCellRenderer(this._colNumAnno-1 , colRenderer );
			

			table.getTable().addListener("cellTap", function(e) {
//  			this.startEditing();
			}, table.getTable());
		}
	}
});
