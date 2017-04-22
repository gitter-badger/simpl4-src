/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 */
/**
 * @ignore(JSON5.*)
 * @ignore(JSONEditor.*)
 */
qx.Class.define( "ms123.settings.ConfigEdit", {
	extend: qx.ui.container.Composite,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function( facade ) {
		this.base( arguments );
		this._facade = facade;
		this._model = this._facade.model;
		this._resourceid = this._model.getId();
		console.log( "resourceid:", this._resourceid );
		this.__init();
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		__init: function() {
			this.setLayout( new qx.ui.layout.Dock() );

			var toolbar = this._createToolbar();
			this.add( toolbar, {
				edge: "south"
			} );
			this._load();
		},
		_load: function() {
			try {
				var data = ms123.util.Remote.rpcSync( "setting:getResourceSetting", {
					namespace: this._facade.storeDesc.getNamespace(),
					settingsid: this._facade.settingsid,
					resourceid: this._resourceid
				} );
				if ( data ) {
					this._schemaname = data[ ms123.settings.Config.SETTINGS_SCHEMANAME ];
					var schemaString = ms123.util.Remote.rpcSync( "git:searchContent", {
						reponame: "global",//this._facade.storeDesc.getNamespace(),
						name: this._schemaname,
						type: "sw.schema"
					} );
					console.log( "Data:", data[ ms123.settings.Config.SETTINGS_DATA ] );
					console.log( "Schema:", schemaString );
					this._initForm( schemaString, data[ ms123.settings.Config.SETTINGS_DATA ] );
				} else {
					ms123.form.Dialog.alert( "settings.ConfigEdit._load:no schema found" );
				}
			} catch ( e ) {
				ms123.form.Dialog.alert( "settings.ConfigEdit._load:" + e );
			}
		},
		_initForm: function( schemaString, formData ) {
			try {
				var schema = JSON5.parse( schemaString );
				this._schema = schema;
				var form = null;
				var init = null;
				if( schema.type ){
					form = schema;
				}else{
					form = this._translate( schema[ "form" ] );
					init = schema[ "init" ];
				}
				if ( init && init.method ) {
					init.parameter = init.parameter || {};
					init.parameter.form = form;
					var params = {
						service: "simpl4",
						method: init.method,
						parameter: init.parameter,
						async: true,
						context: this,
						failed: function( e ) {
							console.error( "settings.ConfigEdit._initForm:", e );
							ms123.form.Dialog.alert( "settings.ConfigEdit._initForm:" + e );
						},
						completed: ( function( _form ) {
							console.log( "form_:", _form );
							if ( _form ) {
								this._createJsonEditor( _form, formData );
							}
						} ).bind( this )
					}
					return ms123.util.Remote.rpcAsync( params );
				} else {
					this._createJsonEditor( form, formData );
				}
			} catch ( e ) {
				console.error( "settings.ConfigEdit._initForm2:", e );
				ms123.form.Dialog.alert( "settings.ConfigEdit._initForm2:" + e );
			}
		},
		_saveForm: function( formData ) {
			try {
				var schema = this._schema;
				var form = null;
				var save = null;
				if( schema.type ){
					form = schema;
				}else{
					form = this._translate( schema[ "form" ] );
					save = schema[ "save" ];
				}
				if ( save && save.method) {
					save.parameter = save.parameter || {};
					save.parameter.formData = formData;
					var params = {
						service: "simpl4",
						method: save.method,
						parameter: save.parameter,
						async: true,
						context: this,
						failed: function( e ) {
							console.error( "settings.ConfigEdit._saveForm:", e );
							ms123.form.Dialog.alert( "settings.ConfigEdit._saveForm:" + e );
						},
						completed: ( function( _data ) {
							console.log( "formData:", _data );
							this._setResourceSetting( formData );
						} ).bind( this )
					}
					return ms123.util.Remote.rpcAsync( params );
				} else {
					this._setResourceSetting( formData );
				}
			} catch ( e ) {
				console.error( "settings.ConfigEdit._saveForm2:", e );
				ms123.form.Dialog.alert( "settings.ConfigEdit._saveForm2:" + e );
			}
		},
		_save: function() {
			var errors = this._editor.validate();
			if ( errors.length ) {
				console.log( "errors:", errors );
				var txt = "";
				for ( var i = 0; i < errors.length; i++ ) {
					var e = errors[ i ];
					txt += e.path + ":" + e.message + "<br/>";
				}
				ms123.form.Dialog.alert( this.tr( "settings.not_valid" ) + "<br/>" + txt );
				return;
			}
			var data = this._editor.getValue();
			console.log( "Data:", data );
			this._saveForm( data );
		},
		_setResourceSetting: function( data ) {
			try {
				var settings = {};
				settings[ ms123.settings.Config.SETTINGS_DATA ] = data;
				settings[ ms123.settings.Config.SETTINGS_SCHEMANAME ] = this._schemaname;
				ms123.util.Remote.rpcSync( "setting:setResourceSetting", {
					namespace: this._facade.storeDesc.getNamespace(),
					settingsid: this._facade.settingsid,
					resourceid: this._resourceid,
					settings: settings
				} );
				ms123.form.Dialog.alert( this.tr( "settings.properties_saved" ) );
				ms123.config.ConfigManager.clearCache();
			} catch ( e ) {
				ms123.form.Dialog.alert( "settings._setResourceSetting:" + e );
			}
		},
		_translate: function( o ) {
			if ( typeof o == "string" ) {
				if ( o.match( /^%/ ) ) {
					var tr = this.tr( o.substring( 1 ) ).toString();
					if ( tr ) {
						o = tr;
					}
				}
				return o;
			}
			for ( var i in o ) {
				if ( typeof o[ i ] == "function" ) continue;
				o[ i ] = this._translate( o[ i ] );
			}
			return o;
		},
		_createJsonEditor: function( schema, formData ) {
			var widget = new qx.ui.embed.Html( "<div></div>" ).set( {
				overflowY: "auto",
				overflowX: "auto"
			} );
			this.add( widget, {
				edge: "center"
			} );
			widget.addListenerOnce( "appear", function() {
				var c = widget.getContentElement().getDomElement();
				JSONEditor.plugins.selectize.enable = true;
				this._editor = new JSONEditor( c, {
					theme: "html",
					disable_properties:true,
					disable_edit_json:true,
					disable_collapse:true,
					disable_array_delete_last_row:true,
					disable_array_delete_all_rows:true,
					no_additional_properties:true,
					required_by_default:true,
					iconlib: "fontawesome4",
					schema: schema
				} );
				this._editor.setValue( formData );
			}, this );
		},
		_createToolbar: function() {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing( 5 );
			toolbar.addSpacer();
			var buttonSave = new qx.ui.toolbar.Button( this.tr( "meta.lists.savebutton" ), "icon/16/actions/document-save.png" );
			buttonSave.setToolTipText( this.tr( "meta.lists.fs.save" ) );
			buttonSave.addListener( "execute", function() {
				this._save();
			}, this );
			toolbar._add( buttonSave );
			return toolbar;
		}
	}
} );
