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
	@asset(qx/icon/${qx.icontheme}/22/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/places/*)
	@asset(qx/icon/${qx.icontheme}/22/status/*)
	@asset(qx/icon/${qx.icontheme}/16/apps/*)
	@asset(qx/icon/${qx.icontheme}/16/mimetypes/*)

	@asset(ms123/icons/*)
	@asset(ms123/*)
*/

qx.Class.define("ms123.config.BasisEditor", {
 extend: qx.core.Object,
 include : qx.locale.MTranslation,


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_managedApp:null,
		_removeUnusedFields: function( fields, selectables ){
			var ret = [];
			for(var i=0; i< fields.length;i++){
				if( this._contains( selectables,fields[i] )){
					ret.push(fields[i]);
				}
			}
			return ret;
		},
		_contains: function (selectables, f) {
			for(var i=0; i< selectables.length;i++){
				if( selectables[i].name == f.name ) return true;
			}
			return false;
		},
		_convertValue:function(f,tableColumns){
			for( var j=0; j< tableColumns.length;j++){
				var col = tableColumns[j];
				if( col.type == "DoubleSelectBox" ){
					var val = f[col.name];
					console.log("\tvorher:"+val);
					if( val && typeof val == "object" && val.length > 0){
						f[col.name] = qx.util.Serializer.toJson(val);
					}else{
						f[col.name] = null;
					}
					console.log("\tnacher:"+f[col.name]);
				}
			}					
		},
		
		_prefixUrl: function(url ){
			if( this._managedApp ){
				return "/"+this._managedApp+"/"+url;
			}else{
				return url;
			}
		},
		_translate: function (o) {
			if (typeof o == "string") {
				if (o.match(/^%/)) {
					var tr = this.tr(o.substring(1));
					if (tr) {
						o = tr;
					}
				}
				return o;
			}
			for (var i in o) {
 			  if (typeof o[i] == "function")continue;
				o[i] = this._translate(o[i]);
			}
			return o;
		}
	}
});
