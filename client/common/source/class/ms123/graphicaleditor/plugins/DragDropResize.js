/**
 * Copyright (c) 2006
 * Martin Czuchra, Nicolas Peters, Daniel Polak, Willi Tscheschner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 **/

/**
	* @ignore(Clazz)
	* @ignore(Clazz.extend)
	* @ignore($A)
*/


qx.Class.define("ms123.graphicaleditor.plugins.DragDropResize", {
	extend: ms123.oryx.core.AbstractPlugin,
	include: [ms123.util.MBindTo],
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade) {
		this.base(arguments,facade);

		// Initialize variables
		this.currentShapes = []; // Current selected Shapes
		//this.pluginsData 		= [];			// Available Plugins
		this.toMoveShapes = []; // Shapes there will be moved
		this.distPoints = []; // Distance Points for Snap on Grid
		this.isResizing = false; // Flag: If there was currently resized
		this.dragEnable = false; // Flag: If Dragging is enabled
		this.dragIntialized = false; // Flag: If the Dragging is initialized
		this.edgesMovable = true; // Flag: If an edge is docked it is not movable
		this.offSetPosition = {
			x: 0,
			y: 0
		}; // Offset of the Dragging
		this.faktorXY = {
			x: 1,
			y: 1
		}; // The Current Zoom-Faktor
		this.containmentParentNode; // the current future parent node for the dragged shapes
		this.isAddingAllowed = false; // flag, if adding current selected shapes to containmentParentNode is allowed
		this.isAttachingAllowed = false; // flag, if attaching to the current shape is allowed
		this.callbackMouseMove = this.handleMouseMove.bind(this);
		this.callbackMouseUp = this.handleMouseUp.bind(this);

		// Get the SVG-Containernode 
		var containerNode = this.facade.getCanvas().getSvgContainer();

		// Create the Selected Rectangle in the SVG
		this.selectedRect = new ms123.oryx.core.SelectedRect(containerNode);

		// Show grid line if enabled
		if (ms123.oryx.Config.SHOW_GRIDLINE) {
			this.vLine = new ms123.graphicaleditor.plugins.GridLine(containerNode, ms123.graphicaleditor.plugins.GridLine.DIR_VERTICAL);
			this.hLine = new ms123.graphicaleditor.plugins.GridLine(containerNode, ms123.graphicaleditor.plugins.GridLine.DIR_HORIZONTAL);
		}

		// Get a HTML-ContainerNode
		containerNode = this.facade.getCanvas().getHTMLContainer();

		this.scrollNode = this.facade.getCanvas().rootNode.parentNode.parentNode;

		// Create the southeastern button for resizing
		this.resizerSE = new ms123.graphicaleditor.plugins.Resizer(containerNode, "southeast", this.facade);
		this.resizerSE.registerOnResize(this.onResize.bind(this)); // register the resize callback
		this.resizerSE.registerOnResizeEnd(this.onResizeEnd.bind(this)); // register the resize end callback
		this.resizerSE.registerOnResizeStart(this.onResizeStart.bind(this)); // register the resize start callback
		// Create the northwestern button for resizing
		this.resizerNW = new ms123.graphicaleditor.plugins.Resizer(containerNode, "northwest", this.facade);
		this.resizerNW.registerOnResize(this.onResize.bind(this)); // register the resize callback
		this.resizerNW.registerOnResizeEnd(this.onResizeEnd.bind(this)); // register the resize end callback
		this.resizerNW.registerOnResizeStart(this.onResizeStart.bind(this)); // register the resize start callback
		// For the Drag and Drop
		// Register on MouseDown-Event on a Shape
		this.facade.registerOnEvent(ms123.oryx.Config.EVENT_MOUSEDOWN, this.handleMouseDown.bind(this));

	
		this.facade.registerOnEvent(ms123.oryx.Config.EVENT_SELECTION_CHANGED, this.onSelectionChanged.bind(this));
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
		 * On Mouse Down
		 *
		 */
		handleMouseDown: function (event, uiObj) {
			// If the selection Bounds not intialized and the uiObj is not member of current selectio
			// then return
			if (!this.dragBounds || !this.currentShapes.member(uiObj) || !this.toMoveShapes.length) {
				return
			};

			// Start Dragging
			this.dragEnable = true;
			this.dragIntialized = true;
			this.edgesMovable = true;

			// Calculate the current zoom factor
			var a = this.facade.getCanvas().node.getScreenCTM();
			this.faktorXY.x = a.a;
			this.faktorXY.y = a.d;

			// Set the offset position of dragging
			var upL = this.dragBounds.upperLeft();
			this.offSetPosition = {
				x: Event.pointerX(event) - (upL.x * this.faktorXY.x),
				y: Event.pointerY(event) - (upL.y * this.faktorXY.y)
			};

			this.offsetScroll = {
				x: this.scrollNode.scrollLeft,
				y: this.scrollNode.scrollTop
			};

			// Register on Global Mouse-MOVE Event
			document.documentElement.addEventListener(ms123.oryx.Config.EVENT_MOUSEMOVE, this.callbackMouseMove, false);
			// Register on Global Mouse-UP Event
			document.documentElement.addEventListener(ms123.oryx.Config.EVENT_MOUSEUP, this.callbackMouseUp, true);

			return;
		},

		/**
		 * On Key Mouse Up
		 *
		 */
		handleMouseUp: function (event) {

			//disable containment highlighting
			this.facade.raiseEvent({
				type: ms123.oryx.Config.EVENT_HIGHLIGHT_HIDE,
				highlightId: "dragdropresize.contain"
			});

			this.facade.raiseEvent({
				type: ms123.oryx.Config.EVENT_HIGHLIGHT_HIDE,
				highlightId: "dragdropresize.attached"
			});

			// If Dragging is finished
			if (this.dragEnable) {

				// and update the current selection
				if (!this.dragIntialized) {

					// Do Method after Dragging
					this.afterDrag();

					// Check if the Shape is allowed to dock to the other Shape						
					if (this.isAttachingAllowed && this.toMoveShapes.length == 1 && this.toMoveShapes[0] instanceof ms123.oryx.core.Node && this.toMoveShapes[0].dockers.length > 0) {

						// Get the position and the docker					
						var position = this.facade.eventCoordinates(event);
						var docker = this.toMoveShapes[0].dockers[0];



						//Command-Pattern for dragging several Shapes
						var dockCommand = Clazz.extend({
							construct: function (docker, position, newDockedShape, facade) {
								this.docker = docker;
								this.newPosition = position;
								this.newDockedShape = newDockedShape;
								this.newParent = newDockedShape.parent || facade.getCanvas();
								this.oldPosition = docker.parent.bounds.center();
								this.oldDockedShape = docker.getDockedShape();
								this.oldParent = docker.parent.parent || facade.getCanvas();
								this.facade = facade;

								if (this.oldDockedShape) {
									this.oldPosition = docker.parent.absoluteBounds().center();
								}

							},
							execute: function () {
								this.dock(this.newDockedShape, this.newParent, this.newPosition);

								// Raise Event for having the docked shape on top of the other shape
								this.facade.raiseEvent({
									type: ms123.oryx.Config.EVENT_ARRANGEMENT_TOP,
									excludeCommand: true
								})
							},
							rollback: function () {
								this.dock(this.oldDockedShape, this.oldParent, this.oldPosition);
							},
							dock: function (toDockShape, parent, pos) {
								// Add to the same parent Shape
								parent.add(this.docker.parent)


								// Set the Docker to the new Shape
								this.docker.setDockedShape(undefined);
								this.docker.bounds.centerMoveTo(pos)
								this.docker.setDockedShape(toDockShape);
								//this.docker.update();
								this.facade.setSelection([this.docker.parent]);
								this.facade.getCanvas().update();
								this.facade.updateSelection();


							}
						});

						// Instanziate the dockCommand
						var commands = [new dockCommand(docker, position, this.containmentParentNode, this.facade)];
						this.facade.executeCommands(commands);


						// Check if adding is allowed to the other Shape	
					} else if (this.isAddingAllowed) {


						// Refresh all Shapes --> Set the new Bounds
						this.refreshSelectedShapes();

					}

					this.facade.updateSelection();

					//this.currentShapes.each(function(shape) {shape.update()})
					// Raise Event: Dragging is finished
					this.facade.raiseEvent({
						type: ms123.oryx.Config.EVENT_DRAGDROP_END
					});
				}

				if (this.vLine) this.vLine.hide();
				if (this.hLine) this.hLine.hide();
			}

			// Disable 
			this.dragEnable = false;


			// UnRegister on Global Mouse-UP/-Move Event
			document.documentElement.removeEventListener(ms123.oryx.Config.EVENT_MOUSEUP, this.callbackMouseUp, true);
			document.documentElement.removeEventListener(ms123.oryx.Config.EVENT_MOUSEMOVE, this.callbackMouseMove, false);

			return;
		},

		/**
		 * On Key Mouse Move
		 *
		 */
		handleMouseMove: function (event) {
			// If dragging is not enabled, go return
			if (!this.dragEnable) {
				return
			};
			// If Dragging is initialized
			if (this.dragIntialized) {
				var a = this.facade.getCanvas().node.getScreenCTM();
				var upL = this.dragBounds.upperLeft();
				var offSetPosition = {
					x: Event.pointerX(event) - (upL.x * a.a),
					y: Event.pointerY(event) - (upL.y * a.d)
				};
				var xPrev = this.offSetPosition.x;
				var yPrev = this.offSetPosition.y;
				var xCur = offSetPosition.x;
				var yCur = offSetPosition.y;

				console.log("X:"+Math.abs( xPrev-xCur)+"/Y:"+ Math.abs(yPrev-yCur));
				if( Math.abs( xPrev-xCur) < 3 && Math.abs(yPrev-yCur) < 3){
					return;
				}

				// Raise Event: Drag will be started
				this.facade.raiseEvent({
					type: ms123.oryx.Config.EVENT_DRAGDROP_START
				});
				this.dragIntialized = false;

				// And hide the resizers and the highlighting
				this.resizerSE.hide();
				this.resizerNW.hide();

				// if only edges are selected, containmentParentNode must be the canvas
				this._onlyEdges = this.currentShapes.all(function (currentShape) {
					return (currentShape instanceof ms123.oryx.core.Edge);
				});

				//			/* If only edges are selected, check if they are movable. An Edge is
				//			 * movable in case it is not docked
				//			 */
				//			if(this._onlyEdges) {
				//				this.currentShapes.each(function(edge) {
				//					if(edge.isDocked()) {
				//						this.edgesMovable = false;
				//						throw $break;
				//					}
				//				}.bind(this));
				//			}
				// Do method before Drag
				this.beforeDrag();

				this._currentUnderlyingNodes = [];

			}


			// Calculate the new position
			var position = {
				x: Event.pointerX(event) - this.offSetPosition.x,
				y: Event.pointerY(event) - this.offSetPosition.y
			}

			position.x -= this.offsetScroll.x - this.scrollNode.scrollLeft;
			position.y -= this.offsetScroll.y - this.scrollNode.scrollTop;

			// If not the Control-Key are pressed
			var modifierKeyPressed = event.shiftKey || event.ctrlKey;
			if (ms123.oryx.Config.GRID_ENABLED && !modifierKeyPressed) {
				// Snap the current position to the nearest Snap-Point
				position = this.snapToGrid(position);
			} else {
				if (this.vLine) this.vLine.hide();
				if (this.hLine) this.hLine.hide();
			}

			// Adjust the point by the zoom faktor 
			position.x /= this.faktorXY.x;
			position.y /= this.faktorXY.y;

			// Set that the position is not lower than zero
			position.x = Math.max(0, position.x)
			position.y = Math.max(0, position.y)

			// Set that the position is not bigger than the canvas
			var c = this.facade.getCanvas();
			position.x = Math.min(c.bounds.width() - this.dragBounds.width(), position.x)
			position.y = Math.min(c.bounds.height() - this.dragBounds.height(), position.y)


			// Drag this bounds
			this.dragBounds.moveTo(position);

			// Update all selected shapes and the selection rectangle
			//this.refreshSelectedShapes();
			this.resizeRectangle(this.dragBounds);

			this.isAttachingAllowed = false;

			//check, if a node can be added to the underlying node
			var underlyingNodes = $A(this.facade.getCanvas().getAbstractShapesAtPosition(this.facade.eventCoordinates(event)));

			var checkIfAttachable = this.toMoveShapes.length == 1 && this.toMoveShapes[0] instanceof ms123.oryx.core.Node && this.toMoveShapes[0].dockers.length > 0
			checkIfAttachable = checkIfAttachable && underlyingNodes.length != 1


			if (!checkIfAttachable && underlyingNodes.length === this._currentUnderlyingNodes.length && underlyingNodes.all(qx.lang.Function.bind(function (node, index) {
				return this._currentUnderlyingNodes[index] === node
			},this))) {

				return

			} else if (this._onlyEdges) {

				this.isAddingAllowed = true;
				this.containmentParentNode = this.facade.getCanvas();

			} else {

				/* Check the containment and connection rules */
				var options = {
					event: event,
					underlyingNodes: underlyingNodes,
					checkIfAttachable: checkIfAttachable
				};
				this.checkRules(options);

			}

			this._currentUnderlyingNodes = underlyingNodes.reverse();

			//visualize the containment result
			if (this.isAttachingAllowed) {

				this.facade.raiseEvent({
					type: ms123.oryx.Config.EVENT_HIGHLIGHT_SHOW,
					highlightId: "dragdropresize.attached",
					elements: [this.containmentParentNode],
					style: ms123.oryx.Config.SELECTION_HIGHLIGHT_STYLE_RECTANGLE,
					color: ms123.oryx.Config.SELECTION_VALID_COLOR
				});

			} else {

				this.facade.raiseEvent({
					type: ms123.oryx.Config.EVENT_HIGHLIGHT_HIDE,
					highlightId: "dragdropresize.attached"
				});
			}

			if (!this.isAttachingAllowed) {
				if (this.isAddingAllowed) {

					this.facade.raiseEvent({
						type: ms123.oryx.Config.EVENT_HIGHLIGHT_SHOW,
						highlightId: "dragdropresize.contain",
						elements: [this.containmentParentNode],
						color: ms123.oryx.Config.SELECTION_VALID_COLOR
					});

				} else {

					this.facade.raiseEvent({
						type: ms123.oryx.Config.EVENT_HIGHLIGHT_SHOW,
						highlightId: "dragdropresize.contain",
						elements: [this.containmentParentNode],
						color: ms123.oryx.Config.SELECTION_INVALID_COLOR
					});

				}
			} else {
				this.facade.raiseEvent({
					type: ms123.oryx.Config.EVENT_HIGHLIGHT_HIDE,
					highlightId: "dragdropresize.contain"
				});
			}

			// Stop the Event
			//Event.stop(event);
			return;
		},

		//	/**
		//	 * Rollbacks the docked shape of an edge, if the edge is not movable.
		//	 */
		//	redockEdges: function() {
		//		this._undockedEdgesCommand.dockers.each(function(el){
		//			el.docker.setDockedShape(el.dockedShape);
		//			el.docker.setReferencePoint(el.refPoint);
		//		})
		//	},
		/**
		 *  Checks the containment and connection rules for the selected shapes.
		 */
		checkRules: function (options) {
			var event = options.event;
			var underlyingNodes = options.underlyingNodes;
			var checkIfAttachable = options.checkIfAttachable;
			var noEdges = options.noEdges;

			//get underlying node that is not the same than one of the currently selected shapes or
			// a child of one of the selected shapes with the highest z Order.
			// The result is a shape or the canvas
			this.containmentParentNode = underlyingNodes.reverse().find((function (node) {
				return (node instanceof ms123.oryx.core.Canvas) || (((node instanceof ms123.oryx.core.Node) || ((node instanceof ms123.oryx.core.Edge) && !noEdges)) && (!(this.currentShapes.member(node) || this.currentShapes.any(function (shape) {
					return (shape.children.length > 0 && shape.getChildNodes(true).member(node));
				}))));
			}).bind(this));

			if (checkIfAttachable) {

				this.isAttachingAllowed = this.facade.getRules().canConnect({
					sourceShape: this.containmentParentNode,
					edgeShape: this.toMoveShapes[0],
					targetShape: this.toMoveShapes[0]
				});

				if (this.isAttachingAllowed) {
					var point = this.facade.eventCoordinates(event);
					this.isAttachingAllowed = this.containmentParentNode.isPointOverOffset(point.x, point.y);
				}
			}

			if (!this.isAttachingAllowed) {
				//check all selected shapes, if they can be added to containmentParentNode
				this.isAddingAllowed = this.toMoveShapes.all((function (currentShape) {
					if (currentShape instanceof ms123.oryx.core.Edge || currentShape instanceof ms123.oryx.core.controls.Docker || this.containmentParentNode === currentShape.parent) {
						return true;
					} else if (this.containmentParentNode !== currentShape) {

						if (!(this.containmentParentNode instanceof ms123.oryx.core.Edge) || !noEdges) {

							if (this.facade.getRules().canContain({
								containingShape: this.containmentParentNode,
								containedShape: currentShape
							})) {
								return true;
							}
						}
					}
					return false;
				}).bind(this));
			}

			if (!this.isAttachingAllowed && !this.isAddingAllowed && (this.containmentParentNode instanceof ms123.oryx.core.Edge)) {
				options.noEdges = true;
				options.underlyingNodes.reverse();
				this.checkRules(options);
			}
			var c = this.containmentParentNode.getStencil().idWithoutNs(); //@@@MS BoundaryEvent Hack
			var m = this.toMoveShapes[0].getStencil().idWithoutNs();
			if( c == "BPMNDiagram" && m.startsWith("Boundary")){
				this.isAttachingAllowed = false;
				this.isAddingAllowed = false;
			}
		},

		/**
		 * Redraw the selected Shapes.
		 *
		 */
		refreshSelectedShapes: function () {
			// If the selection bounds not initialized, return
			if (!this.dragBounds) {
				return
			}

			// Calculate the offset between the bounds and the old bounds
			var upL = this.dragBounds.upperLeft();
			var oldUpL = this.oldDragBounds.upperLeft();
			var offset = {
				x: upL.x - oldUpL.x,
				y: upL.y - oldUpL.y
			};

			// Instanciate the dragCommand
			var commands = [new ms123.oryx.core.MoveCommand(this.toMoveShapes, offset, this.containmentParentNode, this.currentShapes, this)];
			// If the undocked edges command is setted, add this command
			if (this._undockedEdgesCommand instanceof ms123.oryx.core.Command) {
				commands.unshift(this._undockedEdgesCommand);
			}
			// Execute the commands			
			this.facade.executeCommands(commands);

			// copy the bounds to the old bounds
			if (this.dragBounds) this.oldDragBounds = this.dragBounds.clone();

		},

		/**
		 * Callback for Resize
		 *
		 */
		onResize: function (bounds) {
			// If the selection bounds not initialized, return
			if (!this.dragBounds) {
				return
			}

			this.dragBounds = bounds;
			this.isResizing = true;

			// Update the rectangle 
			this.resizeRectangle(this.dragBounds);
		},

		onResizeStart: function () {
			this.facade.raiseEvent({
				type: ms123.oryx.Config.EVENT_RESIZE_START
			});
		},

		onResizeEnd: function () {

			if (!(this.currentShapes instanceof Array) || this.currentShapes.length <= 0) {
				return;
			}

			// If Resizing finished, the Shapes will be resize
			if (this.isResizing) {

				var commandClass = Clazz.extend({
					construct: function (shape, newBounds, plugin) {
						this.shape = shape;
						this.oldBounds = shape.bounds.clone();
						this.newBounds = newBounds;
						this.plugin = plugin;
					},
					execute: function () {
						this.shape.bounds.set(this.newBounds.a, this.newBounds.b);
						this.update(this.getOffset(this.oldBounds, this.newBounds));

					},
					rollback: function () {
						this.shape.bounds.set(this.oldBounds.a, this.oldBounds.b);
						this.update(this.getOffset(this.newBounds, this.oldBounds))
					},

					getOffset: function (b1, b2) {
						return {
							x: b2.a.x - b1.a.x,
							y: b2.a.y - b1.a.y,
							xs: b2.width() / b1.width(),
							ys: b2.height() / b1.height()
						}
					},
					update: function (offset) {
						this.shape.getLabels().each(function (label) {
							label.changed();
						});

						var allEdges = [].concat(this.shape.getIncomingShapes()).concat(this.shape.getOutgoingShapes())
						// Remove all edges which are included in the selection from the list
						.findAll(qx.lang.Function.bind(function (r) {
							return r instanceof ms123.oryx.core.Edge
						},this))

						this.plugin.layoutEdges(this.shape, allEdges, offset);

						this.plugin.facade.setSelection([this.shape]);
						this.plugin.facade.getCanvas().update();
						this.plugin.facade.updateSelection();
					}
				});

				var bounds = this.dragBounds.clone();
				var shape = this.currentShapes[0];

				if (shape.parent) {
					var parentPosition = shape.parent.absoluteXY();
					bounds.moveBy(-parentPosition.x, -parentPosition.y);
				}

				var command = new commandClass(shape, bounds, this);

				this.facade.executeCommands([command]);

				this.isResizing = false;

				this.facade.raiseEvent({
					type: ms123.oryx.Config.EVENT_RESIZE_END
				});
			}
		},


		/**
		 * Prepare the Dragging
		 *
		 */
		beforeDrag: function () {

			var undockEdgeCommand = Clazz.extend({
				construct: function (moveShapes) {
					this.dockers = moveShapes.collect(function (shape) {
						return shape instanceof ms123.oryx.core.controls.Docker ? {
							docker: shape,
							dockedShape: shape.getDockedShape(),
							refPoint: shape.referencePoint
						} : undefined
					}).compact();
				},
				execute: function () {
					this.dockers.each(function (el) {
						el.docker.setDockedShape(undefined);
					})
				},
				rollback: function () {
					this.dockers.each(function (el) {
						el.docker.setDockedShape(el.dockedShape);
						el.docker.setReferencePoint(el.refPoint);
						//el.docker.update();
					})
				}
			});

			this._undockedEdgesCommand = new undockEdgeCommand(this.toMoveShapes);
			this._undockedEdgesCommand.execute();

		},

		hideAllLabels: function (shape) {

			// Hide all labels from the shape
			shape.getLabels().each(function (label) {
				label.hide();
			});
			// Hide all labels from docked shapes
			shape.getAllDockedShapes().each(function (dockedShape) {
				var labels = dockedShape.getLabels();
				if (labels.length > 0) {
					labels.each(function (label) {
						label.hide();
					});
				}
			});

			// Do this recursive for all child shapes
			// EXP-NICO use getShapes
			shape.getChildren().each((function (value) {
				if (value instanceof ms123.oryx.core.Shape) this.hideAllLabels(value);
			}).bind(this));
		},

		/**
		 * Finished the Dragging
		 *
		 */
		afterDrag: function () {

		},

		/**
		 * Show all Labels at these shape
		 * 
		 */
		showAllLabels: function (shape) {

			// Show the label of these shape
			//shape.getLabels().each(function(label) {
			for (var i = 0; i < shape.length; i++) {
				var label = shape[i];
				label.show();
			} //);
			// Show all labels at docked shapes
			//shape.getAllDockedShapes().each(function(dockedShape) {
			var allDockedShapes = shape.getAllDockedShapes()
			for (var i = 0; i < allDockedShapes.length; i++) {
				var dockedShape = allDockedShapes[i];
				var labels = dockedShape.getLabels();
				if (labels.length > 0) {
					labels.each(function (label) {
						label.show();
					});
				}
			} //);
			// Do this recursive
			//shape.children.each((function(value) {
			for (var i = 0; i < shape.children.length; i++) {
				var value = shape.children[i];
				if (value instanceof ms123.oryx.core.Shape) this.showAllLabels(value);
			} //).bind(this));
		},

		/**
		 * On the Selection-Changed
		 *
		 */
		onSelectionChanged: function (event) {
			var elements = event.elements;
//console.log("dragdropresize.onSelectionChanged:"+elements);

			// Reset the drag-variables
			this.dragEnable = false;
			this.dragIntialized = false;
			this.resizerSE.hide();
			this.resizerNW.hide();

			// If there is no elements
			if (!elements || elements.length == 0) {
				// Hide all things and reset all variables
				this.selectedRect.hide();
				this.currentShapes = [];
				this.toMoveShapes = [];
				this.dragBounds = undefined;
				this.oldDragBounds = undefined;
			} else {

				// Set the current Shapes
				this.currentShapes = elements;

				// Get all shapes with the highest parent in object hierarchy (canvas is the top most parent)
				var topLevelElements = this.facade.getCanvas().getShapesWithSharedParent(elements);
				this.toMoveShapes = topLevelElements;

				this.toMoveShapes = this.toMoveShapes.findAll(function (shape) {
					return shape instanceof ms123.oryx.core.Node && (shape.dockers.length === 0 || !elements.member(shape.dockers.first().getDockedShape()))
				});

				elements.each((function (shape) {
					if (!(shape instanceof ms123.oryx.core.Edge)) {
						return
					}

					var dks = shape.getDockers()

					var hasF = elements.member(dks.first().getDockedShape());
					var hasL = elements.member(dks.last().getDockedShape());

					//				if(!hasL) {
					//					this.toMoveShapes.push(dks.last());
					//				}
					//				if(!hasF){
					//					this.toMoveShapes.push(dks.first())
					//				} 
					/* Enable movement of undocked edges */
					if (!hasF && !hasL) {
						var isUndocked = !dks.first().getDockedShape() && !dks.last().getDockedShape()
						if (isUndocked) {
							this.toMoveShapes = this.toMoveShapes.concat(dks);
						}
					}

					if (shape.dockers.length > 2 && hasF && hasL) {
						this.toMoveShapes = this.toMoveShapes.concat(dks.findAll(function (el, index) {
							return index > 0 && index < dks.length - 1
						}))
					}

				}).bind(this));

				// Calculate the new area-bounds of the selection
				var newBounds = undefined;
				this.toMoveShapes.each(qx.lang.Function.bind(function (value) {
					var shape = value;
					if (value instanceof ms123.oryx.core.controls.Docker) { /* Get the Shape */
						shape = value.parent;
					}

					if (!newBounds) {
						newBounds = shape.absoluteBounds();
					}
					else {
						newBounds.include(shape.absoluteBounds());
					}
				},this));

				if (!newBounds) {
					elements.each(function (value) {
						if (!newBounds) {
							newBounds = value.absoluteBounds();
						} else {
							newBounds.include(value.absoluteBounds());
						}
					});
				}

				// Set the new bounds
				this.dragBounds = newBounds;
				this.oldDragBounds = newBounds.clone();

				// Update and show the rectangle
				this.resizeRectangle(newBounds);
				this.selectedRect.show();

				// Show the resize button, if there is only one element and this is resizeable
				if (elements.length == 1 && elements[0].isResizable) {
					var aspectRatio = elements[0].getStencil().fixedAspectRatio() ? elements[0].bounds.width() / elements[0].bounds.height() : undefined;
					this.resizerSE.setBounds(this.dragBounds, elements[0].minimumSize, elements[0].maximumSize, aspectRatio);
					this.resizerSE.show();
					this.resizerNW.setBounds(this.dragBounds, elements[0].minimumSize, elements[0].maximumSize, aspectRatio);
					this.resizerNW.show();
				} else {
					this.resizerSE.setBounds(undefined);
					this.resizerNW.setBounds(undefined);
				}

				// If Snap-To-Grid is enabled, the Snap-Point will be calculate
				if (ms123.oryx.Config.GRID_ENABLED) {

					// Reset all points
					this.distPoints = [];

					if (this.distPointTimeout) window.clearTimeout(this.distPointTimeout)

					this.distPointTimeout = window.setTimeout(qx.lang.Function.bind(function () {
						// Get all the shapes, there will consider at snapping
						// Consider only those elements who shares the same parent element
						var distShapes = this.facade.getCanvas().getChildShapes(true).findAll(function (value) {
							var parentShape = value.parent;
							while (parentShape) {
								if (elements.member(parentShape)) return false;
								parentShape = parentShape.parent
							}
							return true;
						})

						// The current selection will delete from this array
						//elements.each(function(shape) {
						//	distShapes = distShapes.without(shape);
						//});
						// For all these shapes
						distShapes.each((function (value) {
							if (!(value instanceof ms123.oryx.core.Edge)) {
								var ul = value.absoluteXY();
								var width = value.bounds.width();
								var height = value.bounds.height();

								// Add the upperLeft, center and lowerRight - Point to the distancePoints
								this.distPoints.push({
									ul: {
										x: ul.x,
										y: ul.y
									},
									c: {
										x: ul.x + (width / 2),
										y: ul.y + (height / 2)
									},
									lr: {
										x: ul.x + width,
										y: ul.y + height
									}
								});
							}
						}).bind(this));

					},this), 10)


				}
			}
		},

		/**
		 * Adjust an Point to the Snap Points
		 *
		 */
		snapToGrid: function (position) {

			// Get the current Bounds
			var bounds = this.dragBounds;

			var point = {};

			var ulThres = 6;
			var cThres = 10;
			var lrThres = 6;

			var scale = this.vLine ? this.vLine.getScale() : 1;

			var ul = {
				x: (position.x / scale),
				y: (position.y / scale)
			};
			var c = {
				x: (position.x / scale) + (bounds.width() / 2),
				y: (position.y / scale) + (bounds.height() / 2)
			};
			var lr = {
				x: (position.x / scale) + (bounds.width()),
				y: (position.y / scale) + (bounds.height())
			};

			var offsetX, offsetY;
			var gridX, gridY;

			// For each distant point
			this.distPoints.each(function (value) {

				var x, y, gx, gy;
				if (Math.abs(value.c.x - c.x) < cThres) {
					x = value.c.x - c.x;
					gx = value.c.x;
				}
/* else if (Math.abs(value.ul.x-ul.x) < ulThres){
				x = value.ul.x-ul.x;
				gx = value.ul.x;
			} else if (Math.abs(value.lr.x-lr.x) < lrThres){
				x = value.lr.x-lr.x;
				gx = value.lr.x;
			} */


				if (Math.abs(value.c.y - c.y) < cThres) {
					y = value.c.y - c.y;
					gy = value.c.y;
				}
/* else if (Math.abs(value.ul.y-ul.y) < ulThres){
				y = value.ul.y-ul.y;
				gy = value.ul.y;
			} else if (Math.abs(value.lr.y-lr.y) < lrThres){
				y = value.lr.y-lr.y;
				gy = value.lr.y;
			} */

				if (x !== undefined) {
					offsetX = offsetX === undefined ? x : (Math.abs(x) < Math.abs(offsetX) ? x : offsetX);
					if (offsetX === x) gridX = gx;
				}

				if (y !== undefined) {
					offsetY = offsetY === undefined ? y : (Math.abs(y) < Math.abs(offsetY) ? y : offsetY);
					if (offsetY === y) gridY = gy;
				}
			});


			if (offsetX !== undefined) {
				ul.x += offsetX;
				ul.x *= scale;
				if (this.vLine && gridX) this.vLine.update(gridX);
			} else {
				ul.x = (position.x - (position.x % (ms123.oryx.Config.GRID_DISTANCE / 2)));
				if (this.vLine) this.vLine.hide()
			}

			if (offsetY !== undefined) {
				ul.y += offsetY;
				ul.y *= scale;
				if (this.hLine && gridY) this.hLine.update(gridY);
			} else {
				ul.y = (position.y - (position.y % (ms123.oryx.Config.GRID_DISTANCE / 2)));
				if (this.hLine) this.hLine.hide();
			}

			return ul;
		},

		showGridLine: function () {

		},


		/**
		 * Redraw of the Rectangle of the SelectedArea
		 * param {Object} bounds
		 */
		resizeRectangle: function (bounds) {
			// Resize the Rectangle
			this.selectedRect.resize(bounds);
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
