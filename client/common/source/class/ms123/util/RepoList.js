/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
qx.Class.define("ms123.util.RepoList", {
 extend: qx.core.Object,
 include : [ qx.locale.MTranslation],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (facade,formElement,config) {
		this.base(arguments);
		var repoList = this._getRepos(formElement);
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_getRepos: function (formElement) {
			var completed = (function (ret) {
				var repoList = [];
				repoList.push("-");
				for( var i = 0; i< ret.length;i++){
					var r = ret[i];
					repoList.push( r.name );
				}
				console.log("repoList.rel:"+JSON.stringify(repoList,null,2));
				for(var i=0; i < repoList.length; i++){
					var repoName = repoList[i];
					var listItem = new qx.ui.form.ListItem(repoName,null,repoName);
					if( formElement.addItem){
						formElement.addItem(listItem);
					}else{
						formElement.add(listItem);
					}
				}
				return repoList;
			}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("namespace.getNamespaces") + ":" + details.message);
			}).bind(this);

			var params = {
				service: "git",
				method: "getRepositories",
				parameter: {},
				context: this,
				async: true,
				completed: completed,
				failed: failed
			}
			ms123.util.Remote.rpcAsync(params);
		}
	}
});
