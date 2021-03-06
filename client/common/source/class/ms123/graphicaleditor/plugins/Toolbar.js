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
	* @ignore(Hash)
	* @ignore($A)
*/
qx.Class.define("ms123.graphicaleditor.plugins.Toolbar", {
	extend: qx.ui.container.Composite,
	include: [qx.locale.MTranslation],

	/**
	 * Constructor
	 */
	construct: function (facade) {
		this.base(arguments);
		this.facade = facade;
		this.setLayout(new qx.ui.layout.Dock());
		var toolbar = new qx.ui.toolbar.ToolBar().set({});
		toolbar.setSpacing(0);
		this.add(toolbar,{edge:"west"});
		this._toolbar = toolbar;
		this.groupIndex = new Hash();

		this.plugs = [];
		this.facade.registerOnEvent(ms123.oryx.Config.EVENT_BUTTON_UPDATE, this.onButtonUpdate.bind(this));

		this.facade.registerOnEvent(ms123.oryx.Config.EVENT_SELECTION_CHANGED, this.onSelectionChanged.bind(this));
	},

	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {

		onButtonUpdate: function (event) {
			var button = this.buttons.find(function (button) {
				return button.getUserData("id") === event.id;
			});

			if (event.pressed !== undefined) {
				button.buttonInstance.toggle(event.pressed);
			}
		},
		registryChanged: function (pluginsData) {
			// Sort plugins by group and index
			var newPlugs = pluginsData.sortBy((function (value) {
				return ((this.groupIndex[value.group] != undefined ? this.groupIndex[value.group] : "") + value.group + "" + value.index).toLowerCase();
			}).bind(this));
			var plugs = $A(newPlugs).findAll(qx.lang.Function.bind(function (value) {
				return !this.plugs.include(value) && (!value.target || value.target === ms123.graphicaleditor.plugins.Toolbar)
			}, this));
			if (plugs.length < 1) return;

			this.buttons = [];

			var currentGroupsName = this.plugs.last() ? this.plugs.last().group : plugs[0].group;

			// Map used to store all drop down buttons of current group
			var currentGroupsDropDownButton = {};


			plugs.each((function (value) {
				if (!value.name) {
					return
				}
				this.plugs.push(value);
				// Add seperator if new group begins
				if (currentGroupsName != value.group) {
					this._toolbar.addSpacer();
					currentGroupsName = value.group;
					currentGroupsDropDownButton = {};
				}

				// If an drop down group icon is provided, a split button should be used
				if (value.dropDownGroupIcon) {
					var splitMenu = currentGroupsDropDownButton[value.dropDownGroupIcon];

					// Create a new split button if this is the first plugin using it 
					if (splitMenu === undefined) {
						var menu = new qx.ui.menu.Menu;
						this._toolbar.add(new qx.ui.toolbar.SplitButton("", value.dropDownGroupIcon, menu));
						splitMenu = currentGroupsDropDownButton[value.dropDownGroupIcon] = menu;
					}

					// Create buttons depending on toggle
					if (value.toggle) {
						var button = new qx.ui.toolbar.CheckBox(value.name, value.icon);
						button.addListener("execute", value.functionality, this);

					} else {
						var button = new qx.ui.toolbar.Button(value.name, value.icon);
						button.addListener("execute", value.functionality, this);
					}
					if (value.description) {
						button.setToolTipText(value.description);
					}
					button.setUserData("id", value.id);
					button.setWidth(26);
					value['buttonInstance'] = button;
					splitMenu.add(button);
				} else if (value.addFill) {
					this._toolbar.addSpacer();
				} else { // create normal, simple button
					if (value.toggle) {
						var button = new qx.ui.toolbar.CheckBox("", value.icon);
						button.addListener("execute", value.functionality, this);
					} else {
						var button = new qx.ui.toolbar.Button("", value.icon);
						button.addListener("execute", value.functionality, this);
					}
					if (value.description) {
						button.setToolTipText(value.description);
					}
					button.setUserData("id", value.id);
					button.setWidth(26);
					this._toolbar.add(button);
					value['buttonInstance'] = button;
				}
				this.buttons.push(value);
			}).bind(this));
			this.enableButtons([]);
		},

		onSelectionChanged: function (event) {
			this.enableButtons(event.elements);
		},

		enableButtons: function (elements) {
			// Show the Buttons
			this.buttons.each((function (value) {
				value.buttonInstance.setEnabled(true);

				// If there is less elements than minShapes
				if (value.minShape && value.minShape > elements.length) value.buttonInstance.setEnabled(false);
				// If there is more elements than minShapes
				if (value.maxShape && value.maxShape < elements.length) value.buttonInstance.setEnabled(false);
				// If the plugin is not enabled	
				if (value.isEnabled && !value.isEnabled(value.buttonInstance)) value.buttonInstance.setEnabled(false);

			}).bind(this));
		}
	}
});
