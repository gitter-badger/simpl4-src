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
package org.ms123.common.process.engineapi.process;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.io.InputStream;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.process.engineapi.BaseResource;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.IdentityLink;
import org.apache.commons.io.IOUtils;
import flexjson.*;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.osgi.service.event.EventAdmin;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.osgi.service.event.Event;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_STARTPROCESS_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;
import static org.ms123.common.system.history.HistoryService.HISTORY_HINT;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class StartProcessInstanceResource extends BaseResource {

	private String m_processDefinitionId;

	private String m_processDefinitionKey;

	private String m_processDefinitionName;

	private String m_businessKey;

	private String m_messageName;

	private String m_namespace;

	private Integer m_version;

	private Map<String, Object> m_startParams;

	public StartProcessInstanceResource(ProcessService ps, String namespace, Integer version, String processDefinitionId, String processDefinitionKey, String processDefinitionName, String messageName, String businessKey, Map<String, Object> startParams) {
		super(ps, null);
		m_namespace = namespace;
		m_processDefinitionId = processDefinitionId;
		m_processDefinitionKey = processDefinitionKey;
		m_processDefinitionName = processDefinitionName;
		m_businessKey = businessKey;
		m_messageName = messageName;
		m_startParams = startParams;
		m_version = version;
	}

	public Map startProcessInstance() {
		ProcessDefinition processDefinition = null;
		String uid = null;
		try {
			Map<String, Object> variables = m_startParams;
			if (variables != null) {
				variables.remove("processDefinitionId");
				variables.remove("processDefinitionKey");
				variables.remove("processDefinitionName");
				variables.remove("businessKey");
			} else {
				variables = new HashMap();
			}
			uid = org.ms123.common.system.thread.ThreadContext.getThreadContext().getUserName();
			getPE().getIdentityService().setAuthenticatedUserId(uid);
			info(this, "StartProcessInstanceResource:");
			info(this, "\tm_processDefinitionId:" + m_processDefinitionId);
			info(this, "\tm_processDefinitionKey:" + m_processDefinitionKey);
			info(this, "\tm_processDefinitionName:" + m_processDefinitionName);
			info(this, "\tm_messageName:" + m_messageName);
			if (m_processDefinitionKey != null) {
				ProcessDefinitionQuery query = getPE().getRepositoryService().createProcessDefinitionQuery();
				query = addVersion(query);
				processDefinition = query.processDefinitionKey(m_processDefinitionKey).singleResult();
				if (processDefinition == null) {
					throw new RuntimeException("No process with processDefinitionKey(" + m_processDefinitionKey + ")");
				}
			} else if (m_messageName != null) {
				ProcessDefinitionQuery query = getPE().getRepositoryService().createProcessDefinitionQuery();
				query = addVersion(query);
				processDefinition = query.messageEventSubscriptionName(m_messageName).singleResult();
				if (processDefinition == null) {
					throw new RuntimeException("No process with messageName(" + m_messageName + ")");
				}
			} else if (m_processDefinitionName != null) {
				processDefinition = getPE().getRepositoryService().createProcessDefinitionQuery().processDefinitionName(m_processDefinitionName).singleResult();
				if (processDefinition == null) {
					throw new RuntimeException("No process with processDefinitionName(" + m_processDefinitionName + ")");
				}
			} else {
				processDefinition = getPE().getRepositoryService().createProcessDefinitionQuery().processDefinitionId(m_processDefinitionId).singleResult();
				if (processDefinition == null) {
					throw new RuntimeException("No process with processDefinitionId(" + m_processDefinitionId + ")");
				}
			}
			if (m_processDefinitionId == null) {
				m_processDefinitionId = processDefinition.getId();
			}
			List<IdentityLink> links = getPE().getRepositoryService().getIdentityLinksForProcessDefinition(m_processDefinitionId);
			info(this, "CandidateLinks:" + links);
			if (links.size() > 0) {
				ProcessDefinition pd = getPE().getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processDefinition.getId()).startableByUser(uid).singleResult();
				if (pd == null) {
					if ( !checkGroups(links) ) {
						throw new RuntimeException("User(" + uid + ") not allowed to start process with processDefinitionId(" + m_processDefinitionId + ")");
					}
				}
			}
			String deploymentId = processDefinition.getDeploymentId();
			RepositoryService rs = getPE().getRepositoryService();
			try {
				InputStream is = rs.getResourceAsStream(deploymentId, "initialParameter");
				Map params = (Map) new JSONDeserializer().deserialize(IOUtils.toString(is));
				if (params.get("items") != null) {
					List<Map> items = (List) params.get("items");
					for (Map<String, String> item : items) {
						String name = item.get("name");
						Object value = item.get("value");
						if (value instanceof String) {
							String v = ((String) value).trim();
							if (v.length() > 1 && (v.startsWith("{") || v.startsWith("["))) {
								value = new JSONDeserializer().deserialize(v);
							}
						}
						info(this, "put:" + name + "=" + value);
						variables.put(name, value);
					}
				}
				variables.put("__currentUser", uid);
				variables.put("__startingUser", uid);
				variables.put("__namespace", m_namespace);
			} catch (Exception e) {
				if (e instanceof NullPointerException) {
					e.printStackTrace();
				}
				info(this, "getResourceAsStream:" + e);
			}
			ProcessInstance processInstance = getPE().getRuntimeService().startProcessInstanceById(processDefinition.getId(), m_businessKey, variables);
			info(this,"ExecutionEntity:" + (processInstance instanceof ExecutionEntity));
			if( processInstance instanceof ExecutionEntity ){
				((ExecutionEntity)processInstance).setTenantId(uid);
			}
			variables.put("__processInstanceId", processInstance.getProcessInstanceId());
			info(this, "StartProcessInstanceResource.variables:" + variables);
			Map<String, String> response = new HashMap();
			response.put("id", processInstance.getId());
			response.put("businessKey", processInstance.getBusinessKey());
			response.put("processInstanceId", processInstance.getProcessInstanceId());
			response.put("processDefinitionId", processInstance.getProcessDefinitionId());
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			createLogEntry(processDefinition, uid, e);
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException("Failed to retrieve the process definition parameters", e);
		}
	}

	private boolean checkGroups(List<IdentityLink> links) {
		for (IdentityLink il : links) {
			String groupId = il.getGroupId();
			if (groupId != null) {
				boolean hasRole = hasRole(groupId);
				info(this, "checkGroup(" + groupId + ") hasRole:" + hasRole);
				if (hasRole) {
					return true;
				}
			}
		}
		return false;
	}

	private ProcessDefinitionQuery addVersion(ProcessDefinitionQuery query) {
		if (m_version != null) {
			if (m_version != -1) {
				query = query.processDefinitionVersion(m_version);
			} else {
				query = query.latestVersion();
			}
		}
		return query;
	}

	private void createLogEntry(ProcessDefinition processDefinition, String uid, Exception e) {
		info(this, "createLogEntry.StartProcessInstanceResource");
		Map props = new HashMap();
		Map hint = new HashMap();
		props.put(HISTORY_TYPE, HISTORY_ACTIVITI_STARTPROCESS_EXCEPTION);
		if (processDefinition != null) {
			props.put(HISTORY_KEY, processDefinition.getId());
		} else {
			props.put(HISTORY_KEY, m_processDefinitionId);
		}
		info(this, "createLogEntry.props:" + props);
		Throwable r = getRootCause(e);
		props.put(HISTORY_MSG, r != null ? getStackTrace(r) : getStackTrace(e));
		if (processDefinition != null) {
			hint.put("processDefinitionId", processDefinition.getId());
			hint.put("processDefinitionKey", processDefinition.getKey());
			hint.put("processDefinitionName", processDefinition.getName());
			hint.put("processDeploymentId", processDefinition.getDeploymentId());
		}
		hint.put("startUserId", uid);
		props.put(HISTORY_HINT, m_js.deepSerialize(hint));
		getEventAdmin().sendEvent(new Event(HISTORY_TOPIC, props));
	}
}

