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
package org.ms123.common.process.engineapi.process;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.process.engineapi.BaseResource;
import org.ms123.common.process.engineapi.Util;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
public class ProcessInstanceResource extends BaseResource {

	private String m_processInstanceId;
	private String m_reason;
	private Map m_activityNameIdMap = new HashMap();

	public ProcessInstanceResource(ProcessService ps, String processInstanceId) {
		super(ps, null);
		m_processInstanceId = processInstanceId;
	}
	public ProcessInstanceResource(ProcessService ps, String processInstanceId, String reason) {
		super(ps, null);
		m_processInstanceId = processInstanceId;
		m_reason = reason;
	}

	public Map getProcessInstance() {
		String processInstanceId = m_processInstanceId;
		HistoricProcessInstance instance = getPE().getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		if (instance == null) {
			throw new RuntimeException("Process instance not found for id " + processInstanceId);
		}
		Map<String, Object> responseJSON = new HashMap();
		responseJSON.put("processInstanceId", instance.getId());
		if (instance.getBusinessKey() != null) {
			responseJSON.put("businessKey", instance.getBusinessKey());
		} else {
			responseJSON.put("businessKey", null);
		}
		responseJSON.put("processDefinitionId", instance.getProcessDefinitionId());
		responseJSON.put("startTime", instance.getStartTime().getTime());
		responseJSON.put("startActivityId", instance.getStartActivityId());
		if (instance.getStartUserId() != null) {
			responseJSON.put("startUserId", instance.getStartUserId());
		} else {
			responseJSON.put("startUserId", null);
		}
		if (instance.getEndTime() == null) {
			responseJSON.put("completed", false);
		} else {
			responseJSON.put("completed", true);
			responseJSON.put("endTime", instance.getEndTime().getTime());
			responseJSON.put("endActivityId", instance.getEndActivityId());
			responseJSON.put("duration", instance.getDurationInMillis());
		}
		addTaskList(processInstanceId, responseJSON);
		addActivityList(processInstanceId, responseJSON);
		addVariableList(processInstanceId, responseJSON);
		return responseJSON;
	}

	public Map deleteProcessInstance() {
		String processInstanceId = m_processInstanceId;
		getPE().getRuntimeService().deleteProcessInstance(processInstanceId, m_reason != null ? m_reason : "REST API");
		getPE().getHistoryService().deleteHistoricProcessInstance(processInstanceId);
		Map successNode = new HashMap();
		successNode.put("success", true);
		return successNode;
	}

	private void addTaskList(String processInstanceId, Map<String, Object> responseJSON) {
		List<HistoricTaskInstance> taskList = getPE().getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
		if (taskList != null && taskList.size() > 0) {
			ArrayList tasksJSON = new ArrayList();
			responseJSON.put("tasks", tasksJSON);
			for (HistoricTaskInstance historicTaskInstance : taskList) {
				Map<String, Object> taskJSON = new HashMap();
				taskJSON.put("taskId", historicTaskInstance.getId());
				taskJSON.put("taskDefinitionKey", historicTaskInstance.getTaskDefinitionKey());
				if (historicTaskInstance.getName() != null) {
					taskJSON.put("taskName", historicTaskInstance.getName());
				} else {
					taskJSON.put("taskName", null);
				}
				if (historicTaskInstance.getDescription() != null) {
					taskJSON.put("description", historicTaskInstance.getDescription());
				} else {
					taskJSON.put("description", null);
				}
				if (historicTaskInstance.getOwner() != null) {
					taskJSON.put("owner", historicTaskInstance.getOwner());
				} else {
					taskJSON.put("owner", null);
				}
				if (historicTaskInstance.getAssignee() != null) {
					taskJSON.put("assignee", historicTaskInstance.getAssignee());
				} else {
					taskJSON.put("assignee", null);
				}
				taskJSON.put("startTime", historicTaskInstance.getStartTime().getTime());
				if (historicTaskInstance.getDueDate() != null) {
					taskJSON.put("dueDate", historicTaskInstance.getDueDate().getTime());
				} else {
					taskJSON.put("dueDate", null);
				}
				if (historicTaskInstance.getEndTime() == null) {
					taskJSON.put("completed", false);
				} else {
					taskJSON.put("completed", true);
					taskJSON.put("endTime", historicTaskInstance.getEndTime().getTime());
					taskJSON.put("duration", historicTaskInstance.getDurationInMillis());
				}
				tasksJSON.add(taskJSON);
			}
		}
	}

	private void addActivityList(String processInstanceId, Map<String, Object> responseJSON) {
		List<HistoricActivityInstance> activityList = getPE().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
		if (activityList != null && activityList.size() > 0) {
			ArrayList activitiesJSON = new ArrayList();
			responseJSON.put("activities", activitiesJSON);
			for (HistoricActivityInstance historicActivityInstance : activityList) {
				Map<String, Object> activityJSON = new HashMap();
				activityJSON.put("activityId", historicActivityInstance.getActivityId());
				activityJSON.put("executionId", historicActivityInstance.getExecutionId());
				if (historicActivityInstance.getActivityId() != null) {
					activityJSON.put("activityName", historicActivityInstance.getActivityId());
				} else {
					activityJSON.put("activityName", null);
				}
				activityJSON.put("activityType", historicActivityInstance.getActivityType());
				activityJSON.put("taskId", historicActivityInstance.getTaskId());
				activityJSON.put("assignee", historicActivityInstance.getAssignee());
				activityJSON.put("id", historicActivityInstance.getId());
				activityJSON.put("startTime", historicActivityInstance.getStartTime().getTime());
				if (historicActivityInstance.getEndTime() == null) {
					activityJSON.put("completed", false);
				} else {
					activityJSON.put("completed", true);
					activityJSON.put("endTime", historicActivityInstance.getEndTime().getTime());
					activityJSON.put("duration", historicActivityInstance.getDurationInMillis());
				}
				activitiesJSON.add(activityJSON);
				m_activityNameIdMap.put(historicActivityInstance.getExecutionId(), historicActivityInstance.getActivityId());
			}
			Collections.sort( activitiesJSON, new ListMapComparator());
		}
	}

	private class ListMapComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			return compare( (Map<String,Object>) o1, (Map<String,Object>) o2);
		}
		public int compare(Map<String,Object> m1, Map<String,Object> m2) {
			Long starttime1 = (Long)m1.get("startTime");
			Long starttime2 = (Long)m2.get("startTime");
			return starttime1.compareTo(starttime2);
		}
	}
	private void addVariableList(String processInstanceId, Map<String, Object> responseJSON) {
		List<HistoricVariableInstance> variableList = getPE().getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
		info(this,"variableList:"+variableList);
		info(this,"m_activityNameIdMap:"+m_activityNameIdMap);
		if( variableList != null){
			ArrayList variablesJSON = new ArrayList();
			responseJSON.put("variables", variablesJSON);
			for( HistoricVariableInstance vi : variableList){
				String name = vi.getName();
				Object value = vi.getValue();
				Map<String, Object> variableJSON = new HashMap();
				variableJSON.put("variableName", name);
				if (value != null) {
					if (value instanceof Boolean) {
						variableJSON.put("variableValue", (Boolean) value);
					} else if (value instanceof Long) {
						variableJSON.put("variableValue", (Long) value);
					} else if (value instanceof Double) {
						variableJSON.put("variableValue", (Double) value);
					} else if (value instanceof Float) {
						variableJSON.put("variableValue", (Float) value);
					} else if (value instanceof Integer) {
						variableJSON.put("variableValue", (Integer) value);
					} else {
						variableJSON.put("variableValue", value);
					}
				} else {
					variableJSON.put("variableValue", null);
				}
				variableJSON.put("variableType", vi.getVariableTypeName());
				variableJSON.put("executionId", vi.getExecutionId());
//				variableJSON.put("revision", vi.getRevision());
				variableJSON.put("taskId", vi.getTaskId());
				variableJSON.put("activityInstanceId", vi.getActivityInstanceId());
				//variableJSON.put("time", vi.getTime().getTime());
				if( m_activityNameIdMap.get(vi.getActivityInstanceId()) != null){
					variableJSON.put("activityName", m_activityNameIdMap.get(vi.getActivityInstanceId()));
				}else if ( vi.getActivityInstanceId() != null){
					variableJSON.put("activityName", vi.getActivityInstanceId());
				}else if ( vi.getTaskId() != null){
					variableJSON.put("activityName", vi.getTaskId());
				}else{
					variableJSON.put("activityName", vi.getActivityInstanceId());
				}
				variablesJSON.add(variableJSON);
			}
		}
		List<HistoricDetail> historyVariableList = getPE().getHistoryService().createHistoricDetailQuery().processInstanceId(processInstanceId).variableUpdates().orderByTime().asc().list();
		if (historyVariableList != null && historyVariableList.size() > 0) {
			ArrayList variablesJSON = new ArrayList();
			responseJSON.put("historyVariables", variablesJSON);
			for (HistoricDetail historicDetail : historyVariableList) {
				HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) historicDetail;
				Map<String, Object> variableJSON = new HashMap();
				variableJSON.put("variableName", variableUpdate.getVariableName());
				if (variableUpdate.getValue() != null) {
					if (variableUpdate.getValue() instanceof Boolean) {
						variableJSON.put("variableValue", (Boolean) variableUpdate.getValue());
					} else if (variableUpdate.getValue() instanceof Long) {
						variableJSON.put("variableValue", (Long) variableUpdate.getValue());
					} else if (variableUpdate.getValue() instanceof Double) {
						variableJSON.put("variableValue", (Double) variableUpdate.getValue());
					} else if (variableUpdate.getValue() instanceof Float) {
						variableJSON.put("variableValue", (Float) variableUpdate.getValue());
					} else if (variableUpdate.getValue() instanceof Integer) {
						variableJSON.put("variableValue", (Integer) variableUpdate.getValue());
					} else {
						variableJSON.put("variableValue", variableUpdate.getValue());
					}
				} else {
					variableJSON.put("variableValue", null);
				}
				variableJSON.put("variableType", variableUpdate.getVariableTypeName());
				variableJSON.put("revision", variableUpdate.getRevision());
				variableJSON.put("taskId", variableUpdate.getTaskId());
				variableJSON.put("activityInstanceId", variableUpdate.getActivityInstanceId());
				variableJSON.put("time", variableUpdate.getTime().getTime());
				if( m_activityNameIdMap.get(variableUpdate.getActivityInstanceId()) != null){
					variableJSON.put("activityName", m_activityNameIdMap.get(variableUpdate.getActivityInstanceId())+"("+variableUpdate.getActivityInstanceId()+")");
				}else if ( variableUpdate.getActivityInstanceId() != null){
					variableJSON.put("activityName", variableUpdate.getActivityInstanceId());
				}else if ( variableUpdate.getTaskId() != null){
					variableJSON.put("activityName", variableUpdate.getTaskId());
				}else{
					variableJSON.put("activityName", variableUpdate.getActivityInstanceId());
				}
				variablesJSON.add(variableJSON);
			}
		}
	}
}
