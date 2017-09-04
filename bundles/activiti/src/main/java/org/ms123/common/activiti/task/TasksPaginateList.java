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
package org.ms123.common.activiti.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Task;
import org.activiti.engine.ProcessEngine;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.activiti.BaseResource;
import org.ms123.common.activiti.Util;
import org.ms123.common.activiti.AbstractPaginateList;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.RepositoryServiceImpl;

/**
 */
@SuppressWarnings("unchecked")
public class TasksPaginateList extends AbstractPaginateList {

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
			taskResponse.put("processName", pde.getName());
			taskResponse.put("processTenantId", pde.getTenantId());
			responseList.add(taskResponse);
		}
		return responseList;
	}
}
