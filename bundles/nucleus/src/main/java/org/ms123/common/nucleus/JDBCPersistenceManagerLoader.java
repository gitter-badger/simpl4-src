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
package org.ms123.common.nucleus;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import java.io.File;
import java.util.*;
import javax.jdo.Extent;
import javax.jdo.JDOEnhancer;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.spi.*;
import javax.jdo.Transaction;
import javax.naming.InitialContext;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import org.datanucleus.store.rdbms.datasource.dbcp.managed.*;
import org.ms123.common.libhelper.FileSystemClassLoader;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.tm.TransactionService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
/**
 */
@SuppressWarnings("unchecked")
public class JDBCPersistenceManagerLoader extends AbstractPersistenceManagerLoader {
	private PoolingDataSource m_poolingDataSource;

	public JDBCPersistenceManagerLoader(BundleContext bundleContext, StoreDesc sdesc, File[] baseDirs, ClassLoader aidClassLoader,Map props, TransactionService ts) {
		super(bundleContext, sdesc, baseDirs, aidClassLoader,props, ts);
	}

	protected void setProperties() {
		m_props.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
		m_props.put("datanucleus.rdbms.dynamicSchemaUpdates ", "true");
		m_props.put("datanucleus.storeManagerType", "rdbms");
		m_props.put("datanucleus.metadata.validate", "true");
		m_props.put("datanucleus.schema.autoCreateAll", "true");
		m_props.put("datanucleus.schema.validateTables", "true");
		m_props.put("datanucleus.validateTables", "true");
		m_props.put("datanucleus.TransactionType", "JTA");
		m_props.put("datanucleus.identifier.case", "MixedCase");
		m_props.put("datanucleus.connection.resourceType", "JTA");
		m_props.put("datanucleus.jtaLocator", m_transactionService.getJtaLocator());
		m_props.put("datanucleus.schema.validateConstraints", "false");
		m_props.put("datanucleus.plugin.pluginRegistryClassName", "org.ms123.common.nucleus.OsgiPluginRegistry");
	}

	private Object getService(Class clazz, String vendor) throws Exception{
		Collection<ServiceReference> sr = m_bundleContext.getServiceReferences(clazz, "(dataSourceName="+vendor+")");
		if( sr.size()> 0){
			Object o = m_bundleContext.getService((ServiceReference)sr.toArray()[0]);
			return o;
		}
		return  null;
	}
	protected void setDataSources() {
		try{
			String vendor = m_sdesc.getVendor();
			XADataSource xads = (XADataSource)getService(javax.sql.XADataSource.class, vendor);
			if( xads == null){
				throw new RuntimeException("Datasource not available:"+vendor);
			}
			info(this,"OpenJDBC.xa:"+xads);
			info(this,"OpenJDBC.xa.class:"+xads.getClass());
			info(this,"OpenJDBC:.vendor:"+vendor);
			info(this,"OpenJDBC:"+m_sdesc);
			PoolingDataSource ds = new PoolingDataSource();
			ds.setXaDataSource(xads);
			ds.setUniqueName(m_sdesc.toString());
			ds.setMaxPoolSize(25);
			ds.setAllowLocalTransactions(true);
			ds.setEnableJdbc4ConnectionTest(true);
			ds.setTestQuery(null);
			m_props.put("datanucleus.ConnectionFactory", ds);
			m_poolingDataSource=ds;

			// nontx
			ConnectionPoolDataSource pds = (ConnectionPoolDataSource)getService(javax.sql.ConnectionPoolDataSource.class, vendor);
			info(this,"OpenJDBC.notx:"+pds);
			m_props.put("datanucleus.ConnectionFactory2", pds);
		}catch(Exception e){
			e.printStackTrace();
			error(this, "setDataSources.error:%[exception]s",e);
			throw new RuntimeException("JDBCPersistenceManagerLoader.setDataSources",e);
		}
	}
	public synchronized void close() {
		debug(this,"ClosePostgresql:"+m_sdesc);
		super.close();
		if(m_poolingDataSource != null){
			m_poolingDataSource.close();
		}
	}
	public String toString(){
		return "[JDBCPersistenceManagerLoader:"+m_sdesc+"]";
	}
}
