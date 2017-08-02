/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014,2017] [Manfred Sattler] <manfred@ms123.org>
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
package org.ms123.common.auth;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.io.PrintWriter;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Extent;
import javax.jdo.Query;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.namespace.NamespaceService;
import org.ms123.common.libhelper.Bean2Map;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.rpc.CallService;
import org.osgi.framework.BundleContext;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import flexjson.JSONSerializer;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.*;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Base64.Encoder;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import org.ms123.common.auth.user.*;

/** AuthService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=auth" })
public class AuthServiceImpl implements org.ms123.common.auth.api.AuthService, Constants {

	protected Inflector m_inflector = Inflector.getInstance();
	private JSONSerializer js = new JSONSerializer();

	private DataLayer m_dataLayer;

	private NamespaceService m_namespaceService;
	protected PermissionService m_permissionService;
	protected CallService m_callService;
	private String m_mainNamepace;

	private NucleusService m_nucleusService;

	public AuthServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		System.out.println("AuthServiceImpl.activate.props:" + props);
	}

	protected void deactivate() throws Exception {
		System.out.println("AuthServiceImpl deactivate");
	}

	private StoreDesc getStoreDesc(){
		if( m_mainNamepace == null){
			Map<String,String> branding = m_namespaceService.getBranding();
			m_mainNamepace = branding.get(NamespaceService.MAIN_NAMESPACE);
		}
		StoreDesc sdesc = null;
		if( m_mainNamepace != null){
			sdesc = StoreDesc.get(m_mainNamepace+"_user");
		}
		if( sdesc == null){
			sdesc = StoreDesc.getGlobalData();
		}
		return sdesc;
	}

	public String getAdminUser() {
		String adminuser = System.getProperty("admin_user");
		if (adminuser == null) {
			adminuser = ADMIN_USER;
		}
		return adminuser;
	}

	public Map getUserProperties( String userid) {
		try {
			debug(this,"getUserProperties.userid:" + userid);
			StoreDesc sdesc = getStoreDesc();
			Map ret = getUserByUserid(sdesc, userid);
			debug(this,"getUserProperties.ret:" + ret);
			return ret;
		} catch (RuntimeException e) {
			if (e.getCause() instanceof javax.jdo.JDOObjectNotFoundException) {
				throw new RuntimeException("user_not_exists:" + userid);
			} else {
				throw e;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
		}
	}

	public synchronized List<Map> getUserList( Map mfilter, int startIndex, int numResults){
		StoreDesc sdesc = getStoreDesc();
		List<Map> result = new ArrayList();
		String filter = null;//"userid == '" + id + "'";
		PersistenceManager pm = m_nucleusService.getPersistenceManagerFactory(sdesc).getPersistenceManager();
		Class clazz = m_nucleusService.getClass(sdesc, m_inflector.getClassName(USER_ENTITY));
		Extent e = pm.getExtent(clazz, true);
		Query q = pm.newQuery(e, filter);
		Bean2Map b2m = new Bean2Map();
		try {
			int count = 0;
			Collection coll = (Collection) q.execute();
			Iterator iter = coll.iterator();
			while (iter.hasNext()) {
				if( count < startIndex ) continue;
				if( numResults!=0 && (count-startIndex) > numResults) break;
				count++;
				Object obj = iter.next();
				result.add( b2m.transform(obj, new HashMap()));
			}
		} catch( Exception ex){
			error(this, "getUserList.ex:%[exception]s",ex);
			throw new RuntimeException(ex);
		} finally {
			q.closeAll();
		}
		return result;
	}
	private Map getUserByEmail(StoreDesc sdesc, String email) throws Exception {
		return getUserByFilter(sdesc,null,email);
	}
	private Map getUserByUserid(StoreDesc sdesc, String id) throws Exception {
		return getUserByFilter(sdesc,id,null);
	}
	private Map getUserByFilter(StoreDesc sdesc, String id,String email) throws Exception {
		String filter = "userid == '" + id + "'";
		if( email != null){
			filter = "email == '" + email + "'";
		}
		debug(this,"getUserByUserid:" + filter);
		PersistenceManager pm = m_nucleusService.getPersistenceManagerFactory(sdesc).getPersistenceManager();
		Class clazz = m_nucleusService.getClass(sdesc, m_inflector.getClassName(USER_ENTITY));
		Extent e = pm.getExtent(clazz, true);
		Query q = pm.newQuery(e, filter);
		try {
			Collection coll = (Collection) q.execute();
			Iterator iter = coll.iterator();
			if (iter.hasNext()) {
				Object obj = iter.next();
				Bean2Map b2m = new Bean2Map();
				return b2m.transform(obj, new HashMap());
			}
		} finally {
			q.closeAll();
		}
		return null;
	}

	public List<Map> getUserList( ) {
		return getUserList(null,0,0);
	}
	public List<Map> getUserList( Map filter) {
		return getUserList(filter,0,0);
	}

	public Map getUserData( String userid)  {
		try {
			StoreDesc sdesc = getStoreDesc();
			return getUserByUserid(sdesc, userid);
		} catch (Throwable e) {
			throw new RuntimeException("AuthServiceImpl.getUserData:", e);
		} finally {
		}
	}

	private Map _createUser(String userid, Map data) throws Exception {
		StoreDesc sdesc = getStoreDesc();
		data.put(USER_ID, userid);
		Map ret = m_dataLayer.updateObject(data, null, null, sdesc, USER_ENTITY, userid, null, null);
		userid = (String) data.get(USER_ID);
		String pw = (String) data.get(PASSWD);
		List groups = new ArrayList();
		Boolean created = (Boolean) ret.get("created");
		if (created != null && created) {
		} else {
		}
		return ret;
	}

	private Map _createGroup(String groupid, Map data) throws Exception {
		StoreDesc sdesc = getStoreDesc();
		Map ret = m_dataLayer.updateObject(data, null, null, sdesc, GROUP_ENTITY, groupid, null, null);
		Boolean created = (Boolean) ret.get("created");
		if (created != null && created) {
		}
		return ret;
	}

	/* BEGIN JSON-RPC-API*/
	//@RequiresRoles("admin")
	public Map getUsers(
			@PName("filter")           @POptional Map filter) throws RpcException {
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
			SessionContext sessionContext = m_dataLayer.getSessionContext(sdesc);
			Map params = new HashMap();
			params.put("filter", filter);
			params.put("pageSize", 0);
			Map ret = m_dataLayer.query(sessionContext, params, sdesc, USER_ENTITY);
			if (sessionContext.hasAdminRole())
				return ret;
			List<Map> rows = (List) ret.get("rows");
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
	public Map createUser(
			@PName(USER_ID)            String userid, 
			@PName("data")             Map data) throws RpcException {
		try {
			return _createUser(userid, data);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.createUser:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Map getUser(
			@PName(USER_ID)            String userid) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc();
			return getUserByUserid(sdesc, userid);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.getUser:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Map updateUser(
			@PName(USER_ID)            String userid, 
			@PName("data")             Map data) throws RpcException {
		try {
			return _createUser(userid, data);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.updateUser:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Map deleteUser(
			@PName(USER_ID)            String userid) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc();
			Map ret = m_dataLayer.deleteObject(null, sdesc, USER_ENTITY, userid);
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.deleteUser:", e);
		} finally {
		}
	}
	public Map getUserProperties() throws RpcException {
		try {
			String userid=null;
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
	public Map existsUser(
			@PName(USER_ID)            String userid,
			@PName(EMAIL)            String email
				) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc();
			Map ret = new HashMap();
			ret.put("exists", true);
			try{	
				Map um = getUserByUserid(sdesc, userid);
				if( um == null){
					um = getUserByEmail(sdesc, email);
				}
				if( um == null){
					ret.put("exists", false);
				}
			}catch( Throwable t){
			}	
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.existsUser:", e);
		} finally {
		}
	}

	private boolean _existsUser( String userid, String email){
		Map<String,Boolean> ret = existsUser( userid, email);
		return ret.get("exists");
	}

	public Map requestAccount(
			@PName(USER_ID)            String userid, 
			@PName(EMAIL)            String email, 
			@PName("data")             Map data) throws RpcException {
		try {
			Map ret = new HashMap();
			ret.put("status", "nok");
			String base = (String)data.get("base");
			String link = (String)data.get("link");
			String from = (String)data.get("from");
			String sender = (String)data.get("sender");
			String regards = (String)data.get("regards");
			String credentials = (String)data.get("credentials");
			String subject = (String)data.get("subject");
			String passwdText = (String)data.get("passwdText");
			String okUrl = (String)data.get("okUrl");
			String finalWork = (String)data.get("finalWork");

			PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
				.useDigits(true)
				.useLower(true)
				.useUpper(true)
				.build();
			String passwd = passwordGenerator.generate(8); 
			passwdText += ":" + passwd;

			if( isEmpty(credentials)){
				info(this,"requestAccount("+userid+","+email+"):no credentials");
				return ret;
			}
			if( isEmpty(passwd)){
				info(this,"requestAccount("+userid+","+email+"):passwd empty");
				return ret;
			}
			if( isEmpty(link) ){
				link = "Bitte diesen Link zur Vervollständigung der Registrierung anklicken";
			}
			if( isEmpty(regards)){
				regards = "Regards<br>Manfred";
			}
			if( isEmpty(from)){
				from = "info@simpl4.org";
			}
			if( isEmpty(sender)){
				sender = "dashboard.sendRegistrationMail";
			}
			if( isEmpty(subject)){
				subject = "Registration";
			}
			if( isEmpty( userid) || isEmpty( email)){
				info(this,"requestAccount("+userid+","+email+"):userid/email empty");
				return ret;
			}
			if( isEmpty( okUrl)){
				info(this,"requestAccount("+userid+","+email+"):ok url empty");
				return ret;
			}
			if( _existsUser(userid, email)){
				info(this,"requestAccount("+userid+","+email+"):userid/email exists");
				return ret;
			}
			Map params = new HashMap();
			params.put("to", email);
			params.put("from", from);
			params.put("subject", subject);
			String body = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head>";
						body += "<body bgcolor=\"#ffffff\" text=\"#000000\">";
            body += "<br><br>"+passwdText+"<br><br><a href=\""+getEmailLink(base, okUrl, finalWork, userid, email, credentials,passwd)+"\">"+link+"</a>";
						body += "<div><br><br><i>"+regards+"<i><br></div></body></html>";

			params.put("body", body);

			//m_callService.callCamel(sender, params);
			ExecutorService executor = getExecutorService();
			executor.submit(new MyCallable(sender,params, null));
			ret.put("status", "ok");
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "AuthServiceImpl.requestAccount:", e);
		} finally {
		}
	}

	public void createAccount(
			@PName(USER_ID)            String userid, 
			@PName(EMAIL)            String email, 
			@PName("passwd")            String passwd, 
			@PName("data")            Map data, 
						HttpServletResponse response ) throws RpcException {
		try {
			info(this,"createUser("+userid+","+email+")");
			if( isEmpty( userid) || isEmpty( email) || isEmpty(passwd)){
				info(this,"createUser("+userid+","+email+"):userid/email empty");
				response.setContentType("application/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().close();
				return;
			}
			if( _existsUser(userid, email)){
				info(this,"createUser("+userid+","+email+"):userid/email exists");
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

			String okUrl = (String)data.get("okUrl");
			String finalWork = (String)data.get("finalWork");

			String body = "<html><head> <meta http-equiv=\"refresh\" content=\"1; url="+okUrl+"\"> <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head>";
						body += "<body></body></html>";

			ExecutorService executor = getExecutorService();
			executor.submit(new MyCallable(finalWork,udata, userid));

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

	private String getEmailLink(String base, String okUrl, String finalWork, String userid, String email, String credentials, String passwd){
		Map rpc = new HashMap();
		Map createUserParams = new HashMap();
		createUserParams.put("userid", userid);
		createUserParams.put("email", email);
		createUserParams.put("passwd", passwd);
		Map dataParam = new HashMap();
		dataParam.put("okUrl", okUrl);
		dataParam.put("finalWork", finalWork);
		createUserParams.put("data", dataParam);
		rpc.put("service","auth");
		rpc.put("method","createAccount");
		rpc.put("params",createUserParams);

		String password = "guest";
		String username = "guest";
		byte[] b = (username + ":" + password).getBytes();
		Base64.Encoder encoder = Base64.getEncoder();
		//String credentials = new String(encoder.encode( b ));

		String s = encodeURIComponent(js.deepSerialize(rpc));
		s= base+"/rpc/get?rpc="+s + "&credentials=" + credentials;

		info(this, "getEmailLink:"+s);
		return s;
	}
	private static String encodeURIComponent(String s) {
		String result = null;
		try {
			result = URLEncoder.encode(s, "UTF-8")
				.replaceAll("\\+", "%20")
				.replaceAll("\\%21", "!")
				.replaceAll("\\%27", "'")
				.replaceAll("\\%28", "(")
				.replaceAll("\\%29", ")")
				.replaceAll("\\%7E", "~");
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
			m_permissionService.loginInternal(ns);
			if( this.userid != null){
				_createUser( this.userid, this.params);
			}
			info(this,"callCamel("+this.methodName+"):"+this.params);
			m_callService.callCamel(this.methodName, this.params);
			return null;
		}
	}

	private ExecutorService getExecutorService() {
		return Executors.newSingleThreadExecutor();
	}
	/* END:User registration */


	/* END JSON-RPC-API*/
	@Reference(target = "(kind=jdo)", dynamic = true, optional = true)
	public void setDataLayer(DataLayer dataLayer) {
		System.out.println("AuthServiceImpl.setDataLayer:" + dataLayer);
		m_dataLayer = dataLayer;
	}
	@Reference(dynamic = true, optional = true)
	public void setNamespaceService(NamespaceService ns) {
		System.out.println("AuthServiceImpl.setNamespaceService:" + ns);
		m_namespaceService = ns;
	}

	@Reference(dynamic = true)
	public void setNucleusService(NucleusService paramNucleusService) {
		this.m_nucleusService = paramNucleusService;
		System.out.println("AuthServiceImpl.setNucleusService:" + paramNucleusService);
	}
	@Reference(dynamic = true, optional=true)
	public void setCallService(CallService callService) {
		info(this,"AuthServiceImpl.setCallService:" + callService);
		m_callService = callService;
	}
	@Reference(dynamic = true, optional=true)
	public void setPermissionService(PermissionService paramPermissionService) {
		this.m_permissionService = paramPermissionService;
		info(this,"AuthServiceImpl.setPermissionService:" + paramPermissionService);
	}

}
