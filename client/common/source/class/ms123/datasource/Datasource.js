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
qx.Class.define( "ms123.datasource.Datasource", {
	extend: ms123.datasource.BaseDatasource,
	include: [ qx.locale.MTranslation ],

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function( facade ) {
		this.base( arguments, facade );
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	events: {
		"save": "qx.event.type.Data"
	},
	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_createEditForm: function() {
			var formData = {
				"name": {
					'type': "SelectBox",
					'label': this.tr( "datasource.database" ),
					'exclude': 'url==null || url.length==0',
					'value': "string",
					'options': this._getDbList()
				},
				"url": {
					'type': "TextField",
					'label': this.tr( "datasource.url" ),
					'validation': {
						required: false
					},
					'value': null
				},
				"username": {
					'type': "TextField",
					'label': this.tr( "datasource.username" ),
					'exclude': 'url==null || url.length==0',
					'validation': {
						required: false
					},
					'value': null
				},
				"password": {
					'type': "TextField",
					'label': this.tr( "datasource.password" ),
					'exclude': 'url==null || url.length==0',
					'validation': {
						required: false
					},
					'value': null
				},
				"databasename": {
					'type': "TextField",
					'label': this.tr( "datasource.databasename" ),
					'exclude': 'url==null || url.length==0',
					'validation': {
						required: false
					},
					'value': null
				},
				"datasourcename": {
					'type': "TextField",
					'label': this.tr( "datasource.datasourcename" ),
					'validation': {
						required: true
					},
					'value': null
				},
				"packagename": {
					'type': "TextField",
					'label': this.tr( "datasource.packagename" ),
					'defaultValue': "data",
					'validation': {
						required: true
					},
					'value': null
				},
				"create_jooq_metadata": {
					'type': "Checkbox",
					'label': this.tr( "datasource.create_jooq_metadata" ),
					'defaultValue': true,
					'value': null
				},
				"jooq_inputschema": {
					'type': "TextField",
					'label': this.tr( "datasource.jooq_inputschema" ),
					'value': null
				},
				"jooq_includes": {
					'type': "TextField",
					'label': this.tr( "datasource.jooq_includes" ),
					'value': null
				},
				"jooq_excludes": {
					'type': "TextField",
					'label': this.tr( "datasource.jooq_excludes" ),
					'value': null
				},
				"create_datanucleus_metadata": {
					'type': "Checkbox",
					'label': this.tr( "datasource.create_datanucleus_metadata" ),
					'defaultValue': true,
					'value': null
				},
				"datanucleus_inputschema": {
					'type': "TextField",
					'label': this.tr( "datasource.datanucleus_inputschema" ),
					'value': null
				},
				"datanucleus_includes": {
					'type': "TextField",
					'label': this.tr( "datasource.datanucleus_includes" ),
					'value': null
				},
				"datanucleus_excludes": {
					'type': "TextField",
					'label': this.tr( "datasource.datanucleus_excludes" ),
					'value': null
				},
				"datasource_is_schema_readonly": {
					'type': "Checkbox",
					'label': this.tr( "datasource.is_schema_readonly" ),
					'value': false
				},
				"datasource_is_schema_validate": {
					'type': "Checkbox",
					'label': this.tr( "datasource.is_schema_validate" ),
					'defaultValue': true,
					'value': null
				}
			}
			var context = {};
			context.formData = formData;
			context.buttons = [];
			context.formLayout = [{
				id: "tab1", lineheight:-1
			}];
			this._form = new ms123.widgets.Form(context);
			return this._form;
		}
	}
} );
