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
/*
*/

/**
	* @ignore(Hash)
*/
qx.Class.define("ms123.permissions.Config", {
	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {

		/* Event types */
		EVENT_MOUSEDOWN: "mousedown",
		EVENT_MOUSEUP: "mouseup",
		EVENT_MOUSEOVER: "mouseover",
		EVENT_MOUSEOUT: "mouseout",
		EVENT_MOUSEMOVE: "mousemove",
		EVENT_DBLCLICK: "dblclick",
		EVENT_KEYDOWN: "keydown",
		EVENT_KEYUP: "keyup",

		EVENT_EXECUTE_COMMANDS: "execute_commands",
		EVENT_RESOURCE_SELECTED: "resource_selected",
		EVENT_ROLE_SELECTED: "role_selected",

		/* Copy & Paste */
		EDIT_OFFSET_PASTE: 10,

		/* Key-Codes */
		KEY_CODE_X: 88,
		KEY_CODE_C: 67,
		KEY_CODE_V: 86,
		KEY_CODE_DELETE: 46,
		KEY_CODE_META: 224,
		KEY_CODE_BACKSPACE: 8,
		KEY_CODE_LEFT: 37,
		KEY_CODE_RIGHT: 39,
		KEY_CODE_UP: 38,
		KEY_CODE_DOWN: 40,

		KEY_Code_enter: 12,
		KEY_Code_left: 37,
		KEY_Code_right: 39,
		KEY_Code_top: 38,
		KEY_Code_bottom: 40,

		/* Supported Meta Keys */
		META_KEY_META_CTRL: "metactrl",
		META_KEY_ALT: "alt",
		META_KEY_SHIFT: "shift",

		/* Key Actions */
		KEY_ACTION_DOWN: "down",
		KEY_ACTION_UP: "up"
	}
});
