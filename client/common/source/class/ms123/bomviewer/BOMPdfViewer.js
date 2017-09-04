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
 * @ignore(jQuery.ajax) 
 * @ignore(jQuery.each)
 * @ignore(jQuery.inArray)
 */
qx.Class.define("ms123.bomviewer.BOMPdfViewer", {
	extend: ms123.bomviewer.BOMViewer,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this.base(arguments, context);
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createViewer: function () {
			var url = '/sw/resource/SD_2100097.pdf';
			var mapUrl = '/sw/resource/png_2100097/map.xml';
			var v = new ms123.pdf.PDFViewer({
				url: url,
				scale: "page-height",
				hotspots: this._getHotspots(mapUrl)
			});
			return v;
		},
		_openViewer: function (part) {
			var url = '/sw/resource/SD_' + part + '.pdf';
			var mapUrl = '/sw/resource/png_' + part + '/map.xml';
			this._viewer.open(url, this._getHotspots(mapUrl), "page-width");
		}
	},
	destruct: function () {
		console.error("BOMViewer.close");
		this._viewer.destroy();
	}
});
