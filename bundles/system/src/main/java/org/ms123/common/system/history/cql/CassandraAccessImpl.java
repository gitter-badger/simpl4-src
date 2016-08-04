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
package org.ms123.common.system.history.cql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.ms123.common.cassandra.CassandraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Session;
import org.ms123.common.system.history.CassandraAccess;
import org.ms123.common.system.history.HistoryService;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.core.querybuilder.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.lt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.asc;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

/**
 *
 */
@SuppressWarnings("unchecked")
public class CassandraAccessImpl implements CassandraAccess,HistoryService {
	protected CassandraService m_cassandraService;
	protected Session m_session;
	private PreparedStatement insertHistory;
	private PreparedStatement insertHistoryRoute;
	private PreparedStatement insertActivitiCamelCorrelation;
	private PreparedStatement selectActivitiCamelCorrelation;
	private PreparedStatement selectHistory;
	private PreparedStatement selectHistoryRoute;

	public CassandraAccessImpl(CassandraService cs){
		m_cassandraService = cs;
		initSession();
	}
	public void close(){
		info("CassandraAccessImpl.close");
		if (m_session != null && !m_session.isClosed()) {
			m_session.close();
		}
	}
	public void upsertHistory(String key, Date date, String type, String hint, String msg) {
		String key1 = null;
		String key2 = null;
		if (type != null && HISTORY_CAMEL_TRACE.equals(type)) {
			int pipe = key.lastIndexOf("|");
			if (pipe > 0) {
				key1 = key.substring(0, pipe);
				key2 = key.substring(pipe + 1);
			}
		}
		UUID time = UUIDs.startOf(date.getTime());
		debug("upsertHistory:"+key+"/"+type);
		BoundStatement boundStatement = null;
		boundStatement = new BoundStatement(insertHistory);
		boundStatement.setString(0, key);
		boundStatement.setUUID(1, time);
		boundStatement.setString(2, type);
		boundStatement.setString(3, hint);
		boundStatement.setString(4, msg);
		m_session.execute(boundStatement );
		if (key1 != null) {
			boundStatement = new BoundStatement(insertHistoryRoute);
			boundStatement.setString(0, key1);
			boundStatement.setString(1, key2);
			boundStatement.setUUID(2, time);
			m_session.execute(boundStatement );
		}
	}

	public void upsertAcc(String activitiId, String routeInstanceId) {
		UUID time = UUIDs.startOf(new Date().getTime());
		BoundStatement boundStatement = new BoundStatement(insertActivitiCamelCorrelation);
		boundStatement.setString(0, activitiId);
		boundStatement.setUUID(1, time);
		boundStatement.setString(2, routeInstanceId);
		m_session.execute(boundStatement);
	}

	public List<Map> getHistory(String key, String type, Long startTime, Long endTime) throws Exception {
		debug("getHistory.start:"+key+"/"+type);
		final List<Map> retList = new ArrayList();
		if (startTime == null) {
			startTime = new Date().getTime() - (long) 1 * 1000 * 60 * 60 * 24;
		}
		if (endTime == null) {
			endTime = new Date().getTime() + 1000000;
		}

		UUID uuidStart = UUIDs.startOf(startTime);
		UUID uuidEnd = UUIDs.startOf(endTime);
		Map<String,String> doubleMap = new HashMap<String,String>();
		if (type != null && HISTORY_CAMEL_TRACE.equals(type) && key.indexOf("|") < 0) {
			BoundStatement statement = new BoundStatement(selectHistoryRoute);
			statement.setString(0, key);
			statement.setUUID(1, uuidStart);
			statement.setUUID(2, uuidEnd);
			ResultSet results = m_session.execute(statement);
			List<Row> rows = results.all();
			debug("getHistory.results:"+rows.size());
			for (Row row : rows) {
				Map m = new HashMap();
				String _key = row.getString("routeId") + "|" + row.getString("instanceId");
				if(doubleMap.get(_key) == null){
					doubleMap.put(_key,"");
					List<Map> lm = getOneHistoryEntry(_key, type);
					retList.addAll(lm);
				}
			}
			debug("getHistory.isfullyFetched:"+results.isFullyFetched());
		}else{
			retList.addAll(getOneHistoryEntry(key, type));
		}
		debug("getHistory.return:"+retList.size());
		return retList;
	}

	private List<Map> getOneHistoryEntry(String key, String type) {
		debug("getOneHistoryEntry:"+key+"/"+type);
		List<Map> retList = new ArrayList();

		BoundStatement statement = new BoundStatement(selectHistory);
		statement.setString(0, key);
		statement.setString(1, type);
		ResultSet results = m_session.execute(statement);
		for (Row row : results) {
			Map m = new HashMap();
			m.put(HISTORY_KEY, row.getString(HISTORY_KEY));
			m.put(HISTORY_TIME, new Date(UUIDs.unixTimestamp(row.getUUID(HISTORY_TIME))));
			m.put(HISTORY_TYPE, row.getString(HISTORY_TYPE));
			m.put(HISTORY_HINT, row.getString(HISTORY_HINT));
			m.put(HISTORY_MSG, row.getString(HISTORY_MSG));
			retList.add(m);
		}
		debug("getOneHistoryEntry.return:"+retList.size());
		return retList;
	}

	public Set<String> getActivitiCamelCorrelation(String activitiId) throws Exception {
		Set<String> ret = new LinkedHashSet<String>();

		BoundStatement statement = new BoundStatement(selectActivitiCamelCorrelation);
		statement.setString(0, activitiId);
		ResultSet results = m_session.execute(statement);

		for (Row row : results) {
			 ret.add( row.getString("routeInstanceId"));
		}
		debug("getActivitiCamelCorrelation:"+ret);
		return ret;
	}

	private void initSession() {
		if (m_session != null) {
			return;
		}
		m_session = m_cassandraService.getSession(GLOBAL_KEYSPACE);
		createSchema();
		createStatements();
	}


	private void createStatements(){
		insertHistory = m_session.prepare(insertInto(GLOBAL_KEYSPACE, "history")
				.value("key", bindMarker())
				.value("time", bindMarker())
				.value("type", bindMarker())
				.value("hint", bindMarker())
				.value("msg", bindMarker()));

		insertHistoryRoute = m_session.prepare(insertInto(GLOBAL_KEYSPACE, "history_route")
				.value("routeId", bindMarker())
				.value("instanceId", bindMarker())
				.value("time", bindMarker()));

		insertActivitiCamelCorrelation = m_session.prepare(insertInto(GLOBAL_KEYSPACE, "activiti_camel_correlation")
				.value("activitiId", bindMarker())
				.value("time", bindMarker())
				.value("routeInstanceId", bindMarker()));

		selectActivitiCamelCorrelation = m_session.prepare(select()
            .from(GLOBAL_KEYSPACE, "activiti_camel_correlation")
            .where(eq("activitiId", bindMarker())));

		selectHistory = m_session.prepare(select()
            .from(GLOBAL_KEYSPACE, "history")
            .where( eq("key", bindMarker()))
							 .and(eq("type", bindMarker()))
							 .orderBy( asc("time")));

		selectHistoryRoute = m_session.prepare(select()
						.column("routeId")
						.column("instanceId")
						.column("time")
            .from(GLOBAL_KEYSPACE, "history_route")
            .where( eq("routeId", bindMarker()))
							 .and(gt("time", bindMarker()))
							 .and(lt("time", bindMarker()))
							 .orderBy( asc("time")));
	}

	private void createSchema() {
		m_session.execute(
				"CREATE TABLE IF NOT EXISTS "+GLOBAL_KEYSPACE+".history ("+
				"key text," +
				"type text," +
				"time timeuuid," +
				"hint text," +
				"msg text," +
				"PRIMARY KEY ((key, type), time));" );

		m_session.execute(
				"CREATE TABLE IF NOT EXISTS "+GLOBAL_KEYSPACE+".history_route ("+
				"routeId text,"+
				"time timeuuid,"+
				"instanceId text,"+
				"PRIMARY KEY (routeId, time));" );

		m_session.execute(
				"CREATE TABLE IF NOT EXISTS "+GLOBAL_KEYSPACE+".activiti_camel_correlation ("+
				"activitiId text,"+
				"time timeuuid,"+
				"routeInstanceId text,"+
				"PRIMARY KEY (activitiId, time));" );
	}


	protected static void info(String msg) {
		m_logger.info(msg);
	}

	protected static void debug(String msg) {
		m_logger.debug(msg);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(CassandraAccessImpl.class);
}

