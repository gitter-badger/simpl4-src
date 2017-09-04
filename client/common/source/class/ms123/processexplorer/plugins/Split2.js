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
qx.Class.define("ms123.processexplorer.plugins.Split2", { 
   extend : qx.ui.container.Composite, 

   construct: function(top, bottom){ 
     this.base(arguments); 

     this.setLayout(new qx.ui.layout.VBox()); 
     var topSplitPane = new qx.ui.splitpane.Pane("vertical").set({decorator: null}); 

     // width top <-> bottomSplitPane -> 1/3 <-> 2/3 
     topSplitPane.add(top, 1); 
     topSplitPane.add(bottom, 2); 

     this.add(topSplitPane, {flex: 1}); 
   } 
}); 
