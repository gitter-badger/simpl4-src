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
     2004-2011 1&1 Internet AG, Germany, http://www.1und1.de

   License:
     LGPL: http://www.gnu.org/licenses/lgpl.html
     EPL: http://www.eclipse.org/org/documents/epl-v10.php
     See the LICENSE file in the project's top-level directory for details.

   Authors:
     * Sebastian Werner (wpbasti)
     * Fabian Jakobs (fjakobs)
     * Christian Hagendorn (chris_schmidt)

************************************************************************ */

qx.Class.define("ms123.importing.TreeItem", {
	extend: qx.ui.tree.VirtualTreeItem,

	properties: {
		leadIcon: {
			check: "String",
			event: "changeLeadIcon",
			nullable: true
		},

		mapping: {
			check: "String",
			event: "changeMapping",
			nullable: true
		}
	},

	members: {
		__leadIcon: null,
		__mapping: null,

		_addWidgets: function () {
			var leadIcon = this.__leadIcon = new qx.ui.basic.Image();
			this.bind("leadIcon", leadIcon, "source");
			leadIcon.setWidth(16);

			// Here's our indentation and tree-lines
			this.addSpacer();
			this.addOpenButton();

			// The standard tree icon follows
			this.addIcon();
			this.setIcon("icon/16/places/user-desktop.png");


			// The label
			this.addLabel();


			this.addWidget(leadIcon);

			// All else should be right justified
			this.addWidget(new qx.ui.core.Spacer(), {
				flex: 1
			});

			var text = this.__mapping = new qx.ui.basic.Label();
			this.bind("mapping", text, "value");
			text.setWidth(80);
			this.addWidget(text);
		}
	}
});
