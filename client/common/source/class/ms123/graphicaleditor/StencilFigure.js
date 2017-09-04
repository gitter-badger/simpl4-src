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

qx.Class.define("ms123.graphicaleditor.StencilFigure", {
	extend: ms123.draw2d.SVGFigure,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (stencil,width,height) {
		this.base(arguments,stencil.getView(),width,height);
		this.outputPort = null;
		this.setDimension(50, 50);
	},

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {},

	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		setWorkflow: function ( /*:draw2d.Workflow*/ workflow) {
			this.base(arguments, workflow);

			if (workflow !== null && this.outputPort === null) {
				this.outputPort = new ms123.draw2d.OutputPort();
				this.outputPort.setMaxFanOut(5); // It is possible to add "5" Connector to this port
 				this.outputPort.setName("output");
				this.outputPort.setWorkflow(workflow);
				this.outputPort.setBackgroundColor(new ms123.draw2d.Color(245, 115, 115));
				this.addPort(this.outputPort, this.width, this.height / 2);
			}
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
