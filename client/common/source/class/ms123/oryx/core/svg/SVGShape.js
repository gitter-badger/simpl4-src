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
	* @ignore(SVGRectElement)
	* @ignore(SVGCircleElement)
	* @ignore(SVGImageElement)
	* @ignore(SVGPolygonElement)
	* @ignore(SVGEllipseElement)
	* @ignore(PathParser)
	* @ignore(SVGPathElement)
	* @ignore(SVGPolylineElement)
	* @ignore(SVGLineElement)
	* @ignore(SVGGElement)
*/

qx.Class.define("ms123.oryx.core.svg.SVGShape", {
	extend: qx.core.Object,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (svgElem) {
		this.base(arguments);
		this.type;
		this.element = svgElem;
		this.x = undefined;
		this.y = undefined;
		this.width = undefined;
		this.height = undefined;
		this.oldX = undefined;
		this.oldY = undefined;
		this.oldWidth = undefined;
		this.oldHeight = undefined;
		this.radiusX = undefined;
		this.radiusY = undefined;
		this.isHorizontallyResizable = false;
		this.isVerticallyResizable = false;
		//this.anchors = [];
		this.anchorLeft = true;
		this.anchorRight = false;
		this.anchorTop = true;
		this.anchorBottom = false;

		//attributes of path elements of edge objects
		this.allowDockers = true;
		this.resizeMarkerMid = false;

		this.editPathParser;
		this.editPathHandler;

		this.init(); //initialisation of all the properties declared above.
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
		 * Initializes the values that are defined in the constructor.
		 */
		init: function () {

			/**initialize position and size*/
			if (ms123.oryx.Editor.checkClassType(this.element, SVGRectElement) || ms123.oryx.Editor.checkClassType(this.element, SVGImageElement)) {
				this.type = "Rect";

				var xAttr = this.element.getAttributeNS(null, "x");
				if (xAttr) {
					this.oldX = parseFloat(xAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var yAttr = this.element.getAttributeNS(null, "y");
				if (yAttr) {
					this.oldY = parseFloat(yAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var widthAttr = this.element.getAttributeNS(null, "width");
				if (widthAttr) {
					this.oldWidth = parseFloat(widthAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var heightAttr = this.element.getAttributeNS(null, "height");
				if (heightAttr) {
					this.oldHeight = parseFloat(heightAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}

			} else if (ms123.oryx.Editor.checkClassType(this.element, SVGCircleElement)) {
				this.type = "Circle";

				var cx = undefined;
				var cy = undefined;
				//var r = undefined;
				var cxAttr = this.element.getAttributeNS(null, "cx");
				if (cxAttr) {
					cx = parseFloat(cxAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var cyAttr = this.element.getAttributeNS(null, "cy");
				if (cyAttr) {
					cy = parseFloat(cyAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var rAttr = this.element.getAttributeNS(null, "r");
				if (rAttr) {
					//r = parseFloat(rAttr);
					this.radiusX = parseFloat(rAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				this.oldX = cx - this.radiusX;
				this.oldY = cy - this.radiusX;
				this.oldWidth = 2 * this.radiusX;
				this.oldHeight = 2 * this.radiusX;

			} else if (ms123.oryx.Editor.checkClassType(this.element, SVGEllipseElement)) {
				this.type = "Ellipse";

				var cx = undefined;
				var cy = undefined;
				//var rx = undefined;
				//var ry = undefined;
				var cxAttr = this.element.getAttributeNS(null, "cx");
				if (cxAttr) {
					cx = parseFloat(cxAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var cyAttr = this.element.getAttributeNS(null, "cy");
				if (cyAttr) {
					cy = parseFloat(cyAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var rxAttr = this.element.getAttributeNS(null, "rx");
				if (rxAttr) {
					this.radiusX = parseFloat(rxAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var ryAttr = this.element.getAttributeNS(null, "ry");
				if (ryAttr) {
					this.radiusY = parseFloat(ryAttr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				this.oldX = cx - this.radiusX;
				this.oldY = cy - this.radiusY;
				this.oldWidth = 2 * this.radiusX;
				this.oldHeight = 2 * this.radiusY;

			} else if (ms123.oryx.Editor.checkClassType(this.element, SVGLineElement)) {
				this.type = "Line";

				var x1 = undefined;
				var y1 = undefined;
				var x2 = undefined;
				var y2 = undefined;
				var x1Attr = this.element.getAttributeNS(null, "x1");
				if (x1Attr) {
					x1 = parseFloat(x1Attr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var y1Attr = this.element.getAttributeNS(null, "y1");
				if (y1Attr) {
					y1 = parseFloat(y1Attr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var x2Attr = this.element.getAttributeNS(null, "x2");
				if (x2Attr) {
					x2 = parseFloat(x2Attr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				var y2Attr = this.element.getAttributeNS(null, "y2");
				if (y2Attr) {
					y2 = parseFloat(y2Attr);
				} else {
					throw "Missing attribute in element " + this.element;
				}
				this.oldX = Math.min(x1, x2);
				this.oldY = Math.min(y1, y2);
				this.oldWidth = Math.abs(x1 - x2);
				this.oldHeight = Math.abs(y1 - y2);

			} else if (ms123.oryx.Editor.checkClassType(this.element, SVGPolylineElement) || ms123.oryx.Editor.checkClassType(this.element, SVGPolygonElement)) {
				this.type = "Polyline";

				var pointsArray = [];
				if (this.element.points && this.element.points.numberOfItems) {
					for (var i = 0, size = this.element.points.numberOfItems; i < size; i++) {
						pointsArray.push(this.element.points.getItem(i).x)
						pointsArray.push(this.element.points.getItem(i).y)
					}
				} else {
					var points = this.element.getAttributeNS(null, "points");
					if (points) {
						points = points.replace(/,/g, " ");
						pointsArray = points.split(" ");
						pointsArray = pointsArray.without("");
					} else {
						throw "Missing attribute in element " + this.element;
					}
				}


				if (pointsArray && pointsArray.length && pointsArray.length > 1) {
					var minX = parseFloat(pointsArray[0]);
					var minY = parseFloat(pointsArray[1]);
					var maxX = parseFloat(pointsArray[0]);
					var maxY = parseFloat(pointsArray[1]);

					for (var i = 0; i < pointsArray.length; i++) {
						minX = Math.min(minX, parseFloat(pointsArray[i]));
						maxX = Math.max(maxX, parseFloat(pointsArray[i]));
						i++;
						minY = Math.min(minY, parseFloat(pointsArray[i]));
						maxY = Math.max(maxY, parseFloat(pointsArray[i]));
					}

					this.oldX = minX;
					this.oldY = minY;
					this.oldWidth = maxX - minX;
					this.oldHeight = maxY - minY;
				} else {
					throw "Missing attribute in element " + this.element;
				}

			} else if (ms123.oryx.Editor.checkClassType(this.element, SVGPathElement)) {
				this.type = "Path";

				this.editPathParser = new PathParser();
				this.editPathHandler = new ms123.oryx.core.svg.EditPathHandler();
				this.editPathParser.setHandler(this.editPathHandler);

				var parser = new PathParser();
				var handler = new ms123.oryx.core.svg.MinMaxPathHandler();
				parser.setHandler(handler);
				parser.parsePath(this.element);

				this.oldX = handler.minX;
				this.oldY = handler.minY;
				this.oldWidth = handler.maxX - handler.minX;
				this.oldHeight = handler.maxY - handler.minY;

				delete parser;
				delete handler;
			} else {
				throw "Element is not a shape.";
			}

			/** initialize attributes of oryx namespace */
			//resize
			var resizeAttr = this.element.getAttributeNS(ms123.oryx.Config.NAMESPACE_ORYX, "resize");
			if (resizeAttr) {
				resizeAttr = resizeAttr.toLowerCase();
				if (resizeAttr.match(/horizontal/)) {
					this.isHorizontallyResizable = true;
				} else {
					this.isHorizontallyResizable = false;
				}
				if (resizeAttr.match(/vertical/)) {
					this.isVerticallyResizable = true;
				} else {
					this.isVerticallyResizable = false;
				}
			} else {
				this.isHorizontallyResizable = false;
				this.isVerticallyResizable = false;
			}

			//anchors
			var leftSet = false;
			var topSet = false;
			var anchorAttr = this.element.getAttributeNS(ms123.oryx.Config.NAMESPACE_ORYX, "anchors");
			if (anchorAttr) {
				anchorAttr = anchorAttr.replace("/,/g", " ");
				var anchors = anchorAttr.split(" ").without("");

				for (var i = 0; i < anchors.length; i++) {
					switch (anchors[i].toLowerCase()) {
					case "left":
						this.anchorLeft = true;
						leftSet=true;
						break;
					case "right":
						this.anchorRight = true;
						break;
					case "top":
						this.anchorTop = true;
						leftSet=false;
						break;
					case "bottom":
						this.anchorBottom = true;
						this.anchorTop = false;
						break;
					}
				}
			}
			if( this.anchorBottom && leftSet==false){
				this.anchorLeft= false;
			}

			if( this.anchorBottom && topSet==false){
				this.anchorTop= false;
			}
			if( this.anchorRight && leftSet==false){
				this.anchorLeft= false;
			}

			if( this.anchorRight && topSet==false){
				this.anchorTop= false;
			}
			//allowDockers and resizeMarkerMid
			if (ms123.oryx.Editor.checkClassType(this.element, SVGPathElement)) {
				var allowDockersAttr = this.element.getAttributeNS(ms123.oryx.Config.NAMESPACE_ORYX, "allowDockers");
				if (allowDockersAttr) {
					if (allowDockersAttr.toLowerCase() === "no") {
						this.allowDockers = false;
					} else {
						this.allowDockers = true;
					}
				}

				var resizeMarkerMidAttr = this.element.getAttributeNS(ms123.oryx.Config.NAMESPACE_ORYX, "resizeMarker-mid");
				if (resizeMarkerMidAttr) {
					if (resizeMarkerMidAttr.toLowerCase() === "yes") {
						this.resizeMarkerMid = true;
					} else {
						this.resizeMarkerMid = false;
					}
				}
			}

			this.x = this.oldX;
			this.y = this.oldY;
			this.width = this.oldWidth;
			this.height = this.oldHeight;
		},

		/**
		 * Writes the changed values into the SVG element.
		 */
		update: function () {
				var xAttr = this.element.getAttributeNS(ms123.oryx.Config.NAMESPACE_ORYX, "sw");
				if( xAttr && xAttr.indexOf("abs")!=-1) return;

			if (this.x !== this.oldX || this.y !== this.oldY || this.width !== this.oldWidth || this.height !== this.oldHeight) {
				switch (this.type) {
				case "Rect":
					if (this.x !== this.oldX) this.element.setAttributeNS(null, "x", this.x);
					if (this.y !== this.oldY) this.element.setAttributeNS(null, "y", this.y);
					if (this.width !== this.oldWidth) this.element.setAttributeNS(null, "width", this.width);
					if (this.height !== this.oldHeight) this.element.setAttributeNS(null, "height", this.height);
					break;
				case "Circle":
					//calculate the radius
					//var r;
					//					if(this.width/this.oldWidth <= this.height/this.oldHeight) {
					//						this.radiusX = ((this.width > this.height) ? this.width : this.height)/2.0;
					//					} else {
					this.radiusX = ((this.width < this.height) ? this.width : this.height) / 2.0;
					//}
					this.element.setAttributeNS(null, "cx", this.x + this.width / 2.0);
					this.element.setAttributeNS(null, "cy", this.y + this.height / 2.0);
					this.element.setAttributeNS(null, "r", this.radiusX);
					break;
				case "Ellipse":
					this.radiusX = this.width / 2;
					this.radiusY = this.height / 2;

					this.element.setAttributeNS(null, "cx", this.x + this.radiusX);
					this.element.setAttributeNS(null, "cy", this.y + this.radiusY);
					this.element.setAttributeNS(null, "rx", this.radiusX);
					this.element.setAttributeNS(null, "ry", this.radiusY);
					break;
				case "Line":
					if (this.x !== this.oldX) this.element.setAttributeNS(null, "x1", this.x);

					if (this.y !== this.oldY) this.element.setAttributeNS(null, "y1", this.y);

					if (this.x !== this.oldX || this.width !== this.oldWidth) this.element.setAttributeNS(null, "x2", this.x + this.width);

					if (this.y !== this.oldY || this.height !== this.oldHeight) this.element.setAttributeNS(null, "y2", this.y + this.height);
					break;
				case "Polyline":
					var points = this.element.getAttributeNS(null, "points");
					if (points) {
						points = points.replace(/,/g, " ").split(" ").without("");

						if (points && points.length && points.length > 1) {

							//TODO what if oldWidth == 0?
							var widthDelta = (this.oldWidth === 0) ? 0 : this.width / this.oldWidth;
							var heightDelta = (this.oldHeight === 0) ? 0 : this.height / this.oldHeight;

							var updatedPoints = "";
							for (var i = 0; i < points.length; i++) {
								var x = (parseFloat(points[i]) - this.oldX) * widthDelta + this.x;
								i++;
								var y = (parseFloat(points[i]) - this.oldY) * heightDelta + this.y;
								updatedPoints += x + " " + y + " ";
							}
							this.element.setAttributeNS(null, "points", updatedPoints);
						} else {
							//TODO error
						}
					} else {
						//TODO error
					}
					break;
				case "Path":
					//calculate scaling delta
					//TODO what if oldWidth == 0?
					var widthDelta = (this.oldWidth === 0) ? 0 : this.width / this.oldWidth;
					var heightDelta = (this.oldHeight === 0) ? 0 : this.height / this.oldHeight;

					//use path parser to edit each point of the path
					this.editPathHandler.init(this.x, this.y, this.oldX, this.oldY, widthDelta, heightDelta);
					this.editPathParser.parsePath(this.element);

					//change d attribute of path
					this.element.setAttributeNS(null, "d", this.editPathHandler.d);
					break;
				}

				this.oldX = this.x;
				this.oldY = this.y;
				this.oldWidth = this.width;
				this.oldHeight = this.height;
			}

			// Remove cached variables
			delete this.visible;
			delete this.handler;
		},

		isPointIncluded: function (pointX, pointY) {

			// Check if there are the right arguments and if the node is visible
			if (!pointX || !pointY || !this.isVisible()) {
				return false;
			}

			switch (this.type) {
			case "Rect":
				return (pointX >= this.x && pointX <= this.x + this.width && pointY >= this.y && pointY <= this.y + this.height);
				break;
			case "Circle":
				//calculate the radius
				//				var r;
				//				if(this.width/this.oldWidth <= this.height/this.oldHeight) {
				//					r = ((this.width > this.height) ? this.width : this.height)/2.0;
				//				} else {
				//				 	r = ((this.width < this.height) ? this.width : this.height)/2.0;
				//				}
				return ms123.oryx.core.Math.isPointInEllipse(pointX, pointY, this.x + this.width / 2.0, this.y + this.height / 2.0, this.radiusX, this.radiusX);
				break;
			case "Ellipse":
				return ms123.oryx.core.Math.isPointInEllipse(pointX, pointY, this.x + this.radiusX, this.y + this.radiusY, this.radiusX, this.radiusY);
				break;
			case "Line":
				return ms123.oryx.core.Math.isPointInLine(pointX, pointY, this.x, this.y, this.x + this.width, this.y + this.height);
				break;
			case "Polyline":
				var points = this.element.getAttributeNS(null, "points");

				if (points) {
					points = points.replace(/,/g, " ").split(" ").without("");

					points = points.collect(function (n) {
						return parseFloat(n);
					});

					return ms123.oryx.core.Math.isPointInPolygone(pointX, pointY, points);
				} else {
					return false;
				}
				break;
			case "Path":

				// Cache Path handler
				if (!this.handler) {
					var parser = new PathParser();
					this.handler = new ms123.oryx.core.svg.PointsPathHandler();
					parser.setHandler(this.handler);
					parser.parsePath(this.element);
				}

//console.error("pointHandler.poinbts:"+JSON.stringify(this.handler.points,null,2));
				return ms123.oryx.core.Math.isPointInPolygone(pointX, pointY, this.handler.points);

				break;
			default:
				return false;
			}
		},

		/**
		 * Returns true if the element is visible
		 * param {SVGElement} elem
		 * return boolean
		 */
		isVisible: function (elem) {

			if (this.visible !== undefined) {
				return this.visible;
			}

			if (!elem) {
				elem = this.element;
			}

			var hasOwnerSVG = false;
			try {
				hasOwnerSVG = !! elem.ownerSVGElement;
			} catch (e) {}

			// Is SVG context
			if (hasOwnerSVG) {
				// IF G-Element
				if (ms123.oryx.Editor.checkClassType(elem, SVGGElement)) {
					if (elem.className && elem.className.baseVal == "me") {
						this.visible = true;
						return this.visible;
					}
				}

				// Check if fill or stroke is set
				var fill = elem.getAttributeNS(null, "fill");
				var stroke = elem.getAttributeNS(null, "stroke");
				if (fill && fill == "none" && stroke && stroke == "none") {
					this.visible = false;
				} else {
					// Check if displayed
					var attr = elem.getAttributeNS(null, "display");
					if (!attr) this.visible = this.isVisible(elem.parentNode);
					else if (attr == "none") this.visible = false;
					else this.visible = true;
				}
			} else {
				this.visible = true;
			}

			return this.visible;
		},

		toString: function () {
			return (this.element) ? "SVGShape " + this.element.id : "SVGShape " + this.element;
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
