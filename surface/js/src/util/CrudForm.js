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
clazz.construct.extend( "simpl4.util.CrudForm", {
	/******************************************************************************
	 Static
	 ******************************************************************************/
}, {
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	init: function( namespace, entityname, fields, props ) {
		this.entityname = entityname;
		this.namespace = namespace;
		var tabView = this.getTabview( props.formlayout, entityname );
		this._fields = fields;
		this._form = {};
		var tabLists = this.getTabLists( this._fields );
		var tabKeys = Object.keys( tabLists );
		tabKeys.forEach( function( tabKey ) {
			var tabviewPage = this.getTabViewPage( tabView, tabKey );
			tabviewPage.childShapes = [];
			var layout = tabviewPage.layout || "single";
			var row = null;
			var prevF=null;
			tabLists[ tabKey ].forEach( function( f, i ) {
				if ( layout== "full" || layout == "single" ) {
					row = {
						id: "Row",
						childShapes: []
					}
					tabviewPage.childShapes.push( row );
				}
				var isBreak = f.edittype == "textarea" || (prevF && prevF.edittype=="textarea");
				if ( (layout == "double" || layout == "double.mobile") && (isBreak || ( i == 0 || ( i % 2 ) == 0 ) ) ) {
					row = {
						id: "Row",
						childShapes: []
					}
					tabviewPage.childShapes.push( row );
				}
				row.childShapes.push( this.getFieldShape( f ) );
				prevF = f
			}, this );
		}, this );
		var p = "data";
		var e = entityname;
		if( entityname.indexOf(":") >=0){
			p = entityname.split(":")[0];
			e = entityname.split(":")[1];
		}
		var h1 = p+ "." + e + "." + tabView.childShapes[ 0 ].xf_id;
		if ( tabView.childShapes.length == 1 && h1 == tr( h1 ) ) {
			this._form = tabView.childShapes[ 0 ];
			this._form.id = "Form";
		} else {
			this._form = tabView;
		}
	},
	getFieldShape: function( f ) {
		var shape = {};
		if( !isNaN( f.datatype)){ //OrientDB
		  shape.id = 	this.convertOrientId(f.datatype);
			shape.xf_id = f.name;
		  shape.xf_type = 	this.convertOrientType(f.datatype);
			shape.xf_default = f.default_value !== '' ? f.default_value : null;
			var p = "odata";
			var e = this.entityname;
			if( this.entityname.indexOf(":") >=0){
				p = this.entityname.split(":")[0];
				e = this.entityname.split(":")[1];
			}
			shape.label = tr( p+"." + e + "." + f.name );
			shape.xf_namespace = this.namespace;
			console.log("OrientDB:",shape);
			return shape;
		}
		shape.id = "Input";
		if ( f.edittype == "select" ) {
			shape.id = "Enumselect";
			shape.items = f.selectable_items.getItems();
		}
		if ( f.edittype == "textarea" ) {
			shape.id = "Textarea";
			shape.xf_rows = f.editoptions_rows;
		}
		if ( f.datatype == "binary" ) {
			shape.id = "Upload";
		}
		shape.xf_id = f.name;
		shape.xf_type = this.convertFieldType( f );
		var p = "data";
		var e = this.entityname;
		if( this.entityname.indexOf(":") >=0){
			p = this.entityname.split(":")[0];
			e = this.entityname.split(":")[1];
		}
		shape.label = tr( p+"." + e + "." + f.name );
		if ( f.constraints ) {
			var c = JSON.parse( f.constraints );
			shape.regulaConstraints = simpl4.util.FormManager.constructRegulaConstraints( c );
		}

		if ( f.datatype && ( f.datatype.match( /^relatedto/ ) ) ) {
			var s = f.datatype.split( "/" );
			shape.id = "RelatedTo";
			shape.xf_type = s[ 2 ];
			shape.xf_namespace = this.namespace;
		}

		if( f.form_enabled_expr!=null ){
			shape.xf_enabled = f.form_enabled_expr+'';
		}
		shape.xf_default = f.default_value !== '' ? f.default_value : null;
		return shape;
	},
	convertFieldType: function( f ) {
		var retType = null;
		if ( f.datatype && f.datatype == 'date' ) {
			retType = f.edittype == "text" ? "date" : f.edittype;
		} else if ( f.datatype && ( f.datatype == 'decimal' || f.datatype == 'double' ) ) {
			retType = "double";
		} else if ( f.datatype && ( f.datatype == 'number' || f.datatype == 'integer' || f.datatype == 'long' ) ) {
			retType = "integer";
		}
		if ( retType === "boolean" ) {
			retType = "integer";
		}
		return retType;
	},
	getTabViewPage: function( tabView, tabKey ) {
		var childShapes = tabView.childShapes;
		var ret = null;
		childShapes.forEach( function( c ) {
			if ( c.xf_id == tabKey ) {
				ret = c;
				return;
			}
		}, this );
		return ret;
	},
	getTabLists: function( fields ) {
		var ret = {};
		fields.forEach( function( f ) {
			if ( f.id == "id" ) return;
			var tab = "tab1";
			if ( f.formoptions && f.formoptions.tab ) {
				tab = f.formoptions.tab;
			}
			var list = ret[ tab ];
			if ( list === undefined ) {
				list = [];
				ret[ tab ] = list;
			}
			list.push( f );
		}, this );
		return ret;
	},
	getTabview: function( formLayout, entityname ) {
		var defLayout = {
			"id": "Tabview",
			"childShapes": [ {
				"xf_id": "tab1",
				"id": "Page",
				"childShapes": []
			} ]
		};
		if ( formLayout == null ) {
			return defLayout;
		} else {
			try {
				var lays = formLayout.split( ";" );
				var formLayout = {
					id: "Tabview",
					childShapes: []
				};
				var childShapes = formLayout.childShapes;
				for ( var i = 0; i < lays.length; i++ ) {
					var t = lays[ i ].split( ":" );
					var page = {};
					if ( t.length == 2 ) {
						page.xf_id = t[ 0 ];
						page.layout = t[ 1 ];
					}
					if ( t.length == 1 ) {
						page.xf_id = t[ 0 ];
						page.layout = "single";
					}
					page.id = "Page";
					var p = "data";
					var e = entityname;
					if( entityname.indexOf(":") >=0){
						p = entityname.split(":")[0];
						e = entityname.split(":")[1];
					}
					page.title = tr( p+ "." + e + "." + page.xf_id );
					childShapes.push( page );
				}
			} catch ( e ) {
				console.log( "catch:" + e );
				return defLayout;
			}
			return formLayout;
		}
	},
	convertOrientId:function(type){
	 if( type == "17") type = "Input";
   if( type == "5") type = "Input";
   if( type == "1") type = "Input";
   if( type == "3") type = "Input";
   if( type == "7") type = "Input";
   if( type == "0") type = "Input";
   if( type == "21") type = "Input";
   if( type == "19") type = "Input";
   if( type == "6") type = "Input";
   if( type == "9") type = "Form";
   if( type == "10") type = "Crud";
   if( type == "11") type = "Crud";
   if( type == "12") type = "Crud";
   if( type == "13") type = "Form";
   if( type == "14") type = "Crud";
   if( type == "15") type = "Crud";
   if( type == "16") type = "Crud";
	 return type;
	},
	convertOrientType:function(type){
	 if( type == "17") type = "integer";
   if( type == "5") type = "double";
   if( type == "1") type = "integer";
   if( type == "3") type = "integer";
   if( type == "7") type = "string";
   if( type == "0") type = "boolean";
   if( type == "21") type = "double";
   if( type == "19") type = "date";
   if( type == "6") type = "datetime";
   if( type == "9") type = "Form";
   if( type == "10") type = "Crud";
   if( type == "11") type = "Crud";
   if( type == "12") type = "Crud";
   if( type == "13") type = "Form";
   if( type == "14") type = "Crud";
   if( type == "15") type = "Crud";
   if( type == "16") type = "Crud";
	 return type;
	},
	getSpec: function() {
		return this._form;
	},
	toString: function() {
		return "CrudForm:" + this._fields;
	}

} );
