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
 * A wizard consists of a collection of pages which are all one step in the
 * wizard.
 */
qx.Class.define("ms123.wizard.Page", {
	extend: qx.ui.groupbox.GroupBox,
	implement: ms123.wizard.IPage,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */
	/**
	 * param legend {String} The label of the page.
	 * param icon {String} The icon of the page.
	 */
	construct: function (legend, icon) {
		this.base(arguments, legend, icon);
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	properties: {
		// overridden
		appearance: {
			refine: true,
			init: "wizard-page"
		},

		/**
		 * Previous page to navigate to.
		 */
		previous: {
			check: "ms123.wizard.Page",
			nullable: true,
			init: null,
			event: "changePrevious"
		},

		/**
		 * Next page to navigate to.
		 */
		next: {
			check: "ms123.wizard.Page",
			nullable: true,
			init: null,
			event: "changeNext"
		},

		/**
		 * Whether to allow to go to the previous wizard pane.
		 */
		allowPrevious: {
			check: "Boolean",
			init: false,
			event: "changeAllowPrevious"
		},

		/**
		 * Whether to allow to go to the next wizard pane.
		 */
		allowNext: {
			check: "Boolean",
			init: false,
			event: "changeAllowNext"
		}
	}
});
