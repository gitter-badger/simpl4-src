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
qx.Class.define('ms123.ruleseditor.BooleanCellEditor', {
	extend: qx.ui.table.celleditor.CheckBox,

	construct: function () {
		this.base(arguments);
	},

	members: {
		_createCellEditor: function (cellInfo) {
			if (cellInfo.value === null || cellInfo.value === undefined) {
				cellInfo.value = false;
			}
			return this.base(arguments, cellInfo);
		},


		createCellEditor: function (cellInfo) {
			if (cellInfo.value === undefined) {
				cellInfo.value = null;
			}
			var editor = new qx.ui.container.Composite(new qx.ui.layout.HBox().set({
				alignX: "center",
				alignY: "middle"
			})).set({
				focusable: true
			});

console.log("BooleanCellEditor:"+cellInfo.value);
			var checkbox = new ms123.ruleseditor.TristateCheckBox().set({
				triState: true,
				value: cellInfo.value
			});
			editor.add(checkbox);

			// propagate focus
			editor.addListener("focus", function () {
				checkbox.focus();
			});

			// propagate active state
			editor.addListener("activate", function () {
				checkbox.activate();
			});

			// propagate stopped enter key press to the editor
			checkbox.addListener("keydown", function (e) {
				if (e.getKeyIdentifier() == "Enter") {
					var clone = qx.event.Pool.getInstance().getObject(qx.event.type.KeySequence);
					var target = editor.getContentElement().getDomElement();
					clone.init(e.getNativeEvent(), target, e.getKeyIdentifier());
					clone.setType("keypress");
					qx.event.Registration.dispatchEvent(target, clone);
				}
			}, this);

			return editor;
		},


		getCellEditorValue: function (cellEditor) {
			var value = cellEditor.getChildren()[0].getValue();
			return value;
		}
	}
});
