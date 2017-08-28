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

		String pdId = job.getProcessDefinitionId();
		String namespace = pdId.substring(0, pdId.indexOf(NAMESPACE_DELIMITER));
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

