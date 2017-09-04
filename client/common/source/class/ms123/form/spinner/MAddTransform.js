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
     2008 Derrell Lipman

   License:
     LGPL: http://www.gnu.org/licenses/lgpl.html
     EPL: http://www.eclipse.org/org/documents/epl-v10.php
     See the LICENSE file in the project's top-level directory for details.

   Authors:
     * Derrell Lipman

************************************************************************ */

/**
 * A mixin to add a "transform" key to Spinner's "value" property.
 */
qx.Mixin.define("ms123.form.spinner.MAddTransform", {
	construct: function () {
		// KLUDGE: Add a 'transform' key
		//
		// Since there's currently no way for subclasses to add to a property of
		// the superclass using legitimate code, kludge it by futzing with
		// internals of the Class and Property systems.  This is *not* a good
		// long-term solution!
		if (!qx.ui.form.Spinner.$$properties["value"].transform) {
			qx.ui.form.Spinner.$$properties["value"].transform = "_transformValue";
		}
	},

	members: {
		_transformValue: function (value) {
			return value;
		}
	}
});
