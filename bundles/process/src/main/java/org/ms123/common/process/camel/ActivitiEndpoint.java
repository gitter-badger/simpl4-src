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

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.HistoryService;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultEndpoint;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.workflow.api.WorkflowService;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import flexjson.*;
import org.apache.camel.spi.UriEndpoint;
import org.ms123.common.process.camel.base.BaseEndpoint;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
@UriEndpoint(scheme = "activiti", title = "Activiti", syntax = "activiti:name", consumerClass = ActivitiConsumer.class)
public class ActivitiEndpoint extends BaseEndpoint {

	private JSONDeserializer ds = new JSONDeserializer();
	private RuntimeService m_runtimeService;
	private HistoryService m_historyService;
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

	private PermissionService m_permissionService;
	private WorkflowService m_workflowService;

	public ActivitiEndpoint(String uri, CamelContext camelContext, WorkflowService ws, PermissionService ps) {
		super(uri, camelContext);
		m_runtimeService = ws.getProcessEngine().getRuntimeService();
		m_historyService = ws.getProcessEngine().getHistoryService();
		setRuntimeService(m_runtimeService);
		m_permissionService = ps;
		m_workflowService = ws;
	}

	public Producer createProducer() throws Exception {
		info(this, "ActivitiEndpoint.createProducer");
		return new org.ms123.common.process.camel.ActivitiProducer(this, m_workflowService, m_permissionService);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		info(this, "ActivitiEndpoint.createConsumer");
		ActivitiConsumer consumer = new ActivitiConsumer(this, processor);
		configureConsumer(consumer);
		return consumer;
	}
	public boolean isSingleton() {
		return true;
	}
	public void configureProperties(Map<String, Object> options) {
		info(this, "ActivitiEndpoint:" + options);
		m_options = options;
	}

	public Map getOptions() {
		return m_options;
	}
	public RuntimeService getRuntimeService(){
		return m_runtimeService;
	}
	public HistoryService getHistoryService(){
		return m_historyService;
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

