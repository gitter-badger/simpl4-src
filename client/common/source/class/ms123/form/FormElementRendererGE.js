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
 * Form renderer renderer for {@link qx.ui.form.Form}. 
 */
qx.Class.define("ms123.form.FormElementRendererGE", {
	extend: qx.ui.container.Composite,
	construct: function (formElement, elementDesc) {
		this.base(arguments);
		this._formElement = formElement;
		this._childs = elementDesc.childShapes;
		this.setLayout(new qx.ui.layout.Dock(3, 3));
		var width = elementDesc.bounds.lowerRight.x - elementDesc.bounds.upperLeft.x;
		var height = elementDesc.bounds.lowerRight.y - elementDesc.bounds.upperLeft.y;
		//	this._formElement.setMinWidth(width);
		this._formElement.setWidth(width);
		this._formElement.setMinHeight(height);

		var stencilId = elementDesc.stencil.id.toLowerCase();
		if (stencilId == "textarea") {
			this._formElement._setHeight(height);
		}
		if (stencilId == "input" || stencilId == "moduleselector") {
			this._formElement.setAllowGrowY(false);
		}
		if (stencilId == "tableselect") {
			this._formElement.setHeight(height);
			this._formElement.setAllowGrowY(true);
		}
		if (stencilId == "alert") {
			this._formElement.setHeight(height);
			this._formElement.setAllowGrowY(true);
		}

		this._formElement.setAllowGrowX(true);
		this.add(this._formElement, {
			edge: "center"
		});
		this._createWidget();
	},

	members: {
		_createWidget: function () {
			for (var i = 0; i < this._childs.length; i++) {
				var child = this._childs[i];
				var stencilId = child.stencil.id.toLowerCase();
				var properties = child.properties;
				switch (stencilId) {
				case "label":
					var text = properties.xf_text;
					if( text.match(/^@/)){
						text = this.tr(text.substring(1));
					}				
					var label = new qx.ui.basic.Label(text);
					this.add(label, {
						edge: "north"
					});
					break;
				}
			}
		}
	}
});
