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
/**
	* @ignore(Hash)
	* @ignore(Clazz)
*/

qx.Class.define("ms123.graphicaleditor.plugins.Website", {
	extend: qx.core.Object,
	include: [qx.locale.MTranslation],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade) {
		this.base(arguments);
		this.facade = facade;

		this.facade.registerOnEvent('layout.website.label', this.handleLayoutLabel.bind(this));
		this.facade.registerOnEvent('layout.website.label.menuitem', this.handleLayoutLabelItem.bind(this));
		this.facade.registerOnEvent('layout.website.menuitem', this.handleLayoutItem.bind(this));
		this.facade.registerOnEvent('layout.website.page', this.handleLayoutPage.bind(this));

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
		/**
		 * 
		 * param {Object} event
		 */
		handleLayoutLabel: function (event) {
			var shape = event.shape;
			var moveX = event.moveX;
			var moveY = event.moveY;

			var labels = shape.getChildNodes(false).findAll(function (node) {
				return (node.getStencil().id() === "http://b3mn.org/stencilset/website#Label");
			});

			if (labels.length > 0) {
				labels.each(function (label) {
					var ul = label.bounds.upperLeft();
					var lr = label.bounds.lowerRight();
					ul.y = -label.bounds.height() + moveY;
					lr.y = moveY;
					ul.x = moveX;
					lr.x = label.bounds.width() + moveX;
					label.bounds.set(ul, lr);
				});
			}
		},

		handleLayoutItem: function (event) {
			var shape = event.shape;

			var items = shape.getChildNodes(false).findAll(function (node) {
				return (
					(node.getStencil().id() === "http://b3mn.org/stencilset/website#Menuitem") || 
					(node.getStencil().id() === "http://b3mn.org/stencilset/website#Submenu") || 
					(node.getStencil().id() === "http://b3mn.org/stencilset/website#Menu"));
			});

			if (items.length > 0) {

				items = items.sortBy(function (item) {
					return item.bounds.upperLeft().y;
				});

				var shapeWidth = shape.bounds.width();
				var shapeHeight = 0;
				items.each(function (item) {
					var ul = item.bounds.upperLeft();
					var lr = item.bounds.lowerRight();
					if ( 
						item.getStencil().id() === "http://b3mn.org/stencilset/website#Menu"||
						item.getStencil().id() === "http://b3mn.org/stencilset/website#Submenu"
					) {
						ul.y = shapeHeight + 25;
						shapeHeight += 25;
					} else {
						ul.y = shapeHeight + 5;
						shapeHeight += 5;
					}
					lr.y = ul.y + item.bounds.height();
					shapeHeight += item.bounds.height();
					ul.x = 30;
					lr.x = shapeWidth-3;
					item.bounds.set(ul, lr);
				});

				var upl = shape.bounds.upperLeft();
				shape.bounds.set(upl.x, upl.y, shape.bounds.lowerRight().x, upl.y + shapeHeight + 5);
			}

			var labels = shape.getChildNodes(false).findAll(function (node) {
				return (node.getStencil().id() === "http://b3mn.org/stencilset/website#Label");
			});

			if (labels.length > 0) {
				labels.each(function (label) {
					var ul = label.bounds.upperLeft();
					var lr = label.bounds.lowerRight();
					ul.y = -label.bounds.height() - 1;
					lr.y = -1;
					ul.x = 0;
					lr.x = label.bounds.width();
					label.bounds.set(ul, lr);
				});
			}
		},

		handleLayoutPage: function (event) {
			var shape = event.shape;

			var cases = shape.getChildNodes(false);

			var maxWidth = 0;
			cases.each(function (c) {
				if (c.bounds.width() > maxWidth) maxWidth = c.bounds.width();
			});

			if (cases.length > 0) {

				cases = cases.sortBy(function (c) {
					return c.bounds.upperLeft().y;
				});

				var shapeHeight = 5;
				cases.each(function (c) {
					var ul = c.bounds.upperLeft();
					var lr = c.bounds.lowerRight();
					ul.y = shapeHeight;
					lr.y = ul.y + c.bounds.height();
					shapeHeight += c.bounds.height() + 5;
					ul.x = 0;
					lr.x = maxWidth;
					c.bounds.set(ul, lr);
				});

				var upl = shape.bounds.upperLeft();
				shape.bounds.set(upl.x, upl.y, upl.x + maxWidth, upl.y + shapeHeight + 20);
			}
		},

		handleLayoutLabelItem: function (event) {
			var shape = event.shape;

			var labels = shape.getChildNodes(false).findAll(function (node) {
				return (node.getStencil().id() === "http://b3mn.org/stencilset/website#Label");
			});

			if (labels.length > 0) {
				labels.each(function (label) {
					var ul = label.bounds.upperLeft();
					var lr = label.bounds.lowerRight();
					ul.y = 2;
					ul.x = 2;
					lr.y = 2 + label.bounds.height();
					lr.x = 2 + label.bounds.width();
					label.bounds.set(ul, lr);
				});
			}
		}

	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
