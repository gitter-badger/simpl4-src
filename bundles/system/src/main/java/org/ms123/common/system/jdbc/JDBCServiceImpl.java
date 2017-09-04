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
package org.ms123.common.system.jdbc;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import org.osgi.framework.BundleContext;
import java.util.Dictionary;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.sql.DataSource;
import org.osgi.service.jdbc.DataSourceFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.h2.tools.Server;
import org.h2.engine.SysProperties;
import org.h2.util.OsgiDataSourceFactory;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;

@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=jdbc" })
public class JDBCServiceImpl implements JDBCService {

	private BundleContext m_bundleContext;
	private Server h2Server;

	public JDBCServiceImpl() {
			System.setProperty("h2.bindAddress", "127.0.0.1");
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		try {
			m_bundleContext = bundleContext;
			registerAS400();
			registerJTDS();
			registerH2();
			registerJNDIDataSources();
			startH2Server();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void registerAS400() throws Exception {
		AS400DataSourceFactory dsf = new AS400DataSourceFactory();
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "com.ibm.as400.access.AS400JDBCDriver");
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "as400");
		info(this,"as400.register.dsf:" + dsf);
		info(this,"as400.register.props:" + props);
		m_bundleContext.registerService(DataSourceFactory.class.getName(), dsf, props);
	}
	private void registerJTDS() throws Exception {
		JTDSDataSourceFactory dsf = new JTDSDataSourceFactory();
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "net.sourceforge.jtds.jdbc.Driver");
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "jtds");
		info(this,"jtds.register.dsf:" + dsf);
		info(this,"jtds.register.props:" + props);
		m_bundleContext.registerService(DataSourceFactory.class.getName(), dsf, props);
	}

	private void registerH2() throws Exception {
		OsgiDataSourceFactory dsf = new OsgiDataSourceFactory(new org.h2.Driver());
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "org.h2.Driver");
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "h2");
		info(this,"h2.register.dsf:" + dsf);
		info(this,"h2.register.props:" + props);
		m_bundleContext.registerService(DataSourceFactory.class.getName(), dsf, props);
	}

	public synchronized void registerJNDIDataSources() throws Exception {
		Context ctx = new InitialContext();
		if (ctx == null) {
			throw new RuntimeException("WebsphereJNDI could not create InitalContext ");
		}
		List<String> jndiNames = new ArrayList();
		String jndiName = "jdbc/EtkPool";
		String val = (String) System.getProperty("tpso.jdbc.jndiname");
		if (val != null) {
			jndiName = val;
		}
		jndiNames.add(jndiName);
		for (int i = 1; i < 10; i++) {
			val = (String) System.getProperty("simpl4.jdbc.jndiname" + i);
			if (val != null) {
				jndiName = val;
				jndiNames.add(val);
			}
		}
		for (String name : jndiNames) {
			info(this,"JDBCServiceImpl.jndiLookup.name:" + name);
			DataSource datasource = null;
			try{
				datasource = (DataSource) ctx.lookup(name);
			}catch( javax.naming.NotContextException nce){
			}catch( javax.naming.NameNotFoundException nne){
			}
			if (datasource != null) {
				info(this,"JDBCServiceImpl.DataSource:" + datasource);
				Dictionary<String, String> props = new Hashtable<String, String>();
				props.put("dataSourceName", name);
				m_bundleContext.registerService(DataSource.class.getName(), datasource, props);
			}
		}
	}
	private void startH2Server(){
		try {
			String baseDir = System.getProperty("git.repos");
			Server server = Server.createTcpServer( new String[] { "-tcpPort", "9092", "-tcp", "-baseDir", baseDir  }).start();
			h2Server = server;
			info(this, "H2 Server startet:"+server.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			error(this, "Unable to start h2 server.error:%[exception]s",e);
		}
	}

	public void update(Map<String, Object> props) {
	}

	protected void deactivate() throws Exception {
		info(this, "H2 Server stop:"+h2Server.getStatus());
		h2Server.stop();
		info(this, "H2 Server stopped:"+h2Server.getStatus());
	}
}
