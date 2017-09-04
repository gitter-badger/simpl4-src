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
	* @ignore(Clazz)
*/
qx.Class.define("ms123.processexplorer.plugins.DefinitionDiagram", {
	extend: qx.ui.tabview.Page,
	include: [qx.locale.MTranslation],

	/**
	 * Constructor
	 */
	construct: function (facade) {
		this.base(arguments,this.tr("processexplorer.definition_diagramm"),"icon/16/actions/format-justify-fill.png");
		this.facade = facade;
		this.setLayout(new qx.ui.layout.HBox());

		this.scroll = new qx.ui.container.Scroll();
		this.add(this.scroll,{flex:1});


		this.facade.registerOnEvent(ms123.processexplorer.Config.EVENT_PROCESSDEFINITION_CHANGED, this._handleEvent.bind(this));
		this.facade.registerOnEvent(ms123.processexplorer.Config.EVENT_PROCESSDEPLOYMENT_CHANGED, this._handleEventDeplomentChanged.bind(this));
		this.setEnabled(false);
	},

	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {
		_showDiagram:function(){
			var source = ms123.util.Remote.rpcSync("process:getDefinitionDiagram", {
					processDefinitionId:this._processDefinition.id
				});
			var image = new qx.ui.basic.Image(source);
			this.scroll.add(image);
		},
		_handleEventDeplomentChanged: function (e) {
			this.setEnabled(false);
		},
		_handleEvent: function (e) {
			this._processDefinition = e.processDefinition;
			this._showDiagram();
			this.setEnabled(true);
		}
	}
});
