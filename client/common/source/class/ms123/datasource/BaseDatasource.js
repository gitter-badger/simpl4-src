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
qx.Class.define( "ms123.datasource.BaseDatasource", {
	extend: qx.ui.container.Composite,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function( facade ) {
		this.base( arguments );
		this._facade = facade;

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
			var form = this._createEditForm();
			this.add( form, {
				edge: "center"
			} );
			var toolbar = this._createToolbar();
			this.add( toolbar, {
				edge: "south"
			} );
		},
		init: function( content ) {
			this.__init();
			if( content != null && this._isString(content) && content.length>2){
				this._form.setData( JSON.parse( content ) );
			}
		},
		_isString:function(s) {
			 return typeof(s) === 'string' || s instanceof String;
		 },
		_createEditForm: function() {},
		_isEmpty: function( content ) {
			if ( !content || content.trim().length === 0 ) return true;
			return false;
		},
		_isOrientOk:function(data){
			if( data.packagename == "data"){
				ms123.form.Dialog.alert( this.tr( "datasource.orient_pack_data_not_ok" ) );
				return false;
			}
			if( data.isOrientDB && !this._isEmpty(data.packagename) && !this._isEmpty(data.databasename)){
				return true;
			}
			return false;
		},
		_save: function() {
			var data = this._form.getData();
			var validate = this._form.validate();
			var isOrientOk = this._isOrientOk(data);
			if ( !isOrientOk && !validate ) {
				ms123.form.Dialog.alert( this.tr( "datasource.form_incomplete" ) );
			} else {

				var port = this._getItem( data.name, 'port' );
				if ( this._notEmpty( data.port ) ) {
					port = data.port;
				}
				var datasource = {
					'osgi.jdbc.driver.name': data.name,
					'osgi.jdbc.driver.class': this._getItem( data.name, 'driver' ),
					'databaseName': data.databasename,
					'url': data.url,
					'is_orientdb': data.isOrientDB,
					'user': data.username,
					'password': data.password,
					'port': port,
					'dataSourceName': data.datasourcename,
					'packageName': data.packagename,
					'is_schema_readonly': data.datasource_is_schema_readonly,
					'is_schema_validate': data.datasource_is_schema_validate
				};
				var jooq = {
					'create_jooq_metadata': data.create_jooq_metadata,
					'jooq_inputschema': data.jooq_inputschema,
					'jooq_includes': data.jooq_includes,
					'jooq_excludes': data.jooq_excludes
				};
				var datanucleus = {
					'create_datanucleus_metadata': data.create_datanucleus_metadata,
					'datanucleus_inputschema': data.datanucleus_inputschema,
					'datanucleus_includes': data.datanucleus_includes,
					'datanucleus_excludes': data.datanucleus_excludes
				};

				var config = {
					datasource: datasource,
					jooq: jooq,
					datanucleus: datanucleus
				}
				this.fireDataEvent( "save", JSON.stringify( data, null, 2 ), null );
				this.createMetadata( config);
			}
		},
		createMetadata: function (config) {
			try {
				ms123.util.Remote.rpcSync("dbmeta:createMetadata", {
					namespace: this._facade.storeDesc.getNamespace(),
					config: config
				});
				ms123.form.Dialog.alert(this.tr("datasource.create_metadata_ok"));
				ms123.config.ConfigManager.clearCache();
			} catch (e) {
				console.log("error:",e);
				ms123.form.Dialog.alert(this.tr("datasource.create_metadata_failed")+":"+e);
			}
		},
		_getDbList: function() {
			var databases = this._getDatabases();
			var dbList = [];
			for ( var i = 0; i < databases.length; i++ ) {
				dbList.push( {
					value: databases[ i ].name,
					label: databases[ i ].name
				} );
			}
			return dbList;
		},
		_getItem: function( name, item ) {
			var databases = this._getDatabases();
			for ( var i = 0; i < databases.length; i++ ) {
				if ( name == databases[ i ].name ) {
					return databases[ i ][ item ];
				}
			}
			return null;
		},
		_notEmpty: function( v ) {
			return v == null || v.length == 0;
		},
		_getDatabases: function() {
			return [ {
				name: 'oracle',
				driver: 'oracle.jdbc.OracleDriver',
				port: '1521',
				url: 'jdbc:oracle:thin:@_HOST_:_PORT_:ORCL'
			}, {
				name: 'as400',
				driver: 'com.ibm.as400.access.AS400JDBCDriver',
				url: 'jdbc:as400://_HOST_/_DB_'
			}, {
				name: 'jtds',
				driver: 'net.sourceforge.jtds.jdbc.Driver',
				port: '1433',
				url: 'jdbc:jtds:sqlserver://_HOST_:_PORT_/_DB_'
			}, {
				name: 'mariadb',
				driver: 'org.mariadb.jdbc.Driver',
				port: '3306',
				url: 'jdbc:mariadb://_HOST_:_PORT:/_DB_'
			}, {
				name: 'mysql',
				driver: 'com.mysql.jdbc.Driver',
				port: '3306',
				url: 'jdbc:mysql://_HOST_:_PORT:/_DB_'
			}, {
				name: 'postgresql',
				driver: 'org.postgresql.Driver',
				port: '5432',
				url: 'jdbc:postgresql://_HOST_:_POST_/_DB_'
			}, {
				name: 'h2',
				driver: 'org.h2.Driver',
				port: '9092',
				url: 'jdbc:h2:tcp://_HOST_:_PORT_/_DB_'
			} ];
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
