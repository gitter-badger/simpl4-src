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
	* @ignore(jQuery)
	* @ignore($)
*/
qx.Class.define("ms123.form.MultiUseBox", {
	extend: qx.ui.core.Widget,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (useitCheckboxes) {
		this.base(arguments);
		var layout = new qx.ui.layout.HBox();
		this._setLayout(layout);

		this.setMaxWidth(40);
		this.setMaxHeight(25);
		this._createChildControl("selectbox");

	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */

	properties: {
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		// overridden
		_createChildControlImpl: function (id, hash) {
			var control;
			switch (id) {
			case "selectbox":
				control = new qx.ui.form.SelectBox();
				control.setFocusable(false);
				control.setKeepActive(true);
				control.addState("inner");
				var tempItem = new qx.ui.form.ListItem("", "icon/16/actions/media-playback-stop.png", "ignore");
				tempItem.setToolTipText(this.tr("multiusebox.ignore"));
				control.add(tempItem);
				tempItem = new qx.ui.form.ListItem("", "icon/16/actions/dialog-ok.png", "replace");
				tempItem.setToolTipText(this.tr("multiusebox.replace"));
				control.add(tempItem);
				tempItem = new qx.ui.form.ListItem("", "icon/16/actions/list-add.png", "add");
				tempItem.setToolTipText(this.tr("multiusebox.add"));
				control.add(tempItem);
				tempItem = new qx.ui.form.ListItem("", "icon/16/actions/list-remove.png", "remove");
				tempItem.setToolTipText(this.tr("multiusebox.remove"));
				control.add(tempItem);
				control.setMaxWidth(35);
				this._add(control);
				break;
			}
			return control;
		},
		setValue: function (val) {
			if( typeof val == "boolean" ){
				val = "ignore";
			}
			var sb = this.getChildControl("selectbox");
			var widgets = sb.getSelectables(true);
			for( var i=0;i < widgets.length;i++){
				var w = widgets[i];
				if( w.getModel() == val ){
					sb.setSelection([w]);
					break;
				}
			}
		},
		getValue: function () {
			var sb = this.getChildControl("selectbox");
			var selection = sb.getSelection();
			return selection[0].getModel();
		}
	}
});
