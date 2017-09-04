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
package org.ms123.common.activiti.process;

import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.activiti.BaseResource;

/**
 */
public class ProcessInstancesResource extends BaseResource {

	private Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();

	private String m_processDefinitionId;
	private String m_processDefinitionKey;

	private String m_businessKey;
	private String m_namespace;

	private Boolean m_unfinished;

	private Boolean m_finished;

	public ProcessInstancesResource(ActivitiService as, Map<String, Object> listParams, String processDefinitionId, String processDefinitionKey,String businessKey, Boolean unfinished, Boolean finished, String namespace) {
		super(as, listParams);
		m_processDefinitionId = processDefinitionId;
		m_processDefinitionKey = processDefinitionKey;
		m_businessKey = businessKey;
		m_unfinished = unfinished;
		m_finished = finished;
		m_namespace = namespace;
		properties.put("id", HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
		properties.put("processDefinitionId", HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
		properties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
		properties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);
		properties.put("endTime", HistoricProcessInstanceQueryProperty.END_TIME);
		properties.put("duration", HistoricProcessInstanceQueryProperty.DURATION);
	}

	public Map getProcessInstances() {
		HistoricProcessInstanceQuery query = getPE().getHistoryService().createHistoricProcessInstanceQuery();
		query = query.processInstanceTenantId(m_namespace);
		if (m_unfinished != null) {
			if (m_unfinished) {
				query = query.unfinished();
			} else {
				query = query.finished();
			}
		}
		if (m_finished != null) {
			if (m_finished) {
				query = query.finished();
			} else {
				query = query.unfinished();
			}
		}
		String processDefinitionId = m_processDefinitionId;
		String processDefinitionKey = m_processDefinitionKey;
		String processInstanceKey = m_businessKey;
		query = processDefinitionId == null ? query : query.processDefinitionId(processDefinitionId);
		query = processDefinitionKey == null ? query : query.processDefinitionKey(processDefinitionKey);
		query = processInstanceKey == null ? query : query.processInstanceBusinessKey(processInstanceKey);
		Map response = new ProcessInstancesPaginateList(this).paginateList(m_listParams, query, "id", properties);
		return response;
	}
}
