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
qx.Class.define('ms123.ruleseditor.HeaderCell', {
	extend: qx.ui.container.Composite,

	construct: function () {
		this.base(arguments);

		var layout = new qx.ui.layout.Grid();
		layout.setRowFlex(0, 1);
		layout.setColumnFlex(0, 1);
		layout.setRowFlex(1, 1);
		this.setLayout(layout);
//		this.setBackgroundColor("white");
	},

	properties: {
		appearance: {
			refine: true,
			init: "table-header-cell"
		},

		label: {
			check: "String",
			init: null,
			nullable: true,
			apply: "_applyLabel"
		},

		sortIcon: {
			check: "String",
			init: null,
			nullable: true,
			apply: "_applySortIcon",
			themeable: true
		},

		icon: {
			check: "String",
			init: null,
			nullable: true,
			apply: "_applyIcon"
		}
	},

	members: {
		// property apply
		_applyLabel: function (value, old) {
			this._showChildControl("label1").setValue(value["header1"]);
			this._showChildControl("label2").setValue(value["header2"]);
			this._showChildControl("label3").setValue(value["header3"]);
			this._showChildControl("label4").setValue(value["header4"]);
		},

		_applySortIcon: function (value, old) {
		},

		_applyIcon: function (value, old) {
			if (value) {
				this._showChildControl("icon").setSource(value);
			} else {
				this._excludeChildControl("icon");
			}
		},

		// overridden
		_createChildControlImpl: function (id, hash) {
			var control;

			var font=(id == "label1") ? qx.bom.Font.fromString("11px sans-serif") : qx.bom.Font.fromString("9px sans-serif");
			var color=(id == "label4") ? "blue" : "black";
			switch (id) {
			case "label1":
			case "label2":
			case "label3":
			case "label4":
				control = new qx.ui.basic.Label().set({
					font:font,
					textColor:color,
					textAlign:"center",
					allowGrowX: true
				});

				var row = 0;
				if( id == "label2") row = 1;
				if( id == "label3") row = 2;
				if( id == "label4") row = 3;
				this._add(control, {
					row: row,
					column: 0
				});
				break;

			case "icon":
				control = new qx.ui.basic.Image(this.getIcon()).set({
					anonymous: true,
					allowShrinkX: true
				});
				this._add(control, {
					row: 0,
					column: 0
				});
				break;
			}

			return control || this.base(arguments, id);
		}
	}
});
