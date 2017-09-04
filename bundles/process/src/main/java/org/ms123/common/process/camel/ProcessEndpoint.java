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
package org.ms123.common.process.camel;

import flexjson.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ProcessEndpoint extends DefaultEndpoint {

	private JSONDeserializer ds = new JSONDeserializer();
	private Map m_options;
	private Map<String, String> processCriteria = new HashMap<String, String>();
	private Map<String, String> taskCriteria = new HashMap<String, String>();
	private String namespace;
	private String events;
	private String signalName;
	private boolean withMetadata;
	private boolean isSendSignal;
	private boolean isSendMessage;
	private boolean isCheckAssignments;
	private String messageName;
	private String headerFields;
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
		ProcessConsumer consumer = new ProcessConsumer(this, processor);
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
		if (data != null) {
			List<Map<String, String>> l = (List) ds.deserialize(data);
			for (Map<String, String> m : l) {
				String name = m.get("name");
				String value = m.get("value");
				ret.put(name, value);
			}
		}
		this.processCriteria = ret;
	}

	public Map<String, String> getProcessCriteria() {
		return this.processCriteria;
	}
}

