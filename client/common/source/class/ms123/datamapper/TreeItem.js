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
qx.Class.define("ms123.datamapper.TreeItem", {
  extend : qx.ui.tree.core.AbstractTreeItem,
	construct: function (side) {
		this.base(arguments);
		this._side = side;
		this.addListenerOnce("changeModel",function(ev){
			this._setId();
		},this);
	},

  properties : {
    appearance : {
      refine : true,
      init : "tree-folder"
    }
  },

  events : {
    open : "qx.event.type.Event"
	},
	members: {
		_addWidgets: function () {
			this.addSpacer();
			this.addOpenButton();
			this.addIcon();
			this.addLabel();
		},
		setTitle:function(name){
			var label = this.getChildControl("label");
			if( this.getModel().getFieldType ){
				label.setValue( name + " : "+ this.getModel().getFieldType() );
			}else{
				label.setValue( name );
			}
		},
		_setId: function () {
			this.getContentElement().addClass(this._side+'TreeItem');
			var label = this.getChildControl("label");
			label.setTextColor(ms123.datamapper.Config.TREE_LABEL_COLOR);
			label.getContentElement().addClass(ms123.datamapper.Config.IDPREFIX+this._side+'TreeItemLabel');
			//this.setTitle( this.getModel().getName() );

			var open = this.getChildControl("open");
			open.getContentElement().addClass('treeItemOpener');
    	open.addListener("click", function(e){
				var data = {
					item:this,
					open:open.hasState("opened")
				};
      	this.fireDataEvent("open", data);
			},this);
		}
	}
});
