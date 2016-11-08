/* ************************************************************************

   qcl - the qooxdoo component library
  
   http://qooxdoo.org/contrib/project/qcl/
  
   Copyright:
     2007-2010 Christian Boulanger
  
   License:
     LGPL: http://www.gnu.org/licenses/lgpl.html
     EPL: http://www.eclipse.org/org/documents/epl-v10.php
     See the LICENSE file in the project's top-level directory for details.
  
   Authors:
   *  Christian Boulanger (cboulanger)
   *  saaj <mail@saaj.me>
  
************************************************************************ */

/**
 * Provides drag&drop to TreeVirtual. Currently, only the "move" action is
 * supported.
 */
qx.Class.define( "ms123.util.DragDropTree", {

	extend: qx.ui.treevirtual.TreeVirtual,
	include: [ qx.ui.treevirtual.MNode ],

	/*
	*****************************************************************************
	   CONSTRUCTOR
	*****************************************************************************
	*/
	construct: function( headings, custom ) {
		this._patchCodebase();

		custom = !custom ? {} : custom;
		custom.tablePaneHeader = function( obj ) {
			/*
			 * This is workaround for disabling draggable tree column.
			 * Also i could not override it by setting Scroller
			 * obj is tablePaneScroller
			 */
			var stub = function() {};
			obj._onChangeCaptureHeader = stub;
			obj._onMousemoveHeader = stub;
			obj._onMousedownHeader = stub;
			obj._onMouseupHeader = stub;
			obj._onClickHeader = stub;

			return new qx.ui.table.pane.Header( obj );
		};

		custom.treeDataCellRenderer = new ms123.util.SimpleTreeDataCellRenderer();
		this.base( arguments, headings, custom );

		this.setAllowDragTypes( [ "*" ] );
		this.setAllowDropTypes( [ "*" ] );

		this._createIndicator();
		this._link = false;

		this.addListener("appear", function() { 
				this.focus();
		}); 

		this.addListener( ms123.oryx.Config.EVENT_KEYDOWN, this._onKeyDown.bind( this ), false );
		this.addListener( ms123.oryx.Config.EVENT_KEYUP, this._onKeyUp.bind( this ), false );
	},

	/*
	*****************************************************************************
	   PROPERTIES
	*****************************************************************************
	*/
	properties: {

		/**
		 * Enable/disable drag and drop
		 */
		enableDragDrop: {
			check: "Boolean",
			apply: "_applyEnableDragDrop",
			event: "changeEnableDragDrop",
			init: false
		},

		/**
		 * a list of node types allowed to be dragged
		 */
		allowDragTypes: {
			check: "Array",
			nullable: true,
			init: null
		},

		/**
		 * drag action(s). If you supply an array, multiple drag actions will be added
		 */
		dragAction: {
			nullable: false,
			init: "move",
			apply: "_applyDragAction"
		},

		/**
		 * the number of milliseconds between scrolling up a row if drag cursor
		 * is on the first row or scrolling down if drag cursor is on last row
		 * during a drag session. You can turn off this behaviour by setting this
		 * property to null.
		 **/
		autoScrollInterval: {
			check: "Number",
			nullable: true,
			init: 100
		},

		/**
		 * whether it is possible to drop between nodes (i.e., for reordering them).
		 * the focus indicator changed to a line to mark where the insertion should take place
		 **/
		allowDropBetweenNodes: {
			check: "Boolean",
			init: true
		},

		/**
		 * array of two-element arrays containing a combination of drag source and
		 * drop target types. Type information is in the nodeTypeProperty of the
		 * userData hash map. If null, allow any combination. "*" can be used to as a
		 * wildcard, i.e. [ ['Foo','*'] ...] will allow the 'Foo' type node to be dropped on any
		 * other type, and [ ['*','Bar'] ...] will allow any type to be dropped on a 'Bar' type node.
		 * The array ['*'] will allow any combination, null will deny any drop.
		 **/
		allowDropTypes: {
			check: "Array",
			nullable: true,
			init: null
		},

		/**
		 * records the target node on which the drag objects has been dropped
		 **/
		dropTarget: {
			check: "Object",
			nullable: true,
			init: null
		},

		/**
		 * provide a hint on where the node has been dropped
		 * (-1 = above the node, 0 = on the node, 1 = below the node)
		 **/
		dropTargetRelativePosition: {
			check: [ -1, 0, 1 ],
			init: 0
		},

		/**
		 * Whether Drag & Drop should be limited to reordering
		 */
		allowReorderOnly: {
			check: "Boolean",
			init: false,
			event: "changeAllowReorderOnly"
		}

	},

	/*
	*****************************************************************************
	   EVENTS
	*****************************************************************************
	*/
	events: {
		/**
		 * Fired before a node is added to the tree. Returns the node, which
		 * can be manipulated.
		 */
		"beforeAddNode": "qx.event.type.Data",

		/**
		 * Fired when a node is remove from tree. Returns the node.
		 * Node will be deleted after event handling quits
		 * Not yet implemented, override prune method
		 */
		//"beforeDeleteNode"   : "qx.event.type.Data",

		/**
		 * Fired when a node changes the position. Returns an object:
		 * {
		 *    'node' : <the node which has changed position>
		 *    'position' : <numeric position within the parent node's children>
		 * }
		 */
		"changeNodePosition": "qx.event.type.Data"
	},

	/*
	*****************************************************************************
	   MEMBERS
	*****************************************************************************
	*/
	members: {

		/*
		---------------------------------------------------------------------------
		   PRIVATE MEMBERS
		---------------------------------------------------------------------------
		 */

		/**
		 * The indicator widget
		 */
		_indicator: null,

		/*
		---------------------------------------------------------------------------
		   INTERNAL METHODS
		---------------------------------------------------------------------------
		 */

		/**
		 * Patch the codebase to make drag & drop in the table possible in the first place
		 */
		_patchCodebase: function() {
			qx.Class.include( qx.ui.treevirtual.TreeVirtual, qx.ui.treevirtual.MNode );
			/*
			 * have not found official way to set validness check for events within widget.
			 * this only works with private optimization turned off
			 */
			qx.event.handler.DragDrop.prototype.setValidDrop = function( value ) {
				this.__validDrop = !!value;
			};
			qx.event.handler.DragDrop.prototype.getValidDrop = function() {
				return this.__validDrop;
			};

			qx.ui.table.pane.Scroller.prototype.getRowForPagePos = function( pageX, pageY ) {
				return this._getRowForPagePos( pageX, pageY );
			};
		},

		/**
		 * Create drop indicator
		 */
		_createIndicator: function() {
			this._indicator = new qx.ui.core.Widget();
			this._indicator.set( {
				backgroundColor: "#ff5252",
				zIndex: 100,
				droppable: true
			} );
			this._hideIndicator();

			this._getPaneClipper().add( this._indicator );
		},

		/**
		 * Hide indicator
		 */
		_hideIndicator: function() {
			this._indicator.setOpacity( 0 );
		},

		/**
		 * Show indicator
		 */
		_showIndicator: function() {
			this._indicator.setOpacity( 1.0 );
		},
		_onKeyUp: function( e ) {
			var iden = e.getKeyIdentifier();
			console.log( "_onKeyUp.iden:" + iden );
			if ( iden == "Shift" ) this._link = false;
		},
		_onKeyDown: function( e ) {
			var iden = e.getKeyIdentifier();
			console.log( "_onKeyDown.iden:" + iden );
			if ( iden == "Shift" ) this._link = true;
		},

		/**
		 * Check if drag element can be dropped
		 * @param sourceData {Map}
		 * @param dropTargetRelativePosition {Integer}
		 * @param dragDetails {Map}
		 * @return {Boolean}
		 */
		_checkDroppable: function( sourceData, dropTargetRelativePosition, dragDetails ) {
			/*
			 * get and save drag target
			 */
			var targetWidget = this;
			var targetRowData = this.getDataModel().getRowData( dragDetails.row );
			if ( !targetRowData ) {
				return false;
			}

			var targetNode = targetRowData[ 0 ];
			if ( !targetNode ) {
				return false;
			}

			var targetParentNode = this.nodeGet( targetNode.parentNodeId );
			this.setDropTarget( targetNode );
			this.setDropTargetRelativePosition( dropTargetRelativePosition );

			/*
			 * @todo the following has to be rewritten to work without the
			 * sourceData var. we should be able to get everything from the
			 * event data.
			 */
			if ( !sourceData ) {
				// we do not have any compatible datatype
				return false;
			}

			/*
			 * use only the first node to determine node type
			 */
			var sourceNode = sourceData.nodeData[ 0 ];
			if ( !sourceNode ) {
				/*
				 * no node to drag
				 */
				return false;
			}

			/*
			 * Whether drag & drop is limited to reordering
			 */
			if ( this.isAllowReorderOnly() ) {
				if ( dropTargetRelativePosition === 0 ) {
					return false;
				}
				if ( targetNode.level !== sourceNode.level ) {
					return false;
				}
			}

			var sourceWidget = sourceData.sourceWidget;

			/*
			 * if we are dragging within the same widget
			 */
			if ( sourceWidget == targetWidget ) {
				/*
				 * prevent drop of nodes on themself
				 */
				if ( sourceNode.nodeId == targetNode.nodeId ) {
					return false;
				}

				/*
				 * prevent drop of parents on children
				 */
				var traverseNode = targetNode;
				while ( traverseNode.parentNodeId ) {
					if ( traverseNode.parentNodeId == sourceNode.nodeId ) {
						return false;
					}
					traverseNode = this.nodeGet( traverseNode.parentNodeId );
				}
			}

			if ( dropTargetRelativePosition != 0 ) {
				if ( sourceNode.parentNodeId == targetNode.parentNodeId ) {
					return true;
				}
			}

			/*
			 * get allowed drop types. disallow drop if none
			 */
			var allowDropTypes = this.getAllowDropTypes();
			if ( !allowDropTypes ) {
				return false;
			}

			/*
			 * everything can be dropped, allow
			 */
			if ( allowDropTypes[ 0 ] == "*" ) {
				return true;
			}

			/*
			 * check legitimate source and target type combinations
			 */
			var sourceType = this.getNodeDragType( sourceNode );
			var targetTypeNode = ( dropTargetRelativePosition != 0 ) ? targetParentNode : targetNode;
			var targetType = this.getNodeDragType( targetTypeNode );

			if ( !targetType ) {
				return false;
			}

			for ( var i = 0; i < allowDropTypes.length; i++ ) {
				if (
					( allowDropTypes[ i ][ 0 ] == sourceType || allowDropTypes[ i ][ 0 ] == "*" ) &&
					( allowDropTypes[ i ][ 1 ] == targetType || allowDropTypes[ i ][ 1 ] == "*" )
				) {
					return true;
				}
			}

			/*
			 * do not allow any drop
			 */
			return false;
		},

		/**
		 * Handle behavior connected to automatic scrolling at the top and the
		 * bottom of the tree
		 *
		 * @param dragDetails {Map}
		 */
		_processAutoscroll: function( dragDetails ) {
			var interval = this.getAutoScrollInterval();
			var details = dragDetails;

			if ( interval ) {
				var scroller = this._getTreePaneScroller();

				if ( !this.__scrollFunctionId && ( details.topDelta > -1 && details.topDelta < 2 ) && details.row != 0 ) {
					// scroll up if drag cursor at the top
					this.__scrollFunctionId = window.setInterval( function() {
						scroller.setScrollY( parseInt( scroller.getScrollY() ) - details.rowHeight );
					}, interval );
				} else if ( !this.__scrollFunctionId && ( details.bottomDelta > 0 && details.bottomDelta < 3 ) ) {
					// scroll down if drag cursor is at the bottom
					this.__scrollFunctionId = window.setInterval( function() {
						scroller.setScrollY( parseInt( scroller.getScrollY() ) + details.rowHeight );
					}, interval );
				} else if ( this.__scrollFunctionId ) {
					window.clearInterval( this.__scrollFunctionId );
					this.__scrollFunctionId = null;
				}
			}
		},

		setIndicatorStyle: function( color ) {
			var domEl = this._indicator.getContentElement().getDomElement();
			if ( domEl ) {
				domEl.style.height = "8px";
				domEl.style.width = "24px";
				domEl.style.backgroundColor = color;
			}
		},
		_processDragInBetween: function( dragDetails ) {
			var dropTarget = this.getDropTarget();
			var color = "#9dcbfe";
			var pos = -1;
			if ( dropTarget && dropTarget.type === qx.ui.treevirtual.SimpleTreeDataModel.Type.BRANCH ) {
				pos = parseInt( dragDetails.deltaY / ( dragDetails.rowHeight / 3 ) ) - 1;
				if ( pos == 0 ) {
					color = "#ff5252";
				}
			} else {
				pos = parseInt( dragDetails.deltaY / ( dragDetails.rowHeight / 2 ) ) == 0 ? -1 : 1;
			}
			this.setIndicatorStyle( color );
			this._indicator.setDomTop( ( ( dragDetails.row - dragDetails.firstRow ) * dragDetails.rowHeight - 2 ) + dragDetails.deltaY );
			this._showIndicator();
			return pos;
		},

		/**
		 * Calculate indicator position and display indicator
		 * @param dragEvent {}
		 * @return {Map}
		 */
		_getDragDetails: function( dragEvent ) {
			// pane scroller widget takes care of mouse events
			var scroller = this._getTreePaneScroller();

			// calculate row and mouse Y position within row
			var paneClipperElem = this._getPaneClipper().getContentElement().getDomElement();
			var paneClipperTopY = qx.bom.element.Location.get( paneClipperElem, "box" ).top;
			var rowHeight = scroller.getTable().getRowHeight();
			var scrollY = scroller.getScrollY();
			if ( scroller.getTable().getKeepFirstVisibleRowComplete() ) {
				scrollY = Math.floor( scrollY / rowHeight ) * rowHeight;
			}

			var tableY = scrollY + dragEvent.getDocumentTop() - paneClipperTopY;
			var row = Math.floor( tableY / rowHeight );
			var deltaY = tableY % rowHeight;

			// calculate relative row position in table
			var firstRow = scroller.getChildControl( "pane" ).getFirstVisibleRow();
			var rowCount = scroller.getChildControl( "pane" ).getVisibleRowCount();
			var lastRow = firstRow + rowCount;
			var scrollY = parseInt( scroller.getScrollY() );
			var topDelta = row - firstRow;
			var bottomDelta = lastRow - row;

			return {
				rowHeight: rowHeight,
				row: row,
				deltaY: deltaY,
				firstRow: firstRow,
				topDelta: topDelta,
				bottomDelta: bottomDelta
			};
		},

		_getPaneClipper: function() {
			return this._getTreePaneScroller().getPaneClipper();
		},

		/**
		 * get tree column pane scroller widget
		 */
		_getTreePaneScroller: function() {
			var column = this.getDataModel().getTreeColumn();
			return this._getPaneScrollerArr()[ column ];
		},

		/*
		---------------------------------------------------------------------------
		   APPLY METHODS
		---------------------------------------------------------------------------
		 */

		/**
		 * enables or disables drag and drop, adds event listeners
		 */
		_applyEnableDragDrop: function( value, old ) {
			if ( old && !value ) {
				this.setDraggable( true );
				this.setDroppable( true );
				this.removeListener( "dragstart", this.__onDragStart, this );
				this.removeListener( "drag", this.__onDragAction, this );
				this.removeListener( "dragover", this.__onDragAction, this );
				this.removeListener( "dragend", this.__onDragEnd, this );
				this.removeListener( "dragleave", this.__onDragEnd, this );
				this.removeListener( "droprequest", this.__onDropRequest, this );

			}

			if ( value && !old ) {
				this.addListener( "dragstart", this.__onDragStart, this );
				this.addListener( "dragover", this.__onDragAction, this ); // dragover must be called *before* drag
				this.addListener( "dragleave", this.__onDragEnd, this );
				this.addListener( "drag", this.__onDragAction, this );
				this.addListener( "dragend", this.__onDragEnd, this );
				this.addListener( "droprequest", this.__onDropRequest, this );
				this.setDraggable( true );
				this.setDroppable( true );
			}
		},

		_applyDragAction: function( value, old ) {
			if ( value !== "move" ) {
				this.error( "Invalid drag action. Currently only 'move' is supported." );
			}
		},

		/*
		---------------------------------------------------------------------------
		   EVENT HANDLERS
		---------------------------------------------------------------------------
		 */

		/**
		 * Handles event fired whem a drag session starts.
		 * @param event {Object} the drag event fired
		 */
		__onDragStart: function( event ) {

			var selection = this.getDataModel().getSelectedNodes();
			var scroller = this._getTreePaneScroller();

			var wrongSelection = false;
			var dataModel = scroller.getTable().getTableModel();
			if ( selection.length > 0 ) {
				var nodeId = selection[ 0 ].nodeId;
				var row = dataModel.getRowFromNodeId( nodeId );
				var frow = this.getFocusedRow();
				if ( row != frow ) {
					wrongSelection = true;
				}
			}

			if ( wrongSelection || selection == null || selection.length == 0 ) {
				var row = this.getFocusedRow();
				if ( qx.lang.Type.isNumber( row ) ) {
					this.resetCellFocus();
					this.getSelectionModel().setSelectionInterval( row, row );
					selection = this.getDataModel().getSelectedNodes();
				}
			}
			if ( selection == null || selection.length == 0 ) {
				return event.preventDefault();
			}



			var types = this.getAllowDragTypes();

			/*
			 * no drag types, no drag is allowed
			 */
			if ( types === null ) {
				return event.preventDefault();
			}

			/*
			 * check drag type
			 */
			if ( types[ 0 ] != "*" ) {
				/*
				 * check for allowed types for all of the selection, i.e. if one
				 * doesn't match, drag is not allowed.
				 */
				for ( var i = 0; i < selection.length; i++ ) {
					var type = null;
					try {
						type = selection[ i ].data.DragDrop.type;
					} catch ( e ) {}
					/*
					 * type is not among the allowed types, do not allow drag
					 */
					if ( types.indexOf( type ) < 0 ) {
						return event.preventDefault();
					}
				}
			}

			// prepare drag data, old style
			var dragData = {
				'nodeData': selection,
				'sourceWidget': this,
				'action': this.getDragAction()
			};
			event.setUserData( "treevirtualnode", dragData );

			/*
			 * drag data, new style
			 */
			event.addAction( this.getDragAction() );
			event.addType( "qx/treevirtual-node" );
		},


		/**
		 * Handles the event fired when a drag session ends (with or without drop).
		 */
		__onDragEnd: function( e ) {
			this._hideIndicator();
		},

		/**
		 * Implementation of drag action for drag & dragover
		 * @param e {qx.event.type.Drag}
		 */
		__onDragAction: function( e ) {
			var target = e.getTarget();
			var sourceData = e.getUserData( "treevirtualnode" );
			var dragDetails = this._getDragDetails( e );
			var valid = false;

			/*
			 * show indicator if we're within the available rows
			 */
			if ( dragDetails.row < this.getDataModel().getRowCount() ) {
				/*
				 * auto-scroll at the beginning and at the end of the column
				 */
				this._processAutoscroll( dragDetails );

				/*
				 * show indicator and return the relative position
				 */
				var dropTargetRelativePosition = this._processDragInBetween( dragDetails );

				/*
				 * check if the dragged item can be dropped at the current
				 * position and change drag cursor accordingly
				 */
				var valid = this._checkDroppable( sourceData, dropTargetRelativePosition, dragDetails );
			}

			/*
			 * set flag whether drop is allowed
			 */
			e.getManager().setValidDrop( valid );

			/*
			 * drag curson
			 */
			if ( valid ) {
				qx.ui.core.DragDropCursor.getInstance().setAction( e.getCurrentAction() );
			} else {
				qx.ui.core.DragDropCursor.getInstance().resetAction();
			}
			return valid;
		},

		/**
		 * Drop request handler
		 * @param e {qx.event.type.Drag}
		 */
		__onDropRequest: function( e ) {
			this.__onDragEnd( e );
			var action = e.getCurrentAction();
			var type = e.getCurrentType();
			var source = e.getCurrentTarget();

			if ( type === "qx/treevirtual-node" ) {
				/*
				 * make a copy of the selection
				 */
				var selection = this.getSelectedNodes();
				var copy = [];
				for ( var i = 0, l = selection.length; i < l; i++ ) {
					if ( !qx.lang.Type.isObject( selection[ i ] ) ) {
						continue;
					}
					copy[ i ] = selection[ i ];
				}

				/*
				 * remove selection
				 */
				this.getSelectionModel().resetSelection();

				/*
				 * Add data to manager
				 */
				if ( copy.length ) {
					e.addData( type, copy );
				}


				return;
			}

			this.error( "Invalid type '" + type + "'" );

		},


		/*
		---------------------------------------------------------------------------
		   API METHODS
		---------------------------------------------------------------------------
		 */


		/**
		 * Move the dragged node from the source to the target node. Takes
		 * the drag even received by the "drop" even handler
		 * @param  e {qx.event.type.Drag}
		 */
		moveNode: function( e ) {
			var action = e.getCurrentAction() || "move";
			var dropTarget = this.getDropTarget();
			var dropPosition = this.getDropTargetRelativePosition();

			if ( !qx.lang.Type.isObject( dropTarget ) ) {
				//this.warn("No valid drop target!");
				return false;
			}

			/*
			 * this method only supports treevirtual nodes
			 */
			if ( e.supportsType( "qx/treevirtual-node" ) ) {
				if ( !dropTarget.children ) {
					this.error( "Drop target is not a folder!" );
					return false;
				}

				/*
				 * check action - only moving nodes is supported inside the
				 * tree
				 */
				if ( action !== "move" ) {
					this.error( "Only the 'move' action is supported." );
					return false;
				}

				/*
				 * dragged nodes
				 */
				var nodes = e.getData( "qx/treevirtual-node" );
				if ( !qx.lang.Type.isArray( nodes ) ) {
					this.error( "No dragged node data" );
					return false;
				}

				/*
				 * move nodes
				 */
				var nodeArr = this.getDataModel().getData();
				for ( var i = 0, l = nodes.length; i < l; i++ ) {
					var node = nodes[ i ];

					/*
					 * remove from parent node of dropped node
					 */
					if ( this._link === false ) {
						var parentNode = nodeArr[ node.parentNodeId ];
						if ( !parentNode ) this.error( "Cannot find the dropped node's parent node!" );
						var pnc = parentNode.children;
						pnc.splice( pnc.indexOf( node.nodeId ), 1 );
					} else {
						var nodeId = this.deepCopyNode( node );
						var nodeArr = this.getDataModel().getData();
						node = nodeArr[ nodeId ];
					}


					/*
					 * drop between nodes: add as a sibling of the drop target
					 */
					if ( this.getAllowDropBetweenNodes() ) {
						if ( dropPosition == 0 && dropTarget.type === qx.ui.treevirtual.SimpleTreeDataModel.Type.BRANCH ) {
							dropTarget.children.push( node.nodeId );
							node.parentNodeId = dropTarget.nodeId;
						} else {
							var targetParentNode = nodeArr[ dropTarget.parentNodeId ]
							if ( !targetParentNode ) this.error( "Cannot find the target node's parent node!" );
							var tpnc = targetParentNode.children;
							var delta = dropPosition > 0 ? 1 : 0;
							var position = tpnc.indexOf( dropTarget.nodeId ) + delta;
							console.log( "dropTarget(" + dropPosition + ";" + position + "):", dropTarget.label + "" );
							tpnc.splice( position, 0, node.nodeId );
							node.parentNodeId = targetParentNode.nodeId;
						}
						this.fireDataEvent( "changeNodePosition", {
							'node': node,
							'position': position
						} );
					}
				}

				/*
				 * re-render the tree
				 */
				this.getDataModel().setData();
			}
		},

		/**
		 * Creates an empty branch (=folder) object. This should really
		 * be part of the data model.
		 * @return {Object}
		 */
		createBranch: function( label, icon ) {
			return {
				type: qx.ui.treevirtual.SimpleTreeDataModel.Type.BRANCH,
				nodeId: null, // must be set
				parentNodeId: null, // must be set
				label: label,
				bSelected: false,
				bOpened: false,
				bHideOpenClose: false,
				icon: icon,
				iconSelected: icon,
				children: [],
				columnData: []
			};
		},

		/**
		 * Creates an empty leaf object. This should really
		 * be part of the data model.
		 * @return {Object}
		 */
		createLeaf: function( label, icon ) {
			var node = this.createBranch( label, icon );
			node.type = qx.ui.treevirtual.SimpleTreeDataModel.Type.LEAF;
			return node;
		},

		/**
		 * Imports a node into the tree at the current drop position. Takes
		 * the drag even received by the "drop" even handler and an array of
		 * node data. Make sure that the node data is valid, since it is not
		 * checked. You can create an empty node using the createBranch() and
		 * createLeaf() methods.
		 *
		 * @param e {qx.event.type.Drag}
		 * @param nodes {Object[]} Array of node data.
		 */
		importNode: function( target, position, nodes ) {
			var dropTarget = target || this.getDropTarget();
			var dropPosition = position != null ? position : this.getDropTargetRelativePosition();

			if ( !qx.lang.Type.isObject( dropTarget ) ) {
				//this.warn("No valid drop target!");
				return false;
			}

			if ( !dropTarget.children ) {
				this.error( "Drop target is not a folder!" );
				return false;
			}

			if ( !qx.lang.Type.isArray( nodes ) ) {
				this.error( "Invalid nodes data" );
				return false;
			}

			/*
			 * copy nodes
			 */
			for ( var i = 0, l = nodes.length; i < l; i++ ) {
				/*
				 * import the node into the tree's node array
				 */
				var nodeId = this.deepCopyNode( nodes[ i ] );
				var nodeArr = this.getDataModel().getData();
				var node = nodeArr[ nodeId ];

				if ( this.getAllowDropBetweenNodes() ) {
					var targetParentNode = nodeArr[ dropTarget.parentNodeId ]
					if ( !targetParentNode ) this.error( "Cannot find the target node's parent node!" );
					var tpnc = targetParentNode.children;
					var delta = dropPosition > 0 ? 1 : 0;
					tpnc.splice( tpnc.indexOf( dropTarget.nodeId ) + delta, 0, node.nodeId );
					node.parentNodeId = targetParentNode.nodeId;
				}
			}

			this.fireDataEvent( "beforeAddNode", node );
			this.getDataModel().setData();
		},

		deepCopyNode: function( node ) {
			var nodeArr = this.getDataModel().getData();
			var node = qx.lang.Object.clone( node, true );
			node.nodeId = nodeArr.length;
			nodeArr.push( node );
			var children = node.children;
			if ( children == null || children.length == 0 ) {
				return node.nodeId;
			}
			var newChildren = [];
			node.children = newChildren;
			for ( var i = 0; i < children.length; i++ ) {
				var nodeId = children[ i ];
				var newNodeId = this.deepCopyNode( nodeArr[ nodeId ] );
				newChildren.push( newNodeId );
			}
			return node.nodeId;
		},


		/**
		 * gets the (drag) type of a node
		 * @param nodeReference {Object|Integer}
		 * @return {Object} the user-supplied type of the node or null if not set
		 */
		getNodeDragType: function( nodeReference ) {
			try {
				if ( typeof nodeReference == "object" ) {
					return nodeReference.data.DragDrop.type;
				} else {
					return this.nodeGet( nodeReference ).data.DragDrop.type;
				}
			} catch ( e ) {
				return null;
			}
		},

		/**
		 * sets the (drag) type of a node
		 * @param nodeReference {Object|Integer}
		 * @param type {String}
		 */
		setNodeDragType: function( nodeReference, type ) {
			if ( typeof type != "string" ) {
				this.error( "Drag Type must be a string, got " + ( typeof type ) );
			}

			var node = this.nodeGet( nodeReference );
			if ( !node.data ) {
				node.data = {};
			}
			if ( !node.data.DragDrop ) {
				node.data.DragDrop = {};
			}

			node.data.DragDrop.type = type;
		}
	}
} );
