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
package org.ms123.common.system.history.orientdb;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ms123.common.system.history.HistoryAccess;
import org.ms123.common.system.history.HistoryService;
import org.ms123.common.system.orientdb.OrientDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 *
 */
@SuppressWarnings("unchecked")
public class OrientDBAccessImpl implements HistoryAccess, HistoryService {
	protected OrientDBService m_orientdbService;
	protected OrientGraph orientGraph;

	public OrientDBAccessImpl(OrientDBService cs) {
		m_orientdbService = cs;
	}

	public void close() {
	}

	public synchronized  void upsertHistory(String key, Date time, String type, String hint, String msg) {
		initHistory();
		try{
			String key1 = null;
			String key2 = null;
			if (type != null && HISTORY_CAMEL_TRACE.equals(type)) {
				int pipe = key.lastIndexOf("|");
				if (pipe > 0) {
					key1 = key.substring(0, pipe);
					key2 = key.substring(pipe + 1);
				}
			}
			Vertex v = orientGraph.addVertex("class:History");
			v.setProperty("key", key);
			v.setProperty("time", time);
			v.setProperty("type", type);
			v.setProperty("hint", hint);
			v.setProperty("msg", msg);
			if (key1 != null) {
				v = orientGraph.addVertex("class:HistoryRoute");
				v.setProperty("routeId", key1);
				v.setProperty("instanceId", key2);
				v.setProperty("time", time);
			}
			orientGraph.commit();
		}catch( Exception e){
			orientGraph.rollback();
		}
	}

	public synchronized void upsertAcc(String activitiId, String routeInstanceId) {
		initHistory();
		try{
			Vertex v = orientGraph.addVertex("class:ActivityCamel");
			v.setProperty("activitiId", activitiId);
			v.setProperty("time", new Date());
			v.setProperty("routeInstanceId", routeInstanceId);
			orientGraph.commit();
		}catch( Exception e){
			orientGraph.rollback();
		}
	}

	public List<Map> getHistory(String key, String type, Long startTime, Long endTime) throws Exception {
		final List<Map> retList = new ArrayList();
		initHistory();
		if (startTime == null) {
			startTime = new Date().getTime() - (long) 1 * 1000 * 60 * 60 * 24;
		}
		if (endTime == null) {
			endTime = new Date().getTime() + 1000000;
		}
		info("getHistory(" + (new Date(startTime)) + "," + (new Date(endTime)) + ":routeId:" + key + "  " + type);
		Map<String, String> doubleMap = new HashMap<String, String>();
		if (type != null && HISTORY_CAMEL_TRACE.equals(type) && key.indexOf("|") < 0) {
			OCommandRequest query = new OSQLSynchQuery("select routeId,instanceId,time from HistoryRoute where routeId=? and time > ? and time <? order by time");
			Iterable<Element> result = orientGraph.command(query).execute(key, new Date(startTime), new Date(endTime));
			for (Element elem : result) {
				Map<String, Object> map = new HashMap<String, Object>();
				String _key = elem.getProperty("routeId") + "|" + elem.getProperty("instanceId");
				if (doubleMap.get(_key) == null) {
					doubleMap.put(_key, "");
					List<Map> lm = _getOneEntry(_key, type);
					retList.addAll(lm);
				}
			}
		} else {
			retList.addAll(_getOneEntry(key, type));
		}
		return retList;
	}

	private List<Map> _getOneEntry(String key, String type) {
		List<Map> retList = new ArrayList();

		info("getHistoryOne:" + key + "  " + type);
		OCommandRequest query = new OSQLSynchQuery("select key,time,type,hint,msg from History where key=? and type=? order by time");
		Iterable<Element> result = orientGraph.command(query).execute(key, type);
		for (Element elem : result) {
			Map m = new HashMap();
			m.put(HISTORY_KEY, elem.getProperty("key"));
			m.put(HISTORY_TIME, elem.getProperty("time"));
			m.put(HISTORY_TYPE, elem.getProperty("type"));
			m.put(HISTORY_HINT, elem.getProperty("hint"));
			m.put(HISTORY_MSG, elem.getProperty("msg"));
			retList.add(m);
		}
		return retList;
	}

	public Set<String> getActivitiCamelCorrelation(String activitiId) throws Exception {
		Set<String> ret = new LinkedHashSet<String>();

		try {
			OCommandRequest query = new OSQLSynchQuery("select routeInstanceId from ActivityCamel  where activitiId=?");
			Iterable<Element> result = orientGraph.command(query).execute(activitiId);
			for (Element elem : result) {
				ret.add(elem.getProperty("routeInstanceId"));
			}
		} catch (Exception e) {
			info("getActivitiCamelCorrelation:" + e.getMessage());
			return null;
		}
		return ret;
	}

	private void initHistory() {
		if (orientGraph != null) {
			return;
		}
		try {
			OrientGraphFactory factory = m_orientdbService.getFactory(HISTORY_DATABASE);
			orientGraph = factory.getTx();
		} catch (Exception e) {
			info("OrientDBAccessImpl.initHistory:" + e.getMessage());
			orientGraph = null;
			e.printStackTrace();
		}
	}

	protected static void info(String msg) {
		System.err.println(msg);
		m_logger.info(msg);
	}

	protected static void debug(String msg) {
		m_logger.debug(msg);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(OrientDBAccessImpl.class);
}

