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
	* @ignore(Clazz)
*/
qx.Mixin.define("ms123.baseeditor.MPlugin",{
	members: {
		getPluginFacade: function () {
			if (!(this._pluginFacade)) {
				this._pluginFacade = {
					offer: this._offer.bind(this),
					getPluginsData: this._getPluginsData.bind(this),
					resetPluginsData: this.resetPluginsData.bind(this),
					executeCommands: this._executeCommands.bind(this),
					isExecutingCommands: this._isExecutingCommands.bind(this),

					registerOnEvent: this._registerOnEvent.bind(this),
					unregisterOnEvent: this._unregisterOnEvent.bind(this),
					raiseEvent: this._handleEvents.bind(this)
				};
			}
			return this._pluginFacade;
		},

		_offer: function (pluginData) {
			if (!this._pluginsData.member(pluginData)) {
				this._pluginsData.push(pluginData);
			}
		},

		resetPluginsData: function () {
			this._pluginsData = [];
		},
		getPluginsData: function () {
			return this._getPluginsData();
		},
		_getPluginsData: function () {
			return this._pluginsData;
		},

//BEG Keyboardhandling
		_registerPluginsOnKeyEvents: function () {
			this._pluginsData.each((function (pluginData) {
				if (pluginData.keyCodes) {
					pluginData.keyCodes.each((function (keyComb) {
						var eventName = "key.event";
						eventName += '.' + keyComb.keyAction;
						if (keyComb.metaKeys) {
							if (keyComb.metaKeys.indexOf(ms123.ruleseditor.Config.META_KEY_META_CTRL) > -1) {
								eventName += "." + ms123.ruleseditor.Config.META_KEY_META_CTRL;
							}
							if (keyComb.metaKeys.indexOf(ms123.ruleseditor.Config.META_KEY_ALT) > -1) {
								eventName += '.' + ms123.ruleseditor.Config.META_KEY_ALT;
							}
							if (keyComb.metaKeys.indexOf(ms123.ruleseditor.Config.META_KEY_SHIFT) > -1) {
								eventName += '.' + ms123.ruleseditor.Config.META_KEY_SHIFT;
							}
						}
						if (keyComb.keyCode) {
							eventName += '.' + keyComb.keyCode;
						}
						if (pluginData.toggle === true && pluginData.buttonInstance) {
							this._registerOnEvent(eventName, function () {
								pluginData.buttonInstance.toggle(!pluginData.buttonInstance.pressed);
								pluginData.functionality.call(pluginData, pluginData.buttonInstance, pluginData.buttonInstance.pressed);
							});
						} else {
							this._registerOnEvent(eventName, pluginData.functionality)
						}

					}).bind(this));
				}
			}).bind(this));
		},

		_initEventListener: function () {
			this.getWindow().addListenerOnce("appear", function () {
				var elem = this.getWindow().getContentElement().getDomElement();
				elem.addEventListener(ms123.ruleseditor.Config.EVENT_KEYDOWN, this._catchKeyDownEvents.bind(this), false);
				elem.addEventListener(ms123.ruleseditor.Config.EVENT_KEYUP, this._catchKeyUpEvents.bind(this), false);
			}, this);
		},

		_catchKeyUpEvents: function (event) {
			if (!event) event = window.event;
			var keyUpEvent = this._createKeyCombEvent(event, ms123.ruleseditor.Config.KEY_ACTION_UP);
			this._handleEvents({
				type: keyUpEvent,
				event: event
			});
		},

		_catchKeyDownEvents: function (event) {
			if (!event) event = window.event;
			var keyDownEvent = this._createKeyCombEvent(event, ms123.oryx.Config.KEY_ACTION_DOWN);
			this._handleEvents({
				type: keyDownEvent,
				event: event
			});
		},

		_createKeyCombEvent: function (keyEvent, keyAction) {
			var pressedKey = keyEvent.which || keyEvent.keyCode;
			var eventName = "key.event";

			if (keyAction) {
				eventName += "." + keyAction;
			}
			if (keyEvent.ctrlKey || keyEvent.metaKey) {
				eventName += "." + ms123.oryx.Config.META_KEY_META_CTRL;
			}
			if (keyEvent.altKey) {
				eventName += "." + ms123.oryx.Config.META_KEY_ALT;
			}
			if (keyEvent.shiftKey) {
				eventName += "." + ms123.oryx.Config.META_KEY_SHIFT;
			}
			return eventName + "." + pressedKey;
		},
//END Keyboardhandling

		/**
		 *  Methods for the PluginFacade
		 */
		_registerOnEvent: function (eventType, callback) {
			if (!(this._eventListeners.keys().member(eventType))) {
				this._eventListeners[eventType] = [];
			}
			this._eventListeners[eventType].push(callback);
		},

		_unregisterOnEvent: function (eventType, callback) {
			if (this._eventListeners.keys().member(eventType)) {
				this._eventListeners[eventType] = this._eventListeners[eventType].without(callback);
			} else {}
		},

		_executeEventImmediately: function (eventObj) {
			if (this._eventListeners.keys().member(eventObj.event.type)) {
				this._eventListeners[eventObj.event.type].each((function (value) {
					value(eventObj.event, eventObj.arg);
				}).bind(this));
			}
		},

		_executeEvents: function () {
			this._queueRunning = true;
			try{
				while (this._eventsQueue.length > 0) {
					var val = this._eventsQueue.shift();
					this._executeEventImmediately(val);
				}
			}catch(e){
				console.error("_executeEvents:"+e);
				console.log(e.stack);
			}finally{
				this._queueRunning = false;
			}
		},

		_handleEvents: function (event, argObj) {
			switch (event.type) {}
			if (event.forceExecution) {
				this._executeEventImmediately({
					event: event,
					arg: argObj
				});
			} else {
				this._eventsQueue.push({
					event: event,
					arg: argObj
				});
			}

			if (!this._queueRunning) {
				this._executeEvents();
			}

			return false;
		},

		_executeCommands: function (commands) {
			if (!this._commandStack) {
				this._commandStack = [];
			}
			if (!this._commandStackExecuted) {
				this._commandStackExecuted = [];
			}
			this._commandStack = [].concat(this._commandStack).concat(commands);

			// Check if already executes
			if (this._commandExecuting) {
				return;
			}

			this._commandExecuting = true;

			// Iterate over all commands
			while (this._commandStack.length > 0) {
				var command = this._commandStack.shift();
				command.execute();
				this._commandStackExecuted.push(command);
			}

			this._handleEvents({
				type: ms123.ruleseditor.Config.EVENT_EXECUTE_COMMANDS,
				commands: this._commandStackExecuted
			});

			// Remove temporary vars
			delete this._commandStack;
			delete this._commandStackExecuted;
			delete this._commandExecuting;


			//this.updateSelection();
		},
		_isExecutingCommands: function () {
			return !!this._commandExecuting;
		}
	}
});
