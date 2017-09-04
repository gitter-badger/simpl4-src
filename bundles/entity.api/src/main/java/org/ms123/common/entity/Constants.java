/**
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
package org.ms123.common.entity.api;

import java.util.*;

public interface Constants {


	public final String RIGHT_ENTITY = "rightmodule";
	public final String LEFT_ENTITY = "leftmodule";
	public final String RIGHT_FIELD = "rightfield";
	public final String LEFT_FIELD = "leftfield";
	public final String RELATION = "relation";
	public final String DATAPACK_PREFIX1 = "app.";
	public final String DATAPACK_PREFIX2 = "data.";

	public final String STATE_FIELD = "_state";
	public final String STATE_OK = "ok";
	public final String STATE_DUP = "dup";
	public final String STATE_NEW = "new";
	public final String STATE_DEL = "del";
	public final String STATE_REFID = "_dup_refid";
	public final String DISABLE_STATESELECT = "disableStateSelect";
	public final List<Map> m_defaultFields = new ArrayList<Map>() {

		{
			add(new HashMap<String, Object>() {

				{
					put("id", "_owner");
					put("name", "_owner");
					put("datatype", "string");
					put("readonly", true);
					put("formula_in", "if(_isnew){ _user } else{ \"_ignore_\"}");
				}
			});
			add(new HashMap<String, Object>() {

				{
					put("id", "_created_by");
					put("name", "_created_by");
					put("datatype", "string");
					put("readonly", true);
					put("formula_in", "if(_isnew){ _user } else{ \"_ignore_\"}");
				}
			});
			add(new HashMap<String, Object>() {

				{
					put("id", "_updated_by");
					put("name", "_updated_by");
					put("datatype", "string");
					put("readonly", true);
					put("formula_in", "_user");
				}
			});
			add(new HashMap<String, Object>() {

				{
					put("id", "_created_at");
					put("name", "_created_at");
					put("datatype", "date");
					put("readonly", true);
					put("formula_in", "if(_isnew){ new Date().getTime() } else {\"_ignore_\"}");
				}
			});
			add(new HashMap<String, Object>() {

				{
					put("id", "_updated_at");
					put("name", "_updated_at");
					put("datatype", "date");
					put("readonly", true);
					put("formula_in", "new Date().getTime()");
				}
			});
			add(new HashMap<String, String>() {

				{
					put("id", "_team_list");
					put("name", "_team_list");
					put("datatype", "array/team");
					put("edittype", "treemultiselect");
					put("selectable_items", "rpc:team:getTeamTree,namespace:\"${NAMESPACE}\",mapping:{value:\"teamid\",title:\"description\",name:\"name\",tooltip:\"(name+'/'+description)\"}");
					put("search_options", "['bw','eq','ne']");
				}
			});
		}
	};
	public final List<Map> m_stateFields = new ArrayList<Map>() {

		{
			add(new HashMap<String, Object>() {

				{
					put("id", "_state");
					put("name", "_state");
					put("datatype", "string");
					put("selectable_items", "["+
																			"{\"value\":null,\"label\":\"%composite.stateselect.new\"},"+
																			"{\"value\":\""+STATE_OK+"\",\"label\":\"%composite.stateselect.ok\"},"+
																			"{\"value\":\""+STATE_DUP+"\",\"label\":\"%composite.stateselect.dup\"}"+
																	"]");
					put("edittype", "select");
					put("readonly", false);
				}
			});
			add(new HashMap<String, Object>() {

				{
					put("id", "_dup_refid");
					put("name", "_dup_refid");
					put("datatype", "string");
					put("readonly", true);
				}
			});
		}
	};

	public final List<Map> m_teamFields = new ArrayList<Map>() {

		{
			add(new HashMap<String, String>() {

				{
					put("id", "_team_list");
					put("name", "_team_list");
					put("datatype", "array/team");
					put("edittype", "treemultiselect");
					put("selectable_items", "rpc:team:getTeamTree,namespace:\"${NAMESPACE}\",mapping:{value:\"teamid\",title:\"description\",name:\"name\",tooltip:\"(name+'/'+description)\"}");
					put("search_options", "['bw','eq','ne']");
				}
			});
		}
	};
}
