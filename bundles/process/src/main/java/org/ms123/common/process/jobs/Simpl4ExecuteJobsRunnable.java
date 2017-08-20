/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014,2017] [Manfred Sattler] <manfred@ms123.org>
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ExecutionContext;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.*;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.runtime.Job;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.system.thread.ThreadContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_JOB_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;
import static com.jcabi.log.Logger.info;

/**
 */
@SuppressWarnings("unchecked")
public class Simpl4ExecuteJobsRunnable implements Runnable {

	private final List<String> jobIds;
	private final JobExecutor jobExecutor;
	private Map<String, String> info;
	private ProcessEngine m_pe;
	private EventAdmin m_eventAdmin;

	public Simpl4ExecuteJobsRunnable(JobExecutor jobExecutor, Map<String, String> info, List<String> jobIds) {
		this.jobExecutor = jobExecutor;
		this.jobIds = jobIds;
		this.info = info;
	}

	public void run() {
		final JobExecutorContext jobExecutorContext = new JobExecutorContext();
		final List<String> currentProcessorJobQueue = jobExecutorContext.getCurrentProcessorJobQueue();
		final CommandExecutor commandExecutor = jobExecutor.getCommandExecutor();
		PermissionService ps = ((Simpl4JobExecutor) jobExecutor).getPermissionService();
		m_pe = ((Simpl4JobExecutor) jobExecutor).getProcessEngine();
		m_eventAdmin = ((Simpl4JobExecutor) jobExecutor).getEventAdmin();

		currentProcessorJobQueue.addAll(jobIds);
		info(this, "Simpl4ExecuteJobsRunnable.start:" + jobIds);
		Context.setJobExecutorContext(jobExecutorContext);
		String ns = info.get("namespace");
		ThreadContext.loadThreadContext(ns, info.get("user"));
		ps.loginInternal(ns);
		String jobId = null;
		try {
			while (!currentProcessorJobQueue.isEmpty()) {
				jobId = currentProcessorJobQueue.remove(0);
				setRetries(commandExecutor, jobId, 1); //@@@MS Problems with Timer cycles
				commandExecutor.execute(new ExecuteJobsCmd(jobId, null)); //@@@MS
			}
		} catch (Exception e) {
			info(this, "createLogEntry.Simpl4ExecuteJobsRunnable");
			createLogEntry(jobId, ns, e);
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		} finally {
			Context.removeJobExecutorContext();
			info(this, "Simpl4ExecuteJobsRunnable.finish");
		}
	}

	private void setRetries(CommandExecutor commandExecutor, final String jobId, final int retries) {
		ManagementService ms = m_pe.getManagementService();
		final Job job = ms.createJobQuery().jobId(jobId).singleResult();
		commandExecutor.execute(new Command<Void>() {

			public Void execute(CommandContext commandContext) {
				JobEntity jobEntity = commandContext.getDbSqlSession().selectById(JobEntity.class, job.getId());
				jobEntity.setRetries(retries);
				return null;
			}

		});
	}

	private void createLogEntry(String jobId, String namespace, Exception e) {
		ManagementService ms = m_pe.getManagementService();
		Job job = ms.createJobQuery().jobId(jobIds.get(0)).singleResult();
		Map props = new HashMap();
		props.put("namespace", namespace);
		props.put(HISTORY_TYPE, HISTORY_ACTIVITI_JOB_EXCEPTION);
		String key = namespace + "/" + getName(job.getProcessDefinitionId()) + "/" + job.getProcessInstanceId();
		props.put(HISTORY_KEY, key);
		info(this, "props:" + props);
		Throwable rc = getRootCause(e);
		props.put(HISTORY_MSG, getStackTrace(rc != null ? rc : e));
		m_eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}

	private String getName(String id) {
		int ind = id.indexOf(":");
		if (ind != -1) {
			return id.substring(0, ind);
		}
		return id;
	}
}

