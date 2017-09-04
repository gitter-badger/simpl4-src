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
package org.ms123.common.entity;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import flexjson.*;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.ParameterParser;
import org.ms123.common.git.GitService;
import org.apache.commons.beanutils.PropertyUtils;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Extent;
import javax.jdo.Query;
import javax.jdo.JDOHelper;
import javax.jdo.Transaction;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.libhelper.Bean2Map;
import static java.text.MessageFormat.format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("unchecked")
class GitMetaDataImpl implements MetaData {

	protected Inflector m_inflector = Inflector.getInstance();

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();
	private GitService m_gitService;

	/**
	 */
	public GitMetaDataImpl(GitService gs) {
		m_gitService = gs;
		m_js.prettyPrint(true);
	}

	public List<Map> getEntitytypes(String storeId) throws Exception {
		StoreDesc sdesc = StoreDesc.get(storeId);
		String namespace = sdesc.getNamespace();
		String pack = sdesc.getPack();
		List<String> types = new ArrayList();
		types.add( ENTITYTYPE_TYPE );
		Map map= m_gitService.getWorkingTree(namespace, format(ENTITYTYPES_PATH,pack), 100, types, null, null,null);
		List<Map> childList = (List)map.get("children");
		map.remove("children");
		for( Map child : childList){
			String name = (String)child.get("name");
			String content= m_gitService.getContent(namespace, format(ENTITYTYPE_PATH,pack,name));
			Map m = (Map)m_ds.deserialize(content);
			m.remove("fields");
			m.remove("children");
			child.remove("children");
			child.putAll(m);
		}
		return childList;
	}
	public List<Map> getEntitytypeInfo(String storeId,List<String> names) throws Exception {
		List<Map> retList = new ArrayList();
		for( String name : names){
			Map<String,Object> et = getEntitytype(storeId, name );
			retList.add(et);
		}
		return retList;
	}

	public List<Map> getFields(String storeId,String entitytype) throws Exception {
		Map<String,Object> et = getEntitytype(storeId, entitytype );
		Map<String,Map> fields = (Map)et.get("fields");
		Map ret = null;
		if( fields != null){
				Set<String> keys = fields.keySet();
				boolean hasPrimary = false;
				for( String key : keys){
					Map<String,Map> field = fields.get(key);
					hasPrimary = getBoolean(field, "primary_key", false);
					if("id".equals(key) || hasPrimary){
						hasPrimary = true;
						break;
					}
				}
				if(!hasPrimary){
					Map idField = new HashMap();
					idField.put("name", "id");
					idField.put("id", "id");
					idField.put("readonly", true);
					idField.put("datatype", "string");
					idField.put("enabled", true);
					idField.put("search_options",  "['cn','bw', 'eq', 'ne']");
					fields.put("id", idField);
				}
				ret = fields;
		}else{
			 ret = new HashMap();
		}
		return mapToList(ret);
	}

	public List<Map> getRelations(String storeId) throws Exception {
		StoreDesc sdesc = StoreDesc.get(storeId);
		String namespace = sdesc.getNamespace();
		String pack = sdesc.getPack();
		String ret = null;
		try{
			ret = m_gitService.getContent(namespace, format(RELATIONS_PATH,pack));
		}catch(Exception e){
			return new ArrayList<Map>();
		}
		return (List)m_ds.deserialize(ret );
	}
	public void saveRelations(String storeId, List<Map> relations) throws Exception{
		StoreDesc sdesc = StoreDesc.get(storeId);
		String namespace = sdesc.getNamespace();
		String pack = sdesc.getPack();
		m_gitService.putContent(namespace, format(RELATIONS_PATH,pack), RELATIONS_TYPE, m_js.deepSerialize(relations));
	}
	public void saveEntitytype(String storeId, String name, Map<String,Object> desc) throws Exception{
		Map<String,Object> fields = (Map)desc.get("fields");
		if( fields == null ){
			try{
				Map<String,Object> et = getEntitytype(storeId, name );
				fields = (Map)et.get("fields");
			}catch(Exception e){
				fields = new HashMap();
			}
			desc.put("fields", fields);
		}

		List pkList = new ArrayList();
		for (Map.Entry<String,Object> entry : fields.entrySet()) {
			Map<String,Object> field = (Map)entry.getValue();
			Object pk = field.get("primary_key");
			if( pk != null && Boolean.valueOf(pk.toString()) == true ){
				pkList.add( (String)field.get("name"));
			}
		}
		desc.put("primaryKeys", pkList);
		StoreDesc sdesc = StoreDesc.get(storeId);
		String namespace = sdesc.getNamespace();
		String pack = sdesc.getPack();
		m_gitService.putContent(namespace, format(ENTITYTYPE_PATH,pack,name), ENTITYTYPE_TYPE, m_js.deepSerialize(desc));
	}

	public Map<String,Object>  getEntitytype(String storeId, String name) throws Exception{
		StoreDesc sdesc = StoreDesc.get(storeId);
		debug("getEntitytype:"+sdesc+"/"+name);
		String pack = StoreDesc.getPackName(name,sdesc.getPack());
		name = StoreDesc.getSimpleEntityName(name);
		String namespace = sdesc.getNamespace();
		//String pack = sdesc.getPack();
		String ret = null;
		String path = null;
		try{
			ret = m_gitService.getContent(namespace, path=format(ENTITYTYPE_PATH,pack,name));
		}catch(Exception e){
			debug("getEntityType:not in "+path);
			boolean isMeta = false;
			if( sdesc.isAidPack()){
				isMeta = true;
			}
			ret = _getEntityTypeGlobal( name,isMeta );
			if( ret == null){
				ret = _getEntityTypeGlobal( name,!isMeta );
			}
		}
		if( ret == null){
			throw new RuntimeException("GitMetaDataImpl.getEntitytype("+name+") not found");
		}
		return (Map)m_ds.deserialize(ret );
	}
	
	private String _getEntityTypeGlobal( String name,  boolean isMeta ){
		debug("getEntitytype.inGlobal"+(isMeta ? "Meta" : "Data")+":"+name);
		StoreDesc sdesc = isMeta ? StoreDesc.getGlobalMeta() : StoreDesc.getGlobalData();
		String ns = sdesc.getNamespace();
		String pa = sdesc.getPack();
		String path = null;
		try{
			return m_gitService.getContent(ns, path = format(ENTITYTYPE_PATH,pa,name));
		}catch(Exception e){
			debug("_getEntityTypeGlobal:not in "+path);
			return null;
		}
	}

	public void deleteEntitytype(String storeId, String name) throws Exception{
		StoreDesc sdesc = StoreDesc.get(storeId);
		String namespace = sdesc.getNamespace();
		String pack = sdesc.getPack();
		m_gitService.deleteObject(namespace, format(ENTITYTYPE_PATH,pack,name));
	}

	public void deleteEntitytypeField(String storeId, String entitytype, String name) throws Exception{
		Map<String,Object> et = getEntitytype(storeId, entitytype );
		Map<String,Object> fields = (Map)et.get("fields");
		if( fields == null){
			 fields = new HashMap();
			et.put("fields", fields);
		}else{
			fields.remove(name);
		}
		saveEntitytype( storeId, entitytype,et);
	}

	public void deleteEntitytypes(String storeId) throws Exception{
		StoreDesc sdesc = StoreDesc.get(storeId);
		String namespace = sdesc.getNamespace();
		String pack = sdesc.getPack();
		m_gitService.deleteObject(namespace, format(ENTITYTYPES_PATH,pack));
	}

	public void saveEntitytypeField(String storeId, String entitytype, String name, Map<String,Object> data) throws Exception{
		Map<String,Object> et = getEntitytype(storeId, entitytype );
		Map<String,Object> fields = (Map)et.get("fields");
		if( fields == null){
			 fields = new HashMap();
			et.put("fields", fields);
		}
		fields.put(name,data);
		saveEntitytype( storeId, entitytype,et);
	}
	public Map<String,Object> getEntitytypeField(String storeId, String entitytype, String name) throws Exception{
		Map<String,Object> et = getEntitytype(storeId, entitytype );
		Map<String,Object> fields = (Map)et.get("fields");
		Map ret = null;
		if( fields != null){
			 ret = (Map)fields.get(name);
		}
		if( ret != null) return ret;
		return new HashMap();
	}
	private List<Map> mapToList(Map<String, Map> map) {
		List<Map> retList = new ArrayList();
		Iterator<Map> it = map.values().iterator();
		while (it.hasNext()) {
			retList.add(it.next());
		}
		return retList;
	}
	public static boolean getBoolean(Map m, String key, boolean def) {
		try {
			Object val = m.get(key);
			if (val == null){
				return def;
			}
			if (val instanceof String) {
				if ("true".equals(val)){
					return true;
				}
				if ("false".equals(val)){
					return false;
				}
			}
			return (Boolean) val;
		} catch (Exception e) {
		}
		return def;
	}
	protected void debug(String msg) {
		//System.out.println(msg);
		m_logger.debug(msg);
	}
	private static final org.slf4j.Logger m_logger = org.slf4j.LoggerFactory.getLogger(GitMetaDataImpl.class);
}
