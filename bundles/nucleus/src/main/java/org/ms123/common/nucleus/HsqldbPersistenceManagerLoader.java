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
package org.ms123.common.nucleus;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import javax.jdo.PersistenceManagerFactory;
import java.util.*;
import java.io.File;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Extent;
import javax.jdo.Query;
import javax.jdo.JDOHelper;
import javax.jdo.JDOEnhancer;
import javax.jdo.Transaction;
import org.ms123.common.libhelper.FileSystemClassLoader;
import javax.jdo.spi.*;
import org.datanucleus.store.rdbms.datasource.dbcp.managed.*;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.tm.TransactionService;

/**
 */
@SuppressWarnings("unchecked")
public class HsqldbPersistenceManagerLoader extends AbstractPersistenceManagerLoader {

	private String m_baseDir;

	public HsqldbPersistenceManagerLoader(BundleContext bundleContext, StoreDesc sdesc, File[] baseDirs, ClassLoader aidClassLoader, Map props, TransactionService ts) {
		super(bundleContext, sdesc, baseDirs, aidClassLoader,props, ts);
	}

	protected void init() {
		String bd = m_sdesc.getStoreBaseDir();
		File fstore = new File(bd);
		if (!fstore.exists()) {
			fstore.mkdirs();
		}
		m_baseDir = fstore.toString();
	}

	protected void setProperties() {
		m_props.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
		m_props.put("datanucleus.rdbms.dynamicSchemaUpdates ", "true");
		m_props.put("datanucleus.storeManagerType", m_sdesc.getStore());
		m_props.put("datanucleus.metadata.validate", "false");
		m_props.put("datanucleus.schema.autoCreateAll", "true");
		m_props.put("datanucleus.schema.validateTables", "false");
		m_props.put("datanucleus.TransactionType", "JTA");
		m_props.put("datanucleus.connection.resourceType", "JTA");
		m_props.put("datanucleus.jtaLocator", m_transactionService.getJtaLocator());
		m_props.put("datanucleus.schema.validateConstraints", "false");
		//		m_props.put("datanucleus.identifier.case", "MixedCase");
		m_props.put("datanucleus.plugin.pluginRegistryClassName", "org.ms123.common.nucleus.OsgiPluginRegistry");
	}

	protected void setDataSources() {
		JDBCXADataSource xa = null;
		try {
			xa = new JDBCXADataSource();
		} catch (java.sql.SQLException e) {
			throw new RuntimeException("HsqldbPersistenceManagerLoader.setDataSources:", e);
		}
		xa.setUser("SA");
		xa.setUrl("jdbc:hsqldb:file:" + m_baseDir + "/db;hsqldb.write_delay_millis=0");
		//xa.setDatabaseName(getDbName(null));
		BasicManagedDataSource b = new BasicManagedDataSource();
		b.setTransactionManager(m_transactionService.getTransactionManager());
		b.setXaDataSourceInstance(xa);
		m_props.put("datanucleus.ConnectionFactory", b);
		JDBCDataSource pd = new JDBCDataSource();
		// nontx
		pd.setUser("SA");
		xa.setUrl("jdbc:hsqldb:file:" + m_baseDir + "/db;hsqldb.write_delay_millis=0");
		//pd.setDatabaseName(getDbName(null));
		m_props.put("datanucleus.ConnectionFactory2", pd);
	}
}
