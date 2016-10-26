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
package org.ms123.common.workflow.tasks;

import java.util.*;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.RepositoryService;
import org.ms123.common.system.thread.ThreadContext;
import flexjson.*;

@SuppressWarnings({ "unchecked", "deprecation" })
public class TaskMessageExecutor extends TaskBaseExecutor implements JavaDelegate {

	protected JSONDeserializer ds = new JSONDeserializer();
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
			log("TaskMessageExecutor(vm):" + vm);
			log("TaskMessageExecutor(isString):" + (vm instanceof String));
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

			log("TaskMessageExecutor(variables):" + variables);
			log("TaskMessageExecutor(processcriteria):" + processCriteria);
			List<ProcessInstance> pl = getProcessInstances(tc, processCriteria, false);
			log("TaskMessageExecutor.processInstanceList:" + pl);
			String messageName = null;
			if (messagename != null) {
				messageName = messagename.getValue(execution).toString();
			}
			String signalName = null;
			if (signalname != null) {
				signalName = signalname.getValue(execution).toString();
			}
			log("TaskMessageExecutor(signalName):" + signalName);
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
		log("messageName:" + messageName);
		RuntimeService runtimeService = tc.getPE().getRuntimeService();
		if (processInstanceList != null) {
			for (ProcessInstance pi : processInstanceList) {
				log("PI:" + pi.getId());
				Execution execution = runtimeService.createExecutionQuery().executionId(pi.getId()).messageEventSubscriptionName(messageName).singleResult();
				log("\tExecution:(" + pi.getId() + ")" + execution);
				if (execution != null) {
					log("doSendMessageEvent:" + messageName + "/" + execution.getId());
					if (variables == null) {
						runtimeService.messageEventReceived(messageName, execution.getId());
					} else {
						runtimeService.messageEventReceived(messageName, execution.getId(), variables);
					}
				} else {
					log("doSendMessageEvent.message(" + messageName + ") not found in process:" + pi.getId());
				}
			}
		}
	}

	private void doSendSignalEvent(TaskContext tc, List<ProcessInstance> processInstanceList, Map<String, Object> variables, String signalName) {
		RuntimeService runtimeService = tc.getPE().getRuntimeService();
		if (processInstanceList == null) {
			log("doSendSignalEvent:" + signalName);
			if (variables == null) {
				runtimeService.signalEventReceived(signalName);
			} else {
				runtimeService.signalEventReceived(signalName, variables);
			}
		} else {
			for (ProcessInstance pi : processInstanceList) {
				Execution execution = runtimeService.createExecutionQuery().executionId(pi.getId()).signalEventSubscriptionName(signalName).singleResult();
				log("\tExecution(" + pi.getId() + "):" + execution);
				if (execution != null) {
					log("doSendSignalEvent:" + signalName + "/" + execution.getId());
					if (variables == null) {
						runtimeService.signalEventReceived(signalName, execution.getId());
					} else {
						runtimeService.signalEventReceived(signalName, execution.getId(), variables);
					}
				} else {
					log("doSendSignalEvent.signal(" + signalName + ") not found in process:" + pi.getId());
				}
			}
		}
	}

	private void doSendSignal(TaskContext tc, List<ProcessInstance> processInstanceList, Map<String, Object> variables) {
		for (ProcessInstance pi : processInstanceList) {
			//if( pi.getParentId() != null){
			signal(tc, pi, variables);
			//}else{
			//	log("doSendSignal.processInstance:don't send signal to -> eid:" + pi.getId() + ",pid:"+pi.getProcessInstanceId());
			//}
		}
	}

	private void signal(TaskContext tc, ProcessInstance execution, Map<String, Object> variables) {
		String ns = tc.getTenantId();
		Map<String, Object> pv = variables;
		new SignalThread(tc, ns, getProcessDefinition(tc, execution), execution, pv).start();
	}

	private class SignalThread extends Thread {
		Execution execution;
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
					log("SignalThread.sending signal to -> eid:" + execution.getId() + ",pid:" + execution.getProcessInstanceId() + ",parentId:" + execution.getParentId());
					if (this.variables != null) {
						runtimeService.signal(execution.getId(), this.variables);
					} else {
						runtimeService.signal(execution.getId());
					}
				} catch (org.activiti.engine.ActivitiOptimisticLockingException e) {
					log("SignalThread:" + e);
					try {
						Thread.sleep(100L);
					} catch (Exception x) {
					}
					continue;
				} catch (Exception e) {
					error(this.tc, "SignalThread(eid:" + execution.getId() + ",pid:" + execution.getProcessInstanceId() + ",parentId:" + execution.getParentId() + "):", e);
				} finally {
					ThreadContext.getThreadContext().finalize(null);
				}
				break;
			}
		}
	}

	private ProcessDefinition getProcessDefinition(TaskContext tc, ProcessInstance processInstance) {
		ProcessDefinition processDefinition = tc.getPE().getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
		if (processDefinition == null) {
			throw new RuntimeException("TaskMessageExecutor:getProcessDefinition:processDefinition not found:" + processInstance);
		}
		log("getProcessDefinition:" + processDefinition + "/" + processDefinition.getTenantId());
		return processDefinition;
	}

	private boolean isEmpty(Object s) {
		if (s == null || "".equals(((String) s).trim())) {
			return true;
		}
		return false;
	}

	private String getName(String s) {
		if (s == null) {
			throw new RuntimeException("TaskMessageExecutor.routename is null");
		}
		return s;
	}
}

