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
 * @ignore(JSON5.*)
 */

qx.Class.define( "ms123.graphicaleditor.plugins.propertyedit.StringPlusField", {
	extend: qx.ui.core.Widget,
	implement: [
		qx.ui.form.IStringForm, qx.ui.form.IForm
	],
	include: [
		qx.ui.form.MForm
	],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function( config, title, key, facade, data ) {
		this.base( arguments );
		this.config = config || {};
		this.title = title;
		this.data = data;

		this.key = key;
		this.facade = facade;
		var layout = new qx.ui.layout.HBox();
		this._setLayout( layout );
		this._init();
	},

	/******************************************************************************
	 EVENTS
	 ******************************************************************************/
	events: {
		"changeValue": "qx.event.type.Data"
	},

	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_init: function() {
			this.textField = this._createChildControl( "textfield" );
			var select = this._createChildControl( "select" );
			var clear = this._createChildControl( "clear" );
			this.setFocusable( true );
		},
		setUserData: function( key, data ) {
			this.textField.setUserData( key, data );
		},
		getUserData: function( key ) {
			return this.textField.getUserData( key );
		},
		setLiveUpdate: function( flag ) {
			this.textField.setLiveUpdate( flag );
		},
		setMaxLength: function( flag ) {
			this.textField.setMaxLength( flag );
		},
		setRequired: function( flag ) {
			this.textField.setRequired( flag );
		},
		resetValue: function() {
			return this.textField.resetValue();
		},
		getValue: function() {
			return this.textField.getValue();
		},
		setValue: function( value ) {
			this.textField.setValue( value );
		},
		// overridden
		_createChildControlImpl: function( id, hash ) {
			var control;
			switch ( id ) {
				case "textfield":
					control = new qx.ui.form.TextField();
					control.setLiveUpdate( true );
					control.setFocusable( false );
					control.setReadOnly( false );
					control.setEnabled( true );
					control.addState( "inner" );
					this._add( control, {
						flex: 1
					} );
					control.addListener( 'changeValue', ( function( e ) {
						console.debug( "innerListener("+this.getFieldKey()+"):old", e.__old + "/new:" + e.__data );
						this.fireDataEvent( "changeValue", e.__data, e.__old );
					} ).bind( this ) )
					control.addListener( 'focusout', ( function( e ) {
						console.debug( "focusout("+this.getFieldKey()+")" );
						this.fireDataEvent( "changeValue", this.getValue(), null );
					} ).bind( this ) )
					break;
				case "select":
					control = this.createActionButton();
					break;
				case "clear":
					var control = new qx.ui.form.Button( null, "resource/ms123/clear.png" ).set( {
						padding: 0,
						margin: 0
					} );
					control.addState( "inner" );
					control.setFocusable( false );
					control.addListener( "execute", function() {
						this.resetValue();
					}, this );
					this._add( control );
					break;
			}
			return control;
		},
		createActionButton: function() {
			var control = new qx.ui.form.Button( null, "resource/ms123/edit2.png" ).set( {
				padding: 0,
				margin: 0,
				maxHeight: 30
			} );
			control.setFocusable( false );
			control.addListener( "execute", function( e ) {
				this._createWindow();
			}, this );
			this._add( control );
			return control;
		},
		/**
		 * Returns the field key.
		 */
		getFieldKey: function() {
			return this.key;
		},

		_createWindow: function() {
			var topContainer = new qx.ui.container.Composite();
			topContainer.setLayout( new qx.ui.layout.VBox( 10 ) );
			var win = this.createWindow( this.title );

			var complexItems = [ {
				"id": "processvariable",
				"name": "Processvariable",
				"name_de": "Processvariable",
				"type": ms123.oryx.Config.TYPE_STRING,
				"value": null,
				"width": 120,
				"optional": false
			}, {
				"id": "servicevariable",
				"name": "Servicevariable",
				"name_de": "Servicevariable",
				"readonly": false,
				"type": ms123.oryx.Config.TYPE_STRING,
				"value": null,
				"width": 120,
				"optional": false
			} ];

			var data = [];
			var method = null;
			var val = this.getValue();
			if ( val != null && val.startsWith("pc:")) {
				var d = JSON5.parse( val.substring(3) );
				data = d.parameter;
				method = d.method;
			}
			console.log( "data:", data );
			var table = this.createTable( complexItems, data );
			var toolbar = this.createToolbar( [ "add", "del" ] );
			this.serviceSelector = this._serviceSelector();
			this.serviceSelector.setData( {
				"service": method
			} );
			topContainer.add( this.serviceSelector );
			topContainer.add( toolbar );
			var config = {
				"helperTree": [ "sw.camel" ],
				"namespace": "docu"
			};
			this._helperTree = new ms123.graphicaleditor.plugins.propertyedit.ResourceDetailTree( config, this.facade );
			var split = this._splitPane( table, this._helperTree );
			win.add( split, {
				edge: "center"
			} );
			win.add( topContainer, {
				edge: "north"
			} );
			var buttons = this.createButtons();
			win.add( buttons, {
				edge: "south"
			} );
			this.editWindow = win;
			win.open();
		},
		handleOkButton: function( e ) {
			this.table.stopEditing();
			var value = this.getTableData();
			var sl = this.serviceSelector.getData();

			var data = {
				'method': sl.service,
				'parameter': value
			}

			data = JSON.stringify( data );
			console.log( "data:" + data );
			this.setValue( "pc:"+data );
			this.editWindow.close();
		},
		_getButtons: function() {
			var list = [];

			var buttonSave = new qx.ui.toolbar.Button( this.tr( "Ok" ), "icon/16/actions/dialog-ok.png" );
			buttonSave.addListener( "execute", function( e ) {
				this.handleOkButton( e );
			}, this );
			list.push( buttonSave );

			var buttonCancel = new qx.ui.toolbar.Button( this.tr( "Cancel" ), "icon/16/actions/dialog-close.png" );
			buttonCancel.addListener( "execute", function() {
				this.editWindow.close();
			}, this );
			list.push( buttonCancel );
			return list;
		},
		_splitPane: function( left, right ) {
			var splitPane = new qx.ui.splitpane.Pane( "horizontal" ).set( {
				decorator: null
			} );

			splitPane.add( left, 6 );
			splitPane.add( right, 2 );
			return splitPane;
		},
		createWindow: function( name ) {
			var win = new qx.ui.window.Window( name, "" ).set( {
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			} );
			win.setLayout( new qx.ui.layout.Dock );
			win.setWidth( 700 );
			win.setHeight( 500 );
			win.setAllowMaximize( false );
			win.setAllowMinimize( false );
			win.setModal( true );
			win.setActive( false );
			win.minimize();
			win.center();
			return win;
		},


		_serviceSelector: function() {
			var formData = {
				"service": {
					'type': "ResourceSelector",
					'label': this.tr( "Service" ),
					'validation': {
						required: true
					},
					'config': {
						'type': 'sw.camel',
						'editable': false,
						'selected_callback': ( function( x ) {
							console.log( "Selected_callback:", x );
							this._helperTree.selectNode(x);
						} ).bind( this )
					},
					'value': ""
				}
			};
			return this.__createForm( formData, "single" );
		},
		__createForm: function( formData, layout ) {
			var form = new ms123.form.Form( {
				"tabs": [ {
					id: "tab1",
					layout: layout,
					lineheight: 22
				} ],
				"formData": formData,
				"buttons": [],
				"useScroll": false,
				"callback": function( m, v ) {},
				"inWindow": false,
				"render": true
			} );
			return form;
		},
		_isMaybeJSON: function( str ) {
			return str != null && str.startsWith( "{" ) && str.endsWith( "}" );
		},
		createTable: function( items, data ) {
			this.items = items;
			console.log( "createTable:", this.getValue() );
			var dialogWidth = 0;

			var colIds = new Array();
			var colHds = new Array();
			var recordType = [];
			for ( var i = 0; i < this.items.length; i++ ) {
				var id = this.items[ i ].id;
				var header = this.items[ i ].name;
				var type = this.items[ i ].type;
				colIds.push( id );
				colHds.push( header );

				if ( type == ms123.oryx.Config.TYPE_CHOICE ) {
					type = ms123.oryx.Config.TYPE_STRING;
				}
				if ( type == ms123.oryx.Config.TYPE_COMBO ) {
					type = ms123.oryx.Config.TYPE_STRING;
				}
				recordType[ i ] = {
					name: id,
					type: type
				};
			}
			this.recordType = recordType;

			var tableModel = new qx.ui.table.model.Simple();
			tableModel.setColumns( colHds, colIds );
			var customMap = {
				tableColumnModel: function( obj ) {
					return new qx.ui.table.columnmodel.Resize( obj );
				}
			};
			var table = new qx.ui.table.Table( tableModel, customMap );
			table.addListener( "cellTap", this.onCellClick, this, false );
			var selModel = table.getSelectionModel();
			selModel.setSelectionMode( qx.ui.table.selection.Model.NO_SELECTION );
			selModel.addListener( "changeSelection", function( e ) {
				var index = selModel.getLeadSelectionIndex();
				this.table.stopEditing();
				if ( this.delButton ) {
					this.delButton.setEnabled( ( index > -1 ) ? true : false );
				}
			}, this );

			var tcm = table.getTableColumnModel();
			table.setStatusBarVisible( false );
			console.log( "items:", this.items );
			for ( var i = 0; i < this.items.length; i++ ) {
				var width = this.items[ i ].width;
				var type = this.items[ i ].type;

				if ( type == ms123.oryx.Config.TYPE_STRING ) {
					var f = null;
					if ( this.items[ i ].filter ) {
						f = new ms123.graphicaleditor.plugins.propertyedit.RegexCellEditor( this.items[ i ].filter );
					} else {
						f = new qx.ui.table.celleditor.TextField();
					}
					tcm.setCellEditorFactory( i, f );
					table.getTableModel().setColumnEditable( i, true );
				}

				var resizeBehavior = tcm.getBehavior();
				resizeBehavior.setWidth( i, width, 1 );

				dialogWidth += width;
			}

			if ( dialogWidth > 900 ) {
				dialogWidth = 900;
			}
			dialogWidth += 32;

			tableModel.setColumns( colHds, colIds );
			this.tableModel = tableModel;
			this.table = table;


			this.setTableData( data );

			table.setDroppable( true );
			table.setFocusCellOnPointerMove( true );

			table.addListener( "drop", this._handleDrop, this );

			var selModel = table.getSelectionModel();
			selModel.setSelectionMode( qx.ui.table.selection.Model.SINGLE_SELECTION );

			return table;
		},
		_handleDrop: function( e ) {
			console.log( "_handleDrop:" + e );
			if ( this.table.isEditing() ) {
				this.table.stopEditing();
			}
			var col = this.table.getFocusedColumn();
			var row = this.table.getFocusedRow();
			if ( col === undefined || row == undefined ) return;
			var target = e.getRelatedTarget();
			var value = null;


			if ( qx.Class.implementsInterface( target, qx.ui.form.IStringForm ) ) {
				value = target.getValue();
			} else {
				value = target.getSelection().getItem( 0 ).getValue();
			}
			console.log( "cell:" + row + "/" + col );
			console.log( "_handleDrop:" + value );
			this.table.getTableModel().setValue( col, row, value );
		},
		createToolbar: function( buttonList ) {
			var toolbar = new qx.ui.toolbar.ToolBar();
			if ( buttonList.indexOf( "add" ) != -1 ) {
				var badd = new qx.ui.toolbar.Button( "", "icon/16/actions/list-add.png" );
				badd.setToolTipText( this.tr( "graphicaleditor.add_record" ) );
				badd.addListener( "execute", function() {
					var initial = this.buildInitial( this.recordType, this.items );
					this.addRecord( initial );
				}, this );
				this.addButton = badd;
				toolbar._add( badd );
			}
			if ( buttonList.indexOf( "del" ) != -1 ) {
				var bdel = new qx.ui.toolbar.Button( "", "icon/16/actions/list-remove.png" );
				bdel.setToolTipText( this.tr( "graphicaleditor.delete_record" ) );
				bdel.setEnabled( false );
				bdel.addListener( "execute", function() {
					this.deleteCurrentRecord();
				}, this );
				toolbar._add( bdel );
				toolbar.add( new qx.ui.core.Spacer(), {
					flex: 1
				} );
				this.delButton = bdel;
			}
			return toolbar;
		},
		createButtons: function() {
			var toolbar = new qx.ui.toolbar.ToolBar();
			toolbar.setSpacing( 5 );
			toolbar.addSpacer();
			toolbar.addSpacer();

			var buttonSave = new qx.ui.toolbar.Button( this.tr( "Ok" ), "icon/16/actions/dialog-ok.png" );
			buttonSave.addListener( "execute", function( e ) {
				this.handleOkButton( e );
			}, this );
			toolbar._add( buttonSave )

			var buttonCancel = new qx.ui.toolbar.Button( this.tr( "Cancel" ), "icon/16/actions/dialog-close.png" );
			buttonCancel.addListener( "execute", function() {
				this.editWindow.close();
			}, this );
			toolbar._add( buttonCancel )

			return toolbar;
		},
		buildInitial: function( recordType, items ) {
			var initial = {};
			for ( var i = 0; i < items.length; i++ ) {
				var id = items[ i ].id;
				initial[ id ] = items[ i ].value;
			}
			var data = qx.util.Serializer.toJson( initial );
			console.log( "initial:" + data );
			return initial;
		},
		deleteCurrentRecord: function() {
			this.table.stopEditing();
			var selModel = this.table.getSelectionModel();
			var index = selModel.getLeadSelectionIndex();
			if ( index > -1 ) {
				this.tableModel.removeRows( index, 1 );
			}
		},
		addRecord: function( map ) {
			this.table.stopEditing();
			var selModel = this.table.getSelectionModel();
			var index = selModel.getLeadSelectionIndex();
			if ( index > -1 ) {
				this.tableModel.addRowsAsMapArray( [ map ], index + 1, true );
			} else {
				this.tableModel.addRowsAsMapArray( [ map ], null, true );
			}
		},
		setTableData: function( data ) {
			this.tableModel.setDataAsMapArray( data, true );
		},

		getTableData: function() {
			var arr = this.tableModel.getDataAsMapArray();
			return arr;
		},
		onCellClick: function( e ) {
			var rownum = e.getRow();
			var colnum = e.getColumn();
			this.table.stopEditing();
			this.table.setFocusedCell( colnum, rownum );
			console.log( "colnum:" + colnum + "/" + rownum + "/" + this.recordType[ colnum ].type );
			if ( this.recordType[ colnum ].type != ms123.oryx.Config.TYPE_BOOLEAN ) {
				this.table.startEditing();
				//var value = this.tableModel.getValue( 0, rownum );
				//this._helperTree.selectNode( value );
				return;
			}
			if ( this.tableModel.getValue( colnum, rownum ) === true ) {
				this.tableModel.setValue( colnum, rownum, false );
			} else {
				this.tableModel.setValue( colnum, rownum, true );
			}
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function() {}

} );
