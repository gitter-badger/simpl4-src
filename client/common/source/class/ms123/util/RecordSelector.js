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
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/apps/*)
	@asset(qx/icon/${qx.icontheme}/48/actions/*)
	@asset(qx/icon/${qx.icontheme}/48/apps/*)
	@ignore($)
*/
qx.Class.define("ms123.util.RecordSelector", {
 extend: qx.core.Object,
 include : [ qx.locale.MTranslation, ms123.searchfilter.MSearchFilter],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (context) {
		this.base(arguments);
		
		this._user = context.user;
		this._modulename = context.modulename;
		this.__storeDesc = context.storeDesc;
		this._selected_callback = context.selected_callback;
		var title = context.title;
		var app = qx.core.Init.getApplication();
		var win = this._createWindow(title);
		this._win = win;
		var table = this._createTable(win);
		var params = {
			modulename: this._modulename,
			onSearch: function (data) {
				table.setFilter(data);
			}
		}
		var sf = this._createSearchFilter(params,this.__storeDesc);
		var sp = this._doLayout(win, sf, table)
		win.add(sp, {
			});
		app.getRoot().add(win);
		win.open();
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	properties: {
	},

	/**
	 *****************************************************************************
	 EVENTS
	 *****************************************************************************
	 */

	events: {
		/** Whenever the value is changed this event is fired
		 *
		 *  Event data: The new text value of the field.
		 */
		"changeValue": "qx.event.type.Data"
	},


	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_createTable: function (win) {
			var self = this;
			var buttons = [ {
				'label': "",
				'icon': "icon/16/actions/dialog-ok.png",
				'callback': function (m) {
					var cr = self._table.getCurrentRecord();
					if( self._selected_callback ){
						self._selected_callback(cr);
					}else{
						if( cr.name ){
							self.setValue( cr.id +"/"+cr.name);
						}else{
							self.setValue( cr.id );
						}
					}
					win.close();
				},
				'value': "select"
			}, {
				'label': "",
				'icon': "icon/16/actions/dialog-close.png",
				'callback': function (m) {
					win.close();
				},
				'value': "cancel"
			} ];
			var cm = new ms123.config.ConfigManager();
			var context = {};
			context.buttons = buttons;
			if( this._modulename == "user"){
				this.__storeDesc = ms123.StoreDesc.getGlobalMetaStoreDesc();
			}
			context.model = cm.getEntityModel(this._modulename,this.__storeDesc,"main-grid", "properties");
			context.modelForm = null;//cm.getEntityModel(this._modulename,this.__storeDesc,"main-form", "properties");
			context.unit_id = "related/+" + this._modulename;
			context.config = this._modulename;
			context.user = this._user;
			context.storeDesc = this.__storeDesc;

			//var value = qx.lang.Json.stringify(context.model.attr("colModel"), null, 4);
			this._table = new ms123.widgets.Table(context);
			this._table.addListener("dblclick", this._onCellDblClick, this);
			return this._table;
		},
		_onCellDblClick:function(e){
			if( this._selected_callback ){
				this._selected_callback(this._table.getCurrentRecord());
				this._win.close();
			}
		},
		_doLayout: function (parent, upperTabView, bottomTabView) {
			var splitpane = new qx.ui.splitpane.Pane("vertical");
			splitpane.setHeight(parent.getHeight());
			splitpane.setDecorator(null);

			var topWidget = upperTabView;
			topWidget.setDecorator(null);
			topWidget.setMinHeight(150);
			splitpane.add(topWidget, 3);

			var bottomWidget = bottomTabView;
			bottomWidget.setDecorator(null);
			bottomWidget.setMinHeight(250);
			splitpane.add(bottomWidget, 5);
			return splitpane;
		},
		_createWindow: function (name) {
			var win = new qx.ui.window.Window(name, "").set({
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			});
			win.setLayout(new qx.ui.layout.Grow);
			win.setWidth(700);
			win.setHeight(500);
			win.setAllowMaximize(false);
			win.setAllowMinimize(false);
			win.setModal(true);
			win.setActive(false);
			win.minimize();
			win.center();
			return win;
		}
	}
});
