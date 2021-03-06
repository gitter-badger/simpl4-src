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
package org.ms123.common.data.api;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Date;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.ms123.common.data.api.LuceneSession;
import javax.transaction.UserTransaction;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.setting.api.SettingService;
import org.ms123.common.git.GitService;
import org.ms123.common.team.api.TeamService;

public interface SessionContext {

	public static String GET_OBJECT = "getObject";
	public static String GET_ID = "getID";
	public static String CHECK_PARAMS = "checkParams";
	public void setUserProperties(Map data);

	public Map getUserProperties();

	public void setDataLayer(DataLayer data);

	public DataLayer getDataLayer();

	public StoreDesc getStoreDesc();

	public void setStoreDesc(StoreDesc data);

	public void setEntityService(EntityService data);

	public void setSettingService(SettingService data);


	public void setGitService(GitService data);

	public void setPermissionService(PermissionService data);
	public PermissionService getPermissionService();

	public void setTeamService(TeamService data);

	public TeamService getTeamService();

	public EntityService getEntityService();

	public SettingService getSettingService();

	public GitService getGitService();

	public void setNucleusService(NucleusService data);

	public NucleusService getNucleusService();

	public UserTransaction getUserTransaction();

	public ClassLoader getClassLoader();

	public ClassLoader getClassLoader(StoreDesc sdesc);

	public Class getClass(StoreDesc sdesc, String className);

	public Class getClass(String className);

	public Object createObject(String entityName);

	public List validateObject(Object objectInsert);

	public List validateObject(Object objectInsert, String entityName);

	public void insertIntoMaster(Object objectInsert, String entityName, Class masterClazz, String fieldName, Object masterId) throws Exception;

	public void makePersistent(Object objectInsert);

	public void populate(Map from, Object to);

	public void populate(Map from, Object to, Map hintsMap);

	public void evaluteFormulas(String entityName, Map<String, Object> mapInsert);

	public void deleteObject(String entityName, Object id) throws Exception;

	public Object getObjectById(Class clazz, Object id);

	public Object getObjectById(StoreDesc sdesc, Class clazz, Object id);
	public Object getObjectByAttr(Class clazz, String attr, Object value);

	public Object getObjectByFilter(Class clazz, String filter);

	public List getObjectsByFilter(Class clazz, String filter);

	public Map deleteObjectById(String entityName, String id) throws Exception;

	public Map getObjectMapById(String entityName, String id);
	public Map insertObjectMap(Map data, String entityName) throws Exception;
	public Map updateObjectMap(Map data, String entityName, String id) throws Exception;

	public void retrieve(Object o);

	public void handleFinally();

	public void handleFinally(UserTransaction ut);

	public void handleException(Throwable e);

	public void handleException(UserTransaction ut, Throwable e);

	public Map executeNamedFilter(String name);

	public Map getNamedFilter(String name);

	public Object getObjectIdByNamedFilter(String name, Map<String, Object> fparams);
	public List<Object> getObjectIdsByNamedFilter(String name, Map<String, Object> fparams);

	public Object getObjectByNamedFilter(String name, Map<String, Object> fparams);
	public List<Object> getObjectsByNamedFilter(String name, Map<String, Object> fparams);

	public Map executeNamedFilter(String name, Map<String, Object> fparams);
	public Map executeNamedFilter(String name, Map<String, Object> fparams,Map<String, Object> options);

	public Map executeFilter(Map filterDesc, Map<String, Object> fparams);
	public Map executeFilter(Map filterDesc, Map<String, Object> fparams,Map<String, Object> options);

	public List query(String entityName, Map filtersMap);

	public List<Object> query(String entityName, String filter);
	public Map getEntitytype(String name);

	public boolean hasTeamPermission(Object o);

	public Map getPermittedFields(String entityName);
	public Map getPermittedFields(String entityName,String actions);

	public boolean isFieldPermitted(String fieldName, String entityName);
	public boolean isFieldPermitted(String fieldName, String entityName,String actions);

	public PersistenceManager getPM();

	public PersistenceManager getPM(StoreDesc sdesc);

	public LuceneSession getLuceneSession();

	public void setLuceneSession(LuceneSession data);

	public String getUserName();

	public boolean hasAdminRole();

	public String getConfigName();

	public void setConfigName(String data);

	public List<Map> getPrimaryKeyFields(String entity);

	public List<Object> persistObjects(Object objs, Map<String,Object> persistenceSpecification);

	public void setProperty(String key, Object value);
	public Object getProperty(String key);
}
