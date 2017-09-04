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
	@asset(qx/icon/${qx.icontheme}/22/actions/dialog-cancel.png)
	@asset(qx/icon/${qx.icontheme}/22/actions/dialog-ok.png)
	@asset(qx/icon/${qx.icontheme}/22/actions/document-save.png)
*/


/**
 * Base class for dialog widgets
 */
qx.Class.define("ms123.form.DialogForm", {
	extend: ms123.form.Dialog,

	/**
	 *****************************************************************************
	 STATICS
	 *****************************************************************************
	 */
	statics: {

		form: function (message, formData, callback, context, inWindow) {
			(new ms123.form.Form({
				"message": message,
				"formData": formData,
				"allowCancel": true,
				"callback": callback,
				"context": context || null,
				"inWindow": inWindow !== undefined ? inWindow : true
			})).show();
		}
	}
});
