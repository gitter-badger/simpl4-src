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

import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.TaskQuery;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.activiti.BaseResource;
import org.ms123.common.activiti.Util;
import org.ms123.common.permission.api.PermissionService;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
public class TasksResource extends BaseResource {

	Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();

	private Map<String, Object> m_queryParams = new HashMap();

	public TasksResource(ActivitiService as, Map<String, Object> listParams, Map<String, Object> queryParams) {
		super(as, listParams);
		m_queryParams = queryParams;
		properties.put("id", TaskQueryProperty.TASK_ID);
		properties.put("name", TaskQueryProperty.NAME);
		properties.put("description", TaskQueryProperty.DESCRIPTION);
		properties.put("priority", TaskQueryProperty.PRIORITY);
		properties.put("assignee", TaskQueryProperty.ASSIGNEE);
		properties.put("executionId", TaskQueryProperty.EXECUTION_ID);
		properties.put("tenantId", TaskQueryProperty.TENANT_ID);
		properties.put("processInstanceId", TaskQueryProperty.PROCESS_INSTANCE_ID);
	}

	public Map getTasks() {
		String personalTaskUserId = Util.getString(m_queryParams, "assignee");
		String ownerTaskUserId = Util.getString(m_queryParams, "owner");
		String involvedTaskUserId = Util.getString(m_queryParams, "involved");
		String candidateTaskUserId = Util.getString(m_queryParams, "candidate");
		String candidateGroupId = Util.getString(m_queryParams, "candidate-group");
		String strPriority = Util.getString(m_queryParams, "priority");
		String strMinPriority = Util.getString(m_queryParams, "minPriority");
		String strMaxPriority = Util.getString(m_queryParams, "maxPriority");
		String strDueDate = Util.getString(m_queryParams, "dueDate");
		String strMinDueDate = Util.getString(m_queryParams, "minDueDate");
		String strMaxDueDate = Util.getString(m_queryParams, "maxDueDate");
		String businessKey = Util.getString(m_queryParams, "businessKey");
		String tenantId = Util.getString(m_queryParams, "tenantId");
		TaskQuery taskQuery = getPE().getTaskService().createTaskQuery();

		if (personalTaskUserId != null) {
			checkUser(personalTaskUserId);
			taskQuery.taskAssignee(personalTaskUserId);
		} else if (ownerTaskUserId != null) {
			checkUser(ownerTaskUserId);
			taskQuery.taskOwner(ownerTaskUserId);
		} else if (involvedTaskUserId != null) {
			checkUser(involvedTaskUserId);
			taskQuery.taskInvolvedUser(involvedTaskUserId);
		} else if (candidateTaskUserId != null) {
			checkUser(candidateTaskUserId);
			taskQuery.taskCandidateUser(candidateTaskUserId);
		} else if (candidateGroupId != null) {
			checkRole(candidateGroupId);
			taskQuery.taskCandidateGroup(candidateGroupId);
		} else {
			throw new RuntimeException("Tasks must be filtered with 'assignee', 'owner', 'involved', 'candidate' or 'candidate-group'");
		}
		String processInstanceId = Util.getString(m_queryParams, "processInstanceId");
		if (processInstanceId != null) {
			taskQuery.processInstanceId(processInstanceId);
		}
		if (strPriority != null) {
			taskQuery.taskPriority(Util.parseToInteger(strPriority));
		} else if (strMinPriority != null) {
			taskQuery.taskMinPriority(Util.parseToInteger(strMinPriority));
		} else if (strMaxPriority != null) {
			taskQuery.taskMaxPriority(Util.parseToInteger(strMaxPriority));
		}
		if (strDueDate != null) {
			taskQuery.dueDate(Util.parseToDate(strDueDate));
		} else if (strMinDueDate != null) {
			taskQuery.dueAfter(Util.parseToDate(strMinDueDate));
		} else if (strMaxDueDate != null) {
			taskQuery.dueBefore(Util.parseToDate(strMaxDueDate));
		}
		if (businessKey != null) {
			taskQuery.processInstanceBusinessKey(businessKey);
		}
		if (tenantId != null) {
			taskQuery.taskTenantId(tenantId);
		}
		Map dataResponse = new TasksPaginateList(this).paginateList(m_listParams, taskQuery, "id", properties);
		return dataResponse;
	}
}
