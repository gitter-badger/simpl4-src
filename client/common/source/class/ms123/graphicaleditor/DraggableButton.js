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
/** **********************************************************************

   qooxdoo - the new era of web development

   http://qooxdoo.org

   Copyright:
     2004-2008 1&1 Internet AG, Germany, http://www.1und1.de

   License:
     LGPL: http://www.gnu.org/licenses/lgpl.html
     EPL: http://www.eclipse.org/org/documents/epl-v10.php
     See the LICENSE file in the project's top-level directory for details.

   Authors:
     * Sebastian Werner (wpbasti)
     * Andreas Ecker (ecker)

************************************************************************ */

/**
 * The normal toolbar button. Like a normal {@link qx.ui.form.Button}
 * but with a style matching the toolbar and without keyboard support.
 */
qx.Class.define("ms123.graphicaleditor.DraggableButton", {
	extend: qx.ui.form.Button,



/*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

	construct: function (label, icon, command) {
		this.base(arguments, label, icon, command);
		this.setDecorator(null);

		// Toolbar buttons should not support the keyboard events
		this.removeListener("keydown", this._onKeyDown);
		this.removeListener("keyup", this._onKeyUp);
		this.removeListener("pointerdown", this._onPointerDown);
		this.addListener("pointerdown", this.__onPointerDown);
	},




/*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

	properties: {
		appearance: {
			refine: true,
			init: "toolbar-button"
		},

		show: {
			refine: true,
			init: "inherit"
		},

		focusable: {
			refine: true,
			init: false
		}
	},


	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {
		__onPointerDown: function (e) {
			if (!e.isLeftPressed()) {
				return;
			}

			//  e.stopPropagation();
			// Activate capturing if the button get a mouseout while
			// the button is pressed.
			this.capture();

			this.removeState("abandoned");
			this.addState("pressed");
		}

	}

});
