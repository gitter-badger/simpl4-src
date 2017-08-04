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

import flexjson.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.io.File;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Extent;
import javax.jdo.Query;
import org.apache.shiro.*;
import org.apache.shiro.subject.*;
import org.apache.commons.beanutils.BeanMap;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.git.GitService;
import org.ms123.common.store.StoreDesc;
import org.apache.shiro.authz.Permission;
import org.mvel2.MVEL;
import org.ms123.common.rpc.CallService;
import org.ms123.common.namespace.NamespaceService;
import org.springframework.util.AntPathMatcher;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.validator.routines.EmailValidator;

/**
 *
 */
@SuppressWarnings({"unchecked","deprecation"})
class BasePermissionServiceImpl implements Constants {

	protected AuthService m_authService;
	protected EmailValidator emailValidator = EmailValidator.getInstance(false,false);

	protected GitService m_gitService;
	protected CallService m_callService;
	protected NamespaceService m_namespaceService;

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();

	protected boolean getBoolean(Object val, boolean def) {
		try {
			if (val instanceof Boolean) {
				return (Boolean) val;
			}
			if (val instanceof String) {
				if ("false".equals(((String) val).toLowerCase())) {
					return false;
				}
				if ("true".equals(((String) val).toLowerCase())) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return def;
	}

	protected boolean noAuth() {
		String sh = System.getProperty("workspace");
		try {
			String basedir = new File(sh).getCanonicalFile().getParent();
			debug("NOAUTH:" + new File(basedir, "noauth").exists());
			return new File(basedir, "noauth").exists();
		} catch (Exception e) {
		}
		return false;
	}

	public boolean hasAdminRole() {
		return hasRole(ADMIN_ROLE);
	}

	public boolean hasRole(String role) {
		if( "global.guest".equals(role)) return true;
		try{
			Subject subject = SecurityUtils.getSubject();
			debug("hasRole:" + role + "/" + subject.hasRole(role));
			return subject.hasRole(role);
		}catch( Exception e){
			error("hasRole:",e);
			return false;
		}
	}

	public boolean isPermitted(Permission wp) {
		Subject subject = SecurityUtils.getSubject();
		boolean ret = subject.isPermitted(wp);
		debug("isPermitted:" + wp + "/" + ret);
		return ret;
	}

	public boolean isPermitted(String permission) {
		Subject subject = SecurityUtils.getSubject();
		WildcardPermission wp = new WildcardPermission(permission);
		boolean ret = subject.isPermitted(wp);
		debug("isPermitted:" + permission + "/" + ret);
		return ret;
	}

	private boolean hasFieldPermissions(WildcardPermission wp, String fieldName) {
		boolean b = hasRole(ADMIN_ROLE);
		if (b)
			return true;
		List<Set<String>> parts = wp.getParts();
		Set<String> keySet = new HashSet();
		keySet.add(fieldName);
		parts.set(parts.size() - 1, keySet);
		b = isPermitted(wp);
		if (b) {
			return true;
		}
		return b;
	}

	public boolean hasEntityPermissions(StoreDesc sdesc, String entity, String actions) {
		if (hasRole(ADMIN_ROLE)) {
			return true;
		}
		info("hasEntityPermissions|"+sdesc.getNamespace() + ":entities:" + sdesc.getPack() + ":" + entity);
		WildcardPermission wp = new WildcardPermission(sdesc.getNamespace() + ":entities:" + sdesc.getPack() + ":" + entity, actions);
		return isPermitted(wp);
	}

	public Map<String, Object> permissionFieldMapFilter(StoreDesc sdesc, String entity, Map<String, Object> fieldMap, String actions) {
		if (hasRole(ADMIN_ROLE)) {
			return fieldMap;
		}
		Map ret = new HashMap();
		WildcardPermission wp = new WildcardPermission(sdesc.getNamespace() + ":entities:" + StoreDesc.getFqEntityName(entity,sdesc)+ ":dummy", actions);
		Iterator<String> it = fieldMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (!hasFieldPermissions(wp, key)) {
				continue;
			}
			ret.put(key, fieldMap.get(key));
		}
		return ret;
	}

	public List<Map> permissionFieldListFilter(StoreDesc sdesc, String entity, List<Map> fieldList, String fieldKey, String actions) {
		debug("permissionFieldListFilter:" + fieldList + "/" + hasRole(ADMIN_ROLE));
		if (hasRole(ADMIN_ROLE)) {
			return fieldList;
		}
		List<Map> ret = new ArrayList();
		WildcardPermission wp = new WildcardPermission(sdesc.getNamespace() + ":entities:" + StoreDesc.getFqEntityName(entity,sdesc) + ":dummy", actions);
		Iterator<Map> it = fieldList.iterator();
		while (it.hasNext()) {
			Map map = it.next();
			if (!hasFieldPermissions(wp, (String) map.get(fieldKey))) {
				continue;
			}
			ret.add(map);
		}
		return ret;
	}

	public List<String> permissionFieldListFilter(StoreDesc sdesc, String entity, List<String> fieldList, String actions) {
		debug("permissionFieldListFilter2:" + fieldList + "/" + hasRole(ADMIN_ROLE));
		if (hasRole(ADMIN_ROLE)) {
			return fieldList;
		}
		List<String> ret = new ArrayList();
		WildcardPermission wp = new WildcardPermission(sdesc.getNamespace() + ":entities:" + StoreDesc.getFqEntityName(entity,sdesc) + ":dummy", actions);
		for (String fieldName : fieldList) {
			if (!hasFieldPermissions(wp, fieldName)) {
				continue;
			}
			ret.add(fieldName);
		}
		return ret;
	}

	public boolean isUserThis(String userid){
		if( userid == null || "".equals(userid)){
			return false;
		}
		String	username = org.ms123.common.system.thread.ThreadContext.getThreadContext().getUserName();
		String	subUserName = org.ms123.common.system.thread.ThreadContext.getThreadContext().getSubUserName();
		info("isUserThis("+userid+"):" + username+"/"+subUserName);
		if( emailValidator.isValid( userid) && !isEmpty(subUserName) ){
			return userid.equals(subUserName);
		}
		return userid.equals(username);
	}
	public boolean hasUserRole(String role){
		if( role == null || "".equals(role)){
			return false;
		}
		String	username = org.ms123.common.system.thread.ThreadContext.getThreadContext().getUserName();
		return getUserRoles(username).indexOf( role ) >= 0;
	}

	public List<String> getUserRoles( String userid ) {
		List<String> roleListRet = new ArrayList();
		if( emailValidator.isValid( userid)){
			roleListRet.add("global.guest");
			return roleListRet;
		}
		if( userid == null) return roleListRet;
		Map userProps = m_authService.getUserProperties(userid);
		List<String> roleList = null;
		if( userProps == null ) {
			return roleListRet;
		}
		String rs = (String) userProps.get(ROLES);
		if (rs != null && rs.length() >0) {
			roleList = Arrays.asList(rs.split("\\s*,\\s*"));
		}
		if (roleList != null) {
			for (String roleid : roleList) {
				String[] s = roleid.split("\\.");
				if (s.length != 2) {
					debug("getUserRoles:wrong roleid:" + roleid);
					continue;
				}
				roleListRet.add(roleid);
			}
		}
		if( !roleListRet.contains("global.guest")){
			roleListRet.add("global.guest");
		}
		List<String>	addRoles = (List)org.ms123.common.system.thread.ThreadContext.getThreadContext().get(ADDITIONAL_ROLES);
		if( addRoles != null ){
			roleListRet.addAll( addRoles );
		}
		debug("Permission.getUser.roleList("+userid+"):" + roleListRet);
		Set setItems = new HashSet(roleListRet);
		roleListRet.clear();
		roleListRet.addAll(setItems);
		return roleListRet;
	} 

	public boolean isFileAccesPermitted(String userName, List<String> permittedUserList, List<String> permittedRoleList) {
		List<String> userRoleList = null;
		try{
			userRoleList = getUserRoles(userName);
		}catch(Exception e){
			error("getUserRoles", e);
			return false;
		}
		info("UserRoleList:" + userRoleList);
		info("PermittedUserList:" + permittedUserList);
		if (permittedUserList != null && permittedUserList.contains(userName)) {
			info("userName(" + userName + ") is allowed:" + permittedUserList);
			return true;
		}
		info("permittedRoleList:" + permittedRoleList);
		for (String userRole : userRoleList) {
			if (permittedRoleList != null && permittedRoleList.contains(userRole)) {
				info("userRole(" + userRole + ") is allowed:" + permittedRoleList);
				return true;
			}
		}
		return false;
	}

	protected List<Map<String, Object>> getAccessRules(String namespace) {
		File file = new File(System.getProperty("git.repos") + "/" + namespace + "/.etc/access-rules.json");
		String content = null;
		try {
			content = readFileToString(file);
		} catch (Exception e) {
			info("LoginFilter.getAccessRules:" + e.getMessage());
			return null;
		}
		return (List) this.m_ds.deserialize(content);
	}

	protected Map<String, Object> getMatchingAccessRule(String resPath, List<Map<String, Object>> rules) {
		if (rules == null) {
			return null;
		}
		debug("getMatchingAccessRule:" + resPath);
		AntPathMatcher a = new AntPathMatcher();
		for (Map<String, Object> rule : rules) {
			String pat = (String) rule.get("pattern");
			boolean ok;
			if( pat.startsWith("/") && !resPath.startsWith("/")){
				ok = a.match(pat, "/"+ resPath);
			}else{
				ok = a.match(pat, resPath);
			}
			debug("\tmatch:" + pat + " -> " + ok);
			if (ok) {
				return rule;
			}
		}
		return null;
	}
	protected List<String> lookForAdditionalRoles(String namespace, String subid){
		String service = (String)m_namespaceService.getBranding().get(SUB_LOGIN);
		service = getFqServiceName( namespace, service );
		Map<String, String> params = new HashMap<String,String>();
		params.put(SUB_ID, subid );
		List<String> roles = (List)m_callService.callCamel( service, params);
		info("lookForAdditionalRoles.adding:"+roles);
		if( roles != null){
			org.ms123.common.system.thread.ThreadContext.getThreadContext().put(ADDITIONAL_ROLES, roles );
		}
		return roles;
	}

	protected String gainRealUser(String namespace, String userId, String password){
		String service = (String)m_namespaceService.getBranding().get(GAIN_REAL_USER);
		info("gainRealUser.service:"+service);
		if( isEmpty( service)){
			return null;
		}
		if( !isEmpty( namespace)){
			service = getFqServiceName( namespace, service );
		}
		Map<String, String> params = new HashMap<String,String>();
		params.put("email", userId );
		params.put("password", password );
		Map<String,Object> map = (Map)m_callService.callCamel( service, params);
		info("gainRealUser.ret:"+map);
		List<String> roles = (List)map.get("roles");
		String realUser = (String)map.get("mainUserId");
		if( roles != null){
			org.ms123.common.system.thread.ThreadContext.getThreadContext().put(ADDITIONAL_ROLES, roles );
		}
		return realUser;
	}

	private String getFqServiceName( String namespace, String service ){
		if( service.indexOf(".") == -1 &&  !"xyz".equals(namespace)){
			service = namespace + "."+ service;
		}
		return service;
	}

	protected void debug(String msg) {
		//System.out.println(msg);
		m_logger.debug(msg);
	}
	protected void info(String msg) {
		m_logger.info(msg);
	}
	protected void error(String msg, Exception e) {
		System.err.println(msg);
		e.printStackTrace();
		m_logger.error(msg,e);
	}
	private static final org.slf4j.Logger m_logger = org.slf4j.LoggerFactory.getLogger(BasePermissionServiceImpl.class);
}
