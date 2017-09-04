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
package org.ms123.common.process.engineapi.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.ProcessEngine;
import org.ms123.common.process.engineapi.BaseResource;
import org.ms123.common.process.engineapi.Util;
import org.ms123.common.process.engineapi.AbstractPaginateList;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;

/**
 */
@SuppressWarnings("unchecked")
public class TasksPaginateList extends AbstractPaginateList implements org.ms123.common.process.Constants{

	private ProcessEngine m_pe;

	public TasksPaginateList(BaseResource br) {
		m_pe = br.getPE();
	}

	protected List processList(List list) {
		List<Map> responseList = new ArrayList<Map>();
		for (Object _task : list) {
			Task task = (Task) _task;
			Map<String, Object> taskResponse = new HashMap();
			taskResponse.put("assignee", task.getAssignee());
			taskResponse.put("createTime", Util.dateToString(task.getCreateTime()));
			taskResponse.put("_createTime", task.getCreateTime().getTime());
			taskResponse.put("delegationState", task.getDelegationState());
			taskResponse.put("description", task.getDescription());
			taskResponse.put("dueDate", Util.dateToString(task.getDueDate()));
			taskResponse.put("executionId", task.getExecutionId());
			taskResponse.put("id", task.getId());
			taskResponse.put("name", task.getName());
			taskResponse.put("owner", task.getOwner());
			taskResponse.put("parentTaskId", task.getParentTaskId());
			taskResponse.put("priority", task.getPriority());
			taskResponse.put("processDefinitionId", task.getProcessDefinitionId());
			taskResponse.put("processInstanceId", task.getProcessInstanceId());
			taskResponse.put("taskDefinitionId", task.getTaskDefinitionKey());
			TaskFormData taskFormData = m_pe.getFormService().getTaskFormData(task.getId());
			if (taskFormData != null) {
				taskResponse.put("formResourceKey", taskFormData.getFormKey());
			}
			ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) m_pe.getRepositoryService()).getDeployedProcessDefinition(task.getProcessDefinitionId());
			String namespace = pde.getKey().substring(0, pde.getKey().indexOf(NAMESPACE_DELIMITER));
			taskResponse.put("processName", pde.getName());
			taskResponse.put("processTenantId", namespace);
			taskResponse.put("processDefinitionKey", pde.getKey());
			taskResponse.put("processNamespace", namespace);
			responseList.add(taskResponse);
		}
		Collections.sort( responseList, new ListMapComparator());
		return responseList;
	}
	private class ListMapComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			return compare( (Map<String,Object>) o1, (Map<String,Object>) o2);
		}
		public int compare(Map<String,Object> m1, Map<String,Object> m2) {
			Long starttime1 = (Long)m1.get("_createTime");
			Long starttime2 = (Long)m2.get("_createTime");
			return starttime1.compareTo(starttime2);
		}
	}
}
