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
package org.ms123.common.data;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.setting.api.SettingService;
import static org.ms123.common.setting.api.Constants.TITLEEXPRESSION;
import static org.ms123.common.setting.api.Constants.RECORDVALIDATION;
import static org.ms123.common.setting.api.Constants.NORESULTSETCOUNT;
import static org.ms123.common.setting.api.Constants.STATESELECT;
import static org.ms123.common.setting.api.Constants.SELECTDISTINCT;
import static org.ms123.common.setting.api.Constants.GLOBAL_SETTINGS;
import static org.ms123.common.entity.api.Constants.STATE_OK;
import static org.ms123.common.entity.api.Constants.STATE_NEW;
import static org.ms123.common.entity.api.Constants.STATE_FIELD;
import static org.ms123.common.entity.api.Constants.DISABLE_STATESELECT;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.utils.annotations.RelatedTo;
import org.ms123.common.data.query.QueryBuilder;
import org.ms123.common.data.query.OrientDBQueryBuilder;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.permission.api.PermissionException;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.git.GitService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.system.orientdb.OrientDBService;
import org.ms123.common.domainobjects.api.DomainObjectsService;
import org.ms123.common.rpc.CallService;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import java.lang.UnsupportedOperationException;
import javax.servlet.http.*;
import static org.joor.Reflect.*;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "kind=orientdb,name=dataLayer" })
public class OrientDBLayerImpl extends BaseOrientDBLayerImpl implements org.ms123.common.data.api.DataLayer {

	private EntityService entityService;

	private AuthService authService;

	private GitService gitService;

	private CallService callService;

	private OrientDBService orientdbService;

	private SettingService settingService;

	private CamelService camelService;

	private DomainObjectsService domainObjectsService;

	private PermissionService permissionService;

	private JSONSerializer js = new JSONSerializer();

	private JSONDeserializer ds = new JSONDeserializer();

	protected Inflector inflector = Inflector.getInstance();

	public void activate() {
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	//insert 
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	public Map insertObject(Map dataMap, StoreDesc sdesc, String entityName) {
		return insertObject(dataMap, null, null, sdesc, entityName, null, null);
	}

	public Map insertObject(Map dataMap, StoreDesc sdesc, String entityName, String entityNameParent, String idParent) {
		return insertObject(dataMap, null, null, sdesc, entityName, entityNameParent, idParent);
	}

	public Map insertObject(Map dataMap, Map filterMap, Map hintsMap, StoreDesc sdesc, String entityName, String entityNameParent, String idParent) {
		debug(this, "insertObject:" + dataMap + ",filterMap:" + filterMap + ",entity:" + entityName);
		Map retMap = new HashMap();
		SessionContext sessionContext = getSessionContext(sdesc);
		OrientGraph orientGraph = this.orientdbService.getOrientGraph(sdesc.getNamespace());
		try {
			orientGraph.begin();
			retMap = insertObject(sessionContext, dataMap, filterMap, hintsMap, entityName, entityNameParent, idParent);
			orientGraph.commit();
			String id = String.valueOf(retMap.get("id"));
			retMap.put("id", id);
		} catch (Throwable e) {
			error(this, "insertObject:%[exception]s", e);
			sessionContext.handleException(e);
		} finally {
			orientGraph.shutdown();
			sessionContext.handleFinally();
		}
		return retMap;
	}

	public Map insertObject(SessionContext sessionContext, Map dataMap, String entityName) throws Exception {
		return insertObject(sessionContext, dataMap, null, null, entityName, null, null);
	}

	public Map insertObject(SessionContext sessionContext, Map dataMap, String entityName, String entityNameParent, String idParent) throws Exception {
		return insertObject(sessionContext, dataMap, null, null, entityName, entityNameParent, idParent);
	}

	public Map insertObject(SessionContext sessionContext, Map dataMap, Map hintsMap, String entityName, String entityNameParent, String idParent) throws Exception {
		return insertObject(sessionContext, dataMap, null, hintsMap, entityName, entityNameParent, idParent);
	}

	public Map insertObject(SessionContext sessionContext, Map dataMap, Map filterMap, Map hintsMap, String entityName, String entityNameParent, String idParent) throws Exception {
		List constraintViolations = validateObject(sessionContext, dataMap, null, entityName, true);
		if (constraintViolations != null) {
			Map<String,Object> retMap = new HashMap();
			retMap.put("constraintViolations", constraintViolations);
			retMap.put("created", null);
			return retMap;
		}
		StoreDesc sdesc = sessionContext.getStoreDesc();
		if (entityNameParent != null) {
			entityName = constructEntityName(sessionContext, entityName, entityNameParent);
		}
		String user = sessionContext.getUserName();
		checkPermissions(sdesc, user, entityName, dataMap, "write");
		entityName = this.inflector.getEntityNameCamelCase(entityName);
		return executeInsertObject(sessionContext, entityName, dataMap);
	}

	public void makePersistent(Object objectInsert) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.makePersistent");
	}

	public void makePersistent(String namespace, Object objectInsert) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.makePersistent");
	}

	public void makePersistent(SessionContext sessionContext, Object objectInsert) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.makePersistent");
	}

	public List validateObject(SessionContext sessionContext, Object objectInsert) {
		return validateObject(sessionContext, objectInsert, null, true);
	}

	public List validateObject(SessionContext sessionContext, Object objectInsert, String entityName) {
		return validateObject(sessionContext, objectInsert, entityName, true);
	}

	public List validateObject(SessionContext sessionContext, Object objectInsert, String entityName, boolean bInsert) {
		return validateObject(sessionContext, objectInsert, null, entityName, bInsert);
	}

	public List validateObject(SessionContext sessionContext, Object objectInsert, Object objectUpdatePre, String entityName, boolean bInsert) {
//	Set cv = m_validator.validate(objectInsert);
		String recordValidation = getRecordValidation(sessionContext,entityName);
		if( recordValidation != null){
			Map<String,Object> params = new HashMap();
//		params.put("constraintViolationList", constructConstraitViolationList(cv));
			params.put("data", objectInsert);
			params.put("mode", bInsert ? "add" : "edit");
			info(this,"validateObject:"+params);
			String service = getFqServiceName( sessionContext.getStoreDesc().getNamespace(), recordValidation);
			Object answer = this.callService.callCamel( service, params);
			info(this,"answer:"+answer);
			if ( answer != null && ((Collection)answer).size() > 0) {
				return new ArrayList((Collection)answer);
			}else{
				return null;
			}
		}else{
//		if (cv.size() > 0) {
//			return constructConstraitViolationList(cv);
//		}
		}
		return null; 
	}

	private String getFqServiceName( String namespace, String service ){
		if( service.indexOf(".") == -1 &&  !"xyz".equals(namespace)){
			service = namespace + "."+ service;
		}
		return service;
	}

	private String getRecordValidation(SessionContext sc, String entityName) {
	  Map m = this.settingService.getPropertiesForEntityView( sc.getStoreDesc().getNamespace(), GLOBAL_SETTINGS, StoreDesc.getFqEntityName(entityName,sc.getStoreDesc()), null);
		String val = (String)m.get(RECORDVALIDATION);
		return val;
	}

	public Object createObject(String namespace, String entityName) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.createObject");
	}

	public Object createObject(SessionContext sessionContext, String entityName) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.createObject");
	}

	public void insertIntoMaster(SessionContext sc, Object objectInsert, String entityName, Class masterClazz, String fieldName, Object masterId) throws Exception {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.createObject");
	}

	public void insertIntoMaster(SessionContext sc, Object objectInsert, String entityName, Object objectMaster, String fieldName) throws Exception {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.createObject");
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	//update 
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	public Map updateObject(Map dataMap, StoreDesc sdesc, String entityName, String id) {
		return updateObject(dataMap, null, null, sdesc, entityName, id, null, null);
	}

	public Map updateObject(Map dataMap, Map filterMap, Map hintsMap, StoreDesc sdesc, String entityName, String id, String entityNameParent, String idParent) {
		debug(this, "updateObject:" + dataMap + ",filterMap:" + filterMap + ",entityName:" + entityName);
		Map retMap = new HashMap();
		SessionContext sessionContext = getSessionContext(sdesc);
		OrientGraph orientGraph = this.orientdbService.getOrientGraph(sdesc.getNamespace());
		try {
			orientGraph.begin();
			retMap = updateObject(sessionContext, dataMap, filterMap, hintsMap, entityName, id, entityNameParent, idParent);
			orientGraph.commit();
		} catch (Throwable e) {
			error(this, "updateObject:%[exception]s", e);
			sessionContext.handleException(e);
		} finally {
			orientGraph.shutdown();
			sessionContext.handleFinally();
		}
		return retMap;
	}

	public Map updateObject(SessionContext sessionContext, Map dataMap, String entityName, String id) throws Exception {
		return updateObject(sessionContext, dataMap, null, null, entityName, id, null, null);
	}

	public Map updateObject(SessionContext sessionContext, Map dataMap, Map hintsMap, String entityName, String id) throws Exception {
		return updateObject(sessionContext, dataMap, null, hintsMap, entityName, id, null, null);
	}

	public Map updateObject(SessionContext sessionContext, Map dataMap, Map filterMap, Map hintsMap, String entityName, String id, String entityNameParent, String idParent) throws Exception {
		StoreDesc sdesc = sessionContext.getStoreDesc();
		if (entityNameParent != null) {
			entityName = constructEntityName(sessionContext, entityName, entityNameParent);
		}
		String user = sessionContext.getUserName();
		checkPermissions(sdesc, user, entityName, dataMap, "write");
		entityName = this.inflector.getEntityNameCamelCase(entityName);
		return executeUpdateObject(sessionContext, entityName, id, dataMap);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	//delete 
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	public Map deleteObject(Map dataMap, StoreDesc sdesc, String entityName, String id) {
		debug(this, "deleteObject:" + dataMap + ",module:" + entityName + ",id:" + id);
		Map retMap = new HashMap();
		SessionContext sessionContext = getSessionContext(sdesc);
		OrientGraph orientGraph = this.orientdbService.getOrientGraph(sdesc.getNamespace());
		try {
			orientGraph.begin();
			retMap = deleteObject(sessionContext, dataMap, entityName, id);
			orientGraph.commit();
		} catch (Throwable e) {
			error(this, "deleteObject:%[exception]s", e);
			sessionContext.handleException(e);
		} finally {
			orientGraph.shutdown();
			sessionContext.handleFinally();
		}
		return retMap;
	}

	public Map deleteObject(SessionContext sessionContext, Map dataMap, String entityName, String id) throws Exception {
		StoreDesc sdesc = sessionContext.getStoreDesc();
		entityName = this.inflector.getEntityNameCamelCase(entityName);
		Class deleteClazz = getClass(sessionContext, entityName);
		return executeDeleteObject(deleteClazz, id);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	//populate 
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	public void populate(Map from, Object to) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.populate");
	}

	public void populate(SessionContext sessionContext, Map from, Object to, Map hintsMap) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.populate");
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	//get 
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	public Map getObject(StoreDesc sdesc, String entityName, String id) {
		return getObject(sdesc, entityName, id, null, null, null);
	}

	public Map getObject(StoreDesc sdesc, String entityName, String id, List fields) {
		return getObject(sdesc, entityName, id, null, fields, null);
	}

	public Map getObject(StoreDesc sdesc, String entityName, String id, String entityNameDetails, List fields) {
		return getObject(sdesc, entityName, id, entityNameDetails, fields, null);
	}

	public Map getObject(StoreDesc sdesc, String entityName, String id, String entityNameDetails, List fields, HttpServletResponse resp) {
		SessionContext sessionContext = getSessionContext(sdesc);
		checkPermissions(sdesc, sessionContext.getUserName(), entityName, null, "read");
		debug(this, "getObject:fields:" + fields + ",entityName:" + entityName + ",id:" + id);
		Class clazz = getClass(sessionContext, entityName);
		OrientGraph orientGraph = this.orientdbService.getOrientGraph(sdesc.getNamespace());
		try {
			return executeGet(clazz, id);
		} finally {
			orientGraph.shutdown();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	//query 
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 

	public Map querySql(SessionContext sessionContext, StoreDesc sdesc, Map params, String sql){
		OrientGraph orientGraph = this.orientdbService.getOrientGraph(sdesc.getNamespace());
		try {
			Class clazz = (Class)params.get("class");
			List<Map> rows =  executeQuery(sessionContext,clazz,clazz.getSimpleName(),sql);
			Map map = new HashMap();
			map.put("rows",rows);
			return map;
		} finally {
			orientGraph.shutdown();
		}
	}


	public Map query(Map params, StoreDesc sdesc, String entityName) {
		return query(params, sdesc, entityName, null, null);
	}

	public Map query(Map params, StoreDesc sdesc, String entityName, String idParent, String entityNameDetails) {
		SessionContext sessionContext = getSessionContext(sdesc);
		try {
			return query(sessionContext, params, sdesc, entityName, idParent, entityNameDetails);
		} catch (Exception e) {
			sessionContext.handleException(e);
		} finally {
			sessionContext.handleFinally();
		}
		return null;
	}

	public Map query(SessionContext sessionContext, Map params, StoreDesc sdesc, String entityName) {
		return query(sessionContext, params, sdesc, entityName, null, null);
	}

	public Map query(SessionContext sessionContext, Map params, StoreDesc sdesc, String entityName, String idParent, String entityNameDetails) {
		debug(this, "query:" + params + ",entityName:" + entityName + ",entityNameDetails:" + entityNameDetails + ",idParent:" + idParent);
		String config = sessionContext.getConfigName();
		checkPermissions(sdesc, sessionContext.getUserName(), entityName, null, "read");
		checkPermissions(sdesc, sessionContext.getUserName(), entityNameDetails, null, "read");
		entityName = this.inflector.getEntityNameCamelCase(entityName);
		Map retMap = new HashMap();
		Map filtersMap = null;
		if (params.get("filter") != null) {
			filtersMap = (Map) params.get("filter");
		}
		OrientGraph orientGraph = this.orientdbService.getOrientGraph(sdesc.getNamespace());
		try {
			Map fieldSets = this.settingService.getFieldSets(config, sdesc.getNamespace(), StoreDesc.getFqEntityName(entityName,sdesc));
			QueryBuilder qb = new OrientDBQueryBuilder(sdesc, entityName, config, sessionContext, filtersMap, (Map) params, fieldSets);
			String where = qb.getWhere() + getOrderBy(params);
			Class clazz = getClass(sessionContext, entityName);
			List<Map> rows = executeQuery(sessionContext, clazz, this.inflector.getClassNameCamelCase(StoreDesc.getSimpleEntityName(entityName)), where);
			info(this, "layer.rows:" + rows);
			retMap.put("rows", rows);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			orientGraph.shutdown();
		}
		return retMap;
	}

	private String getOrderBy(Map params) {
		String orderBy = "";
		if (params.get("orderby") != null && !"".equals(params.get("orderby"))) {
			String oby = (String)params.get("orderby");
			if (oby.indexOf(".") == -1) {
				orderBy = " order by " + oby;
			} else {
				orderBy = " order by " + oby.substring(oby.indexOf(".")+1);
			}
		}
		return orderBy;
	}

	public Map getObjectGraph(StoreDesc sdesc, String entityName, String id) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.getObjectGraph");
	}

	public void evaluteFormulas(SessionContext sessionContext, String entityName, Map<String, Object> map, String direction) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.evaluteFormulas");
	}

	public synchronized SessionContext getSessionContext(String namespace) {
		return getSessionContext(StoreDesc.getNamespaceData(namespace,"odata"));
	}

	public synchronized SessionContext getSessionContext(StoreDesc sdesc) {
		SessionContext sc = new OrientDBSessionContextImpl(this, sdesc, this.orientdbService);
		sc.setGitService(this.gitService);
		sc.setPermissionService(this.permissionService);
		sc.setSettingService(this.settingService);
		sc.setEntityService(this.entityService);
		return sc;
	}

	private void checkPermissions(StoreDesc sdesc, String user, String entityName, Map<String, Object> dataMap, String action) {
		if (entityName == null)
			return;
		entityName = this.inflector.getEntityNameCamelCase(StoreDesc.getSimpleEntityName(entityName));
		debug(this, "checkPermissions:" + entityName + "/" + action + "/" + dataMap);
		boolean b = this.permissionService.hasEntityPermissions(sdesc, entityName, action);
		if (b)
			return;
		throw new PermissionException("OrientDBLayer.checkPermissions(" + sdesc + "/entityName:" + entityName + "/action:" + action + ") not allowed");
	}

	public Class getClass(SessionContext sessionContext, String entityName) {
		StoreDesc sdesc = sessionContext.getStoreDesc();
		return getClass(sdesc, entityName);
	}

	public Class getClass(StoreDesc sdesc, String entityName) {
		ClassLoader cl = this.domainObjectsService.getClassLoader(sdesc);
		String fqCN = sdesc.getPack() + "." + this.inflector.getClassNameCamelCase(StoreDesc.getSimpleEntityName(entityName));
		info(this, "getClass(" + cl + "):" + fqCN);
		try {
			return cl.loadClass(fqCN);
		} catch (ClassNotFoundException c) {
			throw new RuntimeException("OrientDBLayer.getClass(" + fqCN + ") not found");
		}
	}

	public ClassLoader getClassLoader(StoreDesc sdesc) {
		return this.domainObjectsService.getClassLoader(sdesc);
	}

	public String constructEntityName(SessionContext sessionContext, String entityName, String entityNameParent) {
		throw new UnsupportedOperationException("Not implemented:OrientDBLayer.constructEntityName");
	}

	/************************************ C O N V I N I E N T *************************************************/
	public Object getObjectById(String namespace, String entity, Object id) {
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace,"odata");
		SessionContext sessionContext = getSessionContext(sdesc);
		Class clazz = sessionContext.getClass(sdesc, entity);
		return sessionContext.getObjectById(clazz, id);
	}

	public Object getObjectByFilter(String namespace, String entity, String filter) {
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace,"odata");
		SessionContext sessionContext = getSessionContext(sdesc);
		Class clazz = sessionContext.getClass(sdesc, entity);
		return sessionContext.getObjectByFilter(clazz, filter);
	}

	public List<Object> getObjectsByFilter(String namespace, String entity, String filter) {
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace,"odata");
		SessionContext sessionContext = getSessionContext(sdesc);
		Class clazz = sessionContext.getClass(sdesc, entity);
		return sessionContext.getObjectsByFilter(clazz, filter);
	}

	public Object getObjectByNamedFilter(String namespace, String name, Map<String, Object> fparams) {
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace,"odata");
		SessionContext sessionContext = getSessionContext(sdesc);
		return sessionContext.getObjectByNamedFilter(name, fparams);
	}

	public List<Object> getObjectsByNamedFilter(String namespace, String name, Map<String, Object> fparams) {
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace,"odata");
		SessionContext sessionContext = getSessionContext(sdesc);
		return sessionContext.getObjectsByNamedFilter(name, fparams);
	}

	/************************************ C O N F I G ********************************************************/
	@Reference(dynamic = true, optional = true)
	public void setEntityService(EntityService paramEntityService) {
		this.entityService = paramEntityService;
		info(this, "OrientDBLayer.setEntityService:" + paramEntityService);
	}

	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService paramGitService) {
		this.gitService = paramGitService;
		info(this, "OrientDBLayer.setGitService:" + paramGitService);
	}

	@Reference(dynamic = true)
	public void setPermissionService(PermissionService paramPermissionService) {
		this.permissionService = paramPermissionService;
		info(this, "OrientDBLayer.setPermissionService:" + paramPermissionService);
	}

	@Reference(dynamic = true, optional = true)
	public void setSettingService(SettingService paramSettingService) {
		this.settingService = paramSettingService;
		info(this, "OrientDBLayer.setSettingService:" + paramSettingService);
	}

	@Reference
	public void setOrientDBService(OrientDBService paramEntityService) {
		orientdbService = paramEntityService;
		info(this, "OrientDBLayer.setOrientDBService:" + paramEntityService);
	}

	@Reference(dynamic = true, optional = true)
	public void setCamelService(CamelService paramCamelService) {
		this.camelService = paramCamelService;
		info(this,"OrientDBLayer.setCamelService:" + paramCamelService);
	}

	@Reference
	public void setDomainObjectsService(DomainObjectsService paramService) {
		domainObjectsService = paramService;
		info(this, "OrientDBLayer.setDomainObjectsService:" + paramService);
	}

	@Reference(dynamic = true, optional=true)
	public void setCallService(CallService callService) {
		this.callService = callService;
		info(this,"OrientDBLayer.setCallService:" + callService);
	}


	@Reference(dynamic = true, optional = true)
	public void setAuthService(AuthService paramAuthService) {
		this.authService = paramAuthService;
		info(this, "OrientDBLayer.setAuthService:" + paramAuthService);
	}
}

