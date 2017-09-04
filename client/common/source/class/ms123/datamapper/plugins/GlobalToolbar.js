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
qx.Class.define("ms123.datamapper.plugins.GlobalToolbar", {
	extend: ms123.baseeditor.Toolbar,
	include: [qx.locale.MTranslation],

	/**
	 * Constructor
	 */
	construct: function (facade) {
		this.base(arguments,facade);
		this.facade.registerOnEvent(ms123.baseeditor.Config.EVENT_EXECUTE_COMMANDS, this.onUpdate.bind(this));
		this.facade.registerOnEvent(ms123.baseeditor.Config.EVENT_UNDO_ROLLBACK, this.onUpdate.bind(this));
		this.facade.registerOnEvent(ms123.baseeditor.Config.EVENT_UNDO_EXECUTE, this.onUpdate.bind(this));
		this.facade.registerOnEvent(ms123.baseeditor.Config.EVENT_RESET_UNDO, this.onUpdate.bind(this));
	},

	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {

	}
});
