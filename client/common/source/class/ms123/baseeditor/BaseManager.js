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
*/
qx.Class.define('ms123.baseeditor.BaseManager', {
	extend: qx.core.Object,
	include: [qx.locale.MTranslation, ms123.baseeditor.MPlugin],

	construct: function (context) {
		this.setWindow(context.window);
		this._pluginsData = [];
		this._eventsQueue = [];
		this._eventListeners = new Hash();
		this._facade = this.getPluginFacade();
		this._facade.storeDesc = context.storeDesc;
		this._facade.settingsid = context.settingsid;

		this._registerPluginsOnKeyEvents();
		this._initEventListener();
		var window = this.getWindow();
		window.setLayout(new qx.ui.layout.Grow());

		window.set({
			contentPadding: 2
		});
		this._facade.leftSpace = this._createLeftSpace();
		this._facade.rightSpace = this._createRightSpace();
		this._addPlugins();
		var splitPane = this._splitPane( this._facade.leftSpace, this._facade.rightSpace);
		window.add(splitPane );
	},

	properties: {
		window: {
			check: 'Object'
		}
	},

	members: {
		_addPlugins: function () {
		},

		_createLeftSpace: function () {
			var leftSpace = new qx.ui.container.Composite(new qx.ui.layout.VBox()).set({
				allowGrowY: true,
				allowGrowX: true
			});

			leftSpace.setPadding(0);
			return leftSpace;
		},

		_createRightSpace: function () {
			var rightSpace = new qx.ui.container.Composite(new qx.ui.layout.Dock()).set({ });
			rightSpace.setPadding(0);
			return rightSpace;
		},

		_splitPane: function (left, right) {
			var splitPane = new qx.ui.splitpane.Pane("horizontal").set({
				decorator: null
			});

			splitPane.add(left, 3);
			splitPane.add(right, 8);
			return splitPane;
		}
	}
});
