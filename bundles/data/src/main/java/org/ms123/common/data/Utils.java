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
package org.ms123.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jdo.PersistenceManager;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtils;
import org.ms123.common.data.api.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import groovy.lang.*;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.ms123.common.team.api.TeamService;

@SuppressWarnings("unchecked")
public class Utils {

	private static final String TEAMINTERN_ENTITY = "teamintern";
	private static GroovyShell m_groovyShell;
	static {
		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(org.ms123.common.datamapper.GroovyBase.class.getName());
		m_groovyShell = new GroovyShell(config);
	}

	public static String getString(Object newValue, Object oldValue, String mode) {
		if (newValue == null) {
			return null;
		}
		String ret = "";
		if (newValue instanceof List) {
			debug("newValue:" + newValue);
			debug("oldvalue:" + oldValue);
			debug("mode:" + mode);
			if ("replace".equals(mode) || oldValue == null || "".equals(oldValue)) {
				List list = (List) newValue;
				String komma = "";
				for (int i = 0; i < list.size(); i++) {
					ret += komma + list.get(i);
					komma = ",";
				}
			} else if ("add".equals(mode) || "remove".equals(mode)) {
				String[] oValues = ((String) oldValue).split(",");
				List<String> nValues = new ArrayList();
				for (int i = 0; i < oValues.length; i++) {
					nValues.add(oValues[i]);
				}
				List list = (List) newValue;
				for (int i = 0; i < list.size(); i++) {
					if ("add".equals(mode)) {
						if (!nValues.contains(list.get(i))) {
							nValues.add((String) list.get(i));
						}
					} else {
						if (nValues.contains(list.get(i))) {
							nValues.remove(list.get(i));
						}
					}
				}
				String komma = "";
				for (int i = 0; i < nValues.size(); i++) {
					ret += komma + nValues.get(i);
					komma = ",";
				}
			}
			debug("newvalue:" + ret);
		} else {
			ret = String.valueOf(newValue);
		}
		return ret;
	}

	public static Object listContainsId(Collection list, Map map, String idField) throws Exception {
		for (Object o : list) {
			String id1 = (String) PropertyUtils.getProperty(o, idField);
			if (map.get(idField) != null) {
				String id2 = String.valueOf(map.get(idField));
				if (id1.equals(id2)) {
					debug("\treturn:" + o);
					return o;
				}
			}
		}
		return null;
	}

	public static Object listContainsId(PersistenceManager pm, Collection list, Map map) throws Exception {
		for (Object o : list) {
			Object id1 = pm.getObjectId(o);
			if (map.get("id") != null) {
				Object id2 = map.get("id");
				if (String.valueOf(id2).equals(String.valueOf(id1))) {
					debug("listContainsId.return:"+id1);
					return o;
				}
			}
		}
		return null;
	}

	public static <T> boolean isCollectionEqual(Collection<T> lhs, Collection<T> rhs) {
		return lhs.size() == rhs.size() && lhs.containsAll(rhs) && rhs.containsAll(lhs);
	}
	public static  boolean isEmptyObj(Object o) {
		if( o instanceof String){
			String s=(String)o;
			return (s == null || "".equals(s.trim()));
		}
		if( o==null) return true;
		return false;
	}

	public static  boolean isEmpty(String s) {
		return (s == null || "".equals(s.trim()));
	}
	public static boolean containsId(List<String> list, String pk) {
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			if (it.next().endsWith("." + pk)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNum(String s) {
		try {
			Double.parseDouble(s);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean getBoolean(Object value) {
		try {
			return (Boolean) value;
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean getBoolean(Map m, String key, boolean _def) {
		try {
			return (Boolean) m.get(key);
		} catch (Exception e) {
		}
		return _def;
	}

	public static Long getLong(Object o) {
		try {
			if (o instanceof Long) {
				return (Long) o;
			}
			return Long.parseLong(String.valueOf(o));
		} catch (Exception e) {
			return -1L;
		}
	}

	public static String extractId(String s) {
		for (String part : s.split("/")) {
			if (isaId(part))
				return part;
		}
		return null;
	}

	public static boolean isaId(String s) {
		if (s == null || s.length() != 32)
			return false;
		boolean isNumeric = s.matches("\\p{XDigit}+");
		info("isAid:" + s + " -> " + isNumeric);
		return isNumeric;
	}

	public static Object eval(String expr, Map vars, Map<String,Script> scriptCache) {
		try {
			Script script = scriptCache.get(expr);
			if( script == null){
				script = m_groovyShell.parse(expr);
				scriptCache.put(expr,script);
			}
			Binding binding = new Binding(vars);
			script.setBinding(binding);
			return script.run();
		} catch (Throwable e) {
			e.printStackTrace();
			String msg = org.ms123.common.libhelper.Utils.formatGroovyException(e, expr);
			throw new RuntimeException(msg);
		}
	}

	public static Object getTeamintern(SessionContext sc, String teamid) {
		PersistenceManager pm = sc.getPM();
		try {
			Class clazz = sc.getClass(TEAMINTERN_ENTITY);
			Object obj = pm.getObjectById(clazz, teamid);
			debug("Teamintern:" + new HashMap(new BeanMap(obj)));
			return obj;
		} catch (Exception e) {
			throw new RuntimeException("TeamService.getTeamintern(" + teamid + ")", e);
		}
	}
	public static  String getBaseName(String name) {
		if (name == null || name.trim().equals(""))
			return null;
		int lindex = name.lastIndexOf(".");
		if (lindex == -1)
			return name;
		return name.substring(lindex + 1).toLowerCase();
	}

	//TEAM-Helper
	public static List<Map> getTeamChangedList(TeamService ts, Object object, Object objectPre) {
		List<Map> answer=null;
		if( PropertyUtils.isReadable(object,"_team_list")){
			try{
				Set nowList = (Set) PropertyUtils.getProperty(object, "_team_list");
				Set preList = null;
				if( objectPre != null){
					preList = (Set) PropertyUtils.getProperty(objectPre, "_team_list");
				}
				if (nowList != null || preList != null) {
					answer = getTeamChangedList(ts,null, preList, nowList);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return answer;
	}
	public static List<Map> getTeamChangedList(TeamService ts, Map<String,String> tc, Set<Map> preList, Set<Object> nowList) {
		Map<String, Map> nowMap = _toTeamNowMap(nowList);
		List<Map> teams = new ArrayList();
		if( tc == null) tc = getTeamChangedFlags(preList,nowList);
		for (String key : tc.keySet()) {
			if(tc.get(key) == null){
				 continue;
			}
			String op = tc.get(key);
			Map team = null;
			if( nowMap.get(key)!=null){
				team = nowMap.get(key);	
			}else{
				for (Map pre : preList) {
					if( pre.get("teamid").equals(key) ){
						team = pre;	
						break;
					}
				}
			}
			boolean valid = ts.checkTeamDate(team) && !getBoolean(team, "disabled", false);
			team.put("operation", op);
			team.put("valid", valid);
			teams.add(team);	
		}
		
		return teams;
	}

	public static Map getTeamChangedFlags(Set<Map> preList, Set<Object> nowList) {
		Map flags = new HashMap();
		Map<String, Map> nowMap = _toTeamNowMap(nowList);
		if (preList != null) {
			for (Map<String, Object> pre : preList) {
				String teamid = (String) pre.get("teamid");
				Map now = nowMap.get(teamid);
				if (now != null) {
					flags.put(teamid, _teamsEqual(pre, now) ? null : "update");
					nowMap.put(teamid, null);
				} else {
					flags.put(teamid, "delete");
				}
			}
		}
		Set<String> idSet = nowMap.keySet();
		for (String teamId : idSet) {
			if (nowMap.get(teamId) != null) {
				flags.put(teamId, "add");
			}
		}
		return flags;
	}

	private static boolean _teamsEqual(Map<String, Object> pre, Map<String, Object> now) {
		long validFromPre = getLong(pre.get("validFrom"));
		long validFromNow = getLong(now.get("validFrom"));
		long validToPre = getLong(pre.get("validTo"));
		long validToNow = getLong(now.get("validTo"));
		Boolean disabledPre = _getBoolean(pre.get("disabled"));
		Boolean disabledNow = _getBoolean(now.get("disabled"));
		boolean b = validFromPre == validFromNow && validToPre == validToNow && disabledPre == disabledNow;
		return b;
	}

	private static Map<String, Map> _toTeamNowMap(Set<Object> nowList) {
		Map<String, Map> retMap = new HashMap();
		if (nowList == null){
			return retMap;
		}
		for (Object t : nowList) {
			Map team = new HashMap(new BeanMap(t));
			team.remove("teamintern");
			retMap.put((String) team.get("teamid"), team);
		}
		return retMap;
	}

	public static List<String> prepareFields(List<String> fieldList, List<String> aliasList) {
		int i = 0;
		List<String> newList = new ArrayList();
		for (String fn : fieldList) {
			String alias = aliasList.get(i);
			if( alias != null && !(alias.startsWith("@") || alias.startsWith("%"))){
				newList.add(alias);
			}else{
				String f = removeFirstSegment(replaceDollar(fn));
				newList.add(f);
			}	
			i++;
		}
		return newList;
	}
	private static String replaceDollar(String s) {
		return s.replaceAll("\\$", ".");
	}
	private static  String removeFirstSegment(String s) {
		int i = s.indexOf(".");
		if (i == -1) {
			return "";
		}
		return s.substring(i + 1);
	}
	private static Boolean _getBoolean(Object b) {
		try {
			if (b == null){
				return null;
			}
			return (Boolean) b;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected static void debug(String message) {
		m_logger.debug(message);
	}

	protected static void info(String message) {
		m_logger.info(message);
		System.out.println(message);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(Utils.class);
}
