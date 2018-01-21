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
 @asset(qx/icon/${qx.icontheme}/22/apps/*)
 @asset(qx/icon/${qx.icontheme}/22/actions/*)
 @asset(qx/icon/${qx.icontheme}/22/places/*)
 @asset(qx/icon/${qx.icontheme}/22/emotes/*)
 */
qx.Class.define("ms123.MainMenu", {
	extend: qx.ui.form.MenuButton,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (ns) {
		this.base(arguments, this.tr("mainmenu.start") + "/" + ns, "icon/22/actions/go-home.png");

		this._me = {};
		this._me["crud"] = {
			clazz: ms123.Crud,
			name: "ms123.Crud",
			icon:"icon/22/apps/office-database.png"
		};
		this._me["team"] = {
			clazz: ms123.team.TeamEditor,
			name: "ms123.team.TeamEditor",
			icon: "icon/22/apps/office-calendar.png"
		};
		this._me["report"] = {
			clazz: ms123.report.Report,
			name: "ms123.report.Report",
			icon: "icon/22/actions/edit-copy.png"
		};
		this._me["importing"] = {
			clazz: ms123.importing.Importing,
			name: "ms123.importing.Importing",
			icon:"icon/22/actions/edit-copy.png",
			widthFactor:0.9
		};
		this._me["pe"] = {
			clazz: ms123.processexplorer.ProcessExplorer,
			name: "ms123.processexplorer.ProcessExplorer",
			icon:"icon/22/actions/system-run.png"
		};
		this._me["gs"] = {
			clazz: ms123.GlobalSearch,
			name: "ms123.GlobalSearch",
			icon:"icon/22/apps/utilities-log-viewer.png"
		};
		this._me["task"] = {
			clazz: ms123.Task,
			name: "ms123.Task",
			icon:"icon/22/apps/internet-feed-reader.png"
		};
		this._me["perm"] = {
			clazz: ms123.permissions.PermissionEditor,
			name: "ms123.permissions.PermissionEditor",
			icon:"icon/22/actions/system-run.png"
		};
		this._me["setting"] = {
			clazz: ms123.settings.SettingEditor,
			name: "ms123.settings.SettingEditor",
			icon:"icon/22/actions/system-run.png"
		};
		this._me["shell"] = {
			clazz: ms123.shell.ProjectShell,
			name: "ms123.shell.ProjectShell",
			icon:"icon/22/actions/system-run.png",
			widthFactor:0.9
		};
		this._me["trigger"] = {
			clazz: ms123.Crud,
			name: "ms123.Triggereditor",
			icon:"icon/22/actions/view-refresh.png"
		};
		this._me["manager"] = {
			clazz: ms123.namespaces.Manager,
			name: "ms123.namespaces.Manager",
			icon:"icon/22/actions/application-exit.png"
		};
		this._me["user"] = {
			clazz: ms123.Crud,
			name: "ms123.Users",
			icon: "icon/22/emotes/face-smile.png"
		};
		this._me["message"] = {
			clazz: ms123.shell.ProjectShell,
			name: "ms123.shell.Messages",
			icon: "icon/22/actions/edit-copy.png"
		};
		this._me["camel"] = {
			clazz: ms123.shell.ProjectShell,
			name: "Camel",
			icon: "icon/22/actions/edit-copy.png"
		};
		this._me["form"] = {
			clazz: ms123.shell.ProjectShell,
			name: "Forms",
			icon: "icon/22/actions/edit-copy.png"
		};
		this._me["bomviewer"] = {
			clazz: ms123.bomviewer.BOMViewer,
			name: "ms123.bomviewer.BOMViewer",
			icon: "icon/22/apps/utilities-log-viewer.png",
			load: "legacy/js/openseadragon-all.js.gz"
		};
		this._me["pdfviewer"] = {
			clazz: ms123.bomviewer.BOMPdfViewer,
			name: "ms123.bomviewer.BOMPdfViewer",
			icon: "resource/ms123/pdf_icon.gif",
			load: "legacy/js/pdf-all.js.gz"
		};
		this._init(ns);
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	statics: {
		getDataAccess: function (sdesc, entityName) {
			if (entityName == "user") {
				return new ms123.widgets.UserDataAccess();
		//	} else if (entityName == "unit") {
		//		return new ms123.management.UnitDataAccess();
			} else {
				return new ms123.widgets.DefaultDataAccess();
			}
		},
		getToolbar: function (sdesc, entityName) {
			//if (entityName == "unit") {
			//	return new ms123.management.UnitToolbar();
			//} else {
				return null;
			//}
		},
		createWidgetList: function (_module, sdesc, _this) {
console.log("sdesc:",sdesc);
if( _module == null) console.trace();
			var cm = new ms123.config.ConfigManager();
			var pack = sdesc.getPack();
			var widgetList = new Array();
			var mwidget = {};
			mwidget.config = _module.name;
			mwidget.storeDesc = sdesc;
			mwidget.dataAccess = ms123.MainMenu.getDataAccess(sdesc, _module.name);
			mwidget.toolbar = ms123.MainMenu.getToolbar(sdesc, _module.name);

			var props = cm.getPropertiesForEntity(sdesc, _module.name);
//			console.log("Module("+_module.name+"):" + JSON.stringify(_module, null, 2));
//			console.log("Props("+_module.name+"):" + JSON.stringify(props, null, 2));
			mwidget.multipletabs = (props.multiple_tabs != null && props.multiple_tabs == true);
			mwidget.sidebar = (props.sidebar != null && props.sidebar == true);
			mwidget.exclusionlist = (props.exclusion_list != null && props.exclusion_list == true);
			mwidget.teamList = (_module.default_fields != null && _module.default_fields == true);
			mwidget.no_add_del_in_master = (_module.no_add_del_in_master != null && _module.no_add_del_in_master == true);
			mwidget.state_select = (_module.state_select != null && _module.state_select == true);
			mwidget.not_in_menu = (_module.not_in_menu != null && _module.not_in_menu == true);
			mwidget.teams_in_subpanel = (_module.teams_in_subpanel != null && _module.teams_in_subpanel == true);
			mwidget.preFilter = _module.filter;
			mwidget.primaryKeys = _module.primaryKeys;
			widgetList.push(mwidget);

			if (_module.childs != null) {
				for (var j = 0; j < _module.childs.length; j++) {
					var child = _module.childs[j];
					//console.log("modulename:"+child.modulename+"/"+mwidget.teams_in_subpanel);
					if( child.modulename == 'team' && mwidget.teams_in_subpanel == false){
						continue;
					}
					var widget = {};
					var datatype = child.datatype.split("/");
					widget.type = datatype[0] == "list" ? "table" : "nform";
					widget.config = child.modulename;
					widget.storeDesc = mwidget.storeDesc;
					widget.dataAccess = ms123.MainMenu.getDataAccess(sdesc, child.modulename);
					widget.toolbar = ms123.MainMenu.getToolbar(sdesc, child.modulename);
					var props = cm.getPropertiesForEntity(sdesc, child.modulename);
					console.log("Props2:" + JSON.stringify(props, null, 2));
					if (props.multi_add != null && props.multi_add == true) {
						widget.multi_add = true;
					}
					widget.fieldname = child.name;
					widget.dependent = child.dependent;
					widget.primaryKeys = child.primaryKeys;
					widget.tab_title = _this.tr(pack+"." + _module.name + "." + child.name);
					widgetList.push(widget);
				}
			}
			return widgetList;
		}
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {

		_init: function (ns) {
			this._user = ms123.config.ConfigManager.getUserProperties();
			var x = qx.util.Serializer.toJson(this._user);

			var menu = new qx.ui.menu.Menu();
			var globalMetaStoreDesc = ms123.StoreDesc.getGlobalMetaStoreDesc();
			var globalDataStoreDesc = ms123.StoreDesc.getGlobalDataStoreDesc();
			if (ns == "global") {
				var extraButtons = this._createGlobalExtraButtons(globalMetaStoreDesc, globalDataStoreDesc);
				this._createGlobalMenu(menu, extraButtons);
			} else {
				var namespaceMetaStoreDesc = ms123.StoreDesc.getNamespaceMetaStoreDesc();

					var namespaceDataStoreDesc = ms123.StoreDesc.getNamespaceDataStoreDesc("data");
				var extraButtons = this._createExtraButtons(namespaceDataStoreDesc, namespaceMetaStoreDesc);

				var entityButtons = {};
				var packs = ms123.StoreDesc.getNamespacePacks();
				for (var i = 0; i < packs.length; i++) {
					var pack = packs[i];
					var namespaceDataStoreDesc = ms123.StoreDesc.getNamespaceDataStoreDesc(pack);
					if( namespaceDataStoreDesc){
						var modules = new ms123.config.ConfigManager().getEntities(namespaceDataStoreDesc);
						entityButtons[pack]  = this._createEntityButtons(modules, namespaceDataStoreDesc, null);
					}
				}
				this._createMenu(menu, entityButtons, extraButtons);
			}
		},

		_createGlobalExtraButtons: function (globalMetaStoreDesc, globalDataStoreDesc) {
			var extraButtons = {};
			this._createProjectShellButton(extraButtons, this._me["message"], globalMetaStoreDesc, ["messages"], "messages.title", true);
			this._createExtraButton(extraButtons, this._me["manager"], globalDataStoreDesc);
			this._createCompositeButton(extraButtons, this._me["user"], "user", globalMetaStoreDesc);
			this._createProjectShellButton(extraButtons, this._me["camel"], globalMetaStoreDesc, ["camel"], "Camel", true);
			this._createProjectShellButton(extraButtons, this._me["form"], globalMetaStoreDesc, ["forms"], "Forms", true);
			this._createExtraButton(extraButtons, this._me["pe"],  globalMetaStoreDesc);
			this._createExtraButton(extraButtons, this._me["setting"],  globalMetaStoreDesc);
			this._createProjectShellButton(extraButtons, this._me["shell"], globalMetaStoreDesc, null, null, false);
			return extraButtons;
		},

		_createExtraButtons: function (namespaceDataStoreDesc, namespaceMetaStoreDesc) {
			var extraButtons = {};

			this._createTriggerButton(extraButtons, this._me["trigger"], namespaceMetaStoreDesc);
			this._createExtraButton(extraButtons, this._me["report"], namespaceDataStoreDesc);
			this._createExtraButton(extraButtons, this._me["importing"], namespaceDataStoreDesc);
			this._createExtraButton(extraButtons, this._me["team"], namespaceDataStoreDesc);
			this._createExtraButton(extraButtons, this._me["task"], namespaceDataStoreDesc);
			this._createExtraButton(extraButtons, this._me["pe"],  namespaceDataStoreDesc);
			this._createExtraButton(extraButtons, this._me["perm"],  namespaceDataStoreDesc);
			this._createExtraButton(extraButtons, this._me["setting"], namespaceDataStoreDesc);
			this._createExtraButton(extraButtons, this._me["gs"], namespaceDataStoreDesc);
			this._createExtraButton(extraButtons, this._me["bomviewer"], namespaceDataStoreDesc);
			this._createExtraButton(extraButtons, this._me["pdfviewer"], namespaceDataStoreDesc);

			this._createProjectShellButton(extraButtons, this._me["shell"], namespaceMetaStoreDesc, null, null, false);

			return extraButtons;
		},
		_createExtraButton: function (extraButtons, me, sdesc) {
			extraButtons[me.name] = new qx.ui.menu.Button(this.tr(me.name), me.icon, null);
			extraButtons[me.name].addListener("execute", function () {
				var context = {
					config: me.clazz,
					storeDesc: sdesc,
					window_title: this.tr(me.name)
				};
				context.me = me;
				new ms123.DesktopWindow(context);
			});
		},
		_createProjectShellButton: function (extraButtons, me, sdesc, includePluginList, title, hideRoot) {
			extraButtons[me.name] = new qx.ui.menu.Button(this.tr(me.name), me.icon, null);
			extraButtons[me.name].addListener("execute", function () {
				var context = {
					config: me.clazz,
					title: title,
					hideRoot: hideRoot,
					storeDesc: sdesc,
					includePluginList: includePluginList,
					window_title: this.tr(me.name)
				};
				context.me = me;
				new ms123.DesktopWindow(context);
			});
		},
		_createCompositeButton: function (extraButtons, me, config, sdesc) {
			extraButtons[me.name] = new qx.ui.menu.Button(this.tr(me.name), me.icon, null);
			extraButtons[me.name].addListener("execute", function () {
				var context = {
					config: me.clazz,
					storeDesc: sdesc,
					widgets: [{
						storeDesc: sdesc,
						config: config
					}],
					window_title: this.tr(me.name)
				};
				context.me = me;
				new ms123.DesktopWindow(context);
			});
		},
		_createTriggerButton: function (extraButtons, me, sdesc) {
			extraButtons[me.name] = new qx.ui.menu.Button(this.tr(me.name), me.icon, null);
			extraButtons[me.name].addListener("execute", function () {
				var context = {
					config: me.clazz,
					storeDesc: sdesc,
					widgets: [{
						disable: ["copy"],
						storeDesc: sdesc,
						config: "trigger"
					},
					{
						disable: ["copy"],
						type: "table",
						fieldname: "tcondition_list",
						tab_title: this.tr("meta.triggers.tconditions"),
						config: "tcondition"
					},
					{
						disable: ["copy"],
						type: "table",
						tab_title: this.tr("meta.triggers.tactions"),
						fieldname: "taction_list",
						config: "taction"
					}],
					window_title: this.tr(me.name)
				};
				context.me = me;
				new ms123.DesktopWindow(context);
			});
		},
		_createEntityButtons: function (modules, sdesc, exclusions) {
console.log("modules:",modules);
			var cm = new ms123.config.ConfigManager();
			var entityButtons = new Array();
			var entityList = [];
			for (var i = 0; i < modules.length; i++) {
				entityList.push(modules[i].name);
			}
			cm.getAllSettingsForEntityList(sdesc, entityList);
			var pack = sdesc.getPack();
			for (var i = 0; i < modules.length; i++) {
				var module = modules[i];

				var settings = cm.getPropertiesForEntity(sdesc, module.name);
				qx.lang.Object.mergeWith(module, settings);

				var modname = module.name;
				if (exclusions && exclusions.contains(modname)) continue;
				var moduleButton = new qx.ui.menu.Button(this.tr(pack+"." + modname), this._me["crud"].icon);

				if (!module.childs) module.childs = [];
				var add_self_to_subpanel = (module.add_self_to_subpanel != null && module.add_self_to_subpanel == true);
				if (add_self_to_subpanel) {
					var cc = {};
					cc.modulename = modname;
					cc.name = modname;
					cc.index = 60;
					cc.datatype = "one/xxx";
					module.childs.push(cc);
				}
				var not_in_menu = (module.not_in_menu != null && module.not_in_menu == true);
				moduleButton.setUserData("not_in_menu", not_in_menu);

				var self = this;
				moduleButton.addListener("execute", function (_module, _sdesc) {
					return function (e) {
						var widgetList = ms123.MainMenu.createWidgetList(_module, _sdesc, this);
						var context = {
							unit_id: ms123.util.IdGen.nextId(),
							storeDesc: _sdesc,
							//							dataAccess: new ms123.widgets.DefaultDataAccess(),
							config: ms123.Crud,
							window_title: this.tr(_sdesc.getPack()+"." + _module.name),
							widgets: widgetList
						};
						context.me = self._me["crud"];
						new ms123.DesktopWindow(context);
					}
				}(module, sdesc));

				entityButtons.push(moduleButton);

			}
			return entityButtons;
		},
		_createGlobalMenu: function (menu, extraButtons) {
			if (this._user.admin) {
				menu.addSeparator();
				menu.add(extraButtons[this._me["user"].name]);
				menu.add(extraButtons[this._me["manager"].name]);
				if (this._user.admin) {
					menu.add(extraButtons[this._me["shell"].name]);
				}
				if (!ms123.config.ConfigManager.isRuntime()) {
					menu.add(extraButtons[this._me["message"].name]);
					menu.add(extraButtons[this._me["camel"].name]);
					menu.add(extraButtons[this._me["form"].name]);
					menu.add(extraButtons[this._me["pe"].name]);
					menu.add(extraButtons[this._me["setting"].name]);
				}
				menu.addSeparator();
			}
			this.setMenu(menu);
		},

		_getLogout: function () {
			var logout = new qx.ui.menu.Button("Logout user:" + this._user.userid, "icon/22/apps/preferences-users.png");
			logout.addListener("execute", function () {
				window.location.reload();
			}, this);
			return logout;
		},

    // overridden
    _onPointerDown : function(e) {
      this.base(arguments, e);
			if( this._isPointerDown === true){
				return;
			}
			this._isPointerDown = true;

      if(e.getButton() != "left") {
        return;
      }

      var menu = this.getMenu();
      if (menu) {
        if (!menu.isVisible()) {
          this.open();
        } else {
          menu.exclude();
        }
        e.stopPropagation();
      }
    },

    // overridden
    _onPointerUp : function(e) {
      this.base(arguments, e);
			this._isPointerDown = false;
      e.stopPropagation();
    },
		_createMenu: function (menu, entityButtonsMap, extraButtons) {
			menu.add(extraButtons[this._me["team"].name]);
			if (this._user.admin) {
				menu.add(extraButtons[this._me["importing"].name]);
				menu.addSeparator();

				if (!ms123.config.ConfigManager.isRuntime()) {
					menu.add(extraButtons[this._me["trigger"].name]);
					menu.addSeparator();
					menu.add(extraButtons[this._me["shell"].name]);
				}
				menu.add(extraButtons[this._me["perm"].name]);
				menu.add(extraButtons[this._me["setting"].name]);
			}
			if(ms123.config.ConfigManager.hasProcessesAndTasks()){
				menu.add(extraButtons[this._me["task"].name]);
				menu.add(extraButtons[this._me["pe"].name]);
			}
			if(ms123.config.ConfigManager.hasGlobalSearch()){
				menu.add(extraButtons[this._me["gs"].name]);
			}
			menu.addSeparator();
			menu.add(extraButtons[this._me["report"].name]);
			menu.addSeparator();

			var packs = ms123.StoreDesc.getNamespacePacks();
			if( packs.length == 1){
				var entityButtons = entityButtonsMap[packs[0]];
				for (var i = 0; i < entityButtons.length; i++) {
					var eb = entityButtons[i];
					if (!eb.getUserData("not_in_menu")) {
						menu.add(entityButtons[i]);
					}
				}
			}else{
				for (var p = 0; p < packs.length; p++) {
					var pack = packs[p];
					var packMenu = new qx.ui.menu.Menu;

					var entityButtons = entityButtonsMap[pack];
					if( entityButtons==null || entityButtons.length==0) continue;
					for (var b = 0; b < entityButtons.length; b++) {
						var eb = entityButtons[b];
						if (!eb.getUserData("not_in_menu")) {
							packMenu.add(entityButtons[b]);
						}
					}

				 	var packButton = new qx.ui.menu.Button(pack, null, null, packMenu);
					menu.add(packButton);
				}
			}

			menu.addSeparator();
			menu.add(this._getLogout());
			if (ms123.config.ConfigManager.isTest()) {
				menu.add(extraButtons[this._me["bomviewer"].name]);
				menu.add(extraButtons[this._me["pdfviewer"].name]);
			}
			this.setMenu(menu);
		}
	}
});
