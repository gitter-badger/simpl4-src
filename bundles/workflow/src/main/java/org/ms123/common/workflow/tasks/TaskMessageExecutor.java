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
import flexjson.*;

@SuppressWarnings({ "unchecked", "deprecation" })
public class TaskMessageExecutor extends TaskBaseExecutor implements JavaDelegate {

	protected JSONDeserializer ds = new JSONDeserializer();
	private Expression processcriteria;
	private Expression messagename;
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

			String messageName = messagename.getValue(execution).toString();
			log("TaskMessageExecutor(variables):" + variables);
			log("TaskMessageExecutor(processcriteria):" + processCriteria);
			log("TaskMessageExecutor(messageName):" + messageName);
			List<ProcessInstance> pl = getProcessInstances(tc, processCriteria, true);
			doSendMessageEvent(tc, pl, variables, messageName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void doSendMessageEvent(TaskContext tc, List<ProcessInstance> processInstanceList, Map<String, Object> variables, String messageName) {
		log("processInstanceList:" + processInstanceList);
		log("messageName:" + messageName);
		RuntimeService runtimeService = tc.getPE().getRuntimeService();
		if (processInstanceList != null) {
			for (ProcessInstance pi : processInstanceList) {
				log("PI:" + pi.getId());
				Execution execution = runtimeService.createExecutionQuery().executionId(pi.getId()).messageEventSubscriptionName(messageName).singleResult();
				log("\tExecution:" + execution);
				if (execution != null) {
					log("doSendMessageEvent:" + messageName + "/" + execution.getId());
					if (variables == null) {
						runtimeService.messageEventReceived(messageName, execution.getId());
					} else {
						runtimeService.messageEventReceived(messageName, execution.getId(), variables);
					}
				} else {
					log("doSendMessageEvent.message(" + messageName + ") not found in process:" + pi.getId());
					List<Execution> execs = runtimeService.createExecutionQuery().messageEventSubscriptionName(messageName).list();
				}
			}
		}
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

