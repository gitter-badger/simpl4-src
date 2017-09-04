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
package org.ms123.common.workflow;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.jobexecutor.*;
import java.util.concurrent.RejectedExecutionException;
import org.ms123.common.permission.api.PermissionService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ManagementService;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.*;
import org.activiti.engine.history.*;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.osgi.service.event.EventAdmin;

/**
 * 
 */
@SuppressWarnings("unchecked")
public class ShiroJobExecutor extends DefaultJobExecutor {

	ProcessEngine m_pe;
	Map m_beans;

	public ShiroJobExecutor(Map  beans) {
		m_beans = beans;
	}

	public PermissionService getPermissionService() {
		return (PermissionService)m_beans.get(PermissionService.PERMISSION_SERVICE);
	}

	public EventAdmin getEventAdmin() {
		return (EventAdmin)m_beans.get("eventAdmin");
	}

	public void setProcessEngine(ProcessEngine pe) {
		m_pe = pe;
	}

	public ProcessEngine getProcessEngine() {
		return m_pe;
	}

	public void executeJobs(List<String> jobIds) {
		ManagementService ms = m_pe.getManagementService();
		Job job = ms.createJobQuery().jobId(jobIds.get(0)).singleResult();
		log("------>executeJobs:" + jobIds+"/"+job.getProcessInstanceId()+"/"+job.getProcessDefinitionId()+"/"+job.getTenantId());
		Map<String,String> info = getInfo(job.getProcessInstanceId(),job.getProcessDefinitionId(),job.getTenantId());
		try {
			threadPoolExecutor.execute(new ShiroExecuteJobsRunnable(this, info, jobIds));
		} catch (RejectedExecutionException e) {
			rejectedJobsHandler.jobsRejected(this, jobIds);
		}
	}

	protected Map getInfo(String processInstanceId,String processDefinitionId, String tenantId) {
		Map<String,String> info = new HashMap();
		if( processInstanceId != null){
			ProcessInstance processInstance = m_pe.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).processInstanceTenantId(tenantId).singleResult();
			Map<String,Object> vars = m_pe.getRuntimeService().getVariables(processInstanceId);
			log("getInfo.vars:"+vars);
			info.put("user",(String)vars.get("__currentUser"));
			if (processInstance == null) {
				HistoricProcessInstance instance = m_pe.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).processInstanceTenantId(tenantId).singleResult();
				if (instance == null) {
					throw new RuntimeException("ShiroJobExecutor.getInfo:processInstance not found:" + processInstanceId);
				}
				processDefinitionId = instance.getProcessDefinitionId();
			} else {
				processDefinitionId = processInstance.getProcessDefinitionId();
			}
		}else{
			info.put("user","admin");
		}
		RepositoryService repositoryService = m_pe.getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).processDefinitionTenantId(tenantId).singleResult();
		log("getInfo.namespace:"+processDefinition.getTenantId());
		info.put("namespace", processDefinition.getTenantId());
		return info;
	}
	private void log(String message) {
		m_logger.info(message);
	}
	private static final org.slf4j.Logger m_logger = org.slf4j.LoggerFactory.getLogger(ShiroJobExecutor.class);
}
