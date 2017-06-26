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
import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.security.OServerSecurity;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.server.security.authenticator.ODefaultPasswordAuthenticator;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.rpc.CallService;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.setting.api.SettingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.ms123.common.system.thread.ThreadContext;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import static org.apache.commons.io.FileUtils.readFileToString;

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
public class OrientDBServiceImpl implements OrientDBService,FrameworkListener, EventHandler  {
	private Map<String, OrientGraphFactory> factoryMap = new HashMap<String, OrientGraphFactory>();
	private OServer server;
	private OServerAdmin serverAdmin;
	private static String rootPassword = "simpl4";
	private CallService callService;
	private SettingService settingService;
	private static AuthService authService;
	private PermissionService permissionService;
	private ServiceRegistration serviceRegistration;
	static final String[] topics = new String[] { "setting/deleteResource", "setting/setResource" };

	private BundleContext bundleContext;

	public OrientDBServiceImpl() {
		String pw = System.getProperty("ORIENTDB_ROOT_PASSWORD");
		if (pw != null) {
			this.rootPassword = pw;
		}
	}

	public static class Authenticator extends ODefaultPasswordAuthenticator{
		public Authenticator(){
			super();
		}
 		public String authenticate(final String username, final String password) {
			info(this,"OrientDbService.authenticate:"+username);
			if( password == null){
				info(this,"OrientDbService.authenticate("+username+"):password null");
				return null;
			}
			Map	userProps = authService.getUserProperties(username);
			if( userProps == null){
				info(this,"OrientDbService.authenticate("+username+"):unknown");
				return null;
			}
			String pw = (String)userProps.get("password");
			if( password.equals( pw) || ("admin".equals(username) && rootPassword.equals(password))){
				info(this,"OrientDbService.authenticate("+username+"):ok");
				return username;
			}
			info(this,"OrientDbService.authenticate("+username+"):nok");
			return null;
		}
		public boolean isAuthorized(final String username, final String resource) {
			info(this,"OrientDbService.isAuthorized:"+username+"/"+resource);
			return true;
		}
		public OServerUserConfiguration getUser(final String username) {
			info(this,"OrientDbService.getUser:"+username);
			return null;
		}
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bundleContext = bundleContext;
		this.bundleContext.addFrameworkListener(this);
		try {
			info(this, "OrientDbService starting");
			server = OServerMain.create();
			File file = new File("../etc/orientdb-server-config.xml");
			info(this, "OrientDbService startup(" + server + "):" + file);
			server.startup(file);
			server.activate();
			OServerSecurity security = server.getSecurity();
			security.registerSecurityClass(OrientDBServiceImpl.Authenticator.class);

			security.reloadComponent("authentication", getDocument() );

			info(this, "OrientDBService started");
			serverAdmin = new OServerAdmin("remote:127.0.0.1").connect("root", this.rootPassword);
			info(this, "OrientDBService.serverAdmin:" + serverAdmin);

			Dictionary d = new Hashtable();
			d.put(EventConstants.EVENT_TOPIC, topics);
			this.serviceRegistration = this.bundleContext.registerService(EventHandler.class.getName(), this, d);
		} catch (Exception e) {
			error(this, "OrientDBServiceImpl.activate.error:%[exception]s", e);
		}
	}

	private ODocument getDocument(){
		ODocument auth = new ODocument("auth");
		auth.field( "allowDefault", true);

		ODocument a1 = new ODocument("auth1");
		a1.field("name", "Password");
		a1.field("class", "org.ms123.common.system.orientdb.Authenticator");
		a1.field("enabled", true);

		ODocument a2 = new ODocument("auth2");
		a2.field("name", "ServerConfig");
		a2.field("class", "com.orientechnologies.orient.server.security.authenticator.OServerConfigAuthenticator");
		a2.field("enabled", true);

		ODocument a3 = new ODocument("auth3");
		a2.field("name", "SystemAuthenticator");
		a2.field("class", "com.orientechnologies.orient.server.security.authenticator.OSystemUserAuthenticator");
		a2.field("enabled", true);
		List<ODocument> listAuth = new ArrayList<ODocument>();
		listAuth.add( a1);
		listAuth.add( a2);
		listAuth.add( a3);
		auth.field( "authenticators", listAuth);
		info(this,"OrientDBService.odoc:"+auth);
		return auth;
	}

	public void frameworkEvent(FrameworkEvent event) {
		info(this,"OrientServiceImpl.frameworkEvent:"+event);
		if( event.getType() != FrameworkEvent.STARTED){
			return; 
		}
		try{
			setupLiveList();
		}catch(Exception e){
			error(this, "OrientDBServiceImpl.FrameworkEvent.error:%[exception]s", e);
		}
	}
	public void handleEvent(Event event) {
		info(this,"OrientDBServiceImpl.Event: " + event);
		try{
			if( "setting/deleteResource".equals(event.getTopic())){
				clearLiveList();
				setupLiveList();
			}
			if( "setting/setResource".equals(event.getTopic())){
				clearLiveList();
				setupLiveList();
			}
		}catch(Exception e){
			e.printStackTrace();
			error(this, "OrientDBServiceImpl.handleEvent.error:%[exception]s", e);
		}finally{
		}
	}

	public void update(Map<String, Object> props) {
		info(this, "OrientDBServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this, "OrientDBServiceImpl.deactivate");
		server.shutdown();
	}

	/* LiveQuery Begin*/
	private List<OrientDBLive> orientDBLiveList = new ArrayList<OrientDBLive>();
	private void clearLiveList() {
		info(this,"OrientServiceImpl.clearLiveList:"+this.orientDBLiveList);
		for( OrientDBLive oLive : this.orientDBLiveList){
			OrientGraphFactory f = getFactory( oLive.getDatabaseName());
			OrientGraph orientGraph = f.getTx();
			try{
				oLive.unsubscribe( orientGraph );
			}catch(Exception e){
				error(this, "OrientDBServiceImpl.clearLiveList.error("+oLive.getName()+"):%[exception]s", e);
			}finally{
				orientGraph.shutdown();
			}
		}
		info(this,"OrientServiceImpl.clearLiveList.finished");
		this.orientDBLiveList.clear();
	}

	private void setupLiveList() throws Exception{
		List<Map> resSettings = this.settingService.getResourceSettings("global", "configs.orientdbLive");
		info(this,"resSettings:"+resSettings);
		for( Map<String,Map> resMap : resSettings){
			Map data = resMap.get("data");
			List<Map> liveList = (List)data.get("live");
			info(this,"liveList:"+liveList);

			for( Map<String,Object> liveMap : liveList){
				String sql = (String)liveMap.get("sql");
				boolean enabled = (Boolean)liveMap.get("enabled");
				if( isEmpty( sql ) || !enabled) {
					continue;
				}
				OrientGraphFactory f = getFactory( (String)liveMap.get("database"));
				OrientGraph orientGraph = f.getTx();
				try{
					this.orientDBLiveList.add(new OrientDBLive(orientGraph, liveMap,this.callService,this.permissionService));
				}catch(Exception e){
					error(this, "OrientDBServiceImpl.FrameworkEvent.start.error:%[exception]s", e);
				}finally{
					orientGraph.shutdown();
				}
			}
		}
	}
	/* LiveQuery End*/

	@RequiresRoles("admin")
	public void dropVertices(
			@PName("onlyDelete") Boolean onlyDelete, 
			@PName("databaseName") String databaseName, 
			@PName("vertexList")           List<String> vertexList) throws RpcException {
		OrientGraphFactory f = getFactory(databaseName);
		OrientGraph graph = f.getTx();
		try {
			for( String v : vertexList){
				executeUpdate(graph, "delete vertex "+v);
				if( onlyDelete == false){
					graph.dropVertexType(v);
				}
			}
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "OrientDBServiceImpl.dropVertices:", e);
		}finally{
			graph.shutdown();
		}
	}

	/* Begin Teststuff*/
	public void testGraph( @PName("name") String name ) throws RpcException {
		OrientGraph graph = getOrientGraph(name);
		try {
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "OrientDBServiceImpl.testGraph:", e);
		}finally{
			graph.shutdown();
		}
	}
	public void createCity( @PName("name") String name, @PName("city") String city) throws RpcException {
		OrientGraph graph = getOrientGraph(name);
		try {
			executeUpdate( graph, "insert into city(name) values(?) ",city);
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "OrientDBServiceImpl.createCity:", e);
		}finally{
			graph.shutdown();
		}
	}

	public void listCities( @PName("name") String name) throws RpcException {
		OrientGraph graph = getOrientGraph(name);
		try {
			List cities = executeQuery( graph, "select from City");
			info( this, "listCities("+name+"):"+cities);
			return;
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "OrientDBServiceImpl.listCities:", e);
		}finally{
			graph.shutdown();
		}
	}
	/* End Teststuff*/

	private static int maxUserPoolsize = 5;
	private static int maxPoolsize = 50;
	private static MultiUserPool multiUserPool = new MultiUserPool(maxPoolsize);

	public synchronized OrientGraph getOrientGraph(String name){
		info( this, "getOrientGraph("+name+"):start");
		String username = ThreadContext.getThreadContext().getUserName();
		if( !userExists( name, username) ){
			userCreate( name, username );
		}

		OrientGraphFactory factory = multiUserPool.get( name + "/"+ username );
		if( factory == null){
			Map	userProps = this.authService.getUserProperties(username);
			String password = (String)userProps.get("password");
			if( "admin".equals(username) && password == null){
				password = this.rootPassword;
			}
			factory = getFactory( name, username, password, maxUserPoolsize, false, false );
			OrientGraphFactory f = multiUserPool.push( name+"/"+username, factory);
			if( f != null){
				f.close();
			}
		}
		info( this, "getOrientGraph("+name+"):end");
		return factory.getTx();
	}

	public synchronized OrientGraph getOrientGraphRoot(String db) {
		OrientGraphFactory f = getFactory( db, "root", this.rootPassword, 50, false, true);
		return f.getTx();
	}

	public synchronized OrientGraphFactory getFactory(String db) {
		return getFactory( db, "root", this.rootPassword, 50, false, true);
	}

	public synchronized OrientGraphFactory getFactory(String db, boolean autoCommit) {
		return getFactory( db, "root", this.rootPassword, 50, autoCommit, true);
	}

	public synchronized OrientGraphFactory getFactory(String name, String user, String pw, int poolsize, boolean autoCommit, boolean cache) {
		OrientGraphFactory f = cache ? factoryMap.get(name+"/"+autoCommit) : null;
		info(this, "getFactory1("+name+"):"+user+"/"+poolsize);
		
		if (f == null || !dbExists(name)) {
			if (!dbExists(name)) {
				dbCreate(name);
			}
			f = new OrientGraphFactory("remote:127.0.0.1/" + name, user, pw, true);
			f.setupPool(1, poolsize);
			f.setAutoStartTx(autoCommit);
			if( cache){
				factoryMap.put(name+"/"+autoCommit, f);
			}
		}
		info(this, "getFactory2("+name+"):"+f.isAutoStartTx());
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

	private boolean userExists(String name, String username) {
		OrientGraphFactory f = getFactory(name,true);
		OrientGraph graph = f.getTx();
		info(this, "userExists1("+name+"):"+username);
		List users = null;
		try{
			users = executeQuery( graph, "select from OUser where name=?", username);
		}finally{
			graph.shutdown();
		}
		info(this, "userExists2("+name+"):"+users);
		if( users != null && users.size() > 0){
			return true;
		}
		return false;
	}

	private void userCreate(String name,String username) {
		info(this, "userCreate("+name+"):"+username);
		OrientGraphFactory f = getFactory(name,true);
		OrientGraph graph = f.getTx();
		try{
			executeUpdate( graph, "create user "+username+" IDENTIFIED BY foo");
		}finally{
			graph.shutdown();
		}
	}

	@Reference(dynamic = true, optional = true)
	public void setSettingService(SettingService paramSettingService) {
		this.settingService = paramSettingService;
		info(this, "OrientDBServiceImpl.setSettingService:" + paramSettingService);
	}

	@Reference(dynamic = true, optional=true)
	public void setCallService(CallService callService) {
		info(this,"OrientDBServiceImpl.setCallService:" + callService);
		this.callService = callService;
	}
	@Reference(dynamic = true, optional = true)
	public void setPermissionService(PermissionService paramPermissionService) {
		this.permissionService = paramPermissionService;
		info(this,"CallServiceImpl.setPermissionService:" + paramPermissionService);
	}

	@Reference(dynamic = true, optional = true)
	public void setAuthService(AuthService authService) {
		this.authService = authService;
		info(this,"PermissionImpl.setAuthService:" + authService);
	}
}

