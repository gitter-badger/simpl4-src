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

		if( m_sdesc.isSchemaReadonly()){
			m_props.put("datanucleus.rdbms.dynamicSchemaUpdates ", "false");
			m_props.put("datanucleus.schema.autoCreateAll", "false");
		}else{
			m_props.put("datanucleus.rdbms.dynamicSchemaUpdates ", "true");
			m_props.put("datanucleus.schema.autoCreateAll", "true");
		}

		m_props.put("datanucleus.storeManagerType", "rdbms");

		if( m_sdesc.isSchemaValidate()){
			m_props.put("datanucleus.metadata.validate", "true");
			m_props.put("datanucleus.schema.validateTables", "true");
			m_props.put("datanucleus.validateTables", "true");
			m_props.put("datanucleus.schema.validateColumns", "true");
		}else{
			m_props.put("datanucleus.metadata.validate", "false");
			m_props.put("datanucleus.schema.validateTables", "false");
			m_props.put("datanucleus.validateTables", "false");
			m_props.put("datanucleus.schema.validateColumns", "false");
			m_props.put("datanucleus.schema.validateAll", "false");
			m_props.put("datanucleus.schema.validateConstraints", "false");
			m_props.put("datanucleus.rdbms.CheckExistTablesOrViews", "false");
			m_props.put("datanucleus.rdbms.initializeColumnInfo", "NONE");
		}
		m_props.put("datanucleus.schema.validateConstraints", "false");

		m_props.put("datanucleus.useIsNullWhenEqualsNullParameter", "true");
		m_props.put("datanucleus.rdbms.allowColumnReuse", "true");
		m_props.put("datanucleus.TransactionType", "JTA");
		if( !m_sdesc.getVendor().equals("h2")){
			m_props.put("datanucleus.identifier.case", "MixedCase");
		}
		m_props.put("datanucleus.connection.resourceType", "JTA");
		m_props.put("datanucleus.jtaLocator", m_transactionService.getJtaLocator());
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
