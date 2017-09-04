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
	@asset(qx/icon/${qx.icontheme}/22/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/apps/*)
	@asset(ms123/icons/*)
	@asset(ms123/*)
*/

qx.Class.define("ms123.processexplorer.ProcessExplorer", {
	extend: qx.ui.container.Composite,
	include: qx.locale.MTranslation,

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this.base(arguments);
		this.setLayout(new qx.ui.layout.Dock());
		context.window.add(this, {});
		this._eventListeners = new Hash();
		this._facade = this.getPluginFacade();
		this._facade.storeDesc = context.storeDesc;
		this._init(context);

		this.registerDeploymentListener();
	},

	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_init: function (context) {

			var processPage = this._createProcessPage();
			var camelPage = this._createCamelPage();
			var mainTab = new qx.ui.tabview.TabView().set({
				contentPadding: 0
			});
			mainTab.add(processPage, {
				edge: 0
			});
			mainTab.add(camelPage, {
				edge: 0
			});

			this.add(mainTab, {
				edge: "center"
			});
		},
		_createProcessPage:function(){
			var processDefinitions = new ms123.processexplorer.plugins.ProcessDefinitions(this._facade);
			var processHistory = new ms123.processexplorer.plugins.ProcessHistory(this._facade);
			new ms123.processexplorer.plugins.JsonDisplay(this._facade);
			new ms123.processexplorer.plugins.RouteInstanceWindow(this._facade);

			var dock = new qx.ui.layout.Dock();
			var rightSide = new qx.ui.container.Composite(dock);
			dock.setSeparatorX("separator-horizontal");
      dock.setSeparatorY("separator-vertical");
      dock.setSpacingX(5);
      dock.setSpacingY(5);
			rightSide.add(processHistory,{edge:"center"});
			var split2 = new ms123.processexplorer.Split2(processDefinitions, rightSide);


			var page = new qx.ui.tabview.Page(this.tr("processexplorer.processes")).set({
				showCloseButton: false
			});
			page.setLayout(new qx.ui.layout.Grow());
			page.add(split2,{edge:"center"});
			return page;
		},
		_createCamelPage:function(){
			var camelDefinitions = new ms123.processexplorer.plugins.CamelDefinitions(this._facade);
			var camelHistory = new ms123.processexplorer.plugins.CamelHistory(this._facade);
//			new ms123.processexplorer.plugins.JsonDisplay(this._facade);

			var dock = new qx.ui.layout.Dock();
			var rightSide = new qx.ui.container.Composite(dock);
			dock.setSeparatorX("separator-horizontal");
      dock.setSeparatorY("separator-vertical");
      dock.setSpacingX(5);
      dock.setSpacingY(5);
			rightSide.add(camelHistory,{edge:"center"});
			var split2 = new ms123.processexplorer.Split2(camelDefinitions, rightSide);


			var page = new qx.ui.tabview.Page(this.tr("processexplorer.camel")).set({
				showCloseButton: false
			});
			page.setLayout(new qx.ui.layout.Grow());
			page.add(split2,{edge:"center"});
			return page;
		},
		registerDeploymentListener:function(){
			var eventBus = qx.event.message.Bus;
			eventBus.subscribe("processdiagram.deployed", function(msg){
				var data = msg.getData();
				this._facade.raiseEvent({
					type: ms123.processexplorer.Config.EVENT_PROCESSDEPLOYMENT_CHANGED
				});
			}, this);
			eventBus.subscribe("camelroutes.deployed", function(msg){
				var data = msg.getData();
				this._facade.raiseEvent({
					type: ms123.processexplorer.Config.EVENT_CAMELROUTESDEPLOYMENT_CHANGED
				});
			}, this);
		},
		getPluginFacade: function () {
			if (!(this._pluginFacade)) {
				this._pluginFacade = {
					registerOnEvent: this._registerOnEvent.bind(this),
					raiseEvent: this._handleEvents.bind(this)
				};
			}
			return this._pluginFacade;
		},
		_registerOnEvent: function (eventType, callback) {
			if (!(this._eventListeners.keys().member(eventType))) {
				this._eventListeners[eventType] = [];
			}
			this._eventListeners[eventType].push(callback);
		},
		_handleEvents: function (event, argObj) {
			var eventObj = { event: event, arg: argObj };
			if (this._eventListeners.keys().member(eventObj.event.type)) {
				this._eventListeners[eventObj.event.type].each((function (value) {
					value(eventObj.event, eventObj.arg);
				}).bind(this));
			}
		}
	}
});
