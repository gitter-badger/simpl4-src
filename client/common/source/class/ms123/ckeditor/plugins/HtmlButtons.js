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
	@ignore(CKEDITOR.*)

************************************************************************ */

qx.Class.define("ms123.ckeditor.plugins.HtmlButtons", {
	/**
	 * @ignore(CKEDITOR)
	 */
	defer: function () {
/*
 * @file HTML Buttons plugin for CKEditor
 * Copyright (C) 2012 Alfonso Martínez de Lizarrondo
 * A simple plugin to help create custom buttons to insert HTML blocks
 */
		try{
			if(!CKEDITOR) return;
		}catch(e){
			return;
		}

		CKEDITOR.plugins.add('htmlbuttons', {
			init: function (editor) {
				var buttonsConfig = editor.config.htmlbuttons;
				if (!buttonsConfig) return;

				function createCommand(definition) {
					return {
						exec: function (editor) {
						  console.log("insertHtml:"+definition.html);
							editor.insertHtml(definition.html);
						}
					};
				}

				// Create the command for each button
				for (var i = 0; i < buttonsConfig.length; i++) {
					var button = buttonsConfig[i];
					var commandName = button.name;
					editor.addCommand(commandName, createCommand(button, editor));

					/*editor.ui.addButton(commandName, {
						label: button.title,
						command: commandName
					});*/
				}
			} //Init
		});

		/**
		 * An array of buttons to add to the toolbar.
		 * Each button is an object with these properties:
		 *  name: The name of the command and the button (the one to use in the toolbar configuration)
		 *  icon: The icon to use. Place them in the plugin folder
		 *  html: The HTML to insert when the user clicks the button
		 *  title: Title that appears while hovering the button
		 *
		 * Default configuration with some sample buttons:
		 */
		CKEDITOR.config.htmlbuttons = [{
			name: 'h1',
			html: '<h1></h1>',
			title: 'H1'
		},
		{
			name: 'h2',
			html: '<h2></h2>',
			title: 'H2'
		},
		{
			name: 'h3',
			html: '<h3></h3>',
			title: 'H3'
		},
		{
			name: 'h4',
			html: '<h4></h4>',
			title: 'H4'
		}];

	}
});
