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
/*
*/

/**
	* @ignore(Hash)
*/
qx.Class.define("ms123.settings.Config", {
	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {
		SETTINGS_DATA: "data",
		SETTINGS_SCHEMANAME: "schema",
		PACK_DELIM: ":",
		DEFAULT_PACK: "data",
		AID_PACK: "aid",
		getPackName:function(entity){
			if( entity.indexOf(ms123.settings.Config.PACK_DELIM) >=0){
				return entity.split(ms123.settings.Config.PACK_DELIM)[0];	
			}
			return this.DEFAULT_PACK;
		},
		getEntityName:function(entity){
			if( entity.indexOf(ms123.settings.Config.PACK_DELIM) >=0){
				return entity.split(ms123.settings.Config.PACK_DELIM)[1];	
			}
			return entity;
		},
		getFqEntityName:function(entity,sdesc){
			if( entity.indexOf(ms123.settings.Config.PACK_DELIM) >=0){
				return entity;	
			}
			if( sdesc == null) return entity;
			var pack = sdesc.getPack ? sdesc.getPack() : sdesc;
			if( pack == null){
				return entity;
			}
			if( pack == this.DEFAULT_PACK){
				return entity;
			}
			if( pack == this.AID_PACK){
				return entity;
			}
			return pack + this.PACK_DELIM + entity;
		}
	}
});
