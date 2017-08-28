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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.camunda.bpm.engine.impl.TaskQueryProperty;
import org.camunda.bpm.engine.query.QueryProperty;
import org.camunda.bpm.engine.task.TaskQuery;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.process.engineapi.BaseResource;
import org.ms123.common.process.engineapi.Util;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static com.jcabi.log.Logger.info;

/**
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class TasksResource extends BaseResource {
	private String personalTaskUserId;
	private String ownerTaskUserId;
	private String involvedTaskUserId;
	private String candidateGroupId;
	private String strPriority;
	private String strMinPriority;
	private String strMaxPriority;
	private String strDueDate;
	private String strMinDueDate;
	private String strMaxDueDate;
	private String businessKey;

	Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
	private Map<String, Object> m_queryParams = new HashMap();

	public TasksResource(ProcessService ps, Map<String, Object> listParams, Map<String, Object> queryParams) {
		super(ps, listParams);
		m_queryParams = queryParams;
		properties.put("id", TaskQueryProperty.TASK_ID);
		properties.put("name", TaskQueryProperty.NAME);
		properties.put("description", TaskQueryProperty.DESCRIPTION);
		properties.put("priority", TaskQueryProperty.PRIORITY);
		properties.put("assignee", TaskQueryProperty.ASSIGNEE);
		properties.put("executionId", TaskQueryProperty.EXECUTION_ID);
		properties.put("processInstanceId", TaskQueryProperty.PROCESS_INSTANCE_ID);
	}

	public Map getTasks() {
		String candidateTaskUserId = Util.getString(m_queryParams, "candidate");
		this.personalTaskUserId = Util.getString(m_queryParams, "assignee");
		this.ownerTaskUserId = Util.getString(m_queryParams, "owner");
		this.involvedTaskUserId = Util.getString(m_queryParams, "involved");
		this.candidateGroupId = Util.getString(m_queryParams, "candidate-group");
		this.strPriority = Util.getString(m_queryParams, "priority");
		this.strMinPriority = Util.getString(m_queryParams, "minPriority");
		this.strMaxPriority = Util.getString(m_queryParams, "maxPriority");
		this.strDueDate = Util.getString(m_queryParams, "dueDate");
		this.strMinDueDate = Util.getString(m_queryParams, "minDueDate");
		this.strMaxDueDate = Util.getString(m_queryParams, "maxDueDate");
		this.businessKey = Util.getString(m_queryParams, "businessKey");

		boolean isUserQuery = !isEmpty(this.personalTaskUserId) || !isEmpty(this.ownerTaskUserId) || !isEmpty(this.involvedTaskUserId);

		TaskQuery taskQuery = null;
		if (isUserQuery) {
			taskQuery = buildTaskQuery(null, null);
			info(this,"taskResponse.isUserQuery:true");
		} else if (!isEmpty(candidateTaskUserId)) {
			checkUser(candidateTaskUserId);
			taskQuery = buildTaskQuery(candidateTaskUserId, null);
			if (taskQuery.count() > 0) {
				taskQuery = buildTaskQuery(candidateTaskUserId, null);
				info(this,"taskResponse.candidateTaskUserId("+candidateTaskUserId+"):ok");
			} else {
				List<String> candidateRoles = getUserRoles(candidateTaskUserId);
				String candidateGroupExpression = candidateRoles.size() > 0 ? join(candidateRoles,",") : null;
				if( candidateRoles.size()>0){
					info(this,"taskResponse.candidateGroupExpression:"+candidateGroupExpression);
					taskQuery = buildTaskQuery(null, candidateRoles);
				}else{
					throw new RuntimeException("TasksResource: no valid candidateTaskUserId nor candidateGroupExpression");
				}
			}
		} else if (!isEmpty(this.candidateGroupId)) {
			checkRole(this.candidateGroupId);
			taskQuery = buildTaskQuery(null, null);
			info(this,"taskResponse.candidateGroupId:"+this.candidateGroupId);
		}else{
			throw new RuntimeException("Tasks must be filtered with 'assignee', 'owner', 'involved', 'candidate' or 'candidate-group'");
		}

		Map dataResponse = new TasksPaginateList(this).paginateList(m_listParams, taskQuery, "id", properties);
		return dataResponse;
	}

	private TaskQuery buildTaskQuery(String candidateTaskUserId, List<String> candidateGroups) {
		TaskQuery taskQuery = getPE().getTaskService().createTaskQuery();

		if (!isEmpty(this.personalTaskUserId)) {
			checkUser(this.personalTaskUserId);
			taskQuery.taskAssignee(this.personalTaskUserId);
		} else if (!isEmpty(this.ownerTaskUserId)) {
			checkUser(this.ownerTaskUserId);
			taskQuery.taskOwner(this.ownerTaskUserId);
		} else if (!isEmpty(this.involvedTaskUserId)) {
			checkUser(this.involvedTaskUserId);
			taskQuery.taskInvolvedUser(this.involvedTaskUserId);
		} else if (!isEmpty(candidateTaskUserId)) {
			taskQuery.taskCandidateUser(candidateTaskUserId);
		} else if (candidateGroups!=null && candidateGroups.size()>0) {
			taskQuery.taskCandidateGroupIn(candidateGroups);
		} else if (!isEmpty(this.candidateGroupId)) {
			taskQuery.taskCandidateGroup(this.candidateGroupId);
		}

		String processInstanceId = Util.getString(m_queryParams, "processInstanceId");
		if (processInstanceId != null) {
			taskQuery.processInstanceId(processInstanceId);
		}
		if (this.strPriority != null) {
			taskQuery.taskPriority(Util.parseToInteger(this.strPriority));
		} else if (this.strMinPriority != null) {
			taskQuery.taskMinPriority(Util.parseToInteger(this.strMinPriority));
		} else if (this.strMaxPriority != null) {
			taskQuery.taskMaxPriority(Util.parseToInteger(this.strMaxPriority));
		}
		if (this.strDueDate != null) {
			taskQuery.dueDate(Util.parseToDate(this.strDueDate));
		} else if (this.strMinDueDate != null) {
			taskQuery.dueAfter(Util.parseToDate(this.strMinDueDate));
		} else if (this.strMaxDueDate != null) {
			taskQuery.dueBefore(Util.parseToDate(this.strMaxDueDate));
		}
		if (this.businessKey != null) {
			taskQuery.processInstanceBusinessKey(this.businessKey);
		}
		return taskQuery;
	}
}

