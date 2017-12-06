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
qx.Class.define( "ms123.messages.ImportMessages", {
	extend: qx.core.Object,
	include: qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function( facade ) {
		this.base( arguments );
		this._facade = facade;
		this.__storeDesc = facade.storeDesc;
		this.__user = ms123.config.ConfigManager.getUserProperties();
		this.__createImportDialog( facade );
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_importFile: function( text ) {
			console.log( text );
			var arr = this.csvToArray(text);
			console.log( arr );
			this._convertToMessageFormat( arr );
		},
		_convertToMessageFormat:function(arr){
			var langs = [];
			for( var i=1; i < arr[0].length; i++){
				langs.push( arr[0][i] );
			}
			var msgMap = {};
			for( var j=0; j < langs.length;j++){
				msgMap[langs[j]] = [];
			}	
			console.log("langs:",langs);
			for( var i=1; i < arr.length-1;i++){
				var row = arr[i];
				var id = row[0];
				for( var j=0; j < langs.length;j++){
					var msgs = msgMap[langs[j]];
					msgs.push( {msgid:id, msgstr:row[j+1] });
				}	
			}
			console.log("map:",msgMap);
			for( var j=0; j < langs.length;j++){
				var lang = langs[j];
				this._saveMessages( msgMap[lang], lang);
			}	
		},
		_saveMessages: function (data,lang) {
			var completed = (function (data) {
				ms123.form.Dialog.alert(this.tr("messages.messages_saved"));
			}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("messages.saveMessages_failed") + ":" + details.message);
			}).bind(this);

			try {
				var storeId = this._facade.storeDesc.getStoreId();
				var ret = ms123.util.Remote.rpcSync("message:addMessages", {
					namespace: this._facade.storeDesc.getNamespace(),
					overwrite: true,
					lang: lang,
					msgs: data
				});
				completed.call(this, ret);
			} catch (e) {
				failed.call(this, e);
				return;
			}
		},
		csvToArray: function( strData, strDelimiter ) {
			// Check to see if the delimiter is defined. If not,then default to comma.
			strDelimiter = ( strDelimiter || "," );

			var objPattern = new RegExp(
				(
					// Delimiters.
					"(\\" + strDelimiter + "|\\r?\\n|\\r|^)" +

					// Quoted fields.
					"(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|" +

					// Standard fields.
					"([^\\" + strDelimiter + "\\r\\n]*))"
				),
				"gi"
			);

			var arrData = [
				[]
			];

			var arrMatches = null;

			while ( arrMatches = objPattern.exec( strData ) ) {
				var strMatchedDelimiter = arrMatches[ 1 ];

				// Check to see if the given delimiter has a length
				// (is not the start of string) and if it matches
				// field delimiter. If id does not, then we know
				// that this delimiter is a row delimiter.
				if (
					strMatchedDelimiter.length && strMatchedDelimiter !== strDelimiter
				) {
					// Since we have reached a new row of data,add an empty row to our data array.
					arrData.push( [] );
				}
				var strMatchedValue;

				// Now that we have our delimiter out of the way,let's check to see which kind of value we captured (quoted or unquoted).
				if ( arrMatches[ 2 ] ) {

					// We found a quoted value. When we capture this value, unescape any double quotes.
					strMatchedValue = arrMatches[ 2 ].replace(
						new RegExp( "\"\"", "g" ),
						"\""
					);

				} else {
					// We found a non-quoted value.
					strMatchedValue = arrMatches[ 3 ];
				}
				arrData[ arrData.length - 1 ].push( strMatchedValue );
			}
			return ( arrData );
		},
		_createWindow: function( content, name ) {
			console.log( "_createWindow:" );
			var win = new qx.ui.window.Window( "name", "" ).set( {
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			} );
			win.setLayout( new qx.ui.layout.Dock() );
			win.setWidth( 450 );
			win.setHeight( 390 );
			win.setAllowMaximize( false );
			win.moveTo( 200, 50 );
			win.add( content, {
				edge: "center"
			} );
			win.open();
			win.addListener( "changeActive", function( e ) {
				console.log( "changeActive:" + e.getData() );
			}, this );
			win.setModal( true );
			return win;
		},
		_createUploadPage: function() {
			var container = new qx.ui.container.Composite( new qx.ui.layout.Dock( 10 ) );

			var u = ms123.util.Remote._username;
			var p = ms123.util.Remote._password;
			var credentials = ms123.util.Base64.encode( u + ":" + p );

			var uploadForm = new ms123.upload.UploadForm( 'uploadFrm', 'meta/userdata/' + this.__user.userid + '/imports?credentials=' + credentials + "&module=import&method=put" );
			uploadForm.setParameter( 'credentials', credentials );
			uploadForm.setPadding( 8 );

			this._uploadForm = uploadForm;

			var vb = new qx.ui.layout.VBox( 10 )
			uploadForm.setLayout( vb );
			container.add( uploadForm, {
				edge: "north"
			} );

			var l = new qx.ui.basic.Label( this.tr( "import.select_file" ) );
			l.setRich( true );
			uploadForm.add( l );

			var self = this;
			var uploadButton = new ms123.upload.UploadButton( 'importfile', this.tr( "import.select_button" ), 'icon/16/actions/document-save.png', null, true );
			uploadForm.add( uploadButton );
			uploadButton.addListener( "changeFileName", function( e ) {
				console.log( "uploadButton:", e.__data );
				var reader = new FileReader();

				reader.addEventListener( 'loadend', function( e ) {
					var text = e.srcElement.result;
					self._importFile( text );
				}, this );

				var f = e.__data[ 0 ];
				reader.readAsText( f );
			}, this );

			container.add( this._createDropArea(), {
				edge: "center"
			} );

			return container;
		},
		html5drop: function( e ) {
			this.html5dropfiles( e.dataTransfer.files );
			e.stopPropagation();
			e.preventDefault();
		},
		html5dropfiles: function( files ) {
			var self = this;
			for ( var i = 0, f; f = files[ i ]; i++ ) { //here only one file allowed
				var reader = new FileReader();
				reader.addEventListener( 'loadend', function( e ) {
					var text = e.srcElement.result;
					self._importFile( text );
				}, this );
				reader.readAsText( f );
			}
		},
		_createDropArea: function() {
			var l = new qx.ui.basic.Label( this.tr( "import.drop_file" ) ).set( {
				allowGrowX: true,
				allowGrowY: true,
				rich: true,
				backgroundColor: "#dddddd"
			} );
			l.addListener( 'appear', function() {
				var element = l.getContentElement().getDomElement();
				element.ondrop = ms123.importing.ImportDialog.html5drop.bind( this );
				element.ondragover = function() {
					return false;
				}
				element.ondragover = function() {
					return false;
				}

			}, this );
			this._dropArea = l;
			return l;
		},
		__createImportDialog: function( facade ) {
			this._createWindow( this._createUploadPage() );
		}
	}
} );
