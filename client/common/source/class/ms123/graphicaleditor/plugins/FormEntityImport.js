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
/**
 * @ignore(Hash)
 * @ignore(Clazz)
 * @ignore(jQuery.*)
 */

qx.Class.define( "ms123.graphicaleditor.plugins.FormEntityImport", {
	extend: qx.core.Object,
	include: [ qx.locale.MTranslation ],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function( facade, main ) {
		this.base( arguments );
		this.facade = facade;
		this.ns = ms123.StoreDesc.getCurrentNamespace();
		this.serviceTestData = {};


		this.facade.offer( {
			'name': this.tr( "graphicaleditor.plugins.form.entityimport" ),
			'functionality': this.entityImport.bind( this ),
			'group': this.tr( "ge.Save.group" ),
			'icon': "icon/16/actions/insert-text.png",
			'description': "Import",
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
		entityImport: function() {
			this._createDialog();
		},
		_createDialog: function() {
			var formData = {
				"entityname": {
					'type': "resourceselector",
					'config': {
						'editable': false,
						'type': 'sw.entitytype'
					},
					'validation': {
						required: true
					},
					'label': this.tr( "entitytypes.create_class.name" ),
					'value': null
				},
				"messagekey_prefix": {
					'type': "TextField",
					'label': this.tr( "graphicaleditor.plugins.form.msgkey_prefix" ),
					'validation': {
						required: false,
						filter: /[A-Za-z0-9_.@]/
					},
					'value': "@"
				}
			};

			var form = new ms123.form.Form( {
				"formData": formData,
				"allowCancel": true,
				"inWindow": true,
				"callback": function( m ) {
					if ( m !== undefined ) {
						console.log( "m:", m );
						var entityName = m.get( "entityname" );
						var msgkeyPrefix = m.get( "messagekey_prefix" );
						this._createFields( entityName, msgkeyPrefix );
					}
				},
				"context": this
			} );
			form.show();
		},
		_createFields: function( entityName, msgkeyPrefix ) {
			var cm = new ms123.config.ConfigManager();
			var model = cm.getEntityModel( entityName, this.facade.storeDesc, "main-form" );
			var cols = model.attr( "colModel" );
			var fields = [];
			this.currentBounds = {
				lowerRight: {
					"x": 180,
					"y": 54
				},
				upperLeft: {
					x: 30,
					y: 30
				}
			}
			var shapes = this.getShapes();
			for ( var i = 0; i < cols.length; i++ ) {
				if ( this._hasField( shapes[ 0 ], cols[ i ].name ) ) {
					continue;
				}
				var f = this._createField( ( i % 2 ) == 0, cols[ i ], entityName, msgkeyPrefix );
				if ( f ) {
					fields.push( f );
				}
			}
			//console.log( "fields:", JSON.stringify( fields, null, 2 ) );

			this.facade.edit.editPaste( fields );
		},
		_hasField: function( shape, name ) {
			if( !shape) return false;
			var n = shape.properties[ "xf_id" ];
			if ( n == name ) {
				return true;
			}
			var childShapes = shape.childShapes;
			for ( var i = 0; i < childShapes.length; i++ ) {
				var ret = this._hasField( childShapes[ i ], name );
				if ( ret === true ) return true;
			}
			return false;
		},
		_createField: function( even, col, entityName, msgkeyPrefix ) {
			var field = {
				stencil: {},
				resourceId: "xxx",
				properties: {},
				childShapes: [ {
					properties: {},
					resourceId: "xxx",
					stencil: {
						id: 'Label'
					},
					childShapes: [],
					outgoing: [],
					bounds: {
						lowerRight: {
							x: 100,
							y: -1
						},
						upperLeft: {
							x: 0,
							y: -21
						}
					}
				} ],
				outgoing: [],
				bounds: {
					lowerRight: {
						x: 180,
						y: 54
					},
					upperLeft: {
						x: 30,
						y: 30
					}
				}
			};
			var props = field.properties;
			if ( col.hidden === true ) {
				return null;
			}
			var edittype = col.edittype.toLowerCase();
			var isOrient = !isNaN( col.datatype );
			if ( isOrient || edittype == 'text' || edittype == 'textarea' || edittype == 'checkbox' || edittype == 'select' ) {
				var datatype = col.datatype;
				if ( isOrient ) {
					datatype = this.convertOrientType( col.datatype );
					if ( datatype.startsWith( "stencil:" ) ) {
						field.stencil.id = datatype.substring( 8 );
						props.xf_module = "odata:" + col.linkedclass;
					}
				}
				props.xf_type = this.getType( datatype );
				console.log( "Name(" + col.name + "):", props.xf_type );
				props.xf_id = col.id || col.name;
				if ( field.stencil.id == null ) {
					field.stencil.id = 'Input';
				}
				if ( edittype == 'checkbox' ) {
					props.xf_type = 'boolean';
				}
				if ( edittype == 'datetime' ) {
					props.xf_type = 'datetime';
				}
				if ( edittype == 'textarea' ) {
					props.xf_type = 'text';
					field.stencil.id = 'TextArea';
				}
				if ( edittype == 'select' ) {
					props.xf_type = 'text';
					field.stencil.id = 'Enumselect';
					if ( col.selectable_items ) {
						try {
							props.xf_enum = JSON.parse( col.selectable_items._url );
						} catch ( e ) {}
						console.log( "items:", props.xf_enum );
					}
				}
				props.xf_default = col.default_value !== '' ? col.default_value : null;
				if ( msgkeyPrefix == "@" ) {
					var p = ms123.settings.Config.getPackName( entityName );
					var e = ms123.settings.Config.getEntityName( entityName );
					field.childShapes[ 0 ].properties.xf_text = "@" + p + "." + e + "." + props.xf_id;
				} else {
					field.childShapes[ 0 ].properties.xf_text = msgkeyPrefix + props.xf_id;
				}
				field.childShapes[ 0 ].resourceId = ms123.util.IdGen.id();
				field.resourceId = ms123.util.IdGen.id();
				jQuery.extend( true, field.bounds, this.currentBounds );
				if ( !even ) {
					field.bounds.lowerRight.x += 200;
					field.bounds.upperLeft.x += 200;
				} else {
					this.currentBounds.lowerRight.y += 50;
					this.currentBounds.upperLeft.y += 50;
				}

				return field;
			}
			return null;
		},
		getShapes: function( ) {
			var shape = this.facade.getJSON();
			return shape.childShapes;
		},
		getType: function( dt ) {
			if ( dt == "string" ) {
				return "text";
			}
			return dt;
		},
		convertOrientType: function( type ) {
			if ( type == "17" ) type = "integer";
			if ( type == "5" ) type = "double";
			if ( type == "1" ) type = "integer";
			if ( type == "3" ) type = "integer";
			if ( type == "7" ) type = "text";
			if ( type == "0" ) type = "boolean";
			if ( type == "21" ) type = "double";
			if ( type == "19" ) type = "date";
			if ( type == "6" ) type = "datetime";
			if ( type == "9" ) type = "stencil:EmbeddedObj";
			if ( type == "10" ) type = "stencil:EmbeddedList";
			if ( type == "11" ) type = "stencil:EmbeddedList";
			if ( type == "12" ) type = "string";
			if ( type == "13" ) type = "stencil:LinkedObj";
			if ( type == "14" ) type = "stencil:LinkedList";
			if ( type == "15" ) type = "stencil:LinkedList";
			if ( type == "16" ) type = "string";
			return type;
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
