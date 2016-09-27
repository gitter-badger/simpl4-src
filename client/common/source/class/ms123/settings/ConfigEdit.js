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
					this._schemaname = data[ms123.settings.Config.SETTINGS_SCHEMANAME];
					var schema = ms123.util.Remote.rpcSync( "git:searchContent", {
						reponame: this._facade.storeDesc.getNamespace(),
						name: this._schemaname,
						type: "sw.schema"
					} );
					console.log( "Data:", data[ms123.settings.Config.SETTINGS_DATA] );
					console.log( "Schema:", schema );
					this._createJsonEditor( JSON5.parse(schema), data[ms123.settings.Config.SETTINGS_DATA] );
				}else{
					ms123.form.Dialog.alert( "settings.ConfigEdit._load:no schema found"  );
				}
			} catch ( e ) {
				ms123.form.Dialog.alert( "settings.ConfigEdit._load:" + e );
			}
		},
		_save: function() {
			var errors = this._editor.validate();
			if( errors.length){
				console.log("errors:",errors);
				ms123.form.Dialog.alert( this.tr( "settings.not_valid" ) );
				return;
			}
			var data = this._editor.getValue();
			console.log("Data:",data);
			try {
				var settings={};
				settings[ms123.settings.Config.SETTINGS_DATA] = data;
				settings[ms123.settings.Config.SETTINGS_SCHEMANAME] = this._schemaname;
				ms123.util.Remote.rpcSync( "setting:setResourceSetting", {
					namespace: this._facade.storeDesc.getNamespace(),
					settingsid: this._facade.settingsid,
					resourceid: this._resourceid,
					settings: settings
				} );
				ms123.form.Dialog.alert( this.tr( "settings.properties_saved" ) );
				ms123.config.ConfigManager.clearCache();
			} catch ( e ) {
				ms123.form.Dialog.alert( "settings.views.PropertyEdit._save:" + e );
			}
		},
		_createJsonEditor: function( schema, config ) {
			var widget = new qx.ui.embed.Html( "<div></div>" ).set({
				overflowY: "auto",
				overflowX: "auto"
			});
			this.add( widget, {
				edge: "center"
			} );
			widget.addListenerOnce( "appear", function() {
				var c = widget.getContentElement().getDomElement();
				JSONEditor.plugins.selectize.enable = true;
				this._editor = new JSONEditor( c, {
					theme : "html",
					iconlib : "fontawesome4",
					schema: schema
				} );
				this._editor.setValue( config);
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
