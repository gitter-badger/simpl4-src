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
qx.Class.define('ms123.ruleseditor.Table', {

	extend: qx.ui.table.Table,

	construct: function (model, custom) {
		this.base(arguments, model, custom);
		this.setStatusBarVisible(false);
	},

	properties: {
		application: {
			check: 'Object'
		}
	},

	members: {
		_onKeyPress: function (evt) {
			console.log("Table._onKeyPress:" + evt);
			if (!this.getEnabled()) {
				return;
			}

			// No editing mode
			var oldFocusedRow = this.__focusedRow;
			var consumed = true;

			// Handle keys that are independent from the modifiers
			var identifier = evt.getKeyIdentifier();

			if (this.isEditing()) {
				// Editing mode
				if (evt.getModifiers() == 0) {
					switch (identifier) {
					case "Enter":
						this.stopEditing();
						var oldFocusedRow = this.__focusedRow;
						this.moveFocusedCell(0, 1); //@@@MS
						this.getSelectionModel().setSelectionInterval(this.__focusedRow, this.__focusedRow);
						if (this.__focusedRow != oldFocusedRow) {
							//consumed = this.startEditing();
						}

						break;

					case "Escape":
						this.cancelEditing();
						this.focus();
						break;

					default:
						consumed = false;
						break;
					}
				}
				return
			}
			else {
				// No editing mode
				if (evt.isCtrlPressed()) {
					// Handle keys that depend on modifiers
					consumed = true;

					switch (identifier) {
					case "A":
						// Ctrl + A
						var rowCount = this.getTableModel().getRowCount();

						if (rowCount > 0) {
							this.getSelectionModel().setSelectionInterval(0, rowCount - 1);
						}

						break;

					default:
						consumed = false;
						break;
					}
				}
				else {
					// Handle keys that are independent from the modifiers
					console.log("identifier:"+identifier);
					switch (identifier) {
					case "Delete":
						consumed = false; //@@@MS
						break;
					case "Space":
						this.__selectionManager.handleSelectKeyDown(this.__focusedRow, evt);
						break;

					case "F2":
					case "Enter":
						consumed = this.startEditing();
						break;

					case "Home":
						this.setFocusedCell(this.__focusedCol, 0, true);
						break;

					case "End":
						var rowCount = this.getTableModel().getRowCount();
						this.setFocusedCell(this.__focusedCol, rowCount - 1, true);
						break;

					case "Left":
						this.stopEditing();
						this.moveFocusedCell(-1, 0);
						break;

					case "Right":
						this.stopEditing();
						this.moveFocusedCell(1, 0);
						break;

					case "Up":
						this.stopEditing();
						this.moveFocusedCell(0, -1);
						break;

					case "Down":
						this.stopEditing();
						this.moveFocusedCell(0, 1);
						break;

					case "PageUp":
					case "PageDown":
						var scroller = this.getPaneScroller(0);
						var pane = scroller.getTablePane();
						var rowCount = pane.getVisibleRowCount() - 1;
						var rowHeight = this.getRowHeight();
						var direction = (identifier == "PageUp") ? -1 : 1;
						scroller.setScrollY(scroller.getScrollY() + direction * rowCount * rowHeight);
						this.moveFocusedCell(0, direction * rowCount);
						break;

					default:
						consumed = this.startEditing();
						consumed = false; //@@@MS
						break;
					}
				}
			}

			if (oldFocusedRow != this.__focusedRow && this.getRowFocusChangeModifiesSelection()) {
				// The focus moved -> Let the selection manager handle this event
				this.__selectionManager.handleMoveKeyDown(this.__focusedRow, evt);
			}

			if (consumed) {
				evt.preventDefault();
				evt.stopPropagation();
			}
		}
	}
});
