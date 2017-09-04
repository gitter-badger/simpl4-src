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
package org.ms123.common.process.jobs;

import java.util.concurrent.RejectedExecutionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.history.*;
import org.camunda.bpm.engine.impl.jobexecutor.*;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.runtime.*;
import org.camunda.bpm.engine.runtime.Job;
import org.ms123.common.permission.api.PermissionService;
import org.osgi.service.event.EventAdmin;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import static com.jcabi.log.Logger.info;

/**
 * 
 */
@SuppressWarnings("unchecked")
public class Simpl4JobExecutor extends DefaultJobExecutor implements org.ms123.common.process.Constants {

	ProcessEngine m_pe;
	Map m_beans;

	public Simpl4JobExecutor(Map beans) {
		m_beans = beans;
	}

	public PermissionService getPermissionService() {
		return (PermissionService) m_beans.get(PermissionService.PERMISSION_SERVICE);
	}

	public EventAdmin getEventAdmin() {
		return (EventAdmin) m_beans.get("eventAdmin");
	}

	public void setProcessEngine(ProcessEngine pe) {
		registerProcessEngine((ProcessEngineImpl)pe);
		m_pe = pe;
	}

	public ProcessEngine getProcessEngine() {
		return m_pe;
	}

	@Override
	public void executeJobs(List<String> jobIds, ProcessEngineImpl pimpl) {
		ManagementService ms = m_pe.getManagementService();
		Job job = ms.createJobQuery().jobId(jobIds.get(0)).singleResult();

		String pdKey = job.getProcessDefinitionKey();
		String namespace = pdKey.substring(0, pdKey.indexOf(NAMESPACE_DELIMITER));
		info(this, "------>executeJobs:" + jobIds + "/" + job.getProcessInstanceId() + "/" + job.getProcessDefinitionId() + "/" + namespace);
		System.err.println("------>executeJobs:" + jobIds + "/" + job.getProcessInstanceId() + "/" + job.getProcessDefinitionId() + "/" + namespace);
		Map<String, String> info = getInfo(job.getProcessInstanceId(), job.getProcessDefinitionId(), namespace);
		try {
			threadPoolExecutor.execute(new Simpl4ExecuteJobsRunnable(this, info, jobIds));
		} catch (RejectedExecutionException e) {
			rejectedJobsHandler.jobsRejected(jobIds, null, this); //@@@MS
		}
	}

	protected Map getInfo(String processInstanceId, String processDefinitionId, String tenantId) {
		Map<String, String> info = new HashMap();
		if (processInstanceId != null) {
			ProcessInstance processInstance = m_pe.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId)./*processInstanceTenantId(tenantId).*/singleResult();
			Map<String, Object> vars = m_pe.getRuntimeService().getVariables(processInstanceId);
			info(this, "getInfo.vars:" + vars);
			info.put("user", (String) vars.get("__currentUser"));
			if (processInstance == null) {
				HistoricProcessInstance instance = m_pe.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId)./*processInstanceTenantId(tenantId).*/singleResult();
				if (instance == null) {
					throw new RuntimeException("Simpl4JobExecutor.getInfo:processInstance not found:" + processInstanceId);
				}
				processDefinitionId = instance.getProcessDefinitionId();
			} else {
				processDefinitionId = processInstance.getProcessDefinitionId();
			}
		} else {
			info.put("user", "admin");
		}
		RepositoryService repositoryService = m_pe.getRepositoryService();
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId)./*processDefinitionTenantId(tenantId).*/singleResult();
		String namespace = pd.getId().substring(0, pd.getId().indexOf(NAMESPACE_DELIMITER));
		info(this, "getInfo.namespace:" + namespace);
		info.put("namespace", namespace);
		return info;
	}
}

