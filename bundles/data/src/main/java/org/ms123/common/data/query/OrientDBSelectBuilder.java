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
package org.ms123.common.data.query;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.beanutils.*;
import java.lang.reflect.*;
import java.lang.annotation.*;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.store.StoreDesc;
import static org.apache.commons.lang.StringUtils.countMatches;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings({"unchecked","deprecation"})
public class OrientDBSelectBuilder extends JPASelectBuilder implements SelectBuilder {

	public OrientDBSelectBuilder(QueryBuilder qb, StoreDesc sdesc, String entityName, Map filters, Map fieldSets) {
		super(qb, sdesc, entityName, null, filters, fieldSets);
	}
	protected String getCondition(Map<String, Object> rule) {
		Object o = rule.get("field");
		String fullfieldname = "";
		if (o instanceof String) {
			fullfieldname = (String) rule.get("field");
		}
		if (o instanceof Map) {
			fullfieldname = (String) ((Map) rule.get("field")).get("id");
		}
		String[] f = fullfieldname.split("\\.");
		String entityName = (f.length == 2) ? f[0] : m_entityName;
		String selector = entityName;
		debug(this,"getCondition:"+fullfieldname+"|"+selector);
		addSelector(entityName);
		entityName = m_queryBuilder.getEntityForPath(entityName);
		Map configMap = m_queryBuilder.getPermittedFields(StoreDesc.getFqEntityName(entityName,m_pack));
		String fieldname = (f.length == 2) ? f[1] : fullfieldname;
		info(this,"fullfieldname:"+fullfieldname);
		if( fullfieldname.indexOf("$") < 0 && countMatches(fullfieldname,".") == 1){
			fullfieldname = fullfieldname.replace(".","$");	
			info(this,"fullfieldname1:"+fullfieldname);
		}
		fieldname = fullfieldname.substring( fullfieldname.indexOf("$")+1 ).replace("$",".");;
		info(this,"fieldname2:"+fieldname);
		Map c = (Map) configMap.get(fieldname.substring(fieldname.lastIndexOf(".")+1));
		if (fieldname.equals("id")) {
			c = new HashMap();
			c.put("datatype", "string");
		}
		if (c == null) {
			debug(this,"configMap:"+configMap);
			throw new RuntimeException("Query:Field \"" + fieldname + "\" not found in " + m_sdesc + "/" + entityName);
		}
		Object data = rule.get("data");
		if (c.get("datatype").equals("date")) {
			data = getDate(data, rule);
		}
		String op = getOp(fieldname, (String) rule.get("op"), data, c);
		debug(this, "op:"+op);
		return "(" + op + ")";
	}
	protected String getOp(String field, String op, Object _data, Map<String, String> c) {
		String data = String.valueOf(_data);
		String dt = c.get("datatype");
		if (!dt.equals("array/string") && dt.startsWith("array/")) {
			field = field.replace('.', '$');
			addSelector(field);
		}
		System.out.println("field:" + field + ",op:" + op + ",data:" + data+",dt:"+dt);
		if ("gt".equals(op)) {
			return field + " > " + data;
		}
		if ("lt".equals(op)) {
			return field + " < " + data;
		}
		if ("eq".equals(op)) {
			return getEqual(field, data, dt);
		}
		if ("ceq".equals(op)) {
			return getCaseEqual(field, data, dt);
		}
		if ("ne".equals(op)) {
			return getNotEqual(field, data, dt);
		}
		if ("bw".equals(op)) {
			return getBegin(field, data, dt);
		}
		if ("cn".equals(op)) {
			return getContains(field, data, dt);
		}
		if ("in".equals(op)) {
			if( field.indexOf("_team_list.valid") != -1){
				return m_entityName+"$_team_list is not null and "+ field + " is null";
			}else{
				return field + " is null or "+field+" = ''";
			}
		}
		if ("inn".equals(op)) {
			if( field.indexOf("_team_list.valid") != -1){
				return m_entityName+"$_team_list is not null and "+ field + " is not null";
			}else{
				return field + "is not null";
			}
		}
		return "op not found";
	}

	private String getBegin(String f, String d, String dt) {
		d = d.toUpperCase();
		return f + ".toUpperCase() like '" + d + "%'";
	}

	private String getContains(String f, String d, String dt) {
		d = d.toUpperCase();
		if( "".equals(d)){
			return f +" is null or " + f + ".toUpperCase() like '%" + d + "%'";
		}else{
			return f + ".toUpperCase() like '%" + d + "%'";
		}
	}

	private String getRegexp(String f, String d, String dt) {
		d = d.toUpperCase();
		return "UPPER(" + f + ") regexp '.*" + d + ".*'";
	}

	private String getNotEqual(String f, String d, String dt) {
		d = d.toUpperCase();
		if (("7".equals(dt))) {
			return f + ".toUpperCase() <> '" + d + "'";
		} else if ("date".equals(dt)) {
			return f + " <> " + d;
		} else if ("boolean".equals(dt)) {
			return f + " <> " + d;
		} else {
			return f + " <> " + d;
		}
	}

	private String getCaseEqual(String f, String d, String dt) {
		if (("7".equals(dt))) {
			return f + " = '" + d + "'";
		} else if ("boolean".equals(dt)) {
			return f + " = " + d;
		} else if ("date".equals(dt)) {
			return f + " = " + d;
		} else if ("boolean".equals(dt)) {
			return f + " = " + d;
		} else {
			return f + " = " + d;
		}
	}

	private String getEqual(String f, String d, String dt) {
		info(this,"\tgetEqual:" + f + "," + d + "," + dt);
		if (("7".equals(dt))) {
			d = d.toUpperCase();
			if( "".equals(d)){
				return f +" is null or " + f + ".toUpperCase() = ''";
			}else{
				return f + ".toUpperCase() = '" + d + "'";
			}
		} else if ("date".equals(dt)) {
			return f + " = " + d;
		} else if ("boolean".equals(dt)) {
			return f + " = " + d;
		} else {
			d = d.toUpperCase();
			return f + " = " + d;
		}
	}
}
