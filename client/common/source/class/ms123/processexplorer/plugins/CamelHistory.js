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
	@ignore(Hash)
	@ignore(Clazz)
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
*/

qx.Class.define("ms123.processexplorer.plugins.CamelHistory", {
	extend: qx.ui.container.Composite,
	include: [qx.locale.MTranslation],

	/**
	 * Constructor
	 */
	construct: function (facade) {
		this.base(arguments);
		this.facade = facade;
		this.namespace = facade.storeDesc.getNamespace();
		this.setLayout(new qx.ui.layout.Dock());

		this.toolbar = this._createSearchPanelRouteInstances();
		this.add(this.toolbar, {
			edge: "north"
		});

		this._startTimeFrom = null;
		this._startTimeTo = null;

		this.facade.registerOnEvent(ms123.processexplorer.Config.EVENT_CAMELROUTEDEFINITION_CHANGED, this._handleEvent.bind(this));
		this.facade.registerOnEvent(ms123.processexplorer.Config.EVENT_CAMELROUTESDEPLOYMENT_CHANGED, this._handleEventDeplomentChanged.bind(this));
		this.setEnabled(false);
	},

	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {
		_init:function(){
			if( this._splitPane){
				this.remove(this._splitPane);
				this._splitPane.dispose();
			}
			this._processState = 'all';

			var tableInstances = this._createRouteInstancesTable();

			this._routeInstance = 	 new ms123.processexplorer.plugins.CamelHistoryInstance(this.facade);
			var splitpane = new ms123.processexplorer.plugins.Split2(tableInstances,this._routeInstance);
			this.add(splitpane, {
				edge: "center"
			});
			this._splitPane=splitpane;
		},
		_createSearchPanelRouteInstances:function(){
			var formData = {
				"startTimeFrom": {
					'type': "DateField",
					'label': this.tr("processexplorer.history.started_from"),
					'position':"1,0",
					'value': null
				},
				"startTimeTo": {
					'type': "DateField",
					'label': this.tr("processexplorer.history.started_to"),
					'position':"1,1",
					'value': null
				}
			};

			var self = this;
			var form = new ms123.form.Form({
				"tabs": [{
					id: "tab1",
					layout: "double"
				}],
				"useScroll": false,
				"formData": formData,
				"buttons": [],
				"inWindow": false,
				"callback": function (m, v) {
					console.error("callback:"+m+"/"+v);
				},
				"context": self
			});
			this._searchForm = form;
			this.add(form, {
				edge: "north"
			});
			var m = form.getModel();
			form.setData({processState:'all'});
			m.addListener("changeBubble", this.__changeListenerInstances, this);
			return form;
		},
		__changeListenerInstances: function () {
			var data = this._searchForm.getData();
			this._getRouteInstances(data.startTimeFrom, data.startTimeTo);
		},
		_getRouteInstances: function (startTime, endTime) {
			if (!this._camelRouteDefinition) return;
			var completed = (function (data) {
				console.log("Data:"+JSON.stringify(data,null,2));
				this._tableModelRouteInstances.removeRows(0, this._tableModelRouteInstances.getRowCount());
				for (var row = 0; row < data.length; row++) {
					var rmap = data[row];
					rmap._startTime = rmap.startTime;
					rmap._endTime = rmap.endTime;
					rmap.startTime = this._formatTime(rmap.startTime);
					rmap.endTime = this._formatTime(rmap.endTime);
					rmap.duration = rmap._endTime - rmap._startTime;
					this._tableModelRouteInstances.addRowsAsMapArray([rmap], null, true);
				}
			}).bind(this);

			var result = null;
			try {
				var s = this._select;
				result = ms123.util.Remote.rpcSync("history:getRouteInstances", {
					contextKey: this._camelContextKey,
					routeId: this._camelRouteDefinition.id,
					startTime:startTime,
					endTime:endTime
				});
				completed.call(this, result);
			} catch (e) {
				console.log(e.stack);
				ms123.form.Dialog.alert("History._getRouteInstances:" + e);
				return;
			}
		},

		_createRouteInstancesTable: function () {
			var colIds = new Array();
			var colHds = new Array();
			var colWidth = new Array();
			colIds.push("exchangeId");
			colWidth.push("1*");
			colHds.push(this.tr("processexplorer.history.id"));

			colWidth.push(30);
			colIds.push("status");
			colHds.push(this.tr("processexplorer.history.status"));

			colWidth.push(120);
			colIds.push("startTime");
			colHds.push(this.tr("processexplorer.history.starttime"));

			colWidth.push(120);
			colIds.push("endTime");
			colHds.push(this.tr("processexplorer.history.endtime"));

			colWidth.push(50);
			colIds.push("duration");
			colHds.push(this.tr("processexplorer.history.duration"));

			//var tableModel = this._createTableModelInstances();
			var tableModel = new qx.ui.table.model.Simple();
			this._tableModelRouteInstances = tableModel;
			tableModel.setColumns(colHds, colIds);
			var customMap = {
				tableColumnModel: function (obj) {
					return new qx.ui.table.columnmodel.Resize(obj);
				}
			};
			var table = new qx.ui.table.Table(tableModel, customMap);
			table.highlightFocusedRow(false);
			table.setShowCellFocusIndicator(false);
			this._tableInstance = table;
			var tcm = table.getTableColumnModel();

			table.addListener("cellTap", function (e) {
				var colnum = table.getFocusedColumn();
				var rownum = table.getFocusedRow();
				//if( colnum != 2 ) return;
				var map = tableModel.getRowDataAsMap(rownum);
				//if( !(map.status == "error" || map.status=="notstartet")) return;
				/*var msg = map.logEntry.msg;
				this.facade.raiseEvent({
					type: ms123.processexplorer.Config.EVENT_SHOWDETAILS,
					name: "History",
					value: msg
				});*/
			}, this, false);

			tcm.setDataCellRenderer(1, new ms123.processexplorer.plugins.ImageCellRenderer());
			table.getTableModel().setColumnEditable(1, false);

			colWidth.each((function (w, index) {
				var resizeBehavior = tcm.getBehavior();
				resizeBehavior.setWidth(index, w);

			}).bind(this));
			table.setStatusBarVisible(false);
			var selModel = table.getSelectionModel();
			selModel.setSelectionMode(qx.ui.table.selection.Model.SINGLE_SELECTION);
			selModel.addListener("changeSelection", function (e) {
				var index = selModel.getLeadSelectionIndex();
				if( index<0) return;
				var map = tableModel.getRowDataAsMap(index);
				var count = selModel.getSelectedCount();
				if (count == 0) {
					return;
				}
				this._routeInstance.getRouteInstance(this._camelContextKey, this._camelRouteDefinition.id, map.exchangeId);
			}, this);

			return table;
		},
		_formatTime:function(time){
			var m = qx.locale.Manager.getInstance();
			var lang = m.getLanguage();
			var df = new qx.util.format.DateFormat("MM-dd-yyyy HH:mm:ss");
			if (lang == "de") {
				df = new qx.util.format.DateFormat("dd.MM.yyyy HH:mm:ss");
			}
			return qx.bom.String.escape(df.format(new Date(time)));
		},
		_handleEventDeplomentChanged: function (e) {
			if( this._splitPane){
				this.remove(this._splitPane);
				this._splitPane.dispose();
				this._splitPane=null;
			}
			this.setEnabled(false);
		},
		_handleEvent: function (e) {
			if (e && e.camelRouteDefinition) {
				this._camelRouteDefinition = e.camelRouteDefinition;
				this._camelContextKey = e.camelContextKey;
				this._select = e.select;
			}
			console.log("_handleEvent:"+JSON.stringify(this._camelRouteDefinition,null,2));
			this._init();
			var data = this._searchForm.getData();
			this._getRouteInstances(data.startTimeFrom, data.startTimeTo);
			this.setEnabled(true);
		}
	}
});
