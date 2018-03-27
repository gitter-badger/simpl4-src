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
import java.util.List;
import org.ms123.common.store.StoreDesc;
import javax.servlet.http.*;

public interface DataLayer {
	public final String DATA_LAYER = "dataLayer";
	public Map insertObject(Map dataMap, StoreDesc sdesc, String entityName);

	public Map insertObject(Map dataMap, StoreDesc sdesc, String entityName, String entityNameParent, String idParent);

	public Map insertObject(Map dataMap, Map filterMap, Map hintsMap, StoreDesc sdesc, String entityName, String entityNameParent, String idParent);

	public Map insertObject(SessionContext sessionContext, Map dataMap, String entityName) throws Exception;

	public Map insertObject(Map dataMap, String namespace, String entityName) throws Exception;

	public Map insertObject(SessionContext sessionContext, Map dataMap, String entityName, String entityNameParent, String idParent) throws Exception;

	public Map insertObject(SessionContext sessionContext, Map dataMap, Map hintsMap, String entityName, String entityNameParent, String idParent) throws Exception;

	public Map insertObject(SessionContext sessionContext, Map dataMap, Map filterMap, Map hintsMap, String entityName, String entityNameParent, String idParent) throws Exception;

	public Map updateObject(Map dataMap, String namespace, String entityName, String id) throws Exception;

	public Map updateObject(Map dataMap, StoreDesc sdesc, String entityName, String id);

	public Map updateObject(Map dataMap, Map filterMap, Map hintsMap, StoreDesc sdesc, String entityName, String id, String entityNameParent, String parentId);

	public Map updateObject(SessionContext sessionContext, Map dataMap, String entityName, String id) throws Exception;

	public Map updateObject(SessionContext sessionContext, Map dataMap, Map hintsMap, String entityName, String id) throws Exception;

	public Map updateObject(SessionContext sessionContext, Map dataMap, Map filterMap, Map hintsMap, String entityName, String id, String entityNameParent, String idParent) throws Exception;

	public Map deleteObject(Map dataMap, StoreDesc sdesc, String entityName, String id);

	public Map deleteObject(SessionContext sessionContext, Map dataMap, String entityName, String id) throws Exception;

	public Map getObjectGraph(StoreDesc sdesc, String entityName, String id);

	public Map getObject(StoreDesc sdesc, String entityName, String id);

	public Map getObject(StoreDesc sdesc, String entityName, String id, List fields);

	public Map getObject(StoreDesc sdesc, String entityName, String id, String entityNameDetails, List fields);

	public Map getObject(StoreDesc sdesc, String entityName, String id, String entityNameDetails, List fields, HttpServletResponse response);

	public Map querySql(SessionContext sessionContext, StoreDesc sdesc, Map params, String sql);

	public Map query(Map params, StoreDesc sdesc, String entityName);

	public Map query(Map params, StoreDesc sdesc, String entityName, String idParent, String entityNameDetails);

	public Map query(SessionContext sessionContext, Map params, StoreDesc sdesc, String entityName);

	public Map query(SessionContext sessionContext, Map params, StoreDesc sdesc, String entityName, String idParent, String entityNameDetails);

	public SessionContext getSessionContext(StoreDesc sdesc);
	public SessionContext getSessionContext(String namespace);

	public void populate(SessionContext sessionContext, Map from, Object to, Map hintsMap);
	public void populate(Map from, Object to);

	public List validateObject(SessionContext sessionContext, Object objectInsert, String entityName, boolean bInsert);
	public List validateObject(SessionContext sessionContext, Object objectInsert, String entityName);

	public List validateObject(SessionContext sessionContext, Object objectInsert);

	public Object createObject(SessionContext sessionContext, String entityName);
	public Object createObject(String namespace, String entityName);

	public void insertIntoMaster(SessionContext sc, Object objectInsert, String entityName, Class masterClazz, String fieldName, Object masterId) throws Exception;

	public void insertIntoMaster(SessionContext sc, Object objectInsert, String entityName, Object objectMaster, String fieldName) throws Exception;

	public void makePersistent(Object objectInsert);
	public void makePersistent(String namespace, Object objectInsert);
	public void makePersistent(SessionContext sessionContext, Object objectInsert);

	public String constructEntityName(SessionContext sessionContext, String entityName, String entityNameParent);

	public Class getClass(SessionContext sessionContext, String entityName);
	public Class getClass(StoreDesc sdesc, String entityName);
	public ClassLoader getClassLoader(StoreDesc sdesc);

	public void evaluteFormulas(SessionContext sessionContext, String entityName, Map<String, Object> map, String direction);

	/************************************ C O N V I N I E N T *************************************************/
	public Object getObjectByFilter(String namespace, String entity, String filter);
	public List getObjectsByFilter(String namespace, String entity, String filter);
	public Object getObjectById(String namespace, String entity, Object id);
	public Object getObjectByNamedFilter(String namespace, String name, Map<String, Object> fparams);
	public List<Object> getObjectsByNamedFilter(String namespace, String name, Map<String, Object> fparams);

}
