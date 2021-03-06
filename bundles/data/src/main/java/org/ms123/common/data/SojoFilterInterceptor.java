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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Date;
import java.util.Stack;
import java.lang.reflect.*;
import org.ms123.common.utils.*;
import org.ms123.common.store.StoreDesc;
import net.sf.sojo.common.WalkerInterceptor;
import net.sf.sojo.core.Constants;
import net.sf.sojo.interchange.SerializerException;
import net.sf.sojo.util.Util;
import net.sf.sojo.interchange.json.JsonWalkerInterceptor;
import net.sf.sojo.common.*;
import net.sf.sojo.core.*;
import net.sf.sojo.core.conversion.*;
import net.sf.sojo.core.reflect.*;
import net.sf.sojo.navigation.*;
import net.sf.sojo.core.filter.ClassPropertyFilterHandler;
import net.sf.sojo.core.filter.ClassPropertyFilter;
import java.io.ByteArrayInputStream;
import flexjson.*;
import org.ms123.common.libhelper.Base64;
import javax.jdo.annotations.Persistent;
import org.ms123.common.data.api.SessionContext;
import org.apache.commons.beanutils.BeanMap;
import static org.apache.commons.lang3.StringUtils.stripEnd;

@SuppressWarnings("unchecked")
public class SojoFilterInterceptor implements WalkerInterceptor {

	protected Inflector m_inflector = Inflector.getInstance();

	private SessionContext m_sessionContext;

	private List<String> m_aliasList = null;

	private Map<String, Integer> m_fieldMap = new HashMap();

	private Map<String, Integer> m_pathMap = new HashMap();

	private Map m_result = new HashMap();

	private Map m_lastCurrent = new HashMap();

	private Object m_current = null;

	private Stack<String> m_moduleNameStack = new Stack();

	private String m_currentModuleName = "ROOT";
	private String m_pack;
	private Map m_currentFieldMap = new HashMap();

	private boolean m_isAdmin=false;

	public Map getResult() {
		return m_result;
	}
	private static JSONSerializer m_js = new JSONSerializer();

	public void setSessionContext(SessionContext sess) {
		m_sessionContext = sess;
	}
	private void setAdmin(boolean isa) {
		m_isAdmin = isa;
	}
	private boolean isAdmin() {
		return m_isAdmin;
	}

	public static Map filterFields(Object o, SessionContext sc, List<String> fieldList, List<String> aliasList, String pack) {
		SojoFilterInterceptor interceptor = new SojoFilterInterceptor();
		ObjectGraphWalker walker = new ObjectGraphWalker();
		walker.setUseBeanMap(true);
		ReflectionHelper.addSimpleType(org.datanucleus.store.types.wrappers.Date.class);
		walker.setIgnoreNullValues(true);
		interceptor.setFields(fieldList, aliasList);
		interceptor.setPack(pack);
		interceptor.setSessionContext(sc);
	  interceptor.setAdmin(sc.getPermissionService().hasAdminRole());
		walker.addInterceptor(interceptor);
		walker.walk(o);
		return interceptor.getResult();
	}


	public boolean visitElement(Object pvKey, int pvIndex, Object pvValue, int pvType, String pvPath, int pvNumberOfRecursion) {
		pvPath = cleanPath(pvPath);
		boolean isBinary = isBinaryDatatype(pvKey);
		if (isBinary || pvType == Constants.TYPE_SIMPLE) {
			if (m_fieldMap.get(pvPath) == null) {
				return false;
			}
			if (pvKey != null && pvKey.getClass().equals(String.class)) {
				if (m_sessionContext.isFieldPermitted((String) pvKey, StoreDesc.getFqEntityName(m_currentModuleName,m_pack))) {
					String fieldName = (String) pvKey;
					int index = m_fieldMap.get(pvPath);
					String alias = m_aliasList.get(index);
					if (alias != null && alias.length() > 0 && !(alias.startsWith("@")||alias.startsWith("%"))) {
						fieldName = alias;
					}
					if( isBinary ){
						pvValue = toBase64(pvValue);
						((Map) m_current).put(fieldName, pvValue);
						return true;
					}
					if( pvValue instanceof java.util.Date){
						pvValue = ((java.util.Date)pvValue).getTime();
					}
					if( pvValue instanceof String){
						pvValue = stripEnd((String)pvValue," ");
					}
					((Map) m_current).put(fieldName, pvValue);
				}
			}
		} else if (pvType == Constants.TYPE_NULL) {
		} else if (pvValue != null) {
			if (pvKey != null) {
				if (m_pathMap.get(pvPath) == null) {
					return true;
				}
			}
			if (pvType == Constants.TYPE_MAP) {
				Object teams = ((Map) pvValue).get("_team_list");
				if (!isAdmin() && teams != null && ((Collection) teams).size() > 0) {
					for( Object _team : ((Collection<Map>) teams)){ //@@@MS Copy the "team.name" from "teamintern" to "team"
						BeanMap team = new BeanMap(_team);
						if(team.get("name") == null){
							Object _teamintern = (Object)team.get("teamintern");
							if( _teamintern!=null){
								BeanMap teamintern = new BeanMap(_teamintern);
								team.put("name", teamintern.get("name"));
								team.put("teamintern",null);
							}
						}
					}
					if (!m_sessionContext.hasTeamPermission(teams)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void visitIterateableElement(Object pvValue, int pvType, String pvPath, int pvBeginEnd) {
		pvPath = removeLastPointOnPath(pvPath);
		if (pvBeginEnd == Constants.ITERATOR_BEGIN) {
			m_lastCurrent.put(pvPath, m_current);
			if (pvType == Constants.TYPE_ITERATEABLE) {
				List newList = new ArrayList();
				if (m_current instanceof List) {
					((List) m_current).add(newList);
				}
				if (m_current instanceof Map) {
					((Map) m_current).put(getLastSegment(pvPath), newList);
				}
				m_current = newList;
			} else if (pvType == Constants.TYPE_MAP) {
				m_moduleNameStack.push(m_currentModuleName);
				m_currentModuleName = getEntityName(((Map) pvValue).get("class"));
				m_currentFieldMap = getCurrentFieldMap();
				Map newMap = m_current != null ? new HashMap() : m_result;
				if (m_current instanceof List) {
					((List) m_current).add(newMap);
				}
				if (m_current instanceof Map) {
					((Map) m_current).put(getLastSegment(pvPath), newMap);
				}
				m_current = newMap;
			}
		} else if (pvBeginEnd == Constants.ITERATOR_END) {
			m_current = m_lastCurrent.get(pvPath);
			if (pvType == Constants.TYPE_ITERATEABLE) {
			} else if (pvType == Constants.TYPE_MAP) {
				m_currentModuleName = m_moduleNameStack.pop();
				m_currentFieldMap = getCurrentFieldMap();
			}
		}
	}

	public void startWalk(Object pvStartObject) {
	}

	public void endWalk() {
	}

	private Map getCurrentFieldMap(){
		try{
				return m_sessionContext.getPermittedFields(StoreDesc.getFqEntityName(m_currentModuleName,m_pack));
		}catch(Exception e){
			return new HashMap();
		}
	}

	private boolean isBinaryDatatype(Object pvKey){
		if (pvKey != null && pvKey.getClass().equals(String.class)) {
			Map fm = (Map)m_currentFieldMap.get(pvKey);
			if( fm != null){
				String dt = (String)fm.get("datatype");
				if( "binary".equals(dt)){
					return true;
				}
			}
		}
		return false;
	}
	private String toBase64(Object pvValue){
		return Base64.encode( new ByteArrayInputStream((byte[])pvValue));
	}

	private void setPack(String pack){
		m_pack = pack;
	}
	private void setFields(List<String> fieldList, List<String> aliasList) {
		m_aliasList = aliasList;
		m_fieldMap = new HashMap();
		m_pathMap = new HashMap();
		int i = 0;
		for (String fn : fieldList) {
			m_fieldMap.put(removeFirstSegment(replaceDollar(fn)), i++);
		}
		i = 0;
		for (String fn : fieldList) {
			m_pathMap.put(removeLastSegment(removeFirstSegment(replaceDollar(fn))), i++);
		}
	}

	public String removeLastPointOnPath(String pvPath) {
		String lvPath = pvPath;
		if (lvPath.endsWith(".")) {
			lvPath = lvPath.substring(0, lvPath.length() - 1);
		}
		return lvPath;
	}

	private String replaceDollar(String s) {
		return s.replaceAll("\\$", ".");
	}

	private String removeFirstSegment(String s) {
		int i = s.indexOf(".");
		if (i == -1) {
			return "";
		}
		return s.substring(i + 1);
	}

	private String removeLastSegment(String s) {
		int i = s.lastIndexOf(".");
		if (i == -1) {
			return "";
		}
		return s.substring(0, i);
	}

	private String cleanPath(String pvPath) {
		pvPath = pvPath.replaceAll("\\[[0-9]*\\]", "");
		pvPath = pvPath.replaceAll("\\.\\(\\)", "");
		pvPath = pvPath.replaceAll("\\(\\)", "");
		pvPath = pvPath.replaceAll("\\.\\.", "");
		return pvPath;
	}

	private String getLastSegment(String path) {
		return getLastSegment(path, ".");
	}

	private String getLastSegment(String path, String sep) {
		int lastDot = path.lastIndexOf(sep);
		return path.substring(lastDot + 1);
	}

	private String getEntityName(Object clazz) {
		return m_inflector.getEntityName(getLastSegment((String) clazz.toString())).toLowerCase();
	}

	private String printBeginEnd(int num) {
		switch(num) {
			case Constants.ITERATOR_BEGIN:
				return "BEGIN";
			case Constants.ITERATOR_END:
				return "END";
			default:
				return "";
		}
	}

	private String printType(int num) {
		switch(num) {
			case Constants.TYPE_NULL:
				return "NULL";
			case Constants.TYPE_SIMPLE:
				return "SIMPLE";
			case Constants.TYPE_ITERATEABLE:
				return "ITERATEABLE";
			case Constants.TYPE_MAP:
				return "MAP";
			default:
				return "";
		}
	}

}
