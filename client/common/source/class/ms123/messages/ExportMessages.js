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
 * @ignore($)
 */
qx.Class.define( "ms123.messages.ExportMessages", {
	extend: qx.core.Object,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function( langs, lang1, messages1, facade ) {
		this.base( arguments );
		this._langs = langs;
		this._lang1 = lang1;
		this._messages1 = messages1;
		this._facade = facade;
		this.__storeDesc = facade.storeDesc;
		this.__createExportDialog( facade, langs );
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_getMessages: function( lang ) {
			var completed = ( function( data ) {} ).bind( this );

			var failed = ( function( details ) {
				ms123.form.Dialog.alert( this.tr( "messages.getMessages_failed" ) + ":" + details.message );
			} ).bind( this );

			try {
				var ret = ms123.util.Remote.rpcSync( "message:getMessages", {
					namespace: this._facade.storeDesc.getNamespace(),
					lang: lang
				} );
				completed.call( this, ret );
				return ret;
			} catch ( e ) {
				return [];
			}
		},
		_getMessageById: function( all, id ) {
			for ( var c = 0; c < all.length; c++ ) {
				if ( all[ c ].msgid == id ) {
					return all[ c ];
				}
			}
			return {
				msgid: id,
				msgstr: ""
			};
		},
		_export: function( langs ) {
			var map = {};
			console.log( "langs:", langs );
			map[ this._lang1 ] = this._messages1;
			for ( var i = 0; i < langs.length; i++ ) {
				if ( this._lang1 == langs[ i ] ) {
					continue;
				}
				var recs = this._getMessages( langs[ i ] );
				var resultNew = [];
				for ( var j = 0; j < this._messages1.length; j++ ) {
					var ret = this._getMessageById( recs, this._messages1[ j ].msgid );
					resultNew.push( ret );
				}
				map[ langs[ i ] ] = resultNew;
			}
			console.log( "map:", map );
			var keys = Object.keys( map );
			keys.splice( 0, 0, "msgid" );
			var rows = [];
			rows.push( keys );

			for ( var i = 0; i < this._messages1.length; i++ ) {
				var row = [];
				row.push( this._messages1[ i ].msgid );
				row.push( this._messages1[ i ].msgstr );
				for ( var j = 2; j < keys.length; j++ ) {
					var msg = map[ keys[ j ] ][ i ].msgstr;
					row.push( msg );
				}
				rows.push( row );
			}

			console.log( "rows:", rows );
			this.exportToCsv( 'export.csv', rows );
		},
		exportToCsv: function( filename, rows ) {
			var processRow = function( row ) {
				var finalVal = '';
				for ( var j = 0; j < row.length; j++ ) {
					var innerValue = row[ j ] === null ? '' : row[ j ].toString();
					if ( row[ j ] instanceof Date ) {
						innerValue = row[ j ].toLocaleString();
					};
					var result = innerValue.replace( /"/g, '""' );
					if ( result.search( /("|,|\n)/g ) >= 0 )
						result = '"' + result + '"';
					if ( j > 0 )
						finalVal += ',';
					finalVal += result;
				}
				return finalVal + '\n';
			};

			var csvFile = '';
			for ( var i = 0; i < rows.length; i++ ) {
				csvFile += processRow( rows[ i ] );
			}

			var blob = new Blob( [ csvFile ], {
				type: 'text/csv;charset=utf-8;'
			} );
			if ( navigator.msSaveBlob ) { // IE 10+
				navigator.msSaveBlob( blob, filename );
			} else {
				var link = document.createElement( "a" );
				if ( link.download !== undefined ) { // feature detection
					// Browsers that support HTML5 download attribute
					var url = URL.createObjectURL( blob );
					link.setAttribute( "href", url );
					link.setAttribute( "download", filename );
					link.style.visibility = 'hidden';
					document.body.appendChild( link );
					link.click();
					document.body.removeChild( link );
				}
			}
		},
		__createExportDialog: function( facade, langs ) {
			console.log( "__createExportDialog:", langs );
			var opts = [];
			for ( var i = 0; i < langs.length; i++ ) {
				if ( langs[ i ] == this._lang1 ) {
					continue;
				}
				if ( langs[ i ].endsWith( '-' ) ) {
					continue;
				}
				var opt = {
					value: langs[ i ].name,
					label: langs[ i ].name
				};
				opts.push( opt );
			}
			console.log( "__createExportDialog:", opts );
			var formData = {
				"languages": {
					name: "languages",
					header: "%messages.lang.options",
					type: "DoubleSelectBox",
					options: opts
				}
			};

			var self = this;
			var form = new ms123.form.Form( {
				"formData": formData,
				"allowCancel": true,
				"inWindow": true,
				"callback": function( m ) {
					if ( m !== undefined ) {
						var val = m.get( "languages" );
						console.log( "val:", val );
						this._export( val );
					}
				},
				"context": self
			} );
			form.show();
		}
	}
} );
