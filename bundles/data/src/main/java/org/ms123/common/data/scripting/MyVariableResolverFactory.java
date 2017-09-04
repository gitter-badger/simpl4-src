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
package org.ms123.common.data.scripting;

import org.mvel2.UnresolveablePropertyException;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Date;
import java.util.Iterator;
import javax.jdo.PersistenceManager;
import org.apache.commons.beanutils.*;

@SuppressWarnings({ "unchecked" })
public class MyVariableResolverFactory extends MapVariableResolverFactory {

	Map<String, Class> m_module;

	PersistenceManager m_pm;

	public MyVariableResolverFactory(Map variables, Map<String, Class> entity, PersistenceManager pm) {
		this.variables = variables;
		m_pm = pm;
		m_module = entity;
	}

	public MyVariableResolverFactory(Map variables) {
		this.variables = variables;
	}

	public VariableResolver getVariableResolver(String name) {
		//System.out.println("=> MyVariableResolverFactory.getVariableResolver:" + name); 
		VariableResolver vr = variableResolvers.get(name);
		if (vr != null) {
			return vr;
		} else if (variables.containsKey(name)) {
			variableResolvers.put(name, vr = new MapVariableResolver(variables, name));
			return vr;
		} else if (nextFactory != null) {
			vr = nextFactory.getVariableResolver(name);
			return vr;
		}
		throw new UnresolveablePropertyException("unable to resolve variable '" + name + "'");
	}

	public boolean isResolveable(String name) {
		boolean b = (variableResolvers.containsKey(name)) || (variables != null && variables.containsKey(name)) || (nextFactory != null && nextFactory.isResolveable(name));
		if (b) {
			return true;
		}
		if (m_module == null)
			return false;
		long start = new Date().getTime();
		//		System.out.println("=> MyVariableResolverFactory.isResolveable:" + name + "," + m_module); 
		//		System.out.println("\tisResolveable:" + variables); 
		//		System.out.println("\tisResolveable:" + getProperClass(name)); 
		Class clazz = getProperClass(name);
		if (clazz != null) {
			String m = getEntityName(clazz);
			Long id = (Long) variables.get("id");
			if (id == null) {
				id = (Long) variables.get(m + ".id");
			}
			System.out.println("\tID:" + id);
			if (id != null) {
				try {
					Object o = m_pm.getObjectById(clazz, id);
					//					System.out.println("\to:" + o); 
					Object value = PropertyUtils.getProperty(o, name);
					//					System.out.println("\tisResolveable.value:" + value); 
					createVariable(name, value);
					long end = new Date().getTime();
					//					System.out.println("<= Dauer:" + (end - start)); 
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return false;
	}

	private String getEntityName(Class clazz) {
		Iterator<String> it = m_module.keySet().iterator();
		while (it.hasNext()) {
			String mod = it.next();
			Class c = m_module.get(mod);
			if (clazz.equals(c)) {
				return mod;
			}
		}
		return null;
	}

	private Class getProperClass(String var) {
		Iterator<String> it = m_module.keySet().iterator();
		while (it.hasNext()) {
			String mod = it.next();
			Class clazz = m_module.get(mod);
			if (hasProperty(clazz, var)) {
				return clazz;
			}
		}
		return null;
	}

	private boolean hasProperty(Class clazz, String prop) {
		try {
			return clazz.getDeclaredField(prop) != null;
		} catch (Exception e) {
			return false;
		}
	}

	public void clear() {
		variableResolvers.clear();
		variables.clear();
	}
}
