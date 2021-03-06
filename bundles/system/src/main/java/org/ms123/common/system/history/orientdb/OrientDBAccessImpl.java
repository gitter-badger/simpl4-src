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
package org.ms123.common.system.history.orientdb;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.Vertex;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
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
			v.setProperty("hint", checkNull(hint));
			v.setProperty("msg", msg);
			if (key1 != null) {
				v = orientGraph.addVertex("class:HistoryRoute");
				v.setProperty("routeId", key1);
				v.setProperty("instanceId", key2);
				v.setProperty("time", time);
			}
//			debug(this,"upsertHistory(key="+key+",\tkey1="+key1+",\tkey2="+key2+",\ttype="+type+")");
			orientGraph.commit();
		}catch( Exception e){
			error(this, "rollback:%[exception]s",e);
			orientGraph.rollback();
		}
	}

	private String checkNull(String s){
		if( s== null) return "";
		return s;
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
		info(this,"getHistory(" + (new Date(startTime)) + "," + (new Date(endTime)) + ":routeId:" + key + "  " + type);
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

		info(this,"getHistoryOne:" + key + "  " + type);
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
			info(this,"getActivitiCamelCorrelation:" + e.getMessage());
			return null;
		}
		return ret;
	}

	private void createClassAndIndex(){
		try{
			OSchemaProxy schema = orientGraph.getRawGraph().getMetadata().getSchema();
			if( schema.getClass("History") != null){
				return;
			}
			m_orientdbService.executeUpdate(orientGraph, "CREATE CLASS History EXTENDS V");
			m_orientdbService.executeUpdate(orientGraph, "CREATE PROPERTY History.key STRING");
			m_orientdbService.executeUpdate(orientGraph, "CREATE INDEX History.key ON History ( key ) NOTUNIQUE");
		}catch( Exception e){
			error(this, "createClassAndIndex:%[exception]s",e);
			e.printStackTrace();
		}
	}

	private void initHistory() {
		if (orientGraph != null) {
			return;
		}
		try {
			OrientGraphFactory factory = m_orientdbService.getFactory(HISTORY_DATABASE,false);
			orientGraph = factory.getTx();
			createClassAndIndex();
		} catch (Exception e) {
			info(this,"OrientDBAccessImpl.initHistory:" + e.getMessage());
			orientGraph = null;
			e.printStackTrace();
		}
	}

}

