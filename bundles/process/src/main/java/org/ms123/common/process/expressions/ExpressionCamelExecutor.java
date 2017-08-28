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
package org.ms123.common.process.expressions;

import flexjson.*;
import java.util.*;
import groovy.lang.*;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.apache.commons.beanutils.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.ms123.common.rpc.CallService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.camunda.bpm.engine.ProcessEngine;
import static com.jcabi.log.Logger.info;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import static org.ms123.common.rpc.CallService.ACTIVITI_CAMEL_PROPERTIES;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_ACTIVITY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_PROCESS_KEY;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_NAME;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_EXECUTION_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_BUSINESS_KEY;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_DEFINITION_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_DEFINITION_NAME;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_INSTANCE_ID;

@SuppressWarnings({ "unchecked", "deprecation" })
public class ExpressionCamelExecutor  implements org.ms123.common.process.Constants{

	public static Object execute(Map<String, Object> methodDefinition, VariableScope scope, ProcessEngine pe) {
		try {
			String methodname = (String) methodDefinition.get("method");
			List<Map<String, String>> variablesmapping = (List) methodDefinition.get("parameter");
			Map<String, Object> fparams = getParams(scope, variablesmapping);

			ProcessInstance pi = (ProcessInstance) scope;
			Map<String, String> activitiProperties = new TreeMap<String, String>();
			ProcessDefinition processDefinition = pe.getRepositoryService().createProcessDefinitionQuery().processDefinitionId(pi.getProcessDefinitionId()).singleResult();
			String pdKey = processDefinition.getKey();
			String namespace = pdKey.substring(0, pdKey.indexOf(NAMESPACE_DELIMITER));
			activitiProperties.put(HISTORY_ACTIVITI_PROCESS_KEY, namespace + "/" + getProcessName(pi.getProcessDefinitionId()) + "/" + pi.getProcessInstanceId());
//@@@MS			activitiProperties.put(HISTORY_ACTIVITI_ACTIVITY_KEY, pi.getTenantId() + "/" + getProcessName(pi.getProcessDefinitionId()) + "/" + pi.getId() + "/" + pi.getActivityId());
//@@@MS			activitiProperties.put(WORKFLOW_ACTIVITY_ID, pi.getActivityId());
//@@@MS			activitiProperties.put(WORKFLOW_ACTIVITY_NAME, pi.getName());
			activitiProperties.put(WORKFLOW_EXECUTION_ID, pi.getId());
			activitiProperties.put(WORKFLOW_PROCESS_BUSINESS_KEY, pi.getBusinessKey());
			activitiProperties.put(WORKFLOW_PROCESS_DEFINITION_ID, pi.getProcessDefinitionId());
//@@@MS			activitiProperties.put(WORKFLOW_PROCESS_DEFINITION_NAME, pi.getProcessDefinitionName());
			activitiProperties.put(WORKFLOW_PROCESS_INSTANCE_ID, pi.getProcessInstanceId());
			fparams.put(ACTIVITI_CAMEL_PROPERTIES, activitiProperties);
			info(ExpressionCamelExecutor.class, "ExpressionCamelExecutor(" + methodname + "):" + fparams);

			Object answer = getCallService().callCamel(namespace + "." + methodname, fparams);
			info(ExpressionCamelExecutor.class, "ExpressionCamelExecutor.answer:" + answer);

			return answer;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static String getProcessName(String s) {
		if (s == null || "".equals(((String) s).trim())) {
			return "";
		}
		String ss[] = s.split(":");
		return ss[0];
	}

	private static boolean isEmpty(String s) {
		if (s == null || "".equals(s.trim())) {
			return true;
		}
		return false;
	}

	private static CallService getCallService() {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		BundleContext bundleContext = (BundleContext) beans.get("bundleContext");
		ServiceReference ref = bundleContext.getServiceReference(CallService.class.getName());
		CallService callService = (CallService) bundleContext.getService(ref);
		info(ExpressionCamelExecutor.class, "ExpressionCamelExecutor.callService:" + callService);
		return callService;
	}

	private static class ScriptContext {
		public GroovyShell shell = new GroovyShell();;
		public Map<String, Script> scripCache = new HashMap();
	}

	private static ScriptContext scriptContext = new ScriptContext();

	private static Object eval(String expr, Map<String, Object> vars) {
		Script script = scriptContext.scripCache.get(expr);
		if (script == null) {
			script = scriptContext.shell.parse(expr);
			scriptContext.scripCache.put(expr, script);
		}
		Binding binding = new Binding(vars);
		script.setBinding(binding);
		return script.run();
	}

	private static Object getValue(String processvar, Map<String, Object> vars) {
		try {
			Object val = eval(processvar, vars);
			return val;
		} catch (Throwable t) {
			throw new RuntimeException("ExpressionCamelExecutor:Processvariable \"" + processvar + "\" not found", t);
		}
	}

	private static Map<String, Object> getParams(VariableScope scope, List<Map<String, String>> variablesmapping) {
		info(ExpressionCamelExecutor.class, "variablesmapping:" + variablesmapping);
		if (variablesmapping == null) {
			return new HashMap();
		}
		Map<String, Object> vars = scope.getVariables();
		info(ExpressionCamelExecutor.class, "VariableScope:" + vars);
		Map<String, Object> values = new HashMap();
		for (Map<String, String> m : variablesmapping) {
			String processvar = m.get("processvariable");
			Object o = getValue(processvar, vars);
			String servicevar = m.get("servicevariable");
			if (isEmpty(servicevar)) {
				servicevar = processvar;
			}
			values.put(servicevar, o);
		}
		info(ExpressionCamelExecutor.class, "getParams:" + values);
		return values;
	}
}

