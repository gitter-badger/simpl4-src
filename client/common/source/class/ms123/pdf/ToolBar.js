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
 * @ignore(PDFJS.*)
 * @ignore(Promise*)
 * @lint ignoreDeprecated(alert,eval)
 * @ignore(alert*)
 */
qx.Class.define("ms123.pdf.ToolBar", {
	extend: qx.ui.container.Composite,

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (pdfView) {
		this.base(arguments);
		this.pdfView = pdfView;
		this.homeScale = "page-fit";
		this.setLayout(new qx.ui.layout.Canvas());
		var toolbar = new qx.ui.toolbar.ToolBar();
		var homeButton = this._createButton("", "resource/openseadragon/home_rest.png");
		homeButton.addListener("execute", function () {
			pdfView.setScale(this.homeScale, true);
		}, this);
		var zoominButton = this._createButton("", "resource/openseadragon/zoomin_rest.png");
		zoominButton.addListener("execute", function () {
			pdfView.zoomIn();
		}, this);
		var zoomoutButton = this._createButton("", "resource/openseadragon/zoomout_rest.png");
		zoomoutButton.addListener("execute", function () {
			pdfView.zoomOut();
		}, this);

		toolbar.add(zoominButton);
		toolbar.add(zoomoutButton);
		toolbar.add(homeButton);
		toolbar.setBackgroundColor(null);
		toolbar.setDecorator(null);
		this.add(toolbar, {
			top: 0,
			left: 0
		});

		/*var label = new qx.ui.basic.Label("Input label");
		this.label = label;
		this.add(label, {
			top: 0,
			left: 100
		});*/
		//pdfView.addListener("mousemove", this.hover.bind(this));
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
		hover: function (evt) {
			var box = this.getContentLocation("box");
			var mouseX = evt.getDocumentLeft() - box.left
			var mouseY = evt.getDocumentTop() - box.top
			var n = evt._native;
			this.label.setValue("XY:" + this.pdfView.contentElement.scrollLeft + "/" + this.pdfView.contentElement.scrollTop + "|" + mouseX + "/" + mouseY);
		},
		_createButton: function (text, icon) {
			var b = new qx.ui.toolbar.Button(null, icon);
			b.setPaddingTop(0);
			b.setPaddingBottom(0);
			b.setPaddingRight(0);
			b.setDecorator(null);
			b.setBackgroundColor(null);
			return b;
		},
		setScale: function (scale) {
			if (scale == null) {
				this.homeScale = 'page-fit';
			} else {
				this.homeScale = scale;
			}
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
