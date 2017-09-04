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
 */
qx.Class.define('ms123.pdf.PDFViewer', {
	extend: qx.ui.container.Composite,
	include: [qx.locale.MTranslation],
	implement: ms123.bomviewer.IDrawingViewer,
	events: {
		"hotspot": "qx.event.type.Data"
	},

	construct: function (context) {
		this.base(arguments);
		this.setLayout(new qx.ui.layout.Canvas());


		context.controlContainer = this;
		var view = new ms123.pdf.PDFView(context);
		view.addListener("hotspot", function (e) {
			this.fireDataEvent("hotspot", e.getData(), null);
		}, this);
		var toolbar = new ms123.pdf.ToolBar(view);
		this.m_viewer = view;
		this.m_toolbar = toolbar;
		toolbar.setScale(context.scale);
		this.add(view, {
			width: "100%",
			height: "100%",
			top: 0,
			left: 0
		});
		this.add(toolbar, {
			top: 0,
			left: 0
		});
	},

	properties: {},

	members: {
		open: function (url, hotspots, scale) {
			this.m_toolbar.setScale(scale);
			this.m_viewer.open(url, hotspots, scale);
		},
		close: function () {
			this.m_viewer.close();
		},
		destroy: function () {},
		selectHotspot: function (href) {
			this.m_viewer.selectHotspot(href);
		}
	}
});
