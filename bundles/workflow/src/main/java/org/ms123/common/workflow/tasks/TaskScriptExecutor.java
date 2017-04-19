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

import flexjson.*;
import java.util.*;
import java.math.BigInteger;
import javax.transaction.UserTransaction;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.apache.commons.beanutils.*;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.workflow.api.WorkflowService;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.workflow.GroovyTaskDsl;
import org.ms123.common.workflow.GroovyVariablesVisitor;
import org.osgi.service.event.EventAdmin;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionStatus;

@SuppressWarnings("unchecked")
public class TaskScriptExecutor extends TaskBaseExecutor implements JavaDelegate {

	private Expression script;
	private boolean m_ownTransaction = false;

	public TaskScriptExecutor() {
		m_js.prettyPrint(true);
	}

	@Override
	public void execute(DelegateExecution execution) {
		final TaskContext tc = new TaskContext(execution);
		if (script == null) {
			return;
		}
		tc.setScript(script.getValue(execution).toString());

		if (m_ownTransaction) {
			TransactionTemplate tt = getTransactionService().getTransactionTemplate(true);
			tt.execute(new TransactionCallback<Object>() {
				public Object doInTransaction(TransactionStatus paramTransactionStatus) {
					_execute(tc, null);
					return null;
				}
			});
		} else {
			_execute(tc, null);
		}
	}

	public void execute(String namespace, String processDefinitionKey, String pid, String script, final Map addVars, VariableScope variableScope, String hint, DataLayer dataLayer, WorkflowService ws) {
		if (script == null) {
			return;
		}
		final TaskContext tc = new TaskContext();
		tc.setTenantId(namespace);
		tc.setProcessDefinitionKey(processDefinitionKey);
		tc.setHint(hint);
		tc.setPid(pid);
		tc.setScript(script);
		tc.setExecution(variableScope);
		setDataLayer(dataLayer);
		setWorkflowService(ws);

		if (m_ownTransaction) {
			TransactionTemplate tt = getTransactionService().getTransactionTemplate(true);
			tt.execute(new TransactionCallback<Object>() {
				public Object doInTransaction(TransactionStatus paramTransactionStatus) {
					_execute(tc, addVars);
					return null;
				}
			});
		} else {
			_execute(tc, addVars);
		}
	}

	private void _execute(TaskContext tc, Map addVars) {
		m_js.prettyPrint(true);
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
		SessionContext sc = getSessionContext(tc);
		Map<String, Object> vars = new HashMap(tc.getExecution().getVariables());
		if (addVars != null) {
			vars.putAll(addVars);
		}
		Map<String, Object> lvars = new HashMap();
		Map<String, Object> gvars = new HashMap();
		vars.put("lvars", lvars);
		vars.put("gvars", gvars);
		vars.put("execution", tc.getExecution());
		GroovyTaskDsl dsl = getGroovyTaskDsl(tc, sc, vars);
		UserTransaction tx = sc.getUserTransaction();
		Object ret = null;
		try {
			debug("transaction.status:" + tx.getStatus() + "/" + org.ms123.common.system.thread.ThreadContext.getThreadContext());
			ret = dsl.eval(tc.getScript());
			debug("TaskScriptExecutor.gvars:" + gvars);
			debug("TaskScriptExecutor.bindingVariables:" + dsl.getVariables());
			tc.getExecution().setVariables(gvars);
			tc.getExecution().setVariablesLocal(lvars);
			for (String varName : scriptVars.get("unbound")) {
				if (!"gvars".equals(varName) && !"lvars".equals(varName) && dsl.hasVariable(varName)) {
					Object value = dsl.getVariable(varName);
					BigInteger dValueChecksum = checksum(value);
					log("\tVAR(" + varName + "):" + value + " -> " + dValueChecksum + " , " + unboundChecksums.get(varName));
					if (!dValueChecksum.equals(unboundChecksums.get(varName))) {
						log("\t\tsettingProcessVariable:" + varName + " -> " + value);
						tc.getExecution().setVariable(varName, value);
					}
				}
			}
			for (Object o : dsl.getCreatedObjects()) {
				log(tc, "createdObject:" + o);
				sc.retrieve(o);
			}
			for (Object o : dsl.getQueriedObjects()) {
				log(tc, "queriedObject:" + o);
				sc.retrieve(o);
			}
		} catch (Exception e) {
			sc.handleException(tx, e);
		} finally {
			sc.handleFinally(tx);
		}
	}
}

