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

@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=jdbc" })
public class JDBCServiceImpl implements JDBCService {

	private BundleContext m_bundleContext;

	public JDBCServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		try {
			m_bundleContext = bundleContext;
			registerAS400();
			registerJNDIDataSources();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void registerAS400() throws Exception {
		AS400DataSourceFactory dsf = new AS400DataSourceFactory();
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "com.ibm.as400.access.AS400JDBCDriver");
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "as400");
		info("as400.register.dsf:" + dsf);
		info("as400.register.props:" + props);
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
			info("JDBCServiceImpl.jndiLookup.name:" + name);
			DataSource datasource = (DataSource) ctx.lookup(name);
			if (datasource != null) {
				info("JDBCServiceImpl.DataSource:" + datasource);
				Dictionary<String, String> props = new Hashtable<String, String>();
				props.put("dataSourceName", name);
				m_bundleContext.registerService(DataSource.class.getName(), datasource, props);
			}
		}
	}

	public void info(String msg) {
		System.out.println(msg);
	}

	public void update(Map<String, Object> props) {
	}

	protected void deactivate() throws Exception {
	}
}
