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
qx.Class.define('ms123.desktop.TaskBar', {
	extend: qx.ui.container.SlideBar,

	construct: function () {
		this.base(arguments);
	},

	members: {

		_taskButtons: {},

		add: function (item) {
			this.base(arguments, item);
			this.fireDataEvent('favoriteAdded', item);
		},

		remove: function (item) {
			this.base(arguments, item);
			this.fireDataEvent('favoriteRemoved', item);
		},

		addTaskButton: function (taskButton) {
			this.add(taskButton);
		},

		addWindow: function (window) {
			var hashCode = window.toHashCode();
			var flag = false;
			this._taskButtons[hashCode] = new ms123.desktop.TaskButton(window.getCaption(), "XXX" /*windowApplicationName*/ );
			this._taskButtons[hashCode]._window = window;
			var self = this;

			window.addListener('close', function () {
				this.removeWindow(window);
			}, this);

			window.addListener('changeActive', function () {
				if (this._taskButtons[hashCode]) {
					if (window.get('active')) {
						this.selectTaskButton(this._taskButtons[hashCode]);
					} else {
						this.unselectTaskButton(this._taskButtons[hashCode]);
					}
				}
			}, this);

			this._taskButtons[hashCode].clickEvent = this._taskButtons[hashCode].addListener('click', function (e) {
				if (this.get('value')) {
					if (window.getLastMode() == "maximized") {
						window.maximize();
					} else {
						window.show();
					}
					window.set('active', true);
					window.focus();
				} else {
					if (window.get('allowMinimize')) {
						window.minimize();
					}
				}
			});

			this._taskButtons[hashCode].checkedEvent = this._taskButtons[hashCode].addListener('changeValue', function (e) {
				if (this.isValue()) {
					this._buttonWithFocus();
				} else {
					this._buttonWithoutFocus();
				}
			});

			if (!flag) {
				this.addTaskButton(this._taskButtons[hashCode]);
			}
		},

		removeTaskButton: function (taskButton) {
			this.remove(taskButton);
		},

		removeWindow: function (window) {
			var hashCode = window.toHashCode();
			if (this._taskButtons[hashCode]) {
				this.removeTaskButton(this._taskButtons[hashCode]);
				delete this._taskButtons[hashCode];
			}
		},

		selectTaskButton: function (taskButton) {
			taskButton.set('value', true);
		},

		unselectTaskButton: function (taskButton) {
			taskButton.set('value', false);
		}
	}
});
