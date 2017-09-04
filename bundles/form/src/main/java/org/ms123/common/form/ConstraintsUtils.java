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
package org.ms123.common.form;
import javax.validation.*;
import java.util.*;
import flexjson.*;
/**
 */
@SuppressWarnings("unchecked")
public class ConstraintsUtils {
	protected static String makeReadMethodName(String name, Class type) {
		return ((type == Boolean.class) ? "is" : "get") + makeUppercase(name);
	}

	protected static String makeWriteMethodName(String name) {
		return "set" + makeUppercase(name);
	}

	protected static String makeUppercase(String name) {
		if (name == null)
			return null;
		if (name.length() > 0 && !Character.isUpperCase(name.charAt(0))) {
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		}
		return name;
	}
	protected static Map getConsMetaMap(String annoName, List<Map> consMetaList) {
		for (Map val : consMetaList) {
			if (annoName.equals(val.get("clazz"))) {
				return val;
			}
		}
		return null;
	}

	protected static Map getConstraintsMeta(String annoName, Map shape, Map<String, List> constraintsMeta) {
		String stype = ConstraintsUtils.getStringType(shape);
		List<Map> consMetaList = constraintsMeta.get(stype);
		return ConstraintsUtils.getConsMetaMap(annoName, consMetaList);
	}
	protected static Class getType(Map element) {
		String stype = getStringType(element);
		if ("text".equals(stype))
			return String.class;
		if ("number".equals(stype))
			return Integer.class;
		if ("boolean".equals(stype))
			return Boolean.class;
		if ("decimal".equals(stype))
			return Double.class;
		if ("double".equals(stype))
			return Double.class;
		if ("date".equals(stype))
			return Date.class;
		if ("long".equals(stype))
			return Long.class;
		return String.class;
	}

	protected static String getTypeChar(String type) {
		type = type.toLowerCase();
		if( type.indexOf(",")!=-1){
			type = type.split(",")[1];
		}
		if ("double".equals(type))
			return "D";
		if ("string".equals(type))
			return "s";
		if ("sarray".equals(type))
			return "[s";
		if ("int".equals(type))
			return "I";
		if ("long".equals(type))
			return "J";
		return "s";
	}

	protected static String getStringType(Map element) {
		Map properties = (Map) element.get("properties");
		String xf_type = null;
		try {
			xf_type = ((String) properties.get("xf_type")).toLowerCase();
			if ("datetime".equals(xf_type)){
				return "date";
			}
			return xf_type;
		} catch (Exception e) {
			return "text";
		}
	}

	protected static  Map<String, List> getConstraintsData(Map shape) {
		Map properties = (Map) shape.get("properties");
		String xf_type = null;
		try {
			//xf_type = ((String) properties.get("xf_type")).toLowerCase();
			xf_type = getStringType(shape);	
			String constraint = (String) properties.get("xf_constraint_" + xf_type);
			if (constraint == null || constraint.trim().length() == 0) {
				return null;
			}
			JSONDeserializer ds = new JSONDeserializer();
			return (Map) ds.deserialize(constraint);
		} catch (Exception e) {
			return null;
		}
	}
}
