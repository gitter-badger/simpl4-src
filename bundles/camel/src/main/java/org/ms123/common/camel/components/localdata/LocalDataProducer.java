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
package org.ms123.common.camel.components.localdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.StringBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.Message;
import org.apache.camel.util.CamelContextHelper;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.util.ObjectHelper;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.camel.api.ExchangeUtils;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.thread.ThreadContext;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import flexjson.*;

/**
 * The LocalData producer.
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class LocalDataProducer extends DefaultProducer implements LocalDataConstants {
	protected JSONSerializer js = new JSONSerializer();
	protected JSONDeserializer ds = new JSONDeserializer();

	private String m_filterName = null;
	private String m_destination = null;

	private String m_namespace = null;
	private String m_pack = null;

	private String m_objectId = null;
	private String m_where = null;
	private String m_source = null;
	private int m_max = 100;
	private String m_lookupUpdateObjectExpr = null;
	private String m_lookupRelationObjectExpr = null;
	private String m_relation = null;
	private Boolean m_noUpdate = false;
	private Boolean m_disableStateSelect = false;
	private List<Map<String, String>> m_objectCriteria = null;
	private List<Map<String, String>> m_filterParameter = null;

	private String m_entityType = null;

	private LocalDataOperation m_operation;

	private LocalDataEndpoint m_endpoint;

	private Map m_options;
	private PermissionService m_permissionService;
	private CamelService camelService;

	DataLayer m_dataLayer = null;

	public LocalDataProducer(LocalDataEndpoint endpoint) {
		super(endpoint);
		CamelContext camelContext = endpoint.getCamelContext();
		m_endpoint = endpoint;
		m_dataLayer = endpoint.getDataLayer();
		m_options = endpoint.getOptions();
		m_namespace = endpoint.getNamespace();
		m_pack = endpoint.getPack();
		m_filterName = endpoint.getFilterName();
		m_destination = endpoint.getDestination();
		m_objectId = endpoint.getObjectId();
		m_where = endpoint.getWhere();
		m_objectCriteria = endpoint.getObjectCriteria();
		m_filterParameter = endpoint.getFilterParameter();
		m_source = endpoint.getSource();
		m_entityType = endpoint.getEntityType();
		m_lookupRelationObjectExpr = endpoint.getLookupRelationObjectExpr();
		m_lookupUpdateObjectExpr = endpoint.getLookupUpdateObjectExpr();
		m_relation = endpoint.getRelation();
		m_noUpdate = endpoint.isNoUpdate();
		m_disableStateSelect = endpoint.isDisableStateSelect();
		String endpointKey = endpoint.getEndpointKey();
		if (endpointKey.indexOf("?") != -1) {
			endpointKey = endpointKey.split("\\?")[0];
		}
		if (endpointKey.indexOf(":") == -1) {
			throw new RuntimeException("LocalDataProducer.no_operation_in_uri:" + endpointKey);
		}
		String[] path = endpointKey.split(":");
		m_operation = LocalDataOperation.valueOf(path[1].replace("//", ""));
		info(this, "m_operation:" + m_operation);
		if (path.length > 2) {
			m_filterName = path[2].split("\\?")[0];
		}
		if (m_namespace == null) {
			m_namespace = (String) CamelContextHelper.mandatoryLookup(camelContext, "namespace");
		}
		m_permissionService = CamelContextHelper.mandatoryLookup(camelContext, PermissionService.PERMISSION_SERVICE, PermissionService.class);
		this.camelService = (CamelService) endpoint.getCamelContext().getRegistry().lookupByName(CamelService.class.getName());
		m_max = endpoint.getMax();
	}

	public void process(Exchange exchange) throws Exception {
		String ns = m_namespace;
		if (ThreadContext.getThreadContext() == null) {
			ThreadContext.loadThreadContext(ns, "admin");
			m_permissionService.loginInternal(ns);
		}
		invokeOperation(m_operation, exchange);
	}

	/**
	 * Entry method that selects the appropriate MongoDB operation and executes it
	 * @param operation
	 * @param exchange
	 * @throws Exception
	 */
	protected void invokeOperation(LocalDataOperation operation, Exchange exchange) throws Exception {
		switch (operation) {
		case insert:
			doInsert(exchange);
			break;

		case update:
		case updateById:
			doUpdateById(exchange, false);
			break;
		case updateByFilter:
			doUpdateByFilter(exchange, false);
			break;
		case updateByWhere:
			doUpdateByWhere(exchange, false);
			break;
		case updateByCriteria:
			doUpdateByCriteria(exchange, false);
			break;

		case upsert:
		case upsertById:
			doUpdateById(exchange, true);
			break;
		case upsertByFilter:
			doUpdateByFilter(exchange, true);
			break;
		case upsertByWhere:
			doUpdateByWhere(exchange, true);
			break;
		case upsertByCriteria:
			doUpdateByCriteria(exchange, true);
			break;

		case deleteById:
			doDelete(exchange, operation);
			break;
		case deleteByFilter:
			doDelete(exchange, operation);
			break;
		case deleteByWhere:
			doDelete(exchange, operation);
			break;
		case deleteByCriteria:
			doDelete(exchange, operation);
			break;

		case findById:
			doFind(exchange, operation, false);
			break;
		case findByFilter:
			doFind(exchange, operation, false);
			break;
		case findByWhere:
			doFind(exchange, operation, false);
			break;
		case findByCriteria:
			doFind(exchange, operation, false);
			break;

		case findOneById:
			doFind(exchange, LocalDataOperation.findById, true);
			break;
		case findOneByFilter:
			doFind(exchange, LocalDataOperation.findByFilter, true);
			break;
		case findOneByWhere:
			doFind(exchange, LocalDataOperation.findByWhere, true);
			break;
		case findOneByCriteria:
			doFind(exchange, LocalDataOperation.findByCriteria, true);
			break;

		case multiInsertUpdate:
			doMultiInsertUpdate(exchange);
			break;
		default:
			throw new RuntimeException("LocalDataProducer.Operation not supported. Value: " + operation);
		}
	}

	private String getStringCheck(String key, String def) {
		if (isEmpty(def)) {
			throw new RuntimeException("LocalDataProducer." + key + "_is_null");
		}
		info(this, "getStringCheck:" + key + "=" + def);
		return def;
	}

	private void doMultiInsertUpdate(Exchange exchange) {
		String relation = m_relation;
		if ("-".equals(relation)) {
			relation = null;
		}
		Map<String, Object> persistenceSpecification = new HashMap();
		persistenceSpecification.put(LocalDataConstants.LOOKUP_RELATION_OBJECT_EXPR, m_lookupRelationObjectExpr);
		persistenceSpecification.put(LocalDataConstants.LOOKUP_UPDATE_OBJECT_EXPR, m_lookupUpdateObjectExpr);
		persistenceSpecification.put(LocalDataConstants.RELATION, relation);
		persistenceSpecification.put(LocalDataConstants.NO_UPDATE, m_noUpdate);
		info(this, "persistenceSpecification:" + persistenceSpecification);
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		List<Object> result = null;
		Object obj = ExchangeUtils.getSource(m_source, exchange, Object.class);
		info(this, "doMultiInsertUpdate:" + obj);
		try {
			result = sc.persistObjects(obj, persistenceSpecification);
		} catch (Exception e) {
			ex = e;
		}
		Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.multiInsertUpdate);
		processAndTransferResult(result, exchange, ex);
	}

	private void doDelete(Exchange exchange, LocalDataOperation operation) {
		String entityType = ExchangeUtils.getParameter(m_entityType, exchange, String.class, ENTITY_TYPE);
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		Map result = null;
		try {
			if (operation == LocalDataOperation.deleteById) {
				String objectId = ExchangeUtils.getParameter(m_objectId, exchange, String.class);
				info(this, "doDeleteById(" + entityType + "):" + objectId);
				if (isEmpty(objectId)) {
					throw new RuntimeException("LocalDataProducer.doDeleteById:no \"objectId\" given");
				}
				result = sc.deleteObjectById(entityType, objectId);
			}
			if (operation == LocalDataOperation.deleteByFilter) {
				String filterName = m_filterName;
				if (isEmpty(filterName)) {
					throw new RuntimeException("LocalDataProducer.doDeleteById:no filtername given");
				}
				Map filterParameter = buildFilterParameter(exchange);
				List<Object> idList = sc.getObjectIdsByNamedFilter(filterName, filterParameter);
				info(this, "doDeleteByFilter(" + filterName + "," + filterParameter + "):" + idList);
				int i = 0;
				for (Object id : idList) {
					if (i++ >= m_max)
						break;
					result = sc.deleteObjectById(entityType, String.valueOf(id));
				}
			}
			if (operation == LocalDataOperation.deleteByWhere) {
				if (isEmpty(m_where)) {
					throw new RuntimeException("LocalDataProducer.doDeleteByWhere:no whereclause  given");
				}
				String where = ExchangeUtils.getParameter(m_where, exchange, String.class);
				info(this, "doDeleteByWhere(" + entityType + "," + where + ")");
				List<Object> rmObjList = getObjectsByWhere(exchange, sc, entityType, where);
				int i = 0;
				for (Object rmObj : rmObjList) {
					if (i++ >= m_max)
						break;
					Object id = sc.getPM().getObjectId(rmObj);
					result = sc.deleteObjectById(entityType, String.valueOf(id));
				}
			}
			if (operation == LocalDataOperation.deleteByCriteria) {
				String where = buildWhere(exchange, sc, entityType);
				if (m_objectCriteria == null || isEmpty(where)) {
					throw new RuntimeException("LocalDataProducer.doDeleteByCriteria:no criteria  given");
				}
				info(this, "doDeleteByCriteria(" + entityType + "," + where + ")");
				List<Object> rmObjList = getObjectsByWhere(exchange, sc, entityType, where);
				int i = 0;
				for (Object rmObj : rmObjList) {
					if (i++ >= m_max)
						break;
					Object id = sc.getPM().getObjectId(rmObj);
					result = sc.deleteObjectById(entityType, String.valueOf(id));
				}
			}
		} catch (Exception e) {
			ex = e;
		}
		if (result == null) {
			result = new HashMap<String, Object>();
			result.put("notFound", true);
		}
		Message resultMessage = prepareResponseMessage(exchange, operation);
		processAndTransferResult(result, exchange, ex);
	}

	private void doFind(Exchange exchange, LocalDataOperation operation, boolean isOne) {
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		try {
			if (operation == LocalDataOperation.findById) {
				String entityType = ExchangeUtils.getParameter(m_entityType, exchange, String.class, ENTITY_TYPE);
				String objectId = ExchangeUtils.getParameter(m_objectId, exchange, String.class);
				info(this, "doFindById(" + entityType + "):" + objectId);
				if (isEmpty(objectId)) {
					throw new RuntimeException("LocalDataProducer.doFindById:no \"objectId\" given");
				}
				Object res = sc.getObjectMapById(entityType, objectId);
				info(this, "doFindById(" + objectId + "):"+res);
				Message resultMessage = prepareResponseMessage(exchange, operation);
				ExchangeUtils.setDestination(m_destination, res, exchange);
				return;
			}
			List<Object> result = null;
			if (operation == LocalDataOperation.findByFilter) {
				String filterName = m_filterName;
				if (isEmpty(filterName)) {
					throw new RuntimeException("LocalDataProducer.doFindById:no filtername given");
				}
				Map filterParameter = buildFilterParameter(exchange);
				info(this, "doFindByFilter(" + filterName + "," + filterParameter + ")");
				Map retMap = sc.executeNamedFilter(filterName, filterParameter);
				if (retMap == null) {
					result = new ArrayList();
				} else {
					result = (List) retMap.get("rows");
				}
			}
			if (operation == LocalDataOperation.findByWhere) {
				String entityType = ExchangeUtils.getParameter(m_entityType, exchange, String.class, ENTITY_TYPE);
				if (isEmpty(m_where)) {
					throw new RuntimeException("LocalDataProducer.doFindByWhere:no whereclause  given");
				}
				String where = ExchangeUtils.getParameter(m_where, exchange, String.class);
				info(this, "doFindByWhere(" + entityType + "," + where + ")");
				result = getObjectsByWhere(exchange, sc, entityType, where);
			}
			if (operation == LocalDataOperation.findByCriteria) {
				String entityType = ExchangeUtils.getParameter(m_entityType, exchange, String.class, ENTITY_TYPE);
				String where = buildWhere(exchange, sc, entityType);
				if (m_objectCriteria == null || isEmpty(where)) {
					throw new RuntimeException("LocalDataProducer.doFindByCriteria:no criteria  given");
				}
				info(this, "doFindByCriteria(" + entityType + "," + where + ")");
				result = getObjectsByWhere(exchange, sc, entityType, where);
			}
			Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.findByFilter);
			if (!isOne) {
				resultMessage.setHeader(LocalDataConstants.ROW_COUNT, result.size());
				ExchangeUtils.setDestination(m_destination, result, exchange);
			} else {
				Object res = null;
				int size = 0;
				if (result.size() > 0) {
					res = result.get(0);
					size = 1;
				}
				resultMessage.setHeader(LocalDataConstants.ROW_COUNT, size);
				ExchangeUtils.setDestination(m_destination, res, exchange);
			}
		} catch (Exception e) {
			ex = e;
			processAndTransferResult(null, exchange, ex);
		}
	}

	private void doInsert(Exchange exchange) {
		String entityType = ExchangeUtils.getParameter(m_entityType, exchange, String.class, ENTITY_TYPE);
		Map insert = ExchangeUtils.getSource(m_source, exchange, Map.class);
		info(this, "doInsert(" + entityType + "):" + insert);
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		Map result = null;
		try {
			result = sc.insertObjectMap(insert, entityType);
		} catch (Exception e) {
			ex = e;
		}
		processAndTransferResult(result, exchange, ex);
	}

	private void doUpdateById(Exchange exchange, boolean isUpsert) {
		String objectId = ExchangeUtils.getParameter(m_objectId, exchange, String.class);
		if (isEmpty(objectId) && !isUpsert) {
			throw new RuntimeException("LocalDataProducer.doUpdateById:no \"objectId\" given");
		}
		String entityType = ExchangeUtils.getParameter(m_entityType, exchange, String.class, ENTITY_TYPE);
		Map updateData = ExchangeUtils.getSource(m_source, exchange, Map.class);
		if (updateData == null) {
			throw new RuntimeException("LocalDataProducer.doUpdateById:no \"updateData\" given");
		}
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		Map result = null;
		try {
			if (isEmpty(objectId) && isUpsert) {
				result = sc.insertObjectMap(updateData, entityType);
			} else {
				info(this, "doUpdateById(" + entityType + "," + objectId + "):" + updateData);
				result = sc.updateObjectMap(updateData, entityType, objectId);
				if (isUpsert && result.get("notFound") != null) {
					result = sc.insertObjectMap(updateData, entityType);
				}
			}
			info(this, "doUpdateById.result(" + entityType + "," + objectId + "):" + result);
		} catch (Exception e) {
			ex = e;
		}
		processAndTransferResult(result, exchange, ex);
	}

	private void doUpdateByFilter(Exchange exchange, boolean isUpsert) {
		String filterName = m_filterName;
		if (isEmpty(filterName) && !isUpsert) {
			throw new RuntimeException("LocalDataProducer.doUpdateByFilter:no filtername given");
		}
		String entityType = ExchangeUtils.getParameter(m_entityType, exchange, String.class, ENTITY_TYPE);
		Map updateData = ExchangeUtils.getSource(m_source, exchange, Map.class);
		if (updateData == null) {
			throw new RuntimeException("LocalDataProducer.doUpdateByFilter:no \"updateData\" given");
		}
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		Map<String, Object> result = new HashMap<String, Object>();
		int i = 0;
		try {
			info(this, "doUpdateByFilter(" + filterName + "):" + updateData);
			Map filterParameter = buildFilterParameter(exchange);
			List<Object> toObjList = sc.getObjectsByNamedFilter(filterName, filterParameter);
			for (Object toObj : toObjList) {
				if (i++ >= m_max)
					break;
				Object id = getId(sc.getPM().getObjectId(toObj));
				info(this, "doUpdateByFilter(" + filterName + "," + i + "):" + id);
				sc.populate(updateData, toObj);
				result.put("id" + (i > 1 ? i + "" : ""), id);
			}
			if (i == 0 && isUpsert) {
				result = sc.insertObjectMap(updateData, entityType);
			}
		} catch (Exception e) {
			ex = e;
		}
		processAndTransferResult(result, exchange, ex);
	}

	private void doUpdateByWhere(Exchange exchange, boolean isUpsert) {
		if (isEmpty(m_where) && !isUpsert) {
			throw new RuntimeException("LocalDataProducer.doUpdateByWhere:\"where clause\" not given");
		}
		String where = ExchangeUtils.getParameter(m_where, exchange, String.class);
		String entityType = ExchangeUtils.getParameter(m_entityType, exchange, String.class, ENTITY_TYPE);
		Map updateData = ExchangeUtils.getSource(m_source, exchange, Map.class);
		if (updateData == null) {
			throw new RuntimeException("LocalDataProducer.doUpdateByWhere:no \"updateData\" given");
		}
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		Map<String, Object> result = new HashMap<String, Object>();
		int i = 0;
		try {
			info(this, "doUpdateByWhere(" + entityType + "," + where + "):" + updateData);
			List<Object> toObjList = getObjectsByWhere(exchange, sc, entityType, where);
			for (Object toObj : toObjList) {
				if (i++ >= m_max)
					break;
				Object id = getId(sc.getPM().getObjectId(toObj));
				sc.populate(updateData, toObj);
				result.put("id" + (i > 1 ? i + "" : ""), id);
			}
			if (i == 0 && isUpsert) {
				result = sc.insertObjectMap(updateData, entityType);
			}
		} catch (Exception e) {
			ex = e;
		}
		processAndTransferResult(result, exchange, ex);
	}

	private void doUpdateByCriteria(Exchange exchange, boolean isUpsert) {
		if (m_objectCriteria == null && !isUpsert) {
			throw new RuntimeException("LocalDataProducer.doUpdate:\"objectCriteria\" not given");
		}
		String entityType = ExchangeUtils.getParameter(m_entityType, exchange, String.class, ENTITY_TYPE);
		Map updateData = ExchangeUtils.getSource(m_source, exchange, Map.class);
		if (updateData == null) {
			throw new RuntimeException("LocalDataProducer.doUpdateByCriteria:no \"updateData\" given");
		}
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		Map<String, Object> result = new HashMap<String, Object>();
		int max = m_max;
		int i = 0;
		try {
			String where = buildWhere(exchange, sc, entityType);
			info(this, "doUpdateByCriteria(" + entityType + "," + where + "):" + updateData);
			List<Object> toObjList = getObjectsByWhere(exchange, sc, entityType, where);
			for (Object toObj : toObjList) {
				if (i++ >= max)
					break;
				Object id = getId(sc.getPM().getObjectId(toObj));
				sc.populate(updateData, toObj);
				result.put("id" + (i > 1 ? i + "" : ""), id);
			}
			if (i == 0 && isUpsert) {
				result = sc.insertObjectMap(updateData, entityType);
			}
		} catch (Exception e) {
			ex = e;
		}
		processAndTransferResult(result, exchange, ex);
	}

	/*	private void doFindOneByFilter(Exchange exchange) {
	 String filterName = getStringCheck(LocalDataConstants.FILTER_NAME, m_filterName);
	 SessionContext sc = getSessionContext(exchange);
	 Map result = null;
	 Map exVars = ExchangeUtils.getVariablesFromHeaderFields(exchange, m_paramHeaders);
	 Map retMap = sc.executeNamedFilter(filterName, exVars, m_options);
	 if (retMap == null) {
	 } else {
	 List rows = (List) retMap.get("rows");
	 if (rows.size() > 0) {
	 result = (Map) rows.get(0);
	 }
	 }
	 Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.findOneByFilter);
	 resultMessage.setHeader(LocalDataConstants.ROW_COUNT, 1);
	 ExchangeUtils.setDestination(m_destination, result, exchange);
	 }*/
	private String getNamespace(Exchange exchange) {
		String namespace = m_namespace;
		if (namespace == null || "".equals(namespace) || "-".equals(namespace)) {
			namespace = exchange.getProperty("_namespace", String.class);
		} else {
			namespace = ExchangeUtils.getParameter(m_namespace, exchange, String.class, NAMESPACE);
		}
		return namespace;
	}

	private Map<String, String> buildFilterParameter(Exchange exchange) {
		Map<String, String> ret = new HashMap<String, String>();
		if (m_filterParameter == null)
			return ret;
		for (Map<String, String> param : m_filterParameter) {
			info(this, "param:" + param);
			String name = param.get("name");
			String value = param.get("value");
			String val = ExchangeUtils.getParameter(value, exchange, String.class);
			ret.put(name, val);
		}
		info(this, "filterParameter:" + ret);
		return ret;
	}

	private String buildWhere(Exchange exchange, SessionContext sc, String entityType) {
		Map<String, Map> fields = sc.getPermittedFields(entityType);
		StringBuilder b = new StringBuilder();
		String and = "";
		if (m_objectCriteria == null)
			return b.toString();
		for (Map<String, String> criteria : m_objectCriteria) {
			String op = criteria.get("op");
			String name = criteria.get("name");
			String value = criteria.get("value");
			Map<String, String> field = fields.get(name);
			String dt = field != null ? field.get("datatype") : "string";
			Class clazz = getType(dt);
			Object val = ExchangeUtils.getParameter(value, exchange, clazz);
			boolean isAlpha = Character.isJavaLetter(op.charAt(0));
			Boolean isString = "string".equals(dt);
			b.append(and);
			if ("string".equals(op) || "7".equals(op) ) {
				b.append("(");
				b.append(val);
				b.append(")");
			} else {
				b.append("(");
				b.append(name);
				b.append(isAlpha ? "." : " ");

				b.append(op);
				b.append(isAlpha ? "( " : " ");

				b.append(isString ? "\"" : "");
				if (val != null) {
					b.append(val);
				}
				b.append(isString ? "\"" : "");
				b.append(isAlpha ? " ))" : " )");
			}
			and = " && ";
		}
		return b.toString();
	}

	private List<Object> getObjectsByWhere(Exchange exchange, SessionContext sc, String entityType, String where) {
		
		String pack = ExchangeUtils.getParameter(m_pack, exchange, String.class, PACK);
		if( isEmpty( pack)){
			pack = StoreDesc.getPackName(entityType, m_endpoint.isOrientdb() ? "odata" : "data");
		}
		String namespace = getNamespace(exchange);
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace, pack);
		Class clazz = sc.getClass(sdesc, entityType);
		List<Object> objList = sc.getObjectsByFilter(clazz, where);
		if (objList == null) {
			objList = new ArrayList<Object>();
		}
		info(this, "getObjectsByWhere.toObj:" + objList);
		return objList;
	}

	private String getId(Object obj) {
		if (obj != null && obj instanceof javax.jdo.identity.StringIdentity) {
			javax.jdo.identity.StringIdentity si = (javax.jdo.identity.StringIdentity) obj;
			return si.getKey();
		}
		return String.valueOf(obj);
	}

	private Class getType(String dt) {
		Class ret = String.class;
		if ("long".equals(dt)) {
			ret = Long.class;
		} else if ("number".equals(dt)) {
			ret = Integer.class;
		} else if ("decimal".equals(dt)) {
			ret = Double.class;
		} else if ("double".equals(dt)) {
			ret = Double.class;
		} else if ("date".equals(dt)) {
			ret = java.util.Date.class;
		}
		info(this, "getType.ret:" + ret);
		return ret;
	}

	private SessionContext getSessionContext(Exchange exchange) {
		String namespace = m_namespace;
		if (namespace == null || "".equals(namespace) || "-".equals(namespace)) {
			namespace = exchange.getProperty("_namespace", String.class);
		} else {
			namespace = ExchangeUtils.getParameter(m_namespace, exchange, String.class, NAMESPACE);
		}
		String pack = ExchangeUtils.getParameter(m_pack, exchange, String.class, PACK);
		SessionContext sc = m_dataLayer.getSessionContext(StoreDesc.getNamespaceData(namespace, pack));
		return sc;
	}

	private void processAndTransferResult(Object result, Exchange exchange, Exception ex) {
		if (ex != null) {
			exchange.getIn().setHeader(LocalDataConstants.LAST_ERROR, ex.getMessage());
			exchange.setException(ex);
		}
		exchange.getIn().setHeader(LocalDataConstants.WRITE_RESULT, result);
	}

	private Message prepareResponseMessage(Exchange exchange, LocalDataOperation operation) {
		Message answer = exchange.getIn();
		return answer;
	}
}

