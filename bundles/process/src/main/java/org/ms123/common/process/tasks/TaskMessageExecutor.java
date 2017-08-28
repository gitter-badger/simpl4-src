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
package org.ms123.common.process.tasks;

import java.util.*;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.ms123.common.system.thread.ThreadContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.framework.BundleContext;
import org.apache.camel.util.IntrospectionSupport;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;
import static org.ms123.common.system.history.HistoryService.HISTORY_HINT;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_START_PROCESS_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_JOB_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;
import flexjson.*;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings({ "unchecked", "deprecation" })
public class TaskMessageExecutor extends TaskBaseExecutor implements JavaDelegate,org.ms123.common.process.Constants  {

	protected JSONDeserializer ds = new JSONDeserializer();
	protected JSONSerializer js = new JSONSerializer();
	private Expression processcriteria;
	private Expression messagename;
	private Expression signalname;
	private Expression variablesmapping;

	@Override
	public void execute(DelegateExecution execution) {
		TaskContext tc = new TaskContext(execution);
		showVariablenNames(tc);

		try {
			Map<String, Object> variables = getParams(execution, variablesmapping, "messagevar");
			Object vm = processcriteria.getValue(execution);
			List<Map<String, String>> l = null;
			if (vm instanceof String) {
				l = (List) ds.deserialize((String) vm);
			} else {
				l = (List) vm;
			}
			Map<String, String> processCriteria = new HashMap<String, String>();
			for (Map<String, String> m : l) {
				String name = m.get("name");
				String value = m.get("value");
				processCriteria.put(name, value);
			}

			info(this, "TaskMessageExecutor(variables):" + variables);
			info(this, "TaskMessageExecutor(processcriteria):" + processCriteria);
			List<ProcessInstance> pl = getProcessInstances(tc, processCriteria, false);
			for (ProcessInstance p : pl) {
				Map map = new HashMap();
				IntrospectionSupport.getProperties(p, map, null);
				info(this, "\tprocessInstance:" + map);
			}
			String messageName = null;
			if (messagename != null) {
				messageName = messagename.getValue(execution).toString();
			}
			String signalName = null;
			if (signalname != null) {
				signalName = signalname.getValue(execution).toString();
			}
			info(this, "TaskMessageExecutor(signalName):" + signalName);
			if (!isEmpty(messageName)) {
				doSendMessageEvent(tc, pl, variables, messageName);
			} else if (!isEmpty(signalName)) {
				doSendSignalEvent(tc, pl, variables, signalName);
			} else {
				doSendSignal(tc, pl, variables);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void doSendMessageEvent(TaskContext tc, List<ProcessInstance> processInstanceList, Map<String, Object> variables, String messageName) {
		info(this, "messageName:" + messageName);
		RuntimeService runtimeService = tc.getPE().getRuntimeService();
		if (processInstanceList != null) {
			for (ProcessInstance pi : processInstanceList) {
				info(this, "PI:" + pi.getId());
				Execution execution = runtimeService.createExecutionQuery().executionId(pi.getId()).messageEventSubscriptionName(messageName).singleResult();
				info(this, "\tExecution:(" + pi.getId() + ")" + execution);
				if (execution != null) {
					info(this, "doSendMessageEvent:" + messageName + "/" + execution.getId());
					if (variables == null) {
						runtimeService.messageEventReceived(messageName, execution.getId());
					} else {
						runtimeService.messageEventReceived(messageName, execution.getId(), variables);
					}
				} else {
					info(this, "doSendMessageEvent.message(" + messageName + ") not found in process:" + pi.getId());
				}
			}
		}
	}

	private void doSendSignalEvent(TaskContext tc, List<ProcessInstance> processInstanceList, Map<String, Object> variables, String signalName) {
		RuntimeService runtimeService = tc.getPE().getRuntimeService();
		if (processInstanceList == null) {
			info(this, "doSendSignalEvent:" + signalName);
			if (variables == null) {
				runtimeService.signalEventReceived(signalName);
			} else {
				runtimeService.signalEventReceived(signalName, variables);
			}
		} else {
			for (ProcessInstance pi : processInstanceList) {
				Execution execution = runtimeService.createExecutionQuery().executionId(pi.getId()).signalEventSubscriptionName(signalName).singleResult();
				info(this, "\tExecution(" + pi.getId() + "):" + execution);
				if (execution != null) {
					info(this, "doSendSignalEvent:" + signalName + "/" + execution.getId());
					if (variables == null) {
						runtimeService.signalEventReceived(signalName, execution.getId());
					} else {
						runtimeService.signalEventReceived(signalName, execution.getId(), variables);
					}
				} else {
					info(this, "doSendSignalEvent.signal(" + signalName + ") not found in process:" + pi.getId());
				}
			}
		}
	}

	private void doSendSignal(TaskContext tc, List<ProcessInstance> processInstanceList, Map<String, Object> variables) {
		RuntimeService runtimeService = tc.getPE().getRuntimeService();
		for (ProcessInstance pi : processInstanceList) {
			try {
				info(this, "doSendSignal(eid:" + pi.getId() + ",pid:" + pi.getProcessInstanceId() + "):"+variables);
				if (variables != null) {
					runtimeService.signal(pi.getId(), variables);
				} else {
					runtimeService.signal(pi.getId());
				}
			} catch (Exception e) {
				com.jcabi.log.Logger.error(this, "doSendSignal(eid:" + pi.getId() + ",pid:" + pi.getProcessInstanceId() + "):%[exception]s", e);
				createLogEntry(tc, getProcessDefinition(tc, pi), pi, e);
				throw new RuntimeException("doSendSignal", e);
			}
		}
	}

	private void doSendSignalAsync(TaskContext tc, List<ProcessInstance> processInstanceList, Map<String, Object> variables) {
		for (ProcessInstance pi : processInstanceList) {
			signal(tc, pi, variables);
		}
	}

	private void signal(TaskContext tc, ProcessInstance execution, Map<String, Object> variables) {
		String ns = tc.getNamespace();
		Map<String, Object> pv = variables;
		new SignalThread(tc, ns, getProcessDefinition(tc, execution), execution, pv).start();
	}

	private class SignalThread extends Thread {
		ProcessInstance execution;
		TaskContext tc;
		ProcessDefinition processDefinition;
		Map variables;

		String ns;

		public SignalThread(TaskContext tc, String ns, ProcessDefinition pd, ProcessInstance exec, Map<String, Object> variables) {
			this.execution = exec;
			this.variables = variables;
			this.ns = ns;
			this.tc = tc;
			this.processDefinition = pd;
		}

		public void run() {
			ThreadContext.loadThreadContext(ns, "admin");
			tc.getPermissionService().loginInternal(ns);
			RuntimeService runtimeService = tc.getPE().getRuntimeService();
			while (true) {
				try {
					info(this, "SignalThread.sending signal to -> eid:" + execution.getId() + ",pid:" + execution.getProcessInstanceId() );
					if (this.variables != null) {
						runtimeService.signal(execution.getId(), this.variables);
					} else {
						runtimeService.signal(execution.getId());
					}
				} catch (org.camunda.bpm.engine.OptimisticLockingException e) {
					info(this, "SignalThread:" + e);
					try {
						Thread.sleep(100L);
					} catch (Exception x) {
					}
					continue;
				} catch (Exception e) {
					createLogEntry(this.tc, this.processDefinition, this.execution, e);
					com.jcabi.log.Logger.error(this, "SignalThread(eid:" + execution.getId() + ",pid:" + execution.getProcessInstanceId() + "):%[exception]s", e);
				} finally {
					ThreadContext.getThreadContext().finalize(null);
				}
				break;
			}
		}
	}

	private void createLogEntry(TaskContext tc, ProcessDefinition pd, ProcessInstance pi, Exception e) {
		Map beans = ((ProcessEngineConfigurationImpl) tc.getPE().getProcessEngineConfiguration()).getBeans();
		EventAdmin eventAdmin = (EventAdmin) beans.get("eventAdmin");
		Map props = new HashMap();

		String namespace = pd.getId().substring(0, pd.getId().indexOf(NAMESPACE_DELIMITER));
		String key = namespace + "/" + getProcessName(pd.getId()) + "/" + pi.getId();
		props.put(HISTORY_KEY, key);
		props.put(HISTORY_TYPE, HISTORY_ACTIVITI_JOB_EXCEPTION);
		info(this, "createLogEntry.props:" + props);

		Throwable r = getRootCause(e);
		props.put(HISTORY_MSG, r != null ? getStackTrace(r) : getStackTrace(e));
		eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}

	private String getProcessName(String id) {
		int ind = id.indexOf(":");
		if (ind != -1) {
			return id.substring(0, ind);
		}
		return id;
	}

	private ProcessDefinition getProcessDefinition(TaskContext tc, ProcessInstance processInstance) {
		ProcessDefinition processDefinition = tc.getPE().getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
		if (processDefinition == null) {
			throw new RuntimeException("TaskMessageExecutor:getProcessDefinition:processDefinition not found:" + processInstance);
		}
		String namespace = processDefinition.getId().substring(0, processDefinition.getId().indexOf(NAMESPACE_DELIMITER));
		info(this, "getProcessDefinition:" + processDefinition + "/" + namespace);
		return processDefinition;
	}

}

