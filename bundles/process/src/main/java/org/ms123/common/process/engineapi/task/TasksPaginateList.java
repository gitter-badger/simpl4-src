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
