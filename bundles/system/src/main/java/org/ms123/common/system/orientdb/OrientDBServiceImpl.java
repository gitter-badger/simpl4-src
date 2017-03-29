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
package org.ms123.common.system.orientdb;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import com.orientechnologies.common.util.OCallable;
import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.core.sql.OCommandSQL;


import com.orientechnologies.orient.server.OServerMain;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.rpc.RpcException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/** OrientDBService implementation
--------------------------
TX|Autostart|Commit|result
 x|false    |false |ok
 x|true     |false |nok
 x|false    |true  |ok
 x|true     |true  |ok
 -|false    |false |ok
 -|true     |false |nok
 -|false    |true  |ok
 -|true     |true  |nok
--------------------------
Best for transactions
TX|false   |begin and end
initSchema musr be with getRawGraph done
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=orientdb" })
public class OrientDBServiceImpl implements OrientDBService {
	private Map<String, OrientGraphFactory> factoryMap = new HashMap<String, OrientGraphFactory>();
	private OServer server;
	private OServerAdmin serverAdmin;
	private String passwd = "simpl4";

	private BundleContext bundleContext;

	public OrientDBServiceImpl() {
		String pw = System.getProperty("ORIENTDB_ROOT_PASSWORD");
		if (pw != null) {
			passwd = pw;
		}
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bundleContext = bundleContext;
		try {
			info(this, "OrientDbService starting");
			server = OServerMain.create();
			File f = new File("../etc/orientdb-server-config.xml");
			info(this, "OrientDbService startup(" + server + "):" + f);
			server.startup(f);
			server.activate();

			info(this, "OrientDBService started");
			serverAdmin = new OServerAdmin("remote:127.0.0.1").connect("root", passwd);
			info(this, "OrientDBService.serverAdmin:" + serverAdmin);
		} catch (Exception e) {
			e.printStackTrace();
			error(this, "OrientDBServiceImpl.activate.error:%[exception]s", e);
		}
	}

	public void update(Map<String, Object> props) {
		info(this, "OrientDBServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this, "OrientDBServiceImpl.deactivate");
		server.shutdown();
	}

	@RequiresRoles("admin")
	public void dropVertices(
			@PName("databaseName") String databaseName, 
			@PName("vertexList")           List<String> vertexList) throws RpcException {
		OrientGraphFactory f = getFactory(databaseName);
		OrientGraph graph = f.getTx();
		try {
			for( String v : vertexList){
				executeUpdate(graph, "delete vertex "+v);
				graph.dropVertexType(v);
			}
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "OrientDBServiceImpl.dropVertices:", e);
		}finally{
			graph.shutdown();
		}
	}
	public synchronized OrientGraphFactory getFactory(String name) {
		return getFactory( name, false);
	}

	public synchronized OrientGraphFactory getFactory(String name, boolean autoCommit) {
		OrientGraphFactory f = factoryMap.get(name+"/"+autoCommit);
		
		if (f == null || !dbExists(name)) {
			if (!dbExists(name)) {
				dbCreate(name);
			}
			f = new OrientGraphFactory("remote:127.0.0.1/" + name, "root", passwd, true);
			f.setupPool(1, 50);
			f.setAutoStartTx(autoCommit);
			factoryMap.put(name+"/"+autoCommit, f);
		}
		info(this, "getFactory("+name+")"+f.isAutoStartTx());
		return f;
	}



	public List<Map<String, Object>> executeQuery(OrientGraph graph, String sql, Object... args) {
		OCommandRequest query = new OSQLSynchQuery(sql);
		Iterable<Element> ret = graph.command(query).execute(args);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Element elem : ret) {
			Map<String, Object> map = new HashMap<String, Object>();
			list.add(map);
			for (String prop : elem.getPropertyKeys()) {
				map.put(prop, elem.getProperty(prop));
			}
		}
		return list;
	}

	public void executeUpdate(OrientGraph graph, String sql, Object... args) {
		OCommandRequest update = new OCommandSQL(sql);
		graph.command(update).execute(args);
	}

	private void dbCreate(String name) {
		try {
			serverAdmin.createDatabase(name, "graph", "plocal");
			info(this, "dbCreate("+name+")");
		} catch (Exception e) {
			e.printStackTrace();
			error(this, "OrientDBServiceImpl.dbCreate.error:%[exception]s", e);
		}
	}

	private boolean dbExists(String name) {
		try {
			boolean b = serverAdmin.existsDatabase(name, "plocal");
			info(this, "dbExists("+name+"):"+b);
			return b;
		} catch (Exception e) {
			e.printStackTrace();
			error(this, "OrientDBServiceImpl.dbExists.error:%[exception]s", e);
			return false;
		}
	}

}

