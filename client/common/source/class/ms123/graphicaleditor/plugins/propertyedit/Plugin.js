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
	* @ignore(Hash)
	* @ignore($H)
	* @ignore(Clazz)
*/
qx.Class.define("ms123.graphicaleditor.plugins.propertyedit.Plugin", {
	extend: qx.core.Object,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade, parentPanel, diagramName,direction) {
		this.base(arguments);
		this.facade = facade;
		this.parentPanel = parentPanel;
		this.direction = direction;
		this.diagramName = diagramName;

		this.facade.registerOnEvent(ms123.oryx.Config.EVENT_SELECTION_CHANGED, this.onSelectionChanged.bind(this));
		this.init();
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

		init:function(){
			/* The currently selected shapes whos properties will shown */
			this.shapeSelection = new Hash();
			this.shapeSelection.shapes = new Array();
			this.shapeSelection.commonProperties = new Array();
			this.shapeSelection.commonPropertiesValues = new Hash();
			this.editor = new ms123.graphicaleditor.plugins.propertyedit.Editor(this.facade, this.diagramName, this.direction);
			this.parentPanel.add(this.editor);
			this.lastSelection = [];
		},

		onSelectionChanged: function (event) { 
			var isSameSelection = this.isSameSelection(this.lastSelection, event.elements);
			this.lastSelection = event.elements;
			/* Selected shapes */
			this.shapeSelection.shapes = event.elements;

			/* Case: nothing selected */
			if (event.elements.length == 0) {
				this.shapeSelection.shapes = [this.facade.getCanvas()];
			}

			/* subselection available */
			if (event.subSelection) {
				this.shapeSelection.shapes = [event.subSelection];
			}
			this.editor.edit( this.shapeSelection, isSameSelection );
		},
		isSameSelection : function( oldSel, newSel ){
			if( oldSel.length == 0 ) return false;
			if( oldSel.length != newSel.length ) return false;
			for( var i = 0; i < oldSel.length;i++){
				var id1 = oldSel[i].getId();
				var id2 = newSel[i].getId();
				if( id1 != id2 ){
					return false;
				}
			}
			return true;
		}

	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
