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
	@asset(qx/icon/${qx.icontheme}/16/actions/dialog-ok.png)
	@asset(qxe/demo/*)
*/

/**
 * This is the main application class of your custom application "qxe"
 */
qx.Class.define("ms123.wizard.ExampleWizard", {
	extend: qx.core.Object,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */
	construct: function (context) {
		this.base(arguments);

		var wizard = new ms123.wizard.Wizard();
console.log("ExampleWizard:"+wizard);

		wizard.add(this.createPage1("Step 1"));
		wizard.add(this.createPage2("Step 2"));
		wizard.add(this.createPage3("Step 3"));
		wizard.add(this.createPage4("Step 4"));
		wizard.add(this.createPage5("Step 5"));

			context.window.setLayout(new qx.ui.layout.Grow());
		context.window.add( wizard);
		context.window.open();
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */
	members: {
		createPage1: function () {
			var page = new ms123.wizard.Page("Step 1");
			page.setLayout(new qx.ui.layout.Canvas());

			var composite = new qx.ui.container.Composite(new qx.ui.layout.HBox(4));

			var label = new qx.ui.basic.Label("Input label");
			label.setAlignY("middle");

			var textField = new qx.ui.form.TextField("Input textfield");

			composite.add(label);
			composite.add(textField);

			page.add(composite, {
				left: 10,
				top: 10
			});

			return page;
		},

		createPage2: function () {
			var page = new ms123.wizard.Page("Step 2");
			page.setLayout(new qx.ui.layout.Canvas());

			var composite = new qx.ui.container.Composite(new qx.ui.layout.HBox(4));

			var label = new qx.ui.basic.Label("Input label");
			label.setAlignY("middle");

			var textField = new qx.ui.form.TextField("Input textfield");

			composite.add(label);
			composite.add(textField);

			page.add(composite, {
				left: 10,
				top: 10
			});

			return page;
		},

		createPage3: function () {
			var page = new ms123.wizard.Page("Step 3");
			page.setLayout(new qx.ui.layout.Canvas());

			var composite = new qx.ui.container.Composite(new qx.ui.layout.HBox(4));

			var label = new qx.ui.basic.Label("Input label");
			label.setAlignY("middle");

			var textField = new qx.ui.form.TextField("Input textfield");

			composite.add(label);
			composite.add(textField);

			page.add(composite, {
				left: 10,
				top: 10
			});

			return page;
		},

		createPage4: function () {
			var page = new ms123.wizard.Page("Step 4");
			page.setLayout(new qx.ui.layout.Canvas());

			var composite = new qx.ui.container.Composite(new qx.ui.layout.HBox(4));

			var label = new qx.ui.basic.Label("Input label");
			label.setAlignY("middle");

			var textField = new qx.ui.form.TextField("Input textfield");

			composite.add(label);
			composite.add(textField);

			page.add(composite, {
				left: 10,
				top: 10
			});

			return page;
		},

		createPage5: function () {
			var page = new ms123.wizard.Page("Step 5");
			page.setLayout(new qx.ui.layout.Canvas());

			var composite = new qx.ui.container.Composite(new qx.ui.layout.HBox(4));

			var label = new qx.ui.basic.Label("Input label");
			label.setAlignY("middle");

			var textField = new qx.ui.form.TextField("Input textfield");

			composite.add(label);
			composite.add(textField);

			page.add(composite, {
				left: 10,
				top: 10
			});

			return page;
		}
	}
});
