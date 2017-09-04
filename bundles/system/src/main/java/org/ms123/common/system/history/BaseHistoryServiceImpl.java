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
package org.ms123.common.system.history;

import flexjson.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("unchecked")
abstract class BaseHistoryServiceImpl implements HistoryService {
	protected HistoryAccess historyAccess;

	private static String STARTTIME = "startTime";
	private static String ENDTIME = "endTime";
	private static String STATUS = "status";

	protected List<Map> _getRouteInstances(String contextKey, String routeId, java.lang.Long _startTime, java.lang.Long endTime) throws Exception {
		List<Map> retList = new ArrayList();
		List<Map> historyEntries = this.historyAccess.getHistory(contextKey + "/" + routeId, HISTORY_CAMEL_TRACE, _startTime, endTime);
		String currentKey = null;
		Date startTime = null;
		Date prevTime = null;
		boolean hasError = false;
		for (Map entry : historyEntries) {
			String key = (String) entry.get(HISTORY_KEY);
			if ("error".equals(entry.get(HISTORY_HINT))) {
				hasError = true;
			}
			if (!key.equals(currentKey)) {
				if (startTime != null) {
					retList.add(createRecord(startTime, prevTime, currentKey, hasError));
					hasError = false;
				}
				startTime = (Date) entry.get(HISTORY_TIME);
				currentKey = key;
			}
			prevTime = (Date) entry.get(HISTORY_TIME);
		}
		if (startTime != null) {
			retList.add(createRecord(startTime, prevTime, currentKey, hasError));
		}
		sortListByStartTime(retList);
		return retList;
	}

	protected List<Map> _getRouteInstance(String contextKey, String routeId, String exchangeId) throws Exception {
		List<Map> historyEntries = this.historyAccess.getHistory(contextKey + "/" + routeId + "|" + exchangeId, HISTORY_CAMEL_TRACE, null, null);
		return historyEntries;
	}

	private Map createRecord(Date startTime, Date endTime, String key, boolean hasError) {
		Map retMap = new HashMap();
		retMap.put(STARTTIME, startTime);
		retMap.put(ENDTIME, endTime);
		retMap.put(STATUS, hasError ? "error" : "ok");
		int lastSlash = key.lastIndexOf("|");
		retMap.put("exchangeId", key.substring(lastSlash + 1));
		return retMap;
	}

	private void sortListByStartTime(List<Map> list) {
		Collections.sort(list, new TComparable());
	}

	private static class TComparable implements Comparator<Map> {
		@Override
		public int compare(Map m1, Map m2) {
			Date l1 = (Date) m1.get(STARTTIME);
			Date l2 = (Date) m2.get(STARTTIME);
			return l2.compareTo(l1);
		}
	}

	protected static void info(String msg) {
		System.out.println(msg);
		m_logger.info(msg);
	}

	protected static void debug(String msg) {
		m_logger.debug(msg);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(HistoryServiceImpl.class);
}

