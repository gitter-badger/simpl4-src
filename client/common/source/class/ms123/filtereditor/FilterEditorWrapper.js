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
	@ignore(jQuery)
	@asset(qx/icon/${qx.icontheme}/22/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/places/*)
	@asset(qx/icon/${qx.icontheme}/22/status/*)
	@asset(qx/icon/${qx.icontheme}/16/apps/*)
	@asset(qx/icon/${qx.icontheme}/16/mimetypes/*)
	@asset(qx/icon/${qx.icontheme}/16/categories/*)

	@asset(ms123/icons/*)
	@asset(ms123/*)
************************************************************************ */

qx.Class.define("ms123.filtereditor.FilterEditorWrapper", {
	extend: ms123.filtereditor.FilterEditor,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this.base(arguments,context);
		this.context = context;

		if( this.context.data.type == ms123.config.ConfigManager.CS_ENTITY) {
			this.context.window.close();
			ms123.form.Dialog.alert(this.tr("data.filters.filter_not_editable"));
			return;
		}
		this.parentComponent = this.context.window;
		this._filterProps = context.data;
		this._refreshData();
		this.addListener("save",this._saveFilter,this);
		this._init();
		this._createJsonWindow();
		context.window.setCaption("Filter Editor("+this.context.data.name+")");
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_refreshData: function () {
			var id = this._filterProps.id;
			if (id != undefined && id) {
			}else{
				id = this._filterProps.name+"_"+this._filterProps.modulename;
			}
			var url = "data/filter/" + id;
			var map = ms123.util.Remote.sendSync(url + "?what=asRow");
			this._filterProps = map;
			return id;
		},
		_saveFilter:function(e){
			var filterProps = e.getData();
			var url = "data/filter/" + filterProps.id + "?what=asRow";
			var completed = function (e) {
				ms123.form.Dialog.alert(this.tr("data.filters.saved"));
			};
			var failed = function (e) {
				ms123.form.Dialog.alert(this.tr("data.filters.savefailed:" + e));
			};
			var pdata = "modulename=" + filterProps.modulename;
			pdata += "&user=" + filterProps.user;
			pdata += "&type=" + filterProps.type;
			pdata += "&description=" + filterProps.description;
			pdata += "&exclusion=" + encodeURIComponent(qx.util.Serializer.toJson(filterProps.exclusion));
			pdata += "&filter=" + encodeURIComponent(qx.util.Serializer.toJson(filterProps.filter));
			pdata += "&fields=" + encodeURIComponent(qx.util.Serializer.toJson(filterProps.fields));

			var params = {
				url: url,
				method: "PUT",
				data: pdata,
				async: true,
				context: this,
				completed: completed,
				failed: failed
			}
			ms123.util.Remote.send(params);
			return true;
		}
	}
});
