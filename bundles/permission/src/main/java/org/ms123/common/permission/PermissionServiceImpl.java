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
package org.ms123.common.permission;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.io.File;
import com.Ostermiller.util.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.apache.shiro.mgt.*;
import org.apache.shiro.*;
import org.apache.shiro.realm.*;
import org.apache.shiro.authz.*;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.*;
import org.apache.shiro.session.Session;
//import org.apache.shiro.util.ThreadContext;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.git.GitService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.namespace.NamespaceService;
import org.ms123.common.git.GitService;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.system.thread.ThreadContext;
import org.ms123.common.rpc.CallService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import static org.ms123.common.libhelper.Utils.listToList;
import org.apache.commons.validator.routines.EmailValidator;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/** PermissionService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=permission" })
public class PermissionServiceImpl extends BasePermissionServiceImpl implements org.ms123.common.permission.api.PermissionService {

	private String SUBID_DELIM = "#";
	protected MetaData m_gitMetaData;
	protected boolean m_isRuntimeSystem;

	public PermissionServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		info("PermissionServiceImpl.activate.props:" + props);
	}

	protected void deactivate() throws Exception {
		info("PermissionServiceImpl.deactivate");
	}

	public boolean loginInternal(String namespace) {
		return loginInternal(namespace,"admin","admin");
	}
	public boolean loginInternal(String namespace, String username,String password) {
		SimpleAccount sa = new SimpleAccount(username, password, namespace);
		org.ms123.common.system.thread.ThreadContext.loadThreadContext(namespace,username);	
		sa.addRole("admin");
		MyRealm realm = new MyRealm();
		realm.add(sa);
		DefaultSecurityManager sm = createSecurityManager(realm);
		Subject currentUser = newSubject(sm);
		UsernamePasswordToken token = new UsernamePasswordToken("admin", "admin");
		try {
			currentUser.login(token);
		} catch (UnknownAccountException uae) {
			info("2.There is no user with username of " + token.getPrincipal() + "/" + uae);
			return false;
		} catch (IncorrectCredentialsException ice) {
			info("Password for account " + token.getPrincipal() + " was incorrect!");
			return false;
		} catch (LockedAccountException lae) {
			info("The account for username " + token.getPrincipal() + " is locked.  " + "Please contact your administrator to unlock it.");
			return false;
		} catch (AuthenticationException ae) {
			ae.printStackTrace();
			return false;
		}
		return true;
	}
	private void addGuestRole(List<Map> gresult){
		boolean hasGuestRole = false;
		for( Map ro : gresult){
			if( "global.guest".equals(ro.get("name"))){
				hasGuestRole=true;
			}
		}
		if( !hasGuestRole){
			Map rmap = new HashMap();
			rmap.put("name", "global.guest");
			gresult.add(rmap);
		}
	}
	private List<Map> getPermissions(Map userProps, List<String> additionalRoles, String filter){
		List<Map> permissions = new ArrayList();
		Pattern p = null;
		if( filter != null){
			p = Pattern.compile(filter);
		}
		debug("userProps:" + userProps);
		debug("filter:" + filter);
		List<String> roleList = null;
		String rs = (String) userProps.get(ROLES);
		if (rs != null && rs.length() > 0) {
			roleList = Arrays.asList(rs.split("\\s*,\\s*"));
		}
		if( additionalRoles != null){
			if( roleList == null){
				roleList = additionalRoles;
			}else{
				List<String> newList = new ArrayList<String>();
				newList.addAll( roleList);
				newList.addAll( additionalRoles);
				roleList = newList;
			}
		}
		debug("roleList:" + roleList);
		if (roleList != null) {
			for (String roleid : roleList) {
				String[] s = roleid.split("\\.");
				if (s.length != 2) {
					info("PermissionServiceImpl.login:wrong roleid:" + roleid);
					continue;
				}
				Map<String,List<Map>> pm = getRole(s[0], roleid);
				if (pm != null) {
					List<Map> permList = pm.get(PERMISSIONS);
					if (permList != null) {
						for (Map<String,String> perm : permList) {
							if (getBoolean(perm.get("enabled"), false)) {
								String sperm = perm.get("permission");
								if( p == null){
									permissions.add(perm);
								}else{
									boolean b = p.matcher(sperm).matches();
									if( b ){
										permissions.add(perm);
									}
								}
							}
						}
					}
				}
			}
		}
		return permissions;
	}

	public boolean login(String namespace, String username, String password) {
		info("login:" + username + "/" + password + "/namespace:" + namespace);

		String subId = null;
		if( username.indexOf( SUBID_DELIM ) >0 ){
			String x[] = username.split(SUBID_DELIM);
			username = x[0];
			subId = x[1];
		}
		if( ThreadContext.getThreadContext() == null ){
			ThreadContext.loadThreadContext(namespace,username);	
		}
		Map userProps = null;
		boolean userIsAuthenticated = false;
		try {
			if (noAuth()) {
				userProps = new HashMap();
				userProps.put("admin", true);
				username = m_authService.getAdminUser();
			} else {
				info("emailValidator("+username+").isValid:" + emailValidator.isValid(username));
				if( emailValidator.isValid( username)){
					String uname = gainRealUser( namespace, username, password);
					if( !isEmpty(uname)){
						ThreadContext.getThreadContext().setSubUserName( username );
						ThreadContext.getThreadContext().setUserName( uname);
						username=uname;
						userIsAuthenticated = true;
					}
				}
				info("realUser:"+username);
				userProps = m_authService.getUserProperties(username);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (userProps == null) {
			info("1.There is no user with username of " + username);
			return false;
		}
		debug("login:"+userProps);
		String passwordCmp = (String)userProps.get("password");
		if( !userIsAuthenticated && passwordCmp != null){
			if( password == null) password="";
			if( !passwordCmp.trim().equals(password.trim()) &&  !( passwordCmp.equals("") && password.equals("admin"))){
				debug("passwordCmp:"+password+"/"+passwordCmp+"|");
				throw new RuntimeException("Login failed");	
			}
		}
		SimpleAccount sa = new SimpleAccount(username, password, namespace);
		if( "guest".equals(username) && "guest".equals(password)){
			sa.addRole("global.guest");
		}
		
		List<String> additionalRoles=null;
		if( subId != null  ){
			additionalRoles = lookForAdditionalRoles(namespace,subId);
		}
		boolean isAdmin = getBoolean(userProps.get("admin"), false);
		if (isAdmin) {
			sa.addRole("admin");
		} else {
			try {
				//List<Map> permissions = getPermissions(userProps, "^.*:entities:.*");
				List<Map> permissions = getPermissions(userProps, additionalRoles, null);
				Iterator<Map> pit = permissions.iterator();
				while (pit.hasNext()) {
					Map p = pit.next();
					String permission = p.get("permission") + ":" + p.get("actions");
					debug("\tpermission:" + permission);
					sa.addObjectPermission(new WildcardPermission((String) p.get("permission"), (String) p.get("actions")));
				}
				sa.addObjectPermission(new WildcardPermission("*:entities:aid", "read"));
				sa.addObjectPermission(new WildcardPermission("*:entities:*:filter", "read,write"));
				sa.addObjectPermission(new WildcardPermission("*:entities:*:importing", "read,write"));
				sa.addObjectPermission(new WildcardPermission("*:entities:*:report", "read,write"));
				sa.addObjectPermission(new WildcardPermission("global", "read"));
				//if( getBoolean(userProps.get("team_manage"), false)){
				sa.addObjectPermission(new WildcardPermission("*:entities:*:teamintern", "read,write"));
				sa.addObjectPermission(new WildcardPermission("*:entities:*:team", "read"));
				sa.addObjectPermission(new WildcardPermission("*:entities:*:user:userid", "read"));
				//}
				sa.addObjectPermission(new WildcardPermission("*:entities:*:enumeration", "read"));
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		MyRealm realm = new MyRealm();
		realm.add(sa);
		DefaultSecurityManager sm = createSecurityManager(realm);
		Subject currentUser = newSubject(sm);
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		try {
			currentUser.login(token);
		} catch (UnknownAccountException uae) {
			info("2.There is no user with username of " + token.getPrincipal() + "/" + uae);
			return false;
		} catch (IncorrectCredentialsException ice) {
			info("Password for account " + token.getPrincipal() + " was incorrect!");
			return false;
		} catch (LockedAccountException lae) {
			info("The account for username " + token.getPrincipal() + " is locked.  " + "Please contact your administrator to unlock it.");
			return false;
		} catch (AuthenticationException ae) {
			ae.printStackTrace();
			return false;
		}
		return true;
	}

	private DefaultSecurityManager createSecurityManager(Realm realm) {
		DefaultSecurityManager sm = new DefaultSecurityManager(realm);
		DefaultSubjectDAO dao = (DefaultSubjectDAO) sm.getSubjectDAO();
		DefaultSessionStorageEvaluator ev = (DefaultSessionStorageEvaluator) dao.getSessionStorageEvaluator();
		ev.setSessionStorageEnabled(false);
		return sm;
	}

	private Subject newSubject(DefaultSecurityManager securityManager) {
		Subject subject = new Subject.Builder(securityManager).buildSubject();
		SubjectThreadState threadState = new SubjectThreadState(subject);
		threadState.bind();
		return subject;
	}
	private boolean checkPassword( String password, String passwordCmp ){
		if( passwordCmp != null){
			if( password == null) password="";
			if( !passwordCmp.trim().equals(password.trim()) &&  !( passwordCmp.equals("") && password.equals("admin"))){
				debug("passwordCmp:"+password+"/"+passwordCmp+"|");
				return false;
			}
		}
		return true;
	}

	/* BEGIN JSON-RPC-API*/
	public Map checkCredentials(
			@PName("credentials") String credentials ) throws RpcException {
		try {
			
			Map<String,Object> ret = new HashMap<String, Object>();
			ret.put("ok", false);
			if( isEmpty( credentials) || credentials.length() < 6 ){
				return ret;
			}
			credentials = Base64.decode(credentials);
			int ind = credentials.indexOf(":");
			if (ind != -1) {
				String username = credentials.substring(0, ind);
				String password = credentials.substring(ind+1);

				boolean userIsAuthenticated = false;
				if( emailValidator.isValid( username)){
					String uname = gainRealUser( null, username, password);
					if( !isEmpty(uname)){
						username=uname;
						userIsAuthenticated = true;
					}
				}
				info("realUser:("+username+"):"+userIsAuthenticated);
				Map userProps = m_authService.getUserProperties(username);
				if( userProps == null){
					info("Username("+username+"):not found");
					return ret;
				}
				String passwordCmp = (String)userProps.get("password");
				if( !userIsAuthenticated && !checkPassword( password,passwordCmp)){
					info("Username("+username+"):password not ok");
					return ret;
				}
				List<String> roles = getUserRoles( username);
				ret.put("ok", true);
				ret.put("roles", roles);
				return ret;
			}
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "PermissionServiceImpl.checkCredentials:", e);
		}
	}
	@RequiresRoles("admin")
	public List getRoles(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName("withGlobal")       @PDefaultBool(false) @POptional Boolean withGlobal, 
			@PName("mapping")          @POptional Map mapping, 
			@PName("filter")           @POptional String filter) throws RpcException {
		try {
			List result = m_gitMetaData.getRoles(namespace);
			if( withGlobal){
				List gresult = m_gitMetaData.getRoles("global");
				addGuestRole(gresult);
				result.addAll(gresult);
			}
			return listToList(result, mapping, filter);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "PermissionServiceImpl.getRoles:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public List getAllRoles(
			@PName("mapping")          @POptional Map mapping, 
			@PName("filter")           @POptional String filter) throws RpcException {
		try {
			List<Map> result = new ArrayList();
			List<Map> repoList = m_gitService.getRepositories(new ArrayList(), false);
			for (Map<String, String> repo : repoList) {
				String name = repo.get("name");
				//if (GLOBAL.equals(name)) continue;
				List<Map> r = m_gitMetaData.getRoles(name);
				result.addAll(r);
			}
			addGuestRole(result);
			return listToList(result, mapping, filter);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "PermissionServiceImpl.getRoles:", e);
		} finally {
		}
	}

	public List getAllRolesInternal() throws Exception{
		try {
			List<Map> result = new ArrayList();
			List<Map> repoList = m_gitService.getRepositories(new ArrayList(), false);
			for (Map<String, String> repo : repoList) {
				String name = repo.get("name");
				List<Map> r = m_gitMetaData.getRoles(name);
				result.addAll(r);
			}
			addGuestRole(result);
			return listToList(result, null, null);
		} catch (Throwable e) {
			throw new RuntimeException("PermissionServiceImpl.getAllRolesInternal:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public void deleteRole(
			@PName(StoreDesc.NAMESPACE) @POptional String namespace, 
			@PName("name")             String name) throws RpcException {
		try {
			m_gitMetaData.deleteRole(namespace, name);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "PermissionServiceImpl.deleteRole:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public void saveRole(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName("name")             String name, 
			@PName("data")             Map roleData) throws RpcException {
		try {
			m_gitMetaData.saveRole(namespace, name, roleData);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "PermissionServiceImpl.savePermissions:", e);
		} finally {
		}
	}

	public Map getRole(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName("name")             String name) throws RpcException {
		try {
			return m_gitMetaData.getRole(namespace, name);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "PermissionServiceImpl.getRole:", e);
		}
	}

	public List<String> getRolesByUserId(
			@PName("uid")             String uid) throws RpcException {
		try {
			return getUserRoles(uid);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "PermissionServiceImpl.getRolesByUserId:", e);
		}
	}

	public List<Map> getAccessPermissionsForFileList(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName("filenames")             List<String> filenames) throws RpcException {
		try {
			String	username = org.ms123.common.system.thread.ThreadContext.getThreadContext().getUserName();
			List<Map> result = new ArrayList();
			List<Map<String, Object>> rules = getAccessRules(namespace);
			for( String name : filenames){
				Map<String, Object>	accessRule = getMatchingAccessRule(name, rules);
				Map access = new HashMap();
				access.put("filename", name);
				access.put("access", true);
				if (accessRule != null) {
					if (!isFileAccesPermitted(username, (List) accessRule.get(PERMITTED_USERS), (List) accessRule.get(PERMITTED_ROLES))) {
						access.put("access", false);
					}
				}
				result.add(access);
			}
			return result;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "PermissionServiceImpl.getAccessPermissionsForFileList:", e);
		}
	}

	/* END JSON-RPC-API*/
	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		info("PermissionServiceImpl.setGitService:" + gitService);
		m_gitService = gitService;
		m_gitMetaData = new GitMetaDataImpl(this, gitService);
	}

	@Reference(dynamic = true, optional=true)
	public void setNamespaceService(NamespaceService nss) {
		info("PermissionServiceImpl.setNamespaceService:" + nss);
		m_isRuntimeSystem = nss.isRuntimeSystem();
		m_namespaceService = nss;
	}

	@Reference(dynamic = true, optional=true)
	public void setCallService(CallService callService) {
		info("PermissionServiceImpl.setCallService:" + callService);
		m_callService = callService;
	}

	@Reference(dynamic = true, optional = true)
	public void setAuthService(AuthService authService) {
		this.m_authService = authService;
		info("PermissionImpl.setAuthService:" + authService);
	}
}
