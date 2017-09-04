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
 * @lint ignoreDeprecated(alert,confirm,eval) 
 */
qx.Class.define("ms123.codemirror.CodeMirrorUI", {
	extend: qx.ui.toolbar.ToolBar,


	/*******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (mirror, options, mirrorOptions) {
		this.base(arguments);
		this.mirror = mirror;
		this.setSpacing(5);
		this.initialize(options, mirrorOptions);
	},


	events: {},


	/*******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		initialize: function (options, mirrorOptions) {
			var defaultOptions = {
				buttons: ['undo', 'redo', 'find', 'findNext', 'replace', 'replaceAll', 'vimMode','reindent', "foldAll", "wrapLines" ]
			}
			if( options.readOnly){
				defaultOptions = {
					buttons: []
				}
			}
			this.options = options;
			this.setDefaults(this.options, defaultOptions);
			this._vimMode = ms123.config.ConfigManager.isVimMode();
			this._fold = false;
			this._wrap = true;


			this.buttonDefs = {
				'undo': [this.tr("Undo"), "undo", "icon/16/actions/edit-undo.png", this.undo],
				'redo': [this.tr("Redo"), "redo", "icon/16/actions/edit-redo.png", this.redo],
				'find': [this.tr("Find"), "find", "icon/16/actions/system-search.png", this.find],
				'findNext': [this.tr("FindNext"), "find", "resource/ms123/go-down-search.png", this.findNext],
				'replace': [this.tr("Replace"), "replace", "resource/ms123/edit-find-replace.png", this.replace],
				'replaceAll': [this.tr("ReplaceAll"), "replaceAll", "resource/ms123/replace.png", this.replaceAll],
				'vimMode': [this.tr("EditorMode"), "vimMode", "resource/ms123/vim.png", this.vimMode],
				'reindent': [this.tr("Reformat whole document"), "reindent", "icon/16/actions/format-justify-left.png", this.reindent],
				'foldAll': [this.tr("Fold/Unfold"), "foldAll", "icon/16/places/folder-open.png", this.foldAll],
				'wrapLines': [this.tr("Wrap/Nowrap lines"), "wrapLines", "icon/16/actions/object-flip-horizontal.png", this.wrapLines]
			};

			this.self = this;

			var onChange = this.editorChanged.bind(this);
			this.mirror.on("change", onChange);
			this.initButtons();

			if (this.undoButton) this.undoButton.setEnabled(false);
			if (this.redoButton) this.redoButton.setEnabled(false);
		},
		setDefaults: function (object, defaults) {
			for (var option in defaults) {
				if (!object.hasOwnProperty(option)) object[option] = defaults[option];
			}
		},
		initButtons: function () {
			for (var i = 0; i < this.options.buttons.length; i++) {
				var buttonId = this.options.buttons[i];
				var buttonDef = this.buttonDefs[buttonId];
				this.addButton(buttonDef[0], buttonDef[1], buttonDef[2], buttonDef[3], this.buttonFrame);
			}
		},
		addButton: function (name, action, image, func) {
			var button = new qx.ui.toolbar.Button("", image);
			if( action == 'vimMode'){
				this._editorModeButton = button;
				if( this._vimMode ){
					button.setIcon("resource/ms123/vim.png");
				}else{
					button.setIcon("resource/ms123/edit2.png");
				}
			}
			if( action == 'foldAll'){
				this._foldButton = button;
				if( this._fold ){
					button.setIcon("icon/16/places/folder.png");
				}else{
					button.setIcon("icon/16/places/folder-open.png");
				}
			}
			if( action == 'wrapLines'){
				this._wrapButton = button;
				if( this._wrap ){
					button.setIcon("icon/16/actions/object-flip-vertical.png");
				}else{
					button.setIcon("icon/16/actions/object-flip-horizontal.png");
				}
			}
			button.setToolTipText(name);
			button.addListener("execute", func, this);
			this.add(button);
			if (action == 'undo') {
				this.undoButton = button;
			}
			if (action == 'redo') {
				this.redoButton = button;
			}
		},
		editorChanged: function () {
			if (!this.mirror) {
				return
			}
			if( !this.undoButton) return;
			var his = this.mirror.historySize();
			if (his['undo'] > 0) {
				this.undoButton.setEnabled(true);
			} else {
				this.undoButton.setEnabled(false);
			}
			if (his['redo'] > 0) {
				this.redoButton.setEnabled(true);
			} else {
				this.redoButton.setEnabled(false);
			}
		},
		undo: function () {
			this.mirror.execCommand("undo");
		},
		redo: function () {
			this.mirror.execCommand("redo");
		},
		find: function () {
			this.mirror.execCommand("find");
		},
		findNext: function () {
			this.mirror.execCommand("findNext");
		},
		replace: function () {
			this.mirror.execCommand("replace");
		},
		replaceAll: function () {
			this.mirror.execCommand("replaceAll");
		},
		vimMode: function () {
			this._vimMode = !this._vimMode;
			this.mirror.setOption("vimMode", this._vimMode);
				if( this._vimMode ){
					this._editorModeButton.setIcon("resource/ms123/vim.png");
				}else{
					this._editorModeButton.setIcon("resource/ms123/edit2.png");
				}
		},
		reindent: function () {
			this.mirror.execCommand("indentAuto");
		},
		foldAll: function () {
			if( !this._fold ){
				this.mirror.execCommand("foldAll");
				this._foldButton.setIcon("icon/16/places/folder.png");
			}else{
				this.mirror.execCommand("unfoldAll");
				this._foldButton.setIcon("icon/16/places/folder-open.png");
			}
			this._fold = !this._fold;
		},
		wrapLines: function () {
			this._wrap = !this._wrap;
			this.mirror.setOption("lineWrapping", this._wrap);
			if( this._wrap ){
				this._wrapButton.setIcon("icon/16/actions/object-flip-vertical.png");
			}else{
				this._wrapButton.setIcon("icon/16/actions/object-flip-horizontal.png");
			}
		}
	},



	/*******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}
});
