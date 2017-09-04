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
qx.Class.define("ms123.graphicaleditor.Split3", {
	extend: qx.ui.container.Composite,

	construct: function (shapeRepository, editor, propertyPanel,container,direction,place) {
		this.base(arguments);

		this.setLayout(new qx.ui.layout.HBox());
		container.dialogPanel=propertyPanel;

		var mainSplitPane = new qx.ui.splitpane.Pane("horizontal").set({
			decorator: null
		});
		var rightSplitPane = editor;
		var leftSplitPane = shapeRepository;
		if( ["right", "center_bottom"].indexOf(place) != -1){
			rightSplitPane = new qx.ui.splitpane.Pane(direction).set({
				decorator: null
			});
		}else{
			leftSplitPane = new qx.ui.splitpane.Pane("vertical").set({
				decorator: null
			});
			if( ["right_top"].indexOf(place) != -1){
				leftSplitPane.add(propertyPanel, 8);
				leftSplitPane.add(shapeRepository, 5);
			}else{
				leftSplitPane.add(shapeRepository, 5);
				leftSplitPane.add(propertyPanel, 8);
			}
			container.dialogPanel=leftSplitPane;
		}

		if( ["right_bottom", "right_top"].indexOf(place) != -1){
			mainSplitPane.add(rightSplitPane, 10);
			mainSplitPane.add(leftSplitPane, place=="right" ? 2 : 4);
		}else{
			mainSplitPane.add(leftSplitPane, place=="right" ? 2 : 4);
			mainSplitPane.add(rightSplitPane, 10);
		}

		if( ["right", "center_bottom"].indexOf(place) != -1){
			rightSplitPane.add(editor, 6);
			rightSplitPane.add(propertyPanel, place=="right"? 3 : 2);
		}

		this.add(mainSplitPane, {
			flex: 1
		});
	}
});
