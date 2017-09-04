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

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.jdo.Extent;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.transaction.UserTransaction;
import org.apache.commons.beanutils.PropertyUtils;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.permission.api.PermissionException;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.libhelper.Bean2Map;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.utils.UtilsService;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import static java.text.MessageFormat.format;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/** BaseSettingService implementation
 */
@SuppressWarnings("unchecked")
public class BaseSettingServiceImpl implements Constants {

	protected Inflector m_inflector = Inflector.getInstance();

	private Bean2Map m_b2m = new Bean2Map();
	protected boolean m_isRuntimeSystem;

	protected PermissionService m_permissionService;
	protected UtilsService m_utilsService;
	protected EntityService m_entityService;
	protected EventAdmin m_eventAdmin;

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();
	protected MetaData m_gitMetaData;

	protected Map<String, Boolean> m_initMap = new HashMap();

	public BaseSettingServiceImpl() {
	}

	/* P U B L I C - A P I  */
	public Map getFieldSets(String settingsid, String namespace, String entityName) throws Exception {
		debug(this,"getFieldSets:" + settingsid + "/" + entityName + "/" + namespace);
		List<Map> res = getFieldsetsForEntity(namespace, settingsid, entityName);
		debug(this,"\t:" + res);
		return _listToMap(res, "fsname");
	}

	public List getFieldsForEntityView(String namespace, String entity, String view) throws Exception {
		return getFieldsForEntityView(namespace, GLOBAL_SETTINGS, entity, view);
	}

	public List getFieldsForEntityView(String namespace, String settingsid, String entity, String view) throws Exception {
		return getFieldsForEntityView(namespace, settingsid, entity, view, null, null, null);
	}

	public List<Map> getFieldsForEntityView(String namespace, String settingsid, String entity, String view, Map mapping, String filter, String sortField) throws Exception {
		return m_gitMetaData.getFieldsForEntityView(namespace,settingsid,entity,view,mapping,filter,sortField);
	}

	public List getFieldsetsForEntity(String namespace, String entity) throws Exception {
		return getFieldsetsForEntity(namespace, GLOBAL_SETTINGS, entity);
	}

	public List getFieldsetsForEntity(String namespace, String settingsid, String entity) throws Exception {
		return m_gitMetaData.getFieldsetsForEntity(namespace,settingsid,entity);
	}

	public void setResourceSetting(String namespace, String settingsid, String resourceid, Map settings) throws Exception {
		m_gitMetaData.setResourceSetting(namespace,settingsid,resourceid,settings,true);
	}

	public Map getResourceSetting(String namespace, String settingsid, String resourceid) throws Exception {
		return m_gitMetaData.getResourceSetting(namespace,settingsid,resourceid);
	}
	public List<String> getResourceSettingNames(String namespace, String settingsid, String resourcePrefix) throws Exception{
		return m_gitMetaData.getResourceSettingNames(namespace,settingsid,resourcePrefix);
	}

	public void deleteResourceSetting(String namespace, String settingsid, String resourceid) throws Exception {
		m_gitMetaData.deleteResourceSetting(namespace,settingsid,resourceid);
	}


	private String getFilter(String settingsid, String resourceid) {
		String filter = SETTINGS_ENTITY + "." + SETTINGS_ID + " == '" + settingsid + "' && " + RESOURCE_ID + " == '" + resourceid + "'";
		return filter;
	}

	private String getContainerFilter(String settingsid) {
		String filter = SETTINGS_ID + " == '" + settingsid + "'";
		return filter;
	}

	private Object resourceLookup(String resourceid, Collection c) throws Exception {
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			String rid = (String) PropertyUtils.getProperty(o, RESOURCE_ID);
			if (resourceid.equals(rid)) {
				return o;
			}
		}
		return null;
	}

	protected List<Map> _mergeProperties(List<Map> l, Map<String, Map> mapMerge) {
		List<Map> resList = new ArrayList<Map>();
		for (Map<String, Object> m : l) {
			Map<String, Object> merge = mapMerge.get(m.get("name"));
			if (merge == null) {
				continue;
			}
			merge.putAll(m);
			resList.add(merge);
		}
		return resList;
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
