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
qx.Class.define('ms123.datamapper.BaseManager', {
	extend: qx.ui.container.Composite,
	include: [qx.locale.MTranslation, ms123.baseeditor.MPlugin],

	construct: function (context) {
		this.base(arguments);
		this._eventsQueue = [];
		this._eventListeners = new Hash();
		this.resetPluginsData();
		this._facade = this.getPluginFacade();
		this._facade.storeDesc = context.storeDesc;
	},

	properties: {},

	members: {
		_addPlugins: function () {}
	}
});
