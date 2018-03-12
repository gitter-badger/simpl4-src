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
package org.ms123.common.process.camel;

import flexjson.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.apache.camel.*;
import org.apache.camel.core.osgi.utils.BundleContextUtils;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.process.api.ProcessService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
@UriEndpoint(scheme = "process", title = "Process", syntax = "process:name", consumerClass = ProcessConsumer.class)
public class ProcessEndpoint extends DefaultEndpoint implements ProcessConstants,org.ms123.common.process.Constants{

	private JSONDeserializer ds = new JSONDeserializer();
	private Map m_options;
	private Map<String, String> processCriteria = new HashMap<String, String>();
	private List<String> processCriteriaVar = new ArrayList<String>();
	private Map<String, String> taskCriteria = new HashMap<String, String>();
	private String namespace;
	private String events;
	private String includeExpr;
	private String excludeExpr;
	private String signalName;
	private boolean withMetadata;
	private boolean isSendSignal;
	private boolean isSendMessage;
	private boolean isCheckAssignments;
	private String messageName;
	private String deleteReason;
	private String headerFields;
	private String destination;
	private List<Map<String,String>> assigments;
	private String variableNames;
	private String taskOperation;
	private String taskId;
	private String businessKey;

	private PermissionService permissionService;
	private ProcessService processService;
	private BundleContext bundleContext;
	private EventAdmin eventAdmin;

	public ProcessEndpoint(String uri, CamelContext camelContext, ProcessService processService, PermissionService ps) {
		super(uri, camelContext);
		this.permissionService = ps;
		this.processService = processService;
		this.bundleContext = BundleContextUtils.getBundleContext(ProcessEndpoint.class);
		this.eventAdmin = lookupServiceByClass(EventAdmin.class);
	}

	public Producer createProducer() throws Exception {
		info(this, "ProcessEndpoint("+isSingleton()+").createProducer");
		return new org.ms123.common.process.camel.ProcessProducer(this, processService, this.permissionService);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		info(this, "ProcessEndpoint.createConsumer");
		ProcessConsumer consumer = new ProcessConsumer(this, processor,processService);
		configureConsumer(consumer);
		return consumer;
	}
	public boolean isSingleton() {
		return true;
	}
	public void configureProperties(Map<String, Object> options) {
		info(this, "ProcessEndpoint:" + options);
		m_options = options;
	}

	public Map getOptions() {
		return m_options;
	}

	public <T> T lookupServiceByClass(Class<T> clazz) {
		T service = null;
		ServiceReference sr = this.bundleContext.getServiceReference(clazz);
		if (sr != null) {
			service = (T) this.bundleContext.getService(sr);
		}
		if (service == null) {
			throw new RuntimeException("ProcessEndpoint.Cannot resolve service:" + clazz);
		}
		return service;
	}

	public EventAdmin getEventAdmin() {
		return this.eventAdmin;
	}

	public BundleContext getBundleContext() {
		return this.bundleContext;
	}


	public void setNamespace(String data) {
		this.namespace = data;
	}

	public String getNamespace() {
		if ("-".equals(this.namespace)) {
			return null;
		}
		return this.namespace;
	}

	public void setEvents(String data) {
		this.events = data;
	}

	public String getEvents() {
		return this.events;
	}

	public void setIncludeExpr(String data) {
		this.includeExpr = data;
	}

	public String getIncludeExpr() {
		return this.includeExpr;
	}

	public void setExcludeExpr(String data) {
		this.excludeExpr = data;
	}

	public String getExcludeExpr() {
		return this.excludeExpr;
	}

	public void setCheckAssignments(boolean data) {
		this.isCheckAssignments = data;
	}
	public boolean isCheckAssignments() {
		return this.isCheckAssignments;
	}

	public void setMetadata(Boolean data) {
		this.withMetadata = data;
	}
	public Boolean withMetadata() {
		return this.withMetadata;
	}

	public void setSendSignal(Boolean data) {
		this.isSendSignal = data;
	}
	public Boolean isSendSignal() {
		return this.isSendSignal;
	}

	public void setSendMessage(boolean data) {
		this.isSendMessage = data;
	}
	public boolean isSendMessage() {
		return this.isSendMessage;
	}

	public void setSignalName(String data) {
		this.signalName = data;
	}
	public String getSignalName() {
		return this.signalName;
	}

	public void setMessageName(String data) {
		this.messageName = data;
	}
	public String getMessageName() {
		return this.messageName;
	}

	public void setDeleteReason(String data) {
		this.deleteReason = data;
	}
	public String getDeleteReason() {
		return this.deleteReason;
	}

	public void setAssignments(String a) {
		if (a != null) {
			this.assigments = (List)ds.deserialize(a);
		}
	}

	public List<Map<String,String>> getAssignments() {
		return this.assigments;
	}

	public void setHeaderFields(String t) {
		headerFields = t;
	}

	public String getHeaderFields() {
		return headerFields;
	}

	public void setDestination(String t) {
		destination = t;
	}

	public String getDestination() {
		return destination;
	}

	public void setVariableNames(String t) {
		variableNames = t;
	}

	public String getVariableNames() {
		return variableNames;
	}

	public void setTaskOperation(String t) {
		this.taskOperation = t;
	}

	public String getTaskOperation() {
		return this.taskOperation;
	}

	public void setTaskId(String t) {
		this.taskId = t;
	}

	public String getTaskId() {
		return this.taskId;
	}

	public void setBusinessKey(String t) {
		businessKey = t;
	}

	public String getBusinessKey() {
		return businessKey;
	}
	public void setTaskCriteria(String data) {
		Map<String, String> ret = new HashMap<String, String>();
		if (data != null) {
			List<Map<String, String>> l = (List) ds.deserialize(data);
			for (Map<String, String> m : l) {
				String name = m.get("name");
				String value = m.get("value");
				ret.put(name, value);
			}
		}
		this.taskCriteria = ret;
	}

	public Map<String, String> getTaskCriteria() {
		return this.taskCriteria;
	}

	public void setProcessCriteria(String data) {
		Map<String, String> ret = new HashMap<String, String>();
		List<String> retVar = new ArrayList<String>();
		if (data != null) {
			List<Map<String, String>> l = (List) ds.deserialize(data);
			for (Map<String, String> m : l) {
				String name = m.get("name");
				String value = m.get("value");
				if( name.equals(PROCESSVARIABLE)){
					retVar.add( value);
				}else{
					ret.put(name, value);
				}
			}
		}
		this.processCriteria = ret;
		this.processCriteriaVar = retVar;
	}

	public Map<String, String> getProcessCriteria() {
		return this.processCriteria;
	}
	public List<String> getProcessCriteriaVar() {
		return this.processCriteriaVar;
	}
}

