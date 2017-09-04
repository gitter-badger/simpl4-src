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
/*
 */

qx.Class.define( "ms123.graphicaleditor.plugins.LogViewer", {
	extend: qx.ui.container.Composite,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function( config ) {
		this.base( arguments );
		this.config = config || {};
		var layout = new qx.ui.layout.Dock();
		this.setLayout( layout );
		this.add( this._createEditor( config ), {
			edge: "center"
		} );
		this.add( this._createButtons(), {
			edge: "south"
		} );

	},

	/******************************************************************************
	 EVENTS
	 ******************************************************************************/
	events: {
		"save": "qx.event.type.Data"
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {},
	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createEditor: function( config ) {
			var cm = new ms123.codemirror.CodeMirror( config );
			cm.set( {
				height: null,
				width: null
			} );
			this._cm = cm;
			this.reload();
			return cm;
		},

		reload: function( ) {
			var log = null;
			try {
				log = ms123.util.Remote.rpcSync( "log:getLastNLinesFromStdout", {
					lines: 500
				} );
				console.log( "LogViewer:" + log );
			} catch ( e ) {
				ms123.form.Dialog.alert( "LogViewer.reload:" + e.message );
				return;
			}
			this._cm.setValue( log );
			var editor = this._cm.getEditor();
			if( editor ){
				editor.scrollTo( null, 10000 );
			}
		},
		_createButtons: function() {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing( 5 );

			var buttonSave = new qx.ui.toolbar.Button( this.tr( "namespacesmanager.reload" ), "icon/22/actions/object-rotate-left.png" );
			buttonSave.addListener( "execute", function() {
				this.reload();
			}, this );
			toolbar._add( buttonSave )

			toolbar.addSpacer();
			toolbar.addSpacer();

			return toolbar;
		},
		__getResourceUrl: function( name ) {
			var am = qx.util.AliasManager.getInstance();
			return am.resolve( "resource/ms123/" + name );
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function() {}

} );
