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
qx.Class.define("ms123.graphicaleditor.PalettePanel", {
	extend: qx.ui.container.Composite,
	include: [qx.locale.MTranslation],

	/**
	 * Constructor
	 */
	construct: function (stencilmanager) {
		this.base(arguments);
		this.__stencilManager = stencilmanager;
		this.setLayout(new qx.ui.layout.Grow());
		this._createPanels();
	},

	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {
		_createPanels: function () {
			var panels = [];
			var buttons = [];

			var scroll = new qx.ui.container.Scroll();
			this.add(scroll);

			var panelSpace = new qx.ui.container.Composite(new qx.ui.layout.VBox()).set({
				allowShrinkY: false,
				allowGrowX: true
			});

			panelSpace.setPadding(0);
			scroll.add(panelSpace);

			var groupNameList = this.__stencilManager.getGroupNameList();
			for (var i = 0; i < groupNameList.length; i++) {
				var groupName = groupNameList[i];
				var panel = new ms123.widgets.CollapsablePanel(groupName, new qx.ui.layout.VBox());
				var groupList = this.__stencilManager.getGroupListByName(groupName);
				for (var j = 0; j < groupList.length; j++) {
					var stencil = groupList[j];
					var button = this._createButton(stencil.getId(), stencil.getTitle(), stencil.getIcon());

					buttons.push(button);
					panel.add(button);
				}
				panels.push(panel);
				panelSpace.add(panel);
				panel.setValue(false);
			}
			panels[0].setValue(true);
		},
		_createButton: function (stencilid, text, icon) {
			var b = new ms123.graphicaleditor.DraggableButton(text, icon);
			b.setUserData("stencilid", stencilid);
			b.setDraggable(true);
			b.addListener("dragstart", function (e) {
				e.addAction("move");
				console.log("DRAG:dragstart:" + b);

			});
			b.addListener("execute", function (e) {
				console.log("execute:" + b);
			});
			b.setPaddingTop(1);
			b.setPaddingBottom(1);
			return b;
		}
	}
});
