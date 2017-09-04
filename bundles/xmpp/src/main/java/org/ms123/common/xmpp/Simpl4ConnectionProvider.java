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
package org.ms123.common.xmpp;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.File;
import java.util.Properties;
import javax.sql.DataSource;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jivesoftware.database.ConnectionProvider;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 */
public class Simpl4ConnectionProvider implements ConnectionProvider {

	private static final Logger Log = LoggerFactory.getLogger(Simpl4ConnectionProvider.class);

	private PoolingDataSource m_poolingDataSource;

	public Simpl4ConnectionProvider() {
	}

	public boolean isPooled() {
		return true;
	}

	public String getBaseDir() {
		return new File(System.getProperty("workspace"), "openfire").toString();
	}

	public void start() {
		try {
			m_poolingDataSource = getPoolingDataSource();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void restart() {
		destroy();
		start();
	}

	public Connection getConnection() throws SQLException {
		if (m_poolingDataSource == null) {
			throw new SQLException("Simpl4ConnectionProvider.DataSource has not been initialized.");
		}
		return m_poolingDataSource.getConnection();
	}

	private PoolingDataSource getPoolingDataSource() {
		PoolingDataSource ds = new PoolingDataSource();
		ds.setClassName("org.h2.jdbcx.JdbcDataSource");
		ds.setUniqueName("openfire");
		ds.setMaxPoolSize(15);
		ds.setAllowLocalTransactions(true);
		ds.setTestQuery("SELECT 1");
		ds.getDriverProperties().setProperty("user", "sa");
		ds.getDriverProperties().setProperty("password", "sa");
		ds.getDriverProperties().setProperty("URL", "jdbc:h2:file:" + getBaseDir() + "/dbh2;TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0;MV_STORE=TRUE;MVCC=TRUE;CACHE_SIZE=33107;DB_CLOSE_ON_EXIT=FALSE");
		return ds;
	}

	public void destroy() {
		try {
			if (m_poolingDataSource != null) {
				m_poolingDataSource.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
