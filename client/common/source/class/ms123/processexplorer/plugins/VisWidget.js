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
 @ignore(vis.Network)
 */

qx.Class.define("ms123.processexplorer.plugins.VisWidget", {
	extend: qx.ui.container.Scroll,
	construct: function (options, width, height) {
		this.base(arguments);

		this._width = width;
		this._options = options;
		this._height = height;

		this.setScrollbarX("off");
		this.setScrollbarY("off");

		this._visContainer = new qx.ui.embed.Html();
		this.setHeight(height);
		this._visContainer.setHeight(height);
		this._visContainer.setMinHeight(height);
		this._visContainer.setWidth(width);
		this._visContainer.setMaxWidth(width);
		this._visContainer.setMinWidth(width);
		this.add(this._visContainer);
		this.addListener("resize", this._onResize, this);

	},

	members: {
		_onResize:function(){
			console.log("bounds:"+JSON.stringify(this.getBounds(),null,2));
			var w = this.getBounds().width;
			this._visContainer.setWidth(w);
			this._visContainer.setMaxWidth(w);
			this._visContainer.setMinWidth(w);
			this._width = w;

			qx.event.Timer.once(function () {
				this.setData(this._data);
				this.selectNode(this._selectedNode);
			}, this, 200);


		},
		setData: function (data) {
			this._data = data;
			var el = this._visContainer.getContentElement().getDomElement();
			if( data != null){
				var network = new vis.Network(el, data, this._options);
				network.setSize(this._width, this._height)
				network.zoomExtent();
			}else{
				el.innerHTML='';
			}
			this._network = network;
		},
		selectNode:function(nodeid){
			if( nodeid==null) return;
			this._selectedNode=nodeid;
			try{
				this._network.selectNodes([nodeid]);
			}catch(e){
				console.error("VisWidget.selectNodes:"+e);
			}
		}
	}
});
