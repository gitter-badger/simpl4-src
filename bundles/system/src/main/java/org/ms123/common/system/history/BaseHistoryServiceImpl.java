/**
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
import org.ms123.common.cassandra.CassandraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("unchecked")
abstract class BaseHistoryServiceImpl implements HistoryService {
	protected CassandraService m_cassandraService;
	protected CassandraAccess cassandraAccess;

	private static String STARTTIME = "startTime";
	private static String ENDTIME = "endTime";
	private static String STATUS = "status";

	protected List<Map> _getRouteInstances(String contextKey, String routeId, java.lang.Long _startTime, java.lang.Long endTime) throws Exception {
		List<Map> retList = new ArrayList();
		List<Map> historyEntries = this.cassandraAccess.getHistory(contextKey + "/" + routeId, HISTORY_CAMEL_TRACE, _startTime, endTime);
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
		List<Map> historyEntries = this.cassandraAccess.getHistory(contextKey + "/" + routeId + "|" + exchangeId, HISTORY_CAMEL_TRACE, null, null);
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
		System.err.println(msg);
		m_logger.info(msg);
	}

	protected static void debug(String msg) {
		m_logger.debug(msg);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(HistoryServiceImpl.class);
}

