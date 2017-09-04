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
package org.ms123.common.activiti;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;

/**
 */
@SuppressWarnings("unchecked")
public abstract class AbstractPaginateList {

	/**
   * @param query The query to get the paged list from
   * @param listName The name model attribute name to use for the result list
   * @param model The model to put the list and the pagination attributes in
   * @param defaultSort THe default sort column (the rest attribute) that later will be mapped to an internal engine name
   */
	@SuppressWarnings("rawtypes")
	public Map paginateList(Map<String, Object> params, Query query, String defaultSort, Map<String, QueryProperty> properties) {
		// Collect parameters
		int start = Util.getInteger(params, "start", 0);
		int size = Util.getInteger(params, "size", 1000);
		String sort = Util.getString(params, "sort", defaultSort);
		String order = Util.getString(params, "order", "asc");
		// Sort order
		if (sort != null && properties.size() > 0) {
			QueryProperty qp = properties.get(sort);
			if (qp == null) {
				throw new RuntimeException("Value for param 'sort' is not valid, '" + sort + "' is not a valid property");
			}
			((AbstractQuery) query).orderBy(qp);
			if (order.equals("asc")) {
				query.asc();
			} else if (order.equals("desc")) {
				query.desc();
			} else {
				throw new RuntimeException("Value for param 'order' is not valid : '" + order + "', must be 'asc' or 'desc'");
			}
		}
		// Get result and set pagination parameters
		List list = processList(query.listPage(start, size));
		Map response = new HashMap();
		response.put("start", start);
		response.put("size", list.size());
		response.put("sort", sort);
		response.put("order", order);
		response.put("total", query.count());
		response.put("data", list);
		return response;
	}

	@SuppressWarnings("rawtypes")
	protected abstract List processList(List list);
}
