/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ms123.common.camel.components.localdata;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.CamelContext;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.CamelContextHelper;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.camel.api.ExchangeUtils;
import org.ms123.common.system.thread.ThreadContext;
import static com.jcabi.log.Logger.info;

/**
 * The LocalData producer.
 */
@SuppressWarnings({"unchecked","deprecation"})
public class LocalDataProducer extends DefaultProducer {

	private String m_filterName = null;
	private String m_destination = null;
	private String m_paramHeaders = null;

	private String m_namespace = null;

	private String m_objectId = null;
	private String m_source = null;
	private String m_lookupUpdateObjectExpr = null;
	private String m_lookupRelationObjectExpr = null;
	private String m_relation = null;
	private Boolean m_noUpdate = false;
	private Boolean m_disableStateSelect = false;

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
		m_filterName = endpoint.getFilterName();
		m_destination = endpoint.getDestination();
		m_paramHeaders = endpoint.getParamHeaders();
		m_objectId = endpoint.getObjectId();
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
		info(this,"m_operation:" + m_operation);
		if (path.length > 2) {
			m_filterName = path[2].split("\\?")[0];
		}
		if( m_namespace == null){
			m_namespace = (String)CamelContextHelper.mandatoryLookup(camelContext, "namespace");
		}
		m_permissionService = CamelContextHelper.mandatoryLookup(camelContext, PermissionService.class.getName(), PermissionService.class);
		this.camelService = (CamelService) endpoint.getCamelContext().getRegistry().lookupByName(CamelService.class.getName());
	}

	public void process(Exchange exchange) throws Exception {
		String ns = m_namespace;
		if( ThreadContext.getThreadContext() == null){
			System.out.println("getThreadContext");
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
		switch(operation) {
			case findOneByFilter:
				doFindOneByFilter(exchange);
				break;
			case findByFilter:
				doFindByFilter(exchange);
				break;
			case findById:
				doFindById(exchange);
				break;
			case insert:
				doInsert(exchange);
				break;
			case update:
				doUpdate(exchange);
				break;
			case delete:
				doDelete(exchange);
				break;
			case multiInsertUpdate:
				doMultiInsertUpdate(exchange);
				break;
			/*case aggregate:
				doAggregate(exchange);
				break;*/
			default:
				throw new RuntimeException("LocalDataProducer.Operation not supported. Value: " + operation);
		}
	}

	private String evaluate( String def, Exchange e ){
		if( def != null && def.indexOf("${") >= 0){
			String res = this.camelService.evaluate( def, e );
			info(this,"evaluate.result:" + res);
			return res;
		}
		return null;
	}

	private String getStringCheck(Exchange e, String key, String def) {
		String res = evaluate( def, e );
		if( res != null) return res;
		String value = e.getIn().getHeader(key, String.class);
		info(this,"getStringCheck:"+key+"="+value+"/def:"+def);
		if (value == null){
			value = e.getProperty(key, String.class);
		}
		if (value == null && def == null){
			throw new RuntimeException("LocalDataProducer." + key + "_is_null");
		}
		return value != null ? value : def;
	}
	private String getString(Exchange e, String key, String def) {
		String res = evaluate( def, e );
		if( res != null) return res;
		String value = e.getIn().getHeader(key, String.class);
		if (value == null){
			value = e.getProperty(key, String.class);
		}
		info(this,"getString:"+key+"="+value+"/def:"+def);
		return value != null ? value : def;
	}
	private boolean getBoolean(Exchange e, String key, boolean def) {
		Boolean value = e.getIn().getHeader(key, Boolean.class);
		if (value == null){
			value = e.getProperty(key, Boolean.class);
		}
		info(this,"getString:"+key+"="+value+"/def:"+def);
		return value != null ? value : def;
	}
	private void doMultiInsertUpdate(Exchange exchange) {
		String lookupUpdateObjectExpr = getString(exchange, LocalDataConstants.LOOKUP_UPDATE_OBJECT_EXPR, m_lookupUpdateObjectExpr);
		String lookupRelationObjectExpr = getString(exchange, LocalDataConstants.LOOKUP_RELATION_OBJECT_EXPR, m_lookupRelationObjectExpr);
		String relation = getString(exchange, LocalDataConstants.RELATION, m_relation);
		if( "-".equals(relation))relation=null;
		Boolean no_update = getBoolean(exchange, LocalDataConstants.NO_UPDATE, m_noUpdate);
		Map<String,Object> persistenceSpecification = new HashMap();
		persistenceSpecification.put(LocalDataConstants.LOOKUP_RELATION_OBJECT_EXPR,lookupRelationObjectExpr);
		persistenceSpecification.put(LocalDataConstants.LOOKUP_UPDATE_OBJECT_EXPR,lookupUpdateObjectExpr);
		persistenceSpecification.put(LocalDataConstants.RELATION,relation);
		persistenceSpecification.put(LocalDataConstants.NO_UPDATE,no_update);
		System.out.println("persistenceSpecification:"+persistenceSpecification);
		//String entityType = getStringCheck(exchange, LocalDataConstants.ENTITY_TYPE, m_entityType);
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		List<Object> result = null;
		Object obj = ExchangeUtils.getSource( m_source, exchange, Object.class); 
		info(this,"doMultiInsertUpdate:"+obj);
		try {
			result = sc.persistObjects(obj,persistenceSpecification);
		} catch (Exception e) {
			ex = e;
		}
		Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.multiInsertUpdate);
		processAndTransferResult(result, exchange, ex);
	}

	private void doDelete(Exchange exchange) {
		String objectId = getStringCheck(exchange, LocalDataConstants.OBJECT_ID, m_objectId);
		String entityType = getStringCheck(exchange, LocalDataConstants.ENTITY_TYPE, m_entityType);
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		Map result = null;
		try {
			result = sc.deleteObjectById(entityType, objectId);
		} catch (Exception e) {
			ex = e;
		}
		Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.delete);
		processAndTransferResult(result, exchange, ex);
	}

	private void doInsert(Exchange exchange) {
		String entityType = getStringCheck(exchange, LocalDataConstants.ENTITY_TYPE, m_entityType);
//		Map insert = exchange.getIn().getBody(Map.class);
		Map insert = ExchangeUtils.getSource( m_source, exchange, Map.class); 
		info(this,"doInsert("+entityType+"):"+insert);
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		Map result = null;
		try {
			result = sc.insertObjectMap(insert, entityType);
		} catch (Exception e) {
			ex = e;
		}
		Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.insert);
		processAndTransferResult(result, exchange, ex);
		resultMessage.setBody(result);
	}

	private void doUpdate(Exchange exchange) {
		String objectId = getStringCheck(exchange, LocalDataConstants.OBJECT_ID, m_objectId);
		String entityType = getStringCheck(exchange, LocalDataConstants.ENTITY_TYPE, m_entityType);
	//	Map update = exchange.getIn().getBody(Map.class);
		Map update = ExchangeUtils.getSource( m_source, exchange, Map.class); 
		info(this,"doUpdate("+entityType+"):"+update);
		SessionContext sc = getSessionContext(exchange);
		Exception ex = null;
		Map result = null;
		try {
			result = sc.updateObjectMap(update, entityType, objectId);
		} catch (Exception e) {
			ex = e;
		}
		Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.update);
		processAndTransferResult(result, exchange, ex);
	}

	private void doFindById(Exchange exchange) {
		String objectId = getStringCheck(exchange, LocalDataConstants.OBJECT_ID, m_objectId);
		String destination = getString(exchange, LocalDataConstants.DESTINATION, m_destination);
		String entityType = getStringCheck(exchange, LocalDataConstants.ENTITY_TYPE, m_entityType);
		SessionContext sc = getSessionContext(exchange);
		Object ret = sc.getObjectMapById(entityType, objectId);
		Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.findById);
		ExchangeUtils.setDestination(destination, ret, exchange);
	}

	private void doFindByFilter(Exchange exchange) {
		String filterName = getStringCheck(exchange, LocalDataConstants.FILTER_NAME, m_filterName);
		String destination = getString(exchange, LocalDataConstants.DESTINATION, m_destination);
		String paramHeaders = getString(exchange, LocalDataConstants.PARAM_HEADERS, m_paramHeaders);
		Boolean disableStateSelect = getBoolean(exchange, LocalDataConstants.DISABLE_STATESELECT, m_disableStateSelect);
		Map options = m_options != null ? new HashMap(m_options) : new HashMap();
		if( disableStateSelect){
			options.put(LocalDataConstants.DISABLE_STATESELECT, true);
		}
		SessionContext sc = getSessionContext(exchange);
		List result = null;
		Map exVars = ExchangeUtils.prepareVariables(exchange, true,paramHeaders,false,null,false);
		Map retMap = sc.executeNamedFilter(filterName, exVars,options);
		if (retMap == null) {
			result = new ArrayList();
		} else {
			result = (List) retMap.get("rows");
		}
		Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.findByFilter);
		resultMessage.setHeader(LocalDataConstants.ROW_COUNT, result.size());
		ExchangeUtils.setDestination(destination, result, exchange);
	}

	private void doFindOneByFilter(Exchange exchange) {
		String filterName = getStringCheck(exchange, LocalDataConstants.FILTER_NAME, m_filterName);
		String destination = getString(exchange, LocalDataConstants.DESTINATION, m_destination);
		String paramHeaders = getString(exchange, LocalDataConstants.PARAM_HEADERS, m_paramHeaders);
		SessionContext sc = getSessionContext(exchange);
		Map result = null;
		Map exVars = ExchangeUtils.prepareVariables(exchange, true,paramHeaders,false,null,false);
		Map retMap = sc.executeNamedFilter(filterName, exVars,m_options);
		if (retMap == null) {
		} else {
			List rows = (List) retMap.get("rows");
			if (rows.size() > 0) {
				result = (Map) rows.get(0);
			}
		}
		Message resultMessage = prepareResponseMessage(exchange, LocalDataOperation.findOneByFilter);
		resultMessage.setHeader(LocalDataConstants.ROW_COUNT, 1);
		ExchangeUtils.setDestination(destination, result, exchange);
	}

	private SessionContext getSessionContext(Exchange exchange) {
		String namespace = getString(exchange, LocalDataConstants.NAMESPACE, m_namespace);
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace);
		if (sdesc == null){
			throw new RuntimeException("LocalDataProducer.namespace:" + namespace + " not found");
		}
		SessionContext sc = m_dataLayer.getSessionContext(sdesc);
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
