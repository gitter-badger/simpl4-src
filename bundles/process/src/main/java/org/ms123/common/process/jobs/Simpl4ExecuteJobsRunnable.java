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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.cmd.UnlockJobCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ExecutionContext;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.*;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.runtime.Job;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.system.thread.ThreadContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_JOB_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;

import java.util.List;

/**
 */
@SuppressWarnings("unchecked")
public class Simpl4ExecuteJobsRunnable implements Runnable {

	private static final JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;
	private final List<String> jobIds;
	private final JobExecutor jobExecutor;
	private Map<String, String> info;
	private ProcessEngineImpl processEngine;
	private EventAdmin eventAdmin;

	public Simpl4ExecuteJobsRunnable(List<String> jobIds, Map<String, String> info, ProcessEngineImpl processEngine) {
		this.jobIds = jobIds;
		this.info = info;
		this.processEngine = processEngine;
		this.jobExecutor = processEngine.getProcessEngineConfiguration().getJobExecutor();
	}

	public void run() {
		PermissionService ps = ((Simpl4JobExecutor) jobExecutor).getPermissionService();
		this.eventAdmin = ((Simpl4JobExecutor) jobExecutor).getEventAdmin();
		String ns = this.info.get("namespace");
		ThreadContext.loadThreadContext(ns, this.info.get("tenant"));
		ps.loginInternal(ns);

		final JobExecutorContext jobExecutorContext = new JobExecutorContext();

		final List<String> currentProcessorJobQueue = jobExecutorContext.getCurrentProcessorJobQueue();
		CommandExecutor commandExecutor = processEngine.getProcessEngineConfiguration().getCommandExecutorTxRequired();

		info(this, "Simpl4ExecuteJobsRunnable.start("+jobIds+"):" + this.info);
		currentProcessorJobQueue.addAll(jobIds);

		Context.setJobExecutorContext(jobExecutorContext);
		try {
			while (!currentProcessorJobQueue.isEmpty()) {

				String nextJobId = currentProcessorJobQueue.remove(0);
				if (jobExecutor.isActive()) {
					try {
						executeJob(nextJobId, commandExecutor);
					} catch (Throwable t) {
						info(this, "Simpl4ExecuteJobsRunnable.createExceptionLogEntry");
						createExceptionLogEntry(nextJobId, ns, t);
						LOG.exceptionWhileExecutingJob(nextJobId, t);
					}
				} else {
					try {
						unlockJob(nextJobId, commandExecutor);
					} catch (Throwable t) {
						LOG.exceptionWhileUnlockingJob(nextJobId, t);
					}

				}
			}

			jobExecutor.jobWasAdded();

		} finally {
			Context.removeJobExecutorContext();
			info(this, "Simpl4ExecuteJobsRunnable.finish:"+this.info);
		}
	}

	protected void executeJob(String nextJobId, CommandExecutor commandExecutor) {
		ExecuteJobHelper.executeJob(nextJobId, commandExecutor);
	}

	protected void unlockJob(String nextJobId, CommandExecutor commandExecutor) {
		commandExecutor.execute(new UnlockJobCmd(nextJobId));
	}

	private void createExceptionLogEntry(String jobId, String namespace, Throwable e) {
		ManagementService ms = this.processEngine.getManagementService();
		Job job = ms.createJobQuery().jobId(jobIds.get(0)).singleResult();
		Map props = new HashMap();
		props.put("namespace", namespace);
		props.put(HISTORY_TYPE, HISTORY_ACTIVITI_JOB_EXCEPTION);
		String key = namespace + "/" + getName(job.getProcessDefinitionId()) + "/" + job.getProcessInstanceId();
		props.put(HISTORY_KEY, key);
		Throwable rc = getRootCause(e);
		props.put(HISTORY_MSG, getStackTrace(rc != null ? rc : e));
		this.eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}

	private String getName(String id) {
		int ind = id.indexOf(":");
		if (ind != -1) {
			return id.substring(0, ind);
		}
		return id;
	}
}

