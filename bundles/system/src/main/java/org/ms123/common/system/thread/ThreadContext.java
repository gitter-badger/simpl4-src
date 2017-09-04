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
package org.ms123.common.system.thread;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.AbstractMap;
import javax.servlet.http.*;
import flexjson.*;
import eu.bitwalker.useragentutils.*;

/**
 *
 */
@SuppressWarnings("unchecked")
public class ThreadContext {

	private static ThreadLocal<ThreadContext> m_threadLocal = new InheritableThreadLocal<ThreadContext>();

	public static String SESSION_MANAGER = "sessionManager";

	private UserAgent userAgent;
	private String stringUserAgent;
	private String namespace;

	private String userName;
	private String subUserName;
	private Map m_properties = new HashMap();
	private Date startTime;

	private ThreadContext() {
	}

	;

	public static ThreadContext getThreadContext() {
		return m_threadLocal.get();
	}

	public static void loadThreadContext(HttpServletRequest request, HttpServletResponse response) {
		String r[] = request.getPathInfo().split("/");
		String namespace = (r.length < 2) ? "RPC" : r[1];
		String username = (String) request.getAttribute("username");
		String sua = request.getHeader("user-agent");
		if( sua == null) sua = "UNKNOWN";
		UserAgent userAgent = UserAgent.parseUserAgentString(sua);
		loadThreadContext( namespace, cleanUserName(username),userAgent,sua);
	}
	public static void loadThreadContext(String namespace,String username,UserAgent ua, String sua) {
		ThreadContext current = new ThreadContext();
		current.namespace = namespace;
		current.userName = username;
		current.userAgent = ua;
		current.stringUserAgent = sua;
		current.startTime = new Date();
		m_threadLocal.set(current);
	}
	public static void loadThreadContext(String namespace,String username,UserAgent ua) {
		ThreadContext current = new ThreadContext();
		current.namespace = namespace;
		current.userName = username;
		current.userAgent = ua;
		current.startTime = new Date();
		m_threadLocal.set(current);
	}
	public static void loadThreadContext(String namespace,String username) {
		ThreadContext current = new ThreadContext();
		current.namespace = namespace;
		current.userName = username;
		current.startTime = new Date();
		m_threadLocal.set(current);
	}

	public static void loadThreadContext(ThreadContext rc) {
		ThreadContext current = new ThreadContext();
		current.namespace = rc.getAppName();
		current.userName = rc.getUserName();
		current.userAgent = rc.getUserAgent();
		current.stringUserAgent = rc.getStringUserAgent();
		current.startTime = new Date();
		m_threadLocal.set(current);
	}

	private static String SUBID_DELIM ="#";
	private static String cleanUserName( String username){
		if( username != null && username.indexOf( SUBID_DELIM ) >0 ){
			String x[] = username.split(SUBID_DELIM);
			username = x[0];
		}
		return username;
	}
	public void remove(){
		m_threadLocal.remove();
	}
	public String getAppName() {
		return namespace;
	}
	public UserAgent getUserAgent() {
		return userAgent;
	}
	public String getStringUserAgent() {
		return stringUserAgent;
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String un) {
		userName=un;
	}
	public String getSubUserName() {
		return subUserName;
	}
	public void setSubUserName(String un) {
		subUserName=un;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setProperties(Map props){
		m_properties = props;
	}
	public Map getProperties(){
		return m_properties;
	}
	public void put(String key, Object value){
		m_properties.put(key,value);
	}
	public Object get(String key){
		return m_properties.get(key);
	}
	public synchronized void finalize(Throwable t){
		for( Object prop : m_properties.values()){
			if( prop instanceof ThreadFinalizer){
				((ThreadFinalizer)prop).finalize(t);
			}
		}
		m_properties=new HashMap();
	}
	/*public void setSessionManager(SessionManager sc) {
		m_sc = sc;
	}
	public SessionManager  getSessionManager() {
		return m_sc;
	}*/
}
