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
			var pack = sdesc.getPack ? sdesc.getPack() : sdesc;
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
