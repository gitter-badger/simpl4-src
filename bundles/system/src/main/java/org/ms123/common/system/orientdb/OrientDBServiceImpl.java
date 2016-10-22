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
import com.orientechnologies.orient.server.OServerMain;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
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
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=orientdb" })
public class OrientDBServiceImpl  implements OrientDBService {
	private Map<String,OrientGraphFactory> factoryMap = new HashMap<String,OrientGraphFactory>();
	private OServer server;
	private OServerAdmin serverAdmin;
	private String passwd="ms123";

	private BundleContext bundleContext;


	public OrientDBServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bundleContext=bundleContext;
		try {
			info(this, "OrientDbService starting");
			server = OServerMain.create();
			File f = new File("../etc/orientdb-server-config.xml");
			info(this, "OrientDbService startup("+server+"):"+f);
			server.startup(f);
			server.activate();

			info(this, "OrientDBService started");
			serverAdmin = new OServerAdmin("remote:127.0.0.1").connect("root", passwd);
			info(this, "OrientDBService.serverAdmin:"+serverAdmin);
		} catch (Exception e) {
			e.printStackTrace();
			error(this, "OrientDBServiceImpl.activate.error:%[exception]s", e);
		}
	}

	public void update(Map<String, Object> props) {
		info(this,"OrientDBServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this,"OrientDBServiceImpl.deactivate");
		server.shutdown();
	}

	public synchronized OrientGraphFactory getFactory( String name ){
		OrientGraphFactory f = factoryMap.get( name );
		if( f == null ){
			if( !dbExists( name )){
				dbCreate( name );
			}
			f = new OrientGraphFactory("remote:127.0.0.1/"+name, "root", passwd, true);
			factoryMap.put( name, f );
		}
		return f;
	}

	public List<Map<String,Object>> executeQuery(OrientGraph graph, String sql){
		OCommandRequest query = new OSQLSynchQuery(sql);
		OCommandRequest cmd = graph.command( query);
		Iterable<Element> ret = cmd.execute();
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for( Element elem : ret ){
			Map<String,Object> map = new HashMap<String,Object>();
			list.add(map);
			for( String prop : elem.getPropertyKeys()){
				map.put( prop, elem.getProperty(prop));
			}
		}
		return list;
	}

	private void dbCreate( String name){
		try{
			serverAdmin.createDatabase(name, "graph", "plocal");
		}catch(Exception e){
			e.printStackTrace();
			error(this, "OrientDBServiceImpl.dbCreate.error:%[exception]s", e);
		}
	}

	private boolean dbExists( String name){
		try{
			return serverAdmin.existsDatabase(name, "plocal");
		}catch(Exception e){
			e.printStackTrace();
			error(this, "OrientDBServiceImpl.dbExists.error:%[exception]s", e);
			return false;
		}
	}

}

