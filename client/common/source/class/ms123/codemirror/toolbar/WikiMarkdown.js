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
 */
qx.Class.define("ms123.codemirror.toolbar.WikiMarkdown", {
	extend: qx.core.Object,
	include: [qx.locale.MTranslation],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (toolbar, mirror, context) {
		this.base(arguments);
console.error("WikiMarkdown");
		this._toolbar = toolbar;
		this._mirror = mirror;
		this._context = context;
		this._initialize();
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_initialize: function (options, mirrorOptions) {
			this._buttonDefs = {
//				'help': ["Help", "help", "icon/16/actions/help-about.png", this._help.bind(this)],
				'table1': [this.tr("wikimarkdown.table"), "table", "resource/ms123/table.png", this._table.bind(this)],
				'table2': [this.tr("wikimarkdown.table_with_vars"), "table", "resource/ms123/table2.png", this._tableWithVars.bind(this)],
				'table3': [this.tr("wikimarkdown.table_with_loop"), "table", "resource/ms123/insert-table.png", this._tableWithLoop.bind(this)]
			};

			this.self = this;

			var onChange = this._editorChanged.bind(this);
			this._mirror.on("change", onChange);
			this._initButtons();

		},
		_initButtons: function () {
			this._toolbar.setSpacing(5);
				this._toolbar.addSeparator();
			var keys = Object.keys(this._buttonDefs);
			for (var i = 0; i < keys.length; i++) {
				var buttonDef = this._buttonDefs[keys[i]];
				this._toolbar.addButton(buttonDef[0], buttonDef[1], buttonDef[2], buttonDef[3]);
			}
		},
		_editorChanged: function () {
			console.log("WikiMarkdown,.editorChanged:" + this._mirror);
			if (!this.mirror) {
				return
			}
		},
		_table: function () {
			var cm = this._mirror;
			var t='(% frame="none" font="big"%)\n'+
					'|=Header text1 |=Header text2 |= Header text3\n'+
					'| Example11 | Example12 | Example13\n'+
					'| Example21 | Example22 | Example23\n'+
					'| Example31 | Example32 | Example33\n'
			if (cm.somethingSelected()) {
				cm.replaceSelection(t);
			} else {
				cm.replaceRange(t, cm.getCursor());
			}
			console.log("WikiMarkdown:_table:"+this);
		},
		_tableWithVars: function () {
			var cm = this._mirror;
			var t = '<% r = record %>\n'+
					'(% frame="none" font="big" %)\n'+
					'| r.field1 | r.field2\n'+
					'|(%colspan=2%)r.field3\n'+
					'|(%colspan=2%)r.field4\n'
			if (cm.somethingSelected()) {
				cm.replaceSelection(t);
			} else {
				cm.replaceRange(t, cm.getCursor());
			}
			console.log("WikiMarkdown:_tableWithVars:"+this);
		},
		_tableWithLoop: function () {
			var cm = this._mirror;
			var t = '<% l = list %>\n' +
					'(% frame="none" font="big" %)\n' +
					'|=Header1\n' +
					' |=Header2\n' +
					' |=Header3<% l.each{rec -> %>\n' +
					'|$rec.field1\n' +
					' |$rec.field2\n' +
					' |$rec.field3<%}%>\n';

			if (cm.somethingSelected()) {
				cm.replaceSelection(t);
			} else {
				cm.replaceRange(t, cm.getCursor());
			}
			console.log("WikiMarkdown:_tableWithLoop:"+this);
		},
		_help: function () {
			console.log("WikiMarkdown:_help:"+this);
			
		}
	}
});
