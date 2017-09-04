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
package org.ms123.common.camel.jsonconverter;

import flexjson.*;
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
import static org.ms123.common.camel.api.CamelService.PROPERTIES;
import static org.ms123.common.camel.api.CamelService.RPC;
import static org.ms123.common.camel.api.CamelService.PROCEDURENAME;
import static org.ms123.common.camel.api.CamelService.OVERRIDEID;

/**
 *
 */
@SuppressWarnings("unchecked")
public class BaseRouteJsonConverter {

	protected JSONDeserializer m_ds = new JSONDeserializer();
	protected JSONSerializer m_js = new JSONSerializer();

	BaseRouteJsonConverter() {
	}


	protected String getStencilId(Map shape) {
		Map stencil = (Map) shape.get("stencil");
		String id = ((String) stencil.get("id")).toLowerCase();
		return id;
	}

	protected boolean isOnException(Map shape) {
		Map stencil = (Map) shape.get("stencil");
		String id = ((String) stencil.get("id")).toLowerCase();
		return id.equals("onexception");
	}
	protected boolean isOnCompletion(Map shape) {
		Map stencil = (Map) shape.get("stencil");
		String id = ((String) stencil.get("id")).toLowerCase();
		return id.equals("oncompletion");
	}
	protected boolean isFrom(Map shape) {
		return !(isOnException(shape)  || isOnCompletion(shape));
	}

	protected void sortStartShapeList(List<Map> shapes) {
		if( isFrom(shapes.get(0))) return;
		int i=0;
		for( Map shape : shapes){
			if( isFrom(shape)){
				Map temp = shapes.get(0);
				shapes.set(0, shapes.get(i));
				shapes.set(i,temp);
				return;
			}
			i++;
		}
	}

	protected boolean isStartShapeListOk(List<Map> shapes) {
		int cFrom=0;
		int cOnException =0;
		int cOnCompletion =0;
		for(Map shape : shapes){
			if( isFrom(shape)) cFrom++;
			if( isOnException(shape)) cOnException++;
			if( isOnCompletion(shape)) cOnCompletion++;
		}
		return cFrom > 0 && cOnException<2 && cOnCompletion<2;
	}


	protected String getSharedOriginRef(Map shape) {
		Map<String,String> properties = (Map) shape.get(PROPERTIES);
		if( "origin".equals(properties.get("shared"))){
			return properties.get("shareRef");
		}
		return null;
	}

	protected boolean getRpcFlag(Map shape) {
		Map<String,Boolean> properties = (Map) shape.get(PROPERTIES);
		try{
			Boolean rpc = properties.get(RPC);
			return rpc != null && rpc == true;
		}catch(Exception e){
			System.out.println("e:"+e);
			return false;
		}
	}

	protected String getProcedureName(Map shape) {
		Map<String,String> properties = (Map) shape.get(PROPERTIES);
		return properties.get(PROCEDURENAME);
	}

	protected List<Map> getShapeList(List<Map> list, List<String> types) {
		List<Map> retList = new ArrayList();
		for (Map<String, Map> e : list) {
			String sid = getStencilId(e);
			if (types.contains(sid)) {
				retList.add(e);
			}
		}
		return retList;
	}

	protected List<Map> getShapeList(List<Map> list, String type) {
		List<Map> retList = new ArrayList();
		for (Map<String, Map> e : list) {
			String sid = getStencilId(e);
			if (type.toLowerCase().equals(sid)) {
				retList.add(e);
			}
		}
		return retList;
	}

	protected Map getShapeSingle(List<Map> list, String type) {
		for (Map<String, Map> e : list) {
			String sid = getStencilId(e);
			if (type.toLowerCase().equals(sid)) {
				return e;
			}
		}
		return null;
	}

	protected boolean getBoolean(Map properties, String key, boolean def) {
		try {
			if (properties.get(key) == null)
				return def;
			return (Boolean) properties.get(key);
		} catch (Exception e) {
			return def;
		}
	}

	protected int getInteger(Map properties, String key, int def) {
		try {
			if (properties.get(key) == null)
				return def;
			return ((Integer) properties.get(key)).intValue();
		} catch (Exception e) {
			return def;
		}
	}

	protected String getString(Map properties, String key, String def) {
		try {
			String val = (String) properties.get(key);
			if (val == null || "".equals(val.trim())) {
				return def;
			}
			return val;
		} catch (Exception e) {
			return def;
		}
	}

	protected boolean isEmpty(String str) {
		if (str == null || str.length() == 0)
			return true;
		return false;
	}
}
