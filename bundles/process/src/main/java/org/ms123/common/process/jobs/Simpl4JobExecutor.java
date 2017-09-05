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
import org.ms123.common.process.api.ProcessService;
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

	ProcessEngine rootProcessEngine;
	ProcessService processService;

	public Simpl4JobExecutor() {
	}

	public PermissionService getPermissionService() {
		return (PermissionService) ((ProcessEngineImpl) this.rootProcessEngine).getProcessEngineConfiguration().getBeans().get(PermissionService.PERMISSION_SERVICE);
	}

	public EventAdmin getEventAdmin() {
		return (EventAdmin) ((ProcessEngineImpl) this.rootProcessEngine).getProcessEngineConfiguration().getBeans().get("eventAdmin");
	}

	public void setProcessEngine(ProcessEngine pe) {
		registerProcessEngine((ProcessEngineImpl) pe);
		this.rootProcessEngine = pe;
	}

	public void setProcessService(ProcessService ps) {
		this.processService = ps;
	}

	@Override
	public List<ProcessEngineImpl> getProcessEngines() {
		info(this, "Simpl4JobExecutor.getProcessEngines");
		return processEngines;
	}

	@Override
	public void executeJobs(List<String> jobIds, ProcessEngineImpl tenantProcessEngine) {
		ManagementService ms = tenantProcessEngine.getManagementService();
		Job job = ms.createJobQuery().jobId(jobIds.get(0)).singleResult();

		String pdKey = job.getProcessDefinitionKey();
		String namespace = pdKey.substring(0, pdKey.indexOf(NAMESPACE_DELIMITER));
		info(this, "Simpl4JobExecutor.executeJobs(" + tenantProcessEngine.getName() + "):" + jobIds + "/" + job.getProcessInstanceId() + "/" + job.getProcessDefinitionId() + "/" + namespace);
		Map<String, String> info = getInfo(tenantProcessEngine, job.getProcessInstanceId(), job.getProcessDefinitionId(), namespace);
		try {
			threadPoolExecutor.execute(new Simpl4ExecuteJobsRunnable(jobIds, info, tenantProcessEngine));
		} catch (RejectedExecutionException e) {
			logRejectedExecution(tenantProcessEngine, jobIds.size());
			rejectedJobsHandler.jobsRejected(jobIds, null, this); 
		}
	}

	protected Map getInfo(ProcessEngineImpl tenantProcessEngine, String processInstanceId, String processDefinitionId, String tenantId) {
		Map<String, String> info = new HashMap();
		if (processInstanceId != null) {
			ProcessInstance processInstance = tenantProcessEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			Map<String, Object> vars = tenantProcessEngine.getRuntimeService().getVariables(processInstanceId);
			info.put("tenant", tenantProcessEngine.getName());
			info.put("user", (String) vars.get("__currentUser"));
			if (processInstance == null) {
				HistoricProcessInstance instance = tenantProcessEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
				if (instance == null) {
					throw new RuntimeException("Simpl4JobExecutor.getInfo:processInstance not found:" + processInstanceId);
				}
				processDefinitionId = instance.getProcessDefinitionId();
			} else {
				processDefinitionId = processInstance.getProcessDefinitionId();
			}
		} else {
			info.put("user", "admin");
			info.put("tenant", "admin");
		}
		RepositoryService repositoryService = tenantProcessEngine.getRepositoryService();
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
		String namespace = pd.getKey().substring(0, pd.getKey().indexOf(NAMESPACE_DELIMITER));
		info.put("namespace", namespace);
		return info;
	}
}

