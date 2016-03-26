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
		_save: function() {
			var data = this._form.getData();
			var validate = this._form.validate();
			if ( !validate ) {
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
					'user': data.username,
					'password': data.password,
					'port': port,
					'dataSourceName': data.datasourcename
				};
				var jooq = {
					'create_jooq_metadata': data.create_jooq_metadata,
					'jooq_inputschema': data.jooq_inputschema,
					'jooq_includes': data.jooq_includes,
					'jooq_excludes': data.jooq_excludes,
					'jooq_packagename': data.jooq_packagename
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
					storeId: this._facade.storeDesc.getStoreId(),
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
				port: '8084',
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
