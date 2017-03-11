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
			console.log( "entities:", cols );
			var fields = [];
			this.currentBounds = {
				lowerRight: {
					"x": 180,
					"y":54 
				},
				upperLeft: {
					x: 30,
					y: 30
				}
			}
	 		var shapes = this.facade.getSelection();
			console.log("sel1:",shapes);
			shapes = this.facade.edit.getAllShapesToConsider(shapes)
			console.log("sel2:",shapes);
				console.log("cols:",cols);
			for ( var i = 0; i < cols.length; i++ ) {
				if( this._hasField( shapes, cols[i].name )){
					continue;
				}
				var f = this._createField( cols[ i ], msgkeyPrefix );
				if ( f ) {
					fields.push( f );
				}
			}
			console.log( "fields:", JSON.stringify( fields, null, 2 ) );

			this.facade.edit.editPaste( fields );
		},
		_hasField: function(shapes, name){
			var found = false;
			shapes.each(qx.lang.Function.bind(function (shape) {
				var n = shape.properties["oryx-xf_id"];
				if( n == name) found = true;
			}, this));
			return found;
		},
		_createField: function( col, msgkeyPrefix ) {
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
			if( col.hidden===true){
				return null;
			}
			var edittype = col.edittype.toLowerCase();
			if ( edittype == 'text' || edittype == 'textarea' || edittype == 'checkbox' || edittype=='select') {
				props.xf_type = this.getType(col.datatype);
				props.xf_id = col.id || col.name;
				field.stencil.id = 'Input';
				if ( edittype == 'checkbox' ) {
					props.xf_type = 'boolean';
				}
				if ( edittype == 'datetime' ) {
					props.xf_type = 'datetime';
				}
				if ( edittype == 'textarea' ) {
					props.xf_type = 'string';
					field.stencil.id = 'TextArea';
				}
				if ( edittype == 'select' ) {
					props.xf_type = 'string';
					field.stencil.id = 'Enumselect';
					if( col.selectable_items){
						try{
							props.xf_enum = JSON.parse(col.selectable_items._url);
						}catch(e){
						}
						console.log("items:",props.xf_enum);
					}
				}
				field.childShapes[ 0 ].properties.xf_text = msgkeyPrefix + props.xf_id;
				field.childShapes[ 0 ].resourceId = ms123.util.IdGen.id();
				field.resourceId = ms123.util.IdGen.id();
				jQuery.extend( true, field.bounds, this.currentBounds );
				this.currentBounds.lowerRight.y += 40;
				this.currentBounds.upperLeft.y += 40;

				return field;
			}
			return null;
		},
		getType:function(dt){
			if( dt == "string"){
				return "text";
			}
			return dt;
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
