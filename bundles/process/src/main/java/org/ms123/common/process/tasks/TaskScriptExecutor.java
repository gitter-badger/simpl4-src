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
package org.ms123.common.process.tasks;

import flexjson.*;
import java.math.BigInteger;
import java.util.*;
import org.apache.commons.beanutils.*;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.ms123.common.process.tasks.GroovyTaskDsl;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.info;

@SuppressWarnings("unchecked")
public class TaskScriptExecutor extends TaskBaseExecutor implements JavaDelegate {

	private Expression script;

	public TaskScriptExecutor() {
	}

	@Override
	public void execute(DelegateExecution execution) {
		final TaskContext tc = new TaskContext(execution);
		if (script == null) {
			return;
		}
		tc.setScript(script.getValue(execution).toString());

		_execute(tc, null);
	}

	public void execute(String namespace, String processDefinitionKey, String pid, String script, final Map addVars, VariableScope variableScope, String hint) {
		if (script == null) {
			return;
		}
		final TaskContext tc = new TaskContext();
		tc.setNamespace(namespace);
		tc.setProcessDefinitionKey(processDefinitionKey);
		tc.setHint(hint);
		tc.setPid(pid);
		tc.setScript(script);
		tc.setExecution(variableScope);

		_execute(tc, addVars);
	}

	private void _execute(TaskContext tc, Map addVars) {
		debug("TaskScriptExecutor._execute:" + tc.getScript());
		printInfo(tc);
		Map<String, Set<String>> scriptVars = GroovyVariablesVisitor.getVariables(tc.getScript());

		debug("Bound variables:" + scriptVars.get("bound"));
		debug("UnBound variables:" + scriptVars.get("unbound"));
		Set<String> unboundVars = scriptVars.get("unbound");
		Map<String, BigInteger> unboundChecksums = new HashMap<String, BigInteger>();
		for (String var : unboundVars) {
			Object val = tc.getExecution().getVariable(var);
			unboundChecksums.put(var, checksum(val));
		}

		showVariablenNames(tc);
		Map<String, Object> vars = new HashMap(tc.getExecution().getVariables());
		if (addVars != null) {
			vars.putAll(addVars);
		}
		Map<String, Object> lvars = new HashMap();
		Map<String, Object> gvars = new HashMap();
		vars.put("lvars", lvars);
		vars.put("gvars", gvars);
		vars.put("execution", tc.getExecution());
		GroovyTaskDsl dsl = getGroovyTaskDsl(tc, null, vars);
		Object ret = null;
		try {
			ret = dsl.eval(tc.getScript());
			debug("gvars:" + gvars);
			debug("bindingVariables:" + dsl.getVariables());
			tc.getExecution().setVariables(gvars);
			tc.getExecution().setVariablesLocal(lvars);
			for (String varName : scriptVars.get("unbound")) {
				if (!"gvars".equals(varName) && !"lvars".equals(varName) && dsl.hasVariable(varName)) {
					Object value = dsl.getVariable(varName);
					BigInteger dValueChecksum = checksum(value);
					log("\tVariable(" + varName + "):" + value + " -> " + dValueChecksum + " , " + unboundChecksums.get(varName));
					if (!dValueChecksum.equals(unboundChecksums.get(varName))) {
						log("\t\tSettingProcessVariable:" + varName + " -> " + value);
						tc.getExecution().setVariable(varName, value);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
}

