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
import org.activiti.engine.impl.cmd.ExecuteJobsCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.*;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.runtime.Job;
import org.ms123.common.system.thread.ThreadContext;
import org.ms123.common.permission.api.PermissionService;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_JOB_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;


/**
 */
@SuppressWarnings("unchecked")
public class ShiroExecuteJobsRunnable implements Runnable {

  private final List<String> jobIds;
  private final JobExecutor jobExecutor;
	private Map<String,String> info;
	private ProcessEngine m_pe;
	private EventAdmin m_eventAdmin;
  
  public ShiroExecuteJobsRunnable(JobExecutor jobExecutor, Map<String,String> info, List<String> jobIds) {
    this.jobExecutor = jobExecutor;
    this.jobIds = jobIds;
    this.info = info;
  }

  public void run() {
    final MultipleJobsExecutorContext jobExecutorContext = new MultipleJobsExecutorContext();
    final List<String> currentProcessorJobQueue = jobExecutorContext.getCurrentProcessorJobQueue();
    final CommandExecutor commandExecutor = jobExecutor.getCommandExecutor();
		PermissionService ps = ((ShiroJobExecutor)jobExecutor).getPermissionService();
		m_pe = ((ShiroJobExecutor)jobExecutor).getProcessEngine();
		m_eventAdmin = ((ShiroJobExecutor)jobExecutor).getEventAdmin();

    currentProcessorJobQueue.addAll(jobIds);
		log("ShiroExecuteJobsRunnable.start:"+jobIds);
    Context.setJobExecutorContext(jobExecutorContext);
		String ns = info.get("namespace");
		ThreadContext.loadThreadContext(ns, info.get("user"));
		ps.loginInternal(ns);
		String jobId = null;
    try {
      while (!currentProcessorJobQueue.isEmpty()) {
				jobId = currentProcessorJobQueue.remove(0);
				setRetries(commandExecutor, jobId, 1); //@@@MS Problems with Timer cycles
        commandExecutor.execute(new ExecuteJobsCmd(jobId));
      }      
		} catch( Exception e){
		log("createLogEntry.ShiroExecuteJobsRunnable");
			createLogEntry(jobId,ns,e);
			if( e instanceof RuntimeException){
				throw (RuntimeException)e;
			}else{
				throw new RuntimeException(e);
			}
    } finally {
      Context.removeJobExecutorContext();
			log("ShiroExecuteJobsRunnable.finish");
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

	private void createLogEntry(String jobId, String namespace, Exception e){
		ManagementService ms = m_pe.getManagementService();
		Job job = ms.createJobQuery().jobId(jobIds.get(0)).singleResult();
		Map props = new HashMap();
		props.put("namespace", namespace);
		props.put(HISTORY_TYPE, HISTORY_ACTIVITI_JOB_EXCEPTION);
		String key = namespace +"/"+getName(job.getProcessDefinitionId())+"/"+job.getProcessInstanceId();
		props.put(HISTORY_KEY, key);
		log("props:" + props);
		Throwable rc = getRootCause(e);
		props.put(HISTORY_MSG, getStackTrace(rc != null ? rc : e));
		m_eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}
	private String getName(String id){
		int ind = id.indexOf(":");
		if( ind != -1){
			return id.substring(0,ind);
		}
		return id;
	}
	private void log(String message) {
		m_logger.info(message);
		System.err.println(message);
	}
	private static final org.slf4j.Logger m_logger = org.slf4j.LoggerFactory.getLogger(ShiroExecuteJobsRunnable.class);
}
