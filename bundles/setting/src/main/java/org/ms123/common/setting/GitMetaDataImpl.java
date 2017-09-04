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
package org.ms123.common.setting;

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
import org.ms123.common.libhelper.Inflector;
import static java.text.MessageFormat.format;
import org.osgi.service.event.Event;

import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
/**
 */
@SuppressWarnings("unchecked")
class GitMetaDataImpl implements MetaData, Constants {

	protected Inflector m_inflector = Inflector.getInstance();

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();

	private GitService m_gitService;

	private SettingServiceImpl m_ssi;

	private String USERSETTINGS_NAMESPACE = "local";

	/**
	 */
	public GitMetaDataImpl(GitService gs, SettingServiceImpl ssi) {
		m_gitService = gs;
		m_ssi = ssi;
		m_js.prettyPrint(true);
	}

	public Map getResourceSetting(String namespace, String settingsid, String resourceid) throws Exception {
		try {
			boolean isRuntime = m_ssi.m_isRuntimeSystem;		
			String ret = null;
			String path = format(SETTING_PATH, resourceid);
			if( isRuntime){
				if( m_gitService.exists( getRepo(namespace,true), path)){
					ret = m_gitService.getContent(getRepo(namespace,true), path);
				}else{
					ret = m_gitService.getContent(getRepo(namespace,false), path);
				}
			}else{
				ret = m_gitService.getContent(getRepo(namespace), path);
			}
			return (Map) m_ds.deserialize(ret);
		} catch (Exception e) {
			return null;
		}
	}

	public List<Map> getResourceSettings(String settingsid, String resourceid) throws Exception {
		List<Map> repoList = m_gitService.getRepositories(new ArrayList(), false);
		List<Map> retList = new ArrayList<Map>();
		for( Map<String,String> repo : repoList){
			String name = repo.get("name");
			Map res = getResourceSetting( name, settingsid, resourceid );
			if( res != null){
				retList.add( res);
			}
		}
		return retList;
	}

	public List<String> getResourceSettingNames(String namespace, String settingsid, String resourcePrefix) throws Exception {
		List<String> assets = m_gitService.assetList(getRepo(namespace), null, SETTING_TYPE,false);
		List<String> retList = new ArrayList<String>();
		for( String asset : assets){
			int startPoint = asset.lastIndexOf("/");
			if( asset.substring(startPoint+1).startsWith(resourcePrefix)){
				retList.add( asset);
			}
		}
		return retList;
	}

	public void setResourceSetting(String namespace, String settingsid, String resourceid, Map settings, boolean overwrite) throws Exception {
		if (!overwrite && m_gitService.exists(getRepo(namespace), format(SETTING_PATH, resourceid))) {
			debug(this,"setResourceSetting:" + format(SETTING_PATH, resourceid) + "exists");
			return;
		}
		m_gitService.putContent(getRepo(namespace), format(SETTING_PATH, resourceid), SETTING_TYPE, m_js.deepSerialize(settings));
		sendEvent( "setResource", namespace, settingsid, resourceid);
	}

	public void deleteResourceSetting(String namespace, String settingsid, String resourceid) throws Exception {
		debug(this,"deleteResourceSetting:"+getRepo(namespace)+","+settingsid+","+resourceid);
		m_gitService.deleteObjects(getRepo(namespace), SETTINGS_PATH, resourceid);
		sendEvent( "deleteResource", namespace, settingsid, resourceid);
	}

	public Map getFieldSets(String settingsid, String namespace, String entityName) throws Exception {
		debug(this,"getFieldSets:" + settingsid + "/" + entityName + "/" + namespace);
		List<Map> res = getFieldsetsForEntity(namespace, settingsid, entityName);
		debug(this,"\t:" + res);
		return _listToMap(res, "fsname");
	}

	public List<Map> getFieldsetsForEntity(String namespace, String settingsid, String entity) throws Exception {
		String resourceid = format("settings/entities.{0}.fieldsets", entity);
		String settingStr = getResource(namespace, settingsid, resourceid);
		if (settingStr != null) {
			Map m = (Map) m_ds.deserialize(settingStr);
			debug(this,"m:" + m);
			return filterFieldSetFields((List) m.get(FIELDSETS),namespace, entity);
		} else {
			return new ArrayList();
		}
	}

	public List<Map> getFieldsForEntityView(String namespace, String settingsid, String entity, String view) throws Exception {
		return getFieldsForEntityView(namespace, settingsid, entity, view, null, null, null);
	}

	public List<Map> getFieldsForEntityView(String namespace, String settingsid, String entity, String view, Map mapping, String filter, String sortField) throws Exception {
		List<Map> retList = new ArrayList();
		String resourceid = format("settings/entities.{0}.views.{1}.fields", entity, view);
		String settingStr = getResource(namespace, settingsid, resourceid);
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace,StoreDesc.getPackName(entity));
		if (settingStr != null) {
			Map m = (Map) m_ds.deserialize(settingStr);
			debug(this,"m:" + m);
			Map<String, Map> allFields = _listToMap(m_ssi.m_entityService.getFields(sdesc, StoreDesc.getSimpleEntityName(entity), true), "name");
			retList = _mergeProperties((List) m.get(FIELDS), allFields);
			printList("\tAftermerge:", retList);
		} else {
			if (!"duplicate-check".equals(view)) {
				retList = m_ssi.m_entityService.getFields(sdesc, StoreDesc.getSimpleEntityName(entity), false);
			}
		}
		retList = m_ssi.m_permissionService.permissionFieldListFilter(sdesc, StoreDesc.getSimpleEntityName(entity), retList, "name", "read");
		SessionContext sc = m_ssi.getDatalayer(sdesc).getSessionContext(sdesc);
		for (Map m : retList) {
			boolean rd = getBoolean(m,"readonly",false);
			boolean readonly = !sc.isFieldPermitted((String) m.get("name"), StoreDesc.getSimpleEntityName(entity), "write");
			if( rd) readonly = true;
			debug(this,"\tisReadOnly(" + m.get("name") + "," + entity + ")" + readonly);
			m.put("readonly", readonly);
		}
		if (sortField != null) {
			m_ssi.m_utilsService.sortListByField(retList, sortField);
		}
		if (mapping != null || filter != null) {
			retList = m_ssi.m_utilsService.listToList(retList, mapping, filter);
		}
		return retList;
	}

	public Map getPropertiesForEntityView(String namespace, String settingsid, String entity, String view) throws Exception {
		String resourceid = format("settings/entities.{0}.views.{1}.properties", entity, view);
		String settingStr = getResource(namespace, settingsid, resourceid);
		if (settingStr != null) {
			Map m = (Map) m_ds.deserialize(settingStr);
			return m;
		} else {
			return getPropertiesForEntity(namespace, settingsid, entity);
		}
	}

	public Map getPropertiesForEntity(String namespace, String settingsid, String entity) throws Exception {
		String resourceid = format("settings/entities.{0}.properties", entity);
		String settingStr = getResource(namespace, settingsid, resourceid);
		if (settingStr != null) {
			Map m = (Map) m_ds.deserialize(settingStr);
			return m;
		} else {
			return new HashMap();
		}
	}

	private List<Map> filterFieldSetFields(List<Map> fsList, String namespace,String entity){
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace);
		SessionContext sc = m_ssi.getDatalayer(sdesc).getSessionContext(sdesc);
		for(Map fs : fsList){
			List<String> fList = (List<String>)fs.get(FIELDS);
			List<String> nList = new ArrayList();
			fs.put(FIELDS,nList);
			for( String f : fList ){
				if(sc.isFieldPermitted(f, entity, "read")){
					nList.add(f);
				}
			}
		}
		return fsList;
	}

	private String getResource(String namespace, String settingsid, String resourceid) {
		debug(this,"GetResource:" + resourceid+"/"+namespace+"/"+settingsid);
		String ret = null;
		try {
			StoreDesc sdesc = StoreDesc.getNamespaceData(namespace);
			String repo = sdesc.getRepository();
			if( repo == null) repo = sdesc.getNamespace();
			ret = m_gitService.getContent(repo, resourceid);
		} catch (Exception e) {
			try {
				StoreDesc sdesc = StoreDesc.getNamespaceMeta(namespace);
				String repo = sdesc.getRepository();
				if( repo == null) repo = sdesc.getNamespace();
				ret = m_gitService.getContent(repo, resourceid);
			} catch (Exception e1) {
				try {
					ret = m_gitService.getContent("global", resourceid);
				} catch (Exception e2) {}
			}
		}
		debug(this,"GetResource.ret:" + ret);
		return ret;
	}

	private String getRepo(String namespace, boolean dataRepo){
		StoreDesc sdesc = dataRepo ? StoreDesc.getNamespaceData(namespace) : StoreDesc.getNamespaceMeta(namespace);
		String repo = sdesc.getRepository();
		if( repo == null){
			repo = sdesc.getNamespace();
		}
		return repo;
	}

	private String getRepo(String namespace){
		boolean isRuntime = m_ssi.m_isRuntimeSystem;		
		StoreDesc sdesc = isRuntime ? StoreDesc.getNamespaceData(namespace) : StoreDesc.getNamespaceMeta(namespace);
		String repo = sdesc.getRepository();
		if( repo == null){
			repo = sdesc.getNamespace();
		}
		return repo;
	}

	private void printList(String header, List list) {
		debug(this,"----->" + header);
		if (list != null) {
			String komma = "";
			debug(this,"\t");
			Iterator it = list.iterator();
			while (it.hasNext()) {
				Map obj = (Map) it.next();
				debug(this,komma + obj.get("name"));
				komma = ", ";
			}
		}
		debug(this,"");
	}

	protected List<Map> _mergeProperties(List<Map> l, Map<String, Map> mapMerge) {
		List<Map> resList = new ArrayList<Map>();
		for (Map<String, Object> m : l) {
			if (getBoolean(m, "enabled", true) == false) {
				debug(this,"Not enabled:" + m.get("name"));
				continue;
			}
			Map<String, Object> merge = mapMerge.get(m.get("name"));
			if (merge == null) {
				continue;
			}
			merge.putAll(m);
			resList.add(merge);
		}
		return resList;
	}

	protected boolean getBoolean(Map m, String key, boolean def) {
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

	private void sendEvent(String topic, String namespace, String settingsid, String resourceid) {
		Map props = new HashMap();
		props.put("namespace", namespace);
		props.put("settingsid", settingsid);
		props.put("resourceid", resourceid);
		info(this, "SettingService.sendEvent("+topic+"):"+namespace+"|"+settingsid+"|"+resourceid);
		m_ssi.getEventAdmin().postEvent(new Event("setting/" + topic, props));
	}
	protected Map<String, Map> _listToMap(List<Map> list, String key) {
		Map<String, Map> retMap = new HashMap();
		Iterator it = list.iterator();
		for (Map m : list) {
			String k = (String) m.get(key);
			retMap.put(k, m);
		}
		return retMap;
	}
}
