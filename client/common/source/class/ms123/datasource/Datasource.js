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
				"isOrientDB": {
					'type': "Checkbox",
					'label': "OrientDB",
					'defaultValue': false,
					'value': null
				},
				"name": {
					'type': "SelectBox",
					'label': this.tr( "datasource.database" ),
					'exclude': 'url==null || url.length==0 || isOrientDB',
					'value': "string",
					'options': this._getDbList()
				},
				"url": {
					'type': "TextField",
					'label': this.tr( "datasource.url" ),
					'exclude': 'isOrientDB',
					'validation': {
						required: false
					},
					'value': null
				},
				"username": {
					'type': "TextField",
					'label': this.tr( "datasource.username" ),
					'exclude': 'isOrientDB || (url==null || url.length==0)',
					'validation': {
						required: false
					},
					'value': null
				},
				"password": {
					'type': "TextField",
					'label': this.tr( "datasource.password" ),
					'exclude': 'isOrientDB || (url==null || url.length==0)',
					'validation': {
						required: false
					},
					'value': null
				},
				"databasename": {
					'type': "TextField",
					'label': this.tr( "datasource.databasename" ),
					'exclude': '!isOrientDB && (url==null || url.length==0 || name.startsWith("h2"))',
					'validation': {
						required: false
					},
					'value': null
				},
				"datasourcename": {
					'type': "TextField",
					'label': this.tr( "datasource.datasourcename" ),
					'exclude': 'isOrientDB',
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
					'exclude': 'isOrientDB',
					'defaultValue': true,
					'value': null
				},
				"jooq_inputschema": {
					'type': "TextField",
					'label': this.tr( "datasource.jooq_inputschema" ),
					'exclude': 'isOrientDB',
					'value': null
				},
				"jooq_includes": {
					'type': "TextField",
					'label': this.tr( "datasource.jooq_includes" ),
					'exclude': 'isOrientDB',
					'value': null
				},
				"jooq_excludes": {
					'type': "TextField",
					'label': this.tr( "datasource.jooq_excludes" ),
					'exclude': 'isOrientDB',
					'value': null
				},
				"create_datanucleus_metadata": {
					'type': "Checkbox",
					'label': this.tr( "datasource.create_datanucleus_metadata" ),
					'exclude': 'isOrientDB',
					'defaultValue': true,
					'value': null
				},
				"datanucleus_inputschema": {
					'type': "TextField",
					'label': this.tr( "datasource.datanucleus_inputschema" ),
					'exclude': 'isOrientDB',
					'value': null
				},
				"datanucleus_includes": {
					'type': "TextField",
					'label': this.tr( "datasource.datanucleus_includes" ),
					'exclude': 'isOrientDB',
					'value': null
				},
				"datanucleus_excludes": {
					'type': "TextField",
					'label': this.tr( "datasource.datanucleus_excludes" ),
					'exclude': 'isOrientDB',
					'value': null
				},
				"datanucleus_generate_defaultvalue": {
					'type': "Checkbox",
					'label': this.tr( "datasource.datanucleus_generate_defaultvalue" ),
					'exclude': 'isOrientDB',
					'defaultValue': false,
					'value': null
				},
				"datasource_is_schema_readonly": {
					'type': "Checkbox",
					'label': this.tr( "datasource.is_schema_readonly" ),
					'exclude': 'isOrientDB',
					'value': false
				},
				"datasource_is_schema_validate": {
					'type': "Checkbox",
					'label': this.tr( "datasource.is_schema_validate" ),
					'exclude': 'isOrientDB',
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
