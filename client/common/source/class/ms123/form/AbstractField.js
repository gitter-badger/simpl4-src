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
qx.Class.define("ms123.form.AbstractField", {
	extend: qx.ui.core.Widget,
	implement: [
	qx.ui.form.IStringForm, qx.ui.form.IForm],
	include: [
	qx.ui.form.MForm],
	type : "abstract",


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (useitCheckboxes) {
		this.base(arguments);
		var layout = new qx.ui.layout.HBox();
		this._setLayout(layout);

		var textField = this._createChildControl("textfield");
		if( useitCheckboxes ){
			this._createChildControl("checkbox");
		}

		// forward the focusin and focusout events to the textfield. The textfield
		// is not focusable so the events need to be forwarded manually.
		this.addListener("focusin", function (e) {
			textField.fireNonBubblingEvent("focusin", qx.event.type.Focus);
		}, this);

		this.addListener("focusout", function (e) {
			textField.fireNonBubblingEvent("focusout", qx.event.type.Focus);
		}, this);
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */

	properties: {
		// overridden
		appearance: {
			refine: true,
			init: "combobox"
		}
	},

	/**
  *****************************************************************************
     EVENTS
  *****************************************************************************
  */

	events: {
		/** Whenever the value is changed this event is fired
		 *
		 *  Event data: The new text value of the field.
		 */
		"changeValue": "qx.event.type.Data"
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
			case "checkbox":
				control = new qx.ui.form.CheckBox();
				control.setFocusable(false);
				control.setKeepActive(true);
				control.addState("inner");
				control.set({
					height:10,
					maxHeight:10,
					minWidth:12,
					maxWidth:12,
					decorator:"checkbox"
					//decorator:"main"
				});
				control.setToolTipText(this.tr("usebox.use"));
				this._add(control);
				break;
			}
			return control;
		},
		// overridden
		/**
		 * @lint ignoreReferenceField(_forwardStates)
		 */
		_forwardStates: {
			focused: true
		},


		// overridden
		tabFocus: function () {
			var field = this.getChildControl("textfield");

			field.getFocusElement().focus();
			field.selectAllText();
		},


		// overridden
		focus: function () {
			this.base(arguments);
			this.getChildControl("textfield").getFocusElement().focus();
		},


		// interface implementation
		setValue: function (value) {
			var textfield = this.getChildControl("textfield");
			if (textfield.getValue() == value) {
				return;
			}

			// Apply to text field
			textfield.setValue(value);
		},


		// interface implementation
		getValue: function () {
			return this.getChildControl("textfield").getValue();
		},


		// interface implementation
		resetValue: function () {
			this.getChildControl("textfield").setValue(null);
		},


		// useit checkbox
		getCheckBox: function () {
			return this.getChildControl("checkbox");
		},

		/**
		 ---------------------------------------------------------------------------
		 EVENT LISTENERS
		 ---------------------------------------------------------------------------
		 */
		/**
     * Reacts on value changes of the text field and syncs the
     *
     * param e {qx.event.type.Data} Change event
     */
    _onTextFieldChangeValue : function(e) {
      var value = e.getData();
      // Fire event
      this.fireDataEvent("changeValue", value, e.getOldData());
    },


		/**
		 ---------------------------------------------------------------------------
		 TEXTFIELD SELECTION API
		 ---------------------------------------------------------------------------
		 */

		/**
		 * Returns the current selection.
		 * This method only works if the widget is already created and
		 * added to the document.
		 *
		 * @return {String|null}
		 */
		getTextSelection: function () {
			return this.getChildControl("textfield").getTextSelection();
		},


		/**
		 * Returns the current selection length.
		 * This method only works if the widget is already created and
		 * added to the document.
		 *
		 * @return {Integer|null}
		 */
		getTextSelectionLength: function () {
			return this.getChildControl("textfield").getTextSelectionLength();
		},


		/**
		 * Set the selection to the given start and end (zero-based).
		 * If no end value is given the selection will extend to the
		 * end of the textfield's content.
		 * This method only works if the widget is already created and
		 * added to the document.
		 *
		 * param start {Integer} start of the selection (zero-based)
		 * param end {Integer} end of the selection
		 * @return {void}
		 */
		setTextSelection: function (start, end) {
			this.getChildControl("textfield").setTextSelection(start, end);
		},


		/**
		 * Clears the current selection.
		 * This method only works if the widget is already created and
		 * added to the document.
		 *
		 * @return {void}
		 */
		clearTextSelection: function () {
			this.getChildControl("textfield").clearTextSelection();
		},


		/**
		 * Selects the whole content
		 *
		 * @return {void}
		 */
		selectAllText: function () {
			this.getChildControl("textfield").selectAllText();
		}
	}
});
