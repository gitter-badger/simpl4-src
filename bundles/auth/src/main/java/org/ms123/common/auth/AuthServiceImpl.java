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
package org.ms123.common.auth;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.tinkerpop.blueprints.Vertex;
import flexjson.JSONSerializer;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Iterable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.data.query.OrientDBQueryBuilder;
import org.ms123.common.data.query.QueryBuilder;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.namespace.NamespaceService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.rpc.CallService;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.orientdb.OrientDBService;
import org.osgi.framework.BundleContext;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

/** AuthService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=auth" })
public class AuthServiceImpl implements org.ms123.common.auth.api.AuthService, Constants {

	public final String AUTH_DATABASE = "auth";
	protected Inflector m_inflector = Inflector.getInstance();
	private JSONSerializer js = new JSONSerializer();
	protected OrientDBService orientdbService;
	protected OrientGraph orientGraph;
	protected DataLayer dataLayer;

	protected PermissionService permissionService;
	protected CallService callService;

	public AuthServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		System.out.println("AuthServiceImpl.activate.props:" + props);
		initAuth();
	}

	protected void deactivate() throws Exception {
		System.out.println("AuthServiceImpl deactivate");
	}

	public String getAdminUser() {
		String adminuser = System.getProperty("admin_user");
		if (adminuser == null) {
			adminuser = ADMIN_USER;
		}
		return adminuser;
	}

	public Map getUserProperties(String userid) {
		info(this, "getUserProperties.userid:" + userid);
		Map ret = getUserByUserid(userid);
		info(this, "getUserProperties.ret:" + ret);
		if (ret == null) {
			throw new RuntimeException("user_not_exists:" + userid);
		}
		return ret;
	}

	public synchronized List<Map> getUserList(Map mfilter, int startIndex, int numResults) {
		List<Map> result = new ArrayList();
		OCommandRequest query = new OSQLSynchQuery("select from SUser");
		OrientGraph orientGraph = getConnection();
		try {
			int count = 0;
			Iterable<Vertex> res = orientGraph.command(query).execute();
			Iterator<Vertex> iter = res.iterator();
			while (iter.hasNext()) {
				if (count < startIndex)
					continue;
				if (numResults != 0 && (count - startIndex) > numResults)
					break;
				count++;
				Vertex v = iter.next();
				result.add(((OrientVertex) v).getProperties());
			}
		} catch (Exception ex) {
			error(this, "getUserList.ex:%[exception]s", ex);
			throw new RuntimeException(ex);
		} finally {
			orientGraph.shutdown();
		}
		return result;
	}

	private synchronized Map deleteByUserId(String userid) {
		Map<String, String> ret = new HashMap<String, String>();
		OrientGraph orientGraph = getConnection();
		try {
			if (isEmpty(userid) && userid.length() < 3) {
				throw new RuntimeException("AuthServiceImpl.deleteByUserId.userid is empty or too short");
			}
			orientGraph.begin();
			String d = "DELETE VERTEX SUser where userid=?";
			OCommandRequest del = new OCommandSQL(d);
			orientGraph.command(del).execute(userid);
			orientGraph.commit();
		} catch (Exception e) {
			error(this, "deleteByUserId.rollback:%[exception]s", e);
			orientGraph.rollback();
		}finally{
			orientGraph.shutdown();
		}
		return ret;
	}

	public Map getUserByEmail(String email) {
		return getUserByFilter(null, email);
	}

	public Map getUserByUserid(String id) {
		return getUserByFilter(id, null);
	}

	private Map getUserByFilter(String id, String email) {
		OrientGraph orientGraph = getConnection();
		try{
			String filter = "userid = ?";
			if (email != null) {
				filter = "email = ?";
			}
			info(this, "getUserByUserid:" + filter);
			OCommandRequest query = new OSQLSynchQuery("select from SUser where " + filter);
			Iterable<Vertex> result = orientGraph.command(query).execute(email != null ? email : id);
			Iterator<Vertex> it = result.iterator();
			if (it.hasNext()) {
				Vertex v = it.next();
				return ((OrientVertex) v).getProperties();
			}
		}finally{
			orientGraph.shutdown();
		}
		return null;
	}

	private List<Map> getUsersByWhere(String where) throws Exception {
		OrientGraph orientGraph = getConnection();
		List<Map> ret = new ArrayList<Map>();
		try{
			info(this, "getUsersByWhere:" + where);
			OCommandRequest query = new OSQLSynchQuery("select from SUser where " + where);
			Iterable<Vertex> result = orientGraph.command(query).execute();
			Iterator<Vertex> it = result.iterator();
			while (it.hasNext()) {
				Vertex v = it.next();
				ret.add(((OrientVertex) v).getProperties());
			}
		}finally{
			orientGraph.shutdown();
		}
		return ret;
	}

	public List<Map> getUserList() {
		return getUserList(null, 0, 0);
	}

	public List<Map> getUserList(Map filter) {
		return getUserList(filter, 0, 0);
	}

	public Map getUserData(String userid) {
		try {
			return getUserByUserid(userid);
		} catch (Throwable e) {
			throw new RuntimeException("AuthServiceImpl.getUserData:", e);
		} finally {
		}
	}

	private Map _createUser(String userid, Map<String, Object> data) throws Exception {
		data.put(USER_ID, userid);
		info(this, "_createUser:" + userid);
		Map<String, Object> ret = upsertUser(userid, (String) data.get(PASSWD), (String) data.get("email"), (String) data.get("surname"), (String) data.get("givenname"), (String) data.get("homedir"), (String) data.get("ftphomedir"), (List<String>) data.get("roles"), (Boolean) data.get("admin"), (Boolean) data.get("team_manage"));
		return ret;
	}

	private StoreDesc getStoreDesc() {
		StoreDesc sdesc = StoreDesc.get("global_odata");
		return sdesc;
	}

	/* BEGIN JSON-RPC-API*/
	//@RequiresRoles("admin")
	public Map getUsers(@PName("filter") @POptional Map filter) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc();
			if (filter == null) {
				filter = new HashMap();
				Map field1 = new HashMap();
				field1.put("field", USER_ID);
				field1.put("op", "cn");
				field1.put("data", "");
				field1.put("connector", null);
				field1.put("children", new ArrayList());
				List fieldList = new ArrayList();
				fieldList.add(field1);
				filter.put("children", fieldList);
			}
			Map params = new HashMap();
			params.put("filter", filter);
			params.put("pageSize", 0);
			SessionContext sessionContext = this.dataLayer.getSessionContext(sdesc);
			QueryBuilder qb = new OrientDBQueryBuilder(sdesc, "user", "global", sessionContext, filter, (Map) params, null);
			String where = qb.getWhere();
			List<Map> rows = getUsersByWhere(where);
			Map<String, List> ret = new HashMap<String, List>();
			if (this.permissionService.hasRole("admin")) {
				ret.put("rows", rows);
				return ret;
			}
			List f = new ArrayList();
			for (Map row : rows) {
				Map m = new HashMap();
				m.put("userid", row.get("userid"));
				f.add(m);
			}
			ret.put("rows", f);
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.getUsers:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Map createUser(@PName(USER_ID) String userid, @PName("data") Map data) throws RpcException {
		try {
			return _createUser(userid, data);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.createUser:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Map getUser(@PName(USER_ID) String userid) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc();
			return getUserByUserid(userid);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.getUser:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Map updateUser(@PName(USER_ID) String userid, @PName("data") Map data) throws RpcException {
		try {
			return _createUser(userid, data);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.updateUser:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Map deleteUser(@PName(USER_ID) String userid) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc();
			Map ret = deleteByUserId(userid);
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.deleteUser:", e);
		} finally {
		}
	}

	public Map getUserProperties() throws RpcException {
		try {
			String userid = null;
			try {
				userid = org.ms123.common.system.thread.ThreadContext.getThreadContext().getUserName();
			} catch (Exception e) {
				throw new RuntimeException("No userid");
			}
			return getUserProperties(userid);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.getUserProperties:", e);
		} finally {
		}
	}

	/* BEGIN:User registration */
	public Map existsUser(@PName(USER_ID) String userid, @PName(EMAIL) String email) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc();
			Map ret = new HashMap();
			ret.put("exists", true);
			try {
				Map um = getUserByUserid(userid);
				if (um == null) {
					um = getUserByEmail(email);
				}
				if (um == null) {
					ret.put("exists", false);
				}
			} catch (Throwable t) {
			}
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.existsUser:", e);
		} finally {
		}
	}

	private boolean _existsUser(String userid, String email) {
		Map<String, Boolean> ret = existsUser(userid, email);
		return ret.get("exists");
	}

	public Map requestAccount(@PName(USER_ID) String userid, @PName(EMAIL) String email, @PName("data") Map data) throws RpcException {
		try {
			Map ret = new HashMap();
			ret.put("status", "nok");
			String base = (String) data.get("base");
			String link = (String) data.get("link");
			String from = (String) data.get("from");
			String sender = (String) data.get("sender");
			String regards = (String) data.get("regards");
			String credentials = (String) data.get("credentials");
			String subject = (String) data.get("subject");
			String passwdText = (String) data.get("passwdText");
			String okUrl = (String) data.get("okUrl");
			String finalWork = (String) data.get("finalWork");

			PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useLower(true).useUpper(true).build();
			String passwd = passwordGenerator.generate(8);
			passwdText += ":" + passwd;

			if (isEmpty(credentials)) {
				info(this, "requestAccount(" + userid + "," + email + "):no credentials");
				return ret;
			}
			if (isEmpty(passwd)) {
				info(this, "requestAccount(" + userid + "," + email + "):passwd empty");
				return ret;
			}
			if (isEmpty(link)) {
				link = "Bitte diesen Link zur Vervollständigung der Registrierung anklicken";
			}
			if (isEmpty(regards)) {
				regards = "Regards<br>Manfred";
			}
			if (isEmpty(from)) {
				from = "info@simpl4.org";
			}
			if (isEmpty(sender)) {
				sender = "dashboard.sendRegistrationMail";
			}
			if (isEmpty(subject)) {
				subject = "Registration";
			}
			if (isEmpty(userid) || isEmpty(email)) {
				info(this, "requestAccount(" + userid + "," + email + "):userid/email empty");
				return ret;
			}
			if (isEmpty(okUrl)) {
				info(this, "requestAccount(" + userid + "," + email + "):ok url empty");
				return ret;
			}
			if (_existsUser(userid, email)) {
				info(this, "requestAccount(" + userid + "," + email + "):userid/email exists");
				return ret;
			}
			Map params = new HashMap();
			params.put("to", email);
			params.put("from", from);
			params.put("subject", subject);
			String body = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head>";
			body += "<body bgcolor=\"#ffffff\" text=\"#000000\">";
			body += "<br><br>" + passwdText + "<br><br><a href=\"" + getEmailLink(base, okUrl, finalWork, userid, email, credentials, passwd) + "\">" + link + "</a>";
			body += "<div><br><br><i>" + regards + "<i><br></div></body></html>";

			params.put("body", body);

			ExecutorService executor = getExecutorService();
			executor.submit(new MyCallable(sender, params, null));
			ret.put("status", "ok");
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.requestAccount:", e);
		} finally {
		}
	}

	public void createAccount(@PName(USER_ID) String userid, @PName(EMAIL) String email, @PName("passwd") String passwd, @PName("data") Map data, HttpServletResponse response) throws RpcException {
		try {
			info(this, "createUser(" + userid + "," + email + ")");
			if (isEmpty(userid) || isEmpty(email) || isEmpty(passwd)) {
				info(this, "createUser(" + userid + "," + email + "):userid/email empty");
				response.setContentType("application/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().close();
				return;
			}
			if (_existsUser(userid, email)) {
				info(this, "createUser(" + userid + "," + email + "):userid/email exists");
				response.setContentType("application/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().close();
				return;
			}
			Map udata = new HashMap();
			udata.put("userid", userid);
			udata.put("email", email);
			udata.put("password", passwd);
			udata.put("roles", "global.wawi,global.guest");
			//			_createUser( userid, udata);

			String okUrl = (String) data.get("okUrl");
			String finalWork = (String) data.get("finalWork");

			String body = "<html><head> <meta http-equiv=\"refresh\" content=\"1; url=" + okUrl + "\"> <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head>";
			body += "<body></body></html>";

			ExecutorService executor = getExecutorService();
			executor.submit(new MyCallable(finalWork, udata, userid));

			PrintWriter writer = response.getWriter();
			writer.print(body);
			writer.flush();

			response.setContentLength(body.length());
			response.setContentType("application/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().close();
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.createAccount:", e);
		} finally {
		}
	}

	private String getEmailLink(String base, String okUrl, String finalWork, String userid, String email, String credentials, String passwd) {
		Map rpc = new HashMap();
		Map createUserParams = new HashMap();
		createUserParams.put("userid", userid);
		createUserParams.put("email", email);
		createUserParams.put("passwd", passwd);
		Map dataParam = new HashMap();
		dataParam.put("okUrl", okUrl);
		dataParam.put("finalWork", finalWork);
		createUserParams.put("data", dataParam);
		rpc.put("service", "auth");
		rpc.put("method", "createAccount");
		rpc.put("params", createUserParams);

		String password = "guest";
		String username = "guest";
		byte[] b = (username + ":" + password).getBytes();
		Base64.Encoder encoder = Base64.getEncoder();
		//String credentials = new String(encoder.encode( b ));

		String s = encodeURIComponent(js.deepSerialize(rpc));
		s = base + "/rpc/get?rpc=" + s + "&credentials=" + credentials;

		info(this, "getEmailLink:" + s);
		return s;
	}

	private static String encodeURIComponent(String s) {
		String result = null;
		try {
			result = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%21", "!").replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")").replaceAll("\\%7E", "~");
		} catch (UnsupportedEncodingException e) {
			result = s;
		}
		return result;
	}

	public class MyCallable implements Callable<String> {
		String methodName;
		String ns;
		String userid;
		Map params;

		public MyCallable(String mn, Map p, String uid) {
			this.params = p;
			this.userid = uid;
			this.ns = mn.split("\\.")[0];
			this.methodName = mn;
		}

		@Override
		public String call() throws Exception {
			permissionService.loginInternal(ns);
			if (this.userid != null) {
				_createUser(this.userid, this.params);
			}
			info(this, "callCamel(" + this.methodName + "):" + this.params);
			callService.callCamel(this.methodName, this.params);
			return null;
		}
	}

	private ExecutorService getExecutorService() {
		return Executors.newSingleThreadExecutor();
	}

	/* END:User registration */

	/* BEG:Orientdb stuff */
	private void createClassAndIndex() {
		try {
			OSchemaProxy schema = orientGraph.getRawGraph().getMetadata().getSchema();
			if (schema.getClass("SUser") != null) {
				return;
			}
			OClass suser = createClass(schema, "SUser");
			createProperty(suser, "userid", OType.STRING);
			createProperty(suser, "password", OType.STRING);
			createProperty(suser, "email", OType.STRING);
			createProperty(suser, "givenname", OType.STRING);
			createProperty(suser, "surname", OType.STRING);
			createProperty(suser, "homedir", OType.STRING);
			createProperty(suser, "ftphomedir", OType.STRING);
			createProperty(suser, "admin", OType.BOOLEAN);
			createProperty(suser, "team_manage", OType.BOOLEAN);
			createLinkedProperty(suser, "roles", OType.EMBEDDEDLIST, OType.STRING);
			upsertUser("admin", "admin", null, null, null, null, null, null, true, false);
		} catch (Exception e) {
			error(this, "createClassAndIndex:%[exception]s", e);
			e.printStackTrace();
		}
	}

	private OClass createClass(OSchema schema, String className) {
		OClass oClass = schema.getClass(className);
		if (oClass == null) {
			oClass = schema.createClass(className);
			setSuperClasses(schema, oClass, true);
		}
		return oClass;
	}

	private void setSuperClasses(OSchema schema, OClass oClass, boolean restricted) {
		List<OClass> superList = new ArrayList<OClass>();
		superList.add(schema.getClass("V"));
		oClass.setSuperClasses(superList);
	}

	private OProperty createProperty(OClass oClass, String propertyName, OType oType) {
		OProperty prop = oClass.getProperty(propertyName);
		if (prop == null) {
			prop = oClass.createProperty(propertyName, oType);
		}
		return prop;
	}

	private OProperty createLinkedProperty(OClass oClass, String propertyName, OType type, OType linkedClass) {
		OProperty prop = oClass.getProperty(propertyName);
		if (prop == null) {
			prop = oClass.createProperty(propertyName, type, linkedClass);
		}
		return prop;
	}

	private OrientGraph getConnection(){
		try {
			OrientGraphFactory factory = this.orientdbService.getFactory(AUTH_DATABASE, false);
			return factory.getTx();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("AuthServiceImpl.initAuth:" , e);
		}
	}

	private void initAuth() {
		if (orientGraph != null) {
			return;
		}
		try {
			OrientGraphFactory factory = this.orientdbService.getFactory(AUTH_DATABASE, false);
			orientGraph = factory.getTx();
			createClassAndIndex();
		} catch (Exception e) {
			info(this, "AuthServiceImpl.initAuth:" + e.getMessage());
			orientGraph = null;
			e.printStackTrace();
		}
	}

	private synchronized Map<String, Object> upsertUser(String userid, String passwd, String email, String surname, String givenname) {
		return upsertUser(userid, passwd, email, surname, givenname, null, null, new ArrayList<String>(), false, false);
	}

	private synchronized Map<String, Object> upsertUser(String userid, String passwd, String email, String surname, String givenname, String homedir, String ftphomedir, List<String> roles, Boolean admin, Boolean team_manage) {
		initAuth();
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			if (isEmpty(userid) || userid.length() < 3) {
				throw new RuntimeException("AuthServiceImpl.userid is empty or too short");
			}
			this.orientGraph.begin();
			OCommandRequest query = new OSQLSynchQuery("select from SUser where userid=?");
			Iterable<Vertex> result = orientGraph.command(query).execute(userid);
			Iterator<Vertex> it = result.iterator();
			Vertex v = null;
			boolean update = false;
			if (it.hasNext()) {
				v = it.next();
				update = true;
			} else {
				v = orientGraph.addVertex("class:SUser");
			}
			setProperty(v, "userid", userid);
			setProperty(v, "password", passwd);
			setProperty(v, "email", email);
			setProperty(v, "surname", surname);
			setProperty(v, "givenname", givenname);
			setProperty(v, "homedir", homedir);
			setProperty(v, "ftphomedir", ftphomedir);
			setProperty(v, "roles", (roles == null) ? new ArrayList<String>() : roles);
			setProperty(v, "admin", admin);
			setProperty(v, "team_manage", team_manage);
			this.orientGraph.commit();
			if (update) {
				ret.put("updated", true);
			} else {
				ret.put("created", true);
			}
			ret.put("id", userid);
			return ret;
		} catch (Exception e) {
			error(this, "rollback:%[exception]s", e);
			orientGraph.rollback();
			return ret;
		}
	}

	private void setProperty(Vertex v, String pname, Object pvalue) {
		if (pvalue != null) {
			v.setProperty(pname, pvalue);
		}
	}
	/* END:Orientdb stuff */


	/* END JSON-RPC-API*/
	@Reference(target = "(kind=orientdb)", dynamic = true, optional = true)
	public void setDataLayer(DataLayer dataLayer) {
		System.out.println("AuthServiceImpl.setDataLayer:" + dataLayer);
		this.dataLayer = dataLayer;
	}

	@Reference(dynamic = true)
	public void setOrientdbService(OrientDBService paramOrientDBService) {
		this.orientdbService = paramOrientDBService;
	}

	@Reference(dynamic = true, optional = true)
	public void setCallService(CallService callService) {
		info(this, "AuthServiceImpl.setCallService:" + callService);
		this.callService = callService;
	}

	@Reference(dynamic = true, optional = true)
	public void setPermissionService(PermissionService paramPermissionService) {
		this.permissionService = paramPermissionService;
		info(this, "AuthServiceImpl.setPermissionService:" + paramPermissionService);
	}

}

