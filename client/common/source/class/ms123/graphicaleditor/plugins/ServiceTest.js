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
 * @ignore(Hash)
 * @ignore(Clazz)
 * @ignore(jQuery)
 */

qx.Class.define( "ms123.graphicaleditor.plugins.ServiceTest", {
	extend: qx.core.Object,
	include: [ qx.locale.MTranslation ],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function( facade, main ) {
		this.base( arguments );
		this.facade = facade;
		this.serviceTestData = {};


		this.facade.offer( {
			'name': this.tr( "graphicaleditor.plugins.save.cameltest" ),
			'functionality': this.camelTest.bind( this ),
			'group': this.tr( "ge.Save.group" ),
			'icon': "icon/16/actions/go-last.png",
			'description': "Test",
			'index': 4,
			'minShape': 0,
			'maxShape': 0
		} );

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
		camelTest: function() {
			if ( this._win == null ) {
				this.init();
			}
			this._win.open();
		},
		init: function() {
			var serviceShapes = this._getServiceShapes();
			var json = this.facade.getJSON();
			var id = json.properties.overrideid.replace( /\.camel$/, "" );
			var ssMap = this._filterServiceShapes( id, serviceShapes );
			var container = new qx.ui.container.Composite( new qx.ui.layout.Dock() );
			var selectBox = this._createServiceSelector( container, ssMap );
			var win = this._createTestWindow( container );
			this._win = win;

			selectBox.addListener( "changeSelection", function( e ) {
				var key = selectBox.getSelection()[ 0 ].getModel();
				if ( key == "" ) return;
				console.log( "key:", key );
				this._createServiceTestForm( win, container, key, ssMap[ key ] );
			}, this );
			var app = qx.core.Init.getApplication();
			app.getDesktop( ms123.StoreDesc.getCurrentNamespace() ).add( win );
			container.add( this._createServiceReturnView( null ), {
				edge: "south"
			} );
		},
		_createServiceSelector: function( parent, ssMap ) {
			var container = new qx.ui.container.Composite( new qx.ui.layout.HBox() );
			container.add( new qx.ui.basic.Label( "Service:" ) );
			var selectBox = new qx.ui.form.SelectBox();
			var item = new qx.ui.form.ListItem( "", null, "" );
			selectBox.add( item );
			var keys = Object.keys( ssMap )
			for ( var i = 0; i < keys.length; i++ ) {
				var key = keys[ i ];
				var item = new qx.ui.form.ListItem( key, null, key );
				selectBox.add( item );
			}
			container.add( selectBox, {
				flex: true
			} );
			parent.add( container, {
				edge: "north"
			} );
			return selectBox;
		},
		_createServiceTestForm: function( win, parent, key, ss ) {
			console.log( "_createServiceTestForm:", ss );
			var p = ss.properties;
			var pitems = p.rpcParameter ? p.rpcParameter.items : {};
			var formData = {};
			for ( var i = 0; i < pitems.length; i++ ) {
				var pi = pitems[ i ];
				var type = this._getParamType( pi.type );
				formData[ pi.name ] = {
					type: type,
					label: pi.name + "(" + pi.type + ")",
					validation: {
						required: pi.optional === false
					},
					value: pi.defaultValue
				}
			}
			var buttons = [ {
				'label': this.tr( "form.execute" ),
				'icon': "icon/22/actions/dialog-ok.png",
				'callback': ( function( m ) {
					console.log( "callback:", m );
					this.serviceTestData[ key ] = m;
					this._callService( key, m );
				} ).bind( this ),
				'value': "execute"
			}, {
				'label': this.tr( "Cancel" ),
				'icon': "icon/22/actions/dialog-cancel.png",
				'callback': ( function() {
					win.close()
				} ).bind( this ),
				'value': "cancel"
			} ];
			var context = {};
			context.formData = formData;
			context.buttons = buttons;
			context.formLayout = [ {
				id: "tab1",
				lineheight: -1
			} ];
			if ( this._oldForm ) {
				parent.remove( this._oldForm );
			}
			var form = new ms123.widgets.Form( context );
			this._oldForm = form;
			var data = this.serviceTestData[ key ];
			console.log( "serviceTestData:", data );
			if ( data ) {
				form.setData( data );
			}

			parent.add( form, {
				edge: "center"
			} );
		},
		_createServiceReturnView: function( value ) {
			var container = new qx.ui.container.Composite( new qx.ui.layout.Dock() );

			var tab = new qx.ui.tabview.TabView().set( {
				contentPadding: 0,
				minHeight: 400
			} );
			var treePage = new qx.ui.tabview.Page( this.tr( "jsondisplay.formatted" ), "resource/ms123/view.png" ).set( {
				showCloseButton: false
			} );
			treePage.setLayout( new qx.ui.layout.Dock() );
			tab.add( treePage, {
				edge: 0
			} );
			var jsonPage = new qx.ui.tabview.Page( this.tr( "jsondisplay.plain" ), "resource/ms123/json.png" ).set( {
				showCloseButton: false
			} );
			jsonPage.setLayout( new qx.ui.layout.Grow() );
			tab.add( jsonPage, {
				edge: 0
			} );

			var embed = new qx.ui.embed.Html().set( {
				overflowY: "auto",
				overflowX: "auto"
			} );
			embed.addListenerOnce( "appear", function() {
				var el = embed.getContentElement().getDomElement();
				this._htmlDomElement = el;
				treePage.add( this.__createToolbar( el ), {
					edge: "north"
				} );
				if ( value ) {
					jQuery( el ).JSONView( qx.lang.Json.parse( value ), {
						collapsed: false
					} );
					jQuery( el ).JSONView( 'toggle', 2 );
				}
			}, this );
			treePage.add( embed, {
				edge: "center"
			} );

			var msgArea = new qx.ui.form.TextArea();
			msgArea.setFont( qx.bom.Font.fromString( "Mono, 9px" ) );
			msgArea.setValue( value );
			this._msgArea = msgArea;
			jsonPage.add( msgArea );
			container.add( tab, {
				edge: "center"
			} );

			return container;
		},
		_setResult: function( value ) {
			if ( typeof value == "string" ) {
				value = qx.lang.Json.parse( value );
			}
			jQuery( this._htmlDomElement ).JSONView( value, {
				collapsed: false
			} );
			jQuery( this._htmlDomElement ).JSONView( 'toggle', 2 );
			this._msgArea.setValue( JSON.stringify( value, null, 2 ) );
		},
		__createToolbar: function( el ) {
			var toolbar = new qx.ui.toolbar.ToolBar();
			var collapse = new qx.ui.toolbar.Button( this.tr( "jsondisplay.collapse" ), "icon/16/actions/list-remove.png" );
			collapse.addListener( "execute", function() {
				jQuery( el ).JSONView( 'collapse' );
			}, this );
			toolbar._add( collapse );

			var expand = new qx.ui.toolbar.Button( this.tr( "jsondisplay.expand" ), "icon/16/actions/list-add.png" );
			expand.addListener( "execute", function() {
				jQuery( el ).JSONView( 'expand' );
			}, this );
			toolbar._add( expand );

			var toggle = new qx.ui.toolbar.Button( this.tr( "jsondisplay.toggle" ), "icon/16/actions/object-flip-vertical.png" );
			toggle.addListener( "execute", function() {
				jQuery( el ).JSONView( 'toggle' );
			}, this );
			toolbar._add( toggle );

			var level2 = new qx.ui.toolbar.Button( this.tr( "jsondisplay.level2" ), "icon/16/actions/object-flip-vertical.png" );
			level2.addListener( "execute", function() {
				jQuery( el ).JSONView( 'toggle', 2 );
			}, this );
			toolbar._add( level2 );
			return toolbar;
		},

		_callService: function( method, params ) {
			var result;
			try {
				var ns = ms123.StoreDesc.getCurrentNamespace();
				result = ms123.util.Remote.rpcSync( "camelRoute:" + ns + "." + method, params );
			} catch ( e ) {
				ms123.form.Dialog.alert( "ServiceTest._callService:" + e );
				return null;
			}

			this._setResult( result )
		},
		_getParamType: function( type ) {
			if ( type == "string" ) {
				return "TextField"
			}
			if ( type == "string" ) {
				return "Checkbox"
			}
			if ( type == "date" ) {
				return "DateField"
			}
			if ( type == "integer" || type == "long" ) {
				return "NumberField"
			}
			if ( type == "double" ) {
				return "DecimalField"
			}
			return "TextArea"
		},
		_filterServiceShapes: function( id, serviceShapes ) {
			var ret = {};
			for ( var i = 0; i < serviceShapes.length; i++ ) {
				var ss = serviceShapes[ i ];
				var p = ss.properties;
				if ( qx.lang.String.startsWith( p.overrideid, id + ":" ) ) {
					ret[ p.urivalue_name ] = ss;
				}
			}
			return ret;
		},
		_getServiceShapes: function() {
			var procedureShapes;
			try {
				procedureShapes = ms123.util.Remote.rpcSync( "camel:getProcedureShapesForPrefix", {
					prefix: ms123.StoreDesc.getCurrentNamespace() + "/"
				} );
			} catch ( e ) {
				ms123.form.Dialog.alert( "ServiceTest._getServiceShapes:" + e );
				return null;
			}
			return procedureShapes;
		},


		_createTestWindow: function( c ) {
			var win = new qx.ui.window.Window( "ServiceTest", "" ).set( {
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			} );
			win.setLayout( new qx.ui.layout.Dock );
			win.setWidth( 700 );
			win.setHeight( 600 );
			win.setAllowMaximize( false );
			win.setAllowMinimize( false );
			win.setModal( true );
			win.setActive( true );
			win.minimize();
			win.center();

			win.add( c, {
				edge: "center"
			} );
			return win;
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
