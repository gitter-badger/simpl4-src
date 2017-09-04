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
 qooxdoo dialog library
 
 http://qooxdoo.org/contrib/project#dialog
 
 Copyright:
 2007-2010 Christian Boulanger
 
 License:
 LGPL: http://www.gnu.org/licenses/lgpl.html
 EPL: http://www.eclipse.org/org/documents/epl-v10.php
 See the LICENSE file in the project's top-level directory for details.
 
 Authors:
 *  Christian Boulanger (cboulanger)
 ************************************************************************ */

/**
 @asset(qx/icon/${qx.icontheme}/48/status/dialog-information.png)
 */

qx.Class.define("ms123.form.Alert", {
	extend: ms123.form.Dialog,

/*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */
	members: {
		_applyMessage: function (value, old) {
			if (this.getUseHtml()) {
				this._message.setHtml(value);
			} else {
				this._message.setValue(value);
				this._message.setVisibility(value ? "visible" : "excluded");
			}
		},

		/**
		 ---------------------------------------------------------------------------
		 WIDGET LAYOUT
		 ---------------------------------------------------------------------------
		 */

		/**
		 * Create the main content of the widget
		 */
		_createWidgetContent: function () {

			/**
			 * groupbox
			 */
			var groupboxContainer = new qx.ui.groupbox.GroupBox().set({
				contentPadding: [16, 16, 16, 16]
			});
			groupboxContainer.setLayout(new qx.ui.layout.VBox(10));
			this.add(groupboxContainer);

			var hbox = new qx.ui.container.Composite;
			hbox.setLayout(new qx.ui.layout.HBox(10));
			groupboxContainer.add(hbox);

			/**
			 * add image 
			 */
			this._image = new qx.ui.basic.Image("icon/48/status/dialog-information.png");
			hbox.add(this._image);

			if (this.getUseHtml()) {
				this._message = new qx.ui.embed.Html();
				hbox.setWidth(this.getWindowWidth());
				hbox.setHeight(this.getWindowHeight());
				this._message.setOverflowY("auto");
				this._message.setOverflowX("auto");
				this._message.setAllowStretchY(true);
			} else {
				this._message = new qx.ui.basic.Label();
				this._message.setRich(true);
				this._message.setWidth(this.getWindowWidth());
			}
			this._message.setAllowStretchX(true);
			this._message.setSelectable(true);
			hbox.add(this._message, {
				flex: 1
			});

			/**
			 * Ok Button 
			 */
			var okButton = this._createOkButton();

			/**
			 * buttons pane
			 */
			var buttonPane = new qx.ui.container.Composite;
			var bpLayout = new qx.ui.layout.HBox();
			bpLayout.setAlignX("center");
			buttonPane.setLayout(bpLayout);
			if (this._noOkButton == false) {
				buttonPane.add(okButton);
			}
			groupboxContainer.add(buttonPane);
		}
	}
});
