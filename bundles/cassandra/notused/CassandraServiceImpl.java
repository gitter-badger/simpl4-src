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
package org.ms123.common.cassandra;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.*;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import flexjson.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.service.CassandraDaemon;
import org.apache.cassandra.service.CassandraDaemon.Server;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.permission.api.PermissionException;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

/** CassandraService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=cassandra" })
public class CassandraServiceImpl extends BaseCassandraServiceImpl implements CassandraService {

	private JSONDeserializer m_ds = new JSONDeserializer();
	private JSONSerializer m_js = new JSONSerializer();
	private Map<String, Session> m_sessionCache = new ConcurrentHashMap<String,Session>();

	private static final String INTERNAL_CASSANDRA_KEYSPACE = "system";
	private static final String INTERNAL_CASSANDRA_AUTH_KEYSPACE = "system_auth";
	private static final String INTERNAL_CASSANDRA_TRACES_KEYSPACE = "system_traces";

	private EmbeddedCassandra m_embeddedCassandra;
	public CassandraServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		start();
	}

	protected void deactivate() throws Exception {
		info("deactivate:cassandra");
		stopClientSessions();
		stop();
	}

	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
	}

	private void start() {
		m_embeddedCassandra = EmbeddedCassandra.create();
		info("starting EmbeddedCassandra");
		m_embeddedCassandra.start();
	}

	private void stop() {
		info("Stopping cassandra deamon");
		info("cleaning up the Schema keys");
		Schema.instance.clear();
		if( m_embeddedCassandra != null){
			m_embeddedCassandra.stop();
		}
		m_embeddedCassandra = null;
		info("removing MBean");
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.unregisterMBean(new ObjectName("org.apache.cassandra.db:type=DynamicEndpointSnitch"));
		} catch (Exception e) {
			info("Couldn't remove MBean");
			e.printStackTrace();
		}
	}

	private synchronized void stopClientSessions(){
		try{
			Iterator<Session> it = m_sessionCache.values().iterator();
			while( it.hasNext()){
				Session s = it.next();
				if (!s.isClosed()) {
					System.out.println("CassandraServiceImpl.stopClientSessions.session:"+s);
					s.close();
				}
			}
		}catch(Exception e){
			System.out.println("stopClientSessions:"+e);
		}
	}
	public synchronized Session getSession(String keyspaceName) {
		Session session = m_sessionCache.get(keyspaceName);
		if (session != null) {
			if (!session.isClosed()) {
				return session;
			}
		}
		Cluster cluster = Cluster.builder().addContactPoint(DatabaseDescriptor.getListenAddress().getHostName()).withPort(DatabaseDescriptor.getNativeTransportPort()).build();
		KeyspaceMetadata kmd = cluster.getMetadata().getKeyspace(keyspaceName);
		if (kmd == null) {
			session = cluster.connect();
			String cql = "CREATE KEYSPACE " + keyspaceName + " WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};";
			info(cql + "\n");
			session.execute(cql);
			cql = "USE " + keyspaceName + ";";
			info(cql + "\n");
			session.execute(cql);
		} else {
			session = cluster.connect(keyspaceName);
		}
		m_sessionCache.put(keyspaceName, session);
		return session;
	}
}
