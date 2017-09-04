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
package org.ms123.common.workflow;


import java.beans.FeatureDescriptor;
import java.util.*;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 */
@SuppressWarnings("unchecked")
public class ImplicitContextElResolver extends ELResolver {
	private static final Logger m_logger = LoggerFactory.getLogger(ImplicitContextElResolver.class);
	protected static Map<String, Object> m_cache = new HashMap<String, Object>();

	public ImplicitContextElResolver() {
	}

	public Object getValue(ELContext context, Object base, Object property) {
		System.out.println("OsgiContextElResolver:+getValue:" + base + "/" + property);
		if (base == null) {
			String key = (String) property;
			if ("utils".equals(key)) {
				context.setPropertyResolved(true);
				return new Utils();
			}		
		}

		return null;
	}

	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return true;
	}

	public void setValue(ELContext context, Object base, Object property, Object value) {
		if (base == null) {}
	}

	public Class< ? > getCommonPropertyType(ELContext context, Object arg) {
		return Object.class;
	}

	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object arg) {
		return null;
	}

	public Class< ? > getType(ELContext context, Object arg1, Object arg2) {
		return Object.class;
	}

	static class Utils {
		public boolean contains(Object o1, Object o2) {
			return contains(o1, o2, "id");
		}

		public boolean contains(Object o1, Object o2, String key) {
			System.out.println("contains:" + o1 + "/" + o2 + "/" + key);
			if (o1 instanceof List && o2 instanceof List) {
				List l1 = (List) o1;
				List l2 = (List) o2;
			
				if (l1.size() == 0) {
					return true;
				}		
				if (l2.size() == 0) {
					return false;
				}		
				if (l1.get(0) instanceof Map) {
					for (Map m1 : (List<Map>) l1) {
						Object val1 = (Object) m1.get(key);
						boolean ok = false;
						for (Map m2 : (List<Map>) l2) {
							Object val2 = (Object) m2.get(key);
							if (val1.equals(val2)) {
								ok = true;
							}	
						}
						if (!ok) {
							System.out.println("l2:" + l2 + "contains not " + l1);
							return false;
						}
					}
				}
				if (l1.get(0) instanceof String) {
					for (String s1 : (List<String>) l1) {
						boolean ok = false;
						for (String s2 : (List<String>) l2) {
							if (s1.equals(s2)) {
								ok = true;
							}	
						}
						if (!ok) {
							System.out.println("l2:" + l2 + "contains not " + l1);
							return false;
						}
					}
				}
				System.out.println("l2:" + l2 + "contains " + l1);
				return true;
			}
			return false;
		}
	}
}
