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
package org.ms123.common.process.tasks;

import java.util.*;
import java.io.File;
import javax.transaction.UserTransaction;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.apache.commons.beanutils.*;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.dmn.DmnService;
import org.ms123.common.system.registry.RegistryService;
import org.ms123.common.process.tasks.GroovyTaskDsl;
import org.ms123.common.process.ProcessService;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.docbook.DocbookService;
import org.ms123.common.rpc.CallService;
import org.ms123.common.git.GitService;
import org.ms123.common.system.tm.TransactionService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.ms123.common.store.StoreDesc;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import flexjson.*;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import groovy.lang.*;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings("unchecked")
public abstract class TaskBaseExecutor implements Constants {
	protected JSONDeserializer m_ds = new JSONDeserializer();
	protected JSONSerializer m_js = new JSONSerializer();
	private DataLayer m_dataLayer;
	private PermissionService m_permissionService;
	private BundleContext m_bundleContext;
	private CallService m_callService;

	protected File getProcessBasedir(DelegateExecution execution) {
		String ws = System.getProperty("workspace");
		File file = new File(ws, "activiti/" + execution.getProcessInstanceId());
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	protected File getProcessDocBasedir(DelegateExecution execution) {
		String ws = System.getProperty("workspace");
		File file = new File(ws, "activiti/" + execution.getProcessInstanceId() + "/documents");
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	protected void printInfo(TaskContext tc) {
		debug(tc, getInfo(tc));
	}

	protected String getExceptionInfo(TaskContext tc) {
		String hint = "\n-----------------------------\n" + getInfo(tc) + "\n";
		hint += "------------------------------\n";
		return hint;
	}

	protected String getInfo(TaskContext tc) {
		VariableScope execution = tc.getExecution();
		if (!(execution instanceof DelegateExecution)) {
			return "";
		}
		DelegateExecution d = (DelegateExecution) execution;
		String processDefinitionId = ((ExecutionEntity) d).getProcessDefinitionId();
		StringBuffer sb = new StringBuffer();
		sb.append("Namespace:" + tc.getTenantId());
		sb.append("\nCurrentActivityId:" + d.getCurrentActivityId());
		sb.append("\nCurrentActivityName:" + d.getCurrentActivityName());
		sb.append("\nEventName:" + d.getEventName());
		sb.append("\nId:" + d.getId());
		sb.append("\nParentId:" + d.getParentId());
		sb.append("\nProcessDefinitionId:" + processDefinitionId);
		sb.append("\nProcessInstanceId:" + d.getProcessInstanceId());
		return sb.toString();
	}

	protected SessionContext getSessionContext(TaskContext tc) {
		VariableScope execution = tc.getExecution();
		SessionContext sc = null;
		if (m_dataLayer != null) {
			StoreDesc sdesc = StoreDesc.getNamespaceData(tc.getTenantId());
			sc = m_dataLayer.getSessionContext(sdesc);
		} else {
			Map beans = Context.getProcessEngineConfiguration().getBeans();
			DataLayer dataLayer = (DataLayer) beans.get(DataLayer.DATA_LAYER);
			log(tc, "Category:" + tc.getTenantId());
			StoreDesc sdesc = StoreDesc.getNamespaceData(tc.getTenantId());
			log(tc, "Sdesc:" + sdesc);
			sc = dataLayer.getSessionContext(sdesc);
		}
		return sc;
	}

	protected void setDataLayer(DataLayer dl) {
		m_dataLayer = dl;
	}

	protected PermissionService getPermissionService() {
		SessionContext sc = null;
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		return (PermissionService) beans.get(PermissionService.PERMISSION_SERVICE);
	}

	protected DmnService getDmnService() {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		return (DmnService) beans.get(DmnService.DMN_SERVICE);
	}

	protected RegistryService getRegistryService() {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		return (RegistryService) beans.get(RegistryService.REGISTRY_SERVICE);
	}

	protected Object getValue(DelegateExecution execution, String processvar) {
		try {
			if (processvar.indexOf(".") == -1) {
				return execution.getVariable(processvar);
			}
			String[] parts = processvar.split("\\.");
			Object o = execution.getVariable(parts[0]);
			for (int i = 1; i < parts.length; i++) {
				String part = parts[i];
				o = PropertyUtils.getProperty(o, part);
			}
			return o;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("TaskBaseExecutor:Processvariable \"" + processvar + "\" not found");
		}
	}

	protected String getFileContentFromGit(DelegateExecution execution, String namespace, String name, String type) {
		return getGitService(execution).searchContent(namespace, name, type);
	}

	protected DocbookService getDocbookService(DelegateExecution execution) {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		DocbookService ds = (DocbookService) beans.get("docbookService");
		return ds;
	}

	protected EventAdmin getEventAdmin(VariableScope execution) {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		EventAdmin ea = (EventAdmin) beans.get("eventAdmin");
		return ea;
	}

	protected GitService getGitService(VariableScope execution) {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		GitService gs = (GitService) beans.get("gitService");
		return gs;
	}

	protected CallService getCallService() {
		if (m_callService != null)
			return m_callService;
		if (m_bundleContext == null) {
			Map beans = Context.getProcessEngineConfiguration().getBeans();
			m_bundleContext = (BundleContext) beans.get("bundleContext");
		}
		log("TaskBaseExecutor.m_bundleContext:" + m_bundleContext);
		ServiceReference ref = m_bundleContext.getServiceReference(CallService.class.getName());
		if (ref != null) {
			m_callService = (CallService) m_bundleContext.getService(ref);
		}
		log("TaskBaseExecutor.m_callService:" + m_callService);
		return m_callService;
	}

	protected void setValue(DelegateExecution execution, String processvar, Object value) throws Exception {
		if (processvar.indexOf(".") == -1) {
			execution.setVariable(processvar, value);
		}
		String[] parts = processvar.split("\\.");
		Object o = execution.getVariable(parts[0]);
		if (o == null) {
			o = new HashMap();
			execution.setVariable(parts[0], o);
		}
		for (int i = 1; i < parts.length; i++) {
			String part = parts[i];
			if (i < (parts.length - 1)) {
				Object o1 = PropertyUtils.getProperty(o, part);
				if (o1 == null) {
					o1 = new HashMap();
					PropertyUtils.setProperty(o, part, o1);
				}
				o = o1;
			} else {
				PropertyUtils.setProperty(o, part, value);
			}
		}
	}

	protected void showVariablenNames(TaskContext tc) {
		VariableScope execution = tc.getExecution();
		debug(tc, "ProcessVariables(" + this.getClass().getSimpleName() + "):" + execution.getVariableNames());
		for (String x : execution.getVariableNames()) {
			log(tc, "\t" + x + "=" + execution.getVariable(x));
		}
	}

	protected GroovyTaskDsl getGroovyTaskDsl(TaskContext tc, SessionContext sc, Map<String, Object> vars) {
		GroovyTaskDsl dsl = null;
		VariableScope _execution = tc.getExecution();
		if (_execution instanceof DelegateExecution) {
			DelegateExecution execution = (DelegateExecution) _execution;
			String processDefinitionId = ((ExecutionEntity) execution).getProcessDefinitionId();
			dsl = new GroovyTaskDsl(getEventAdmin(execution), tc.getTenantId(), tc.getProcessDefinitionKey(), execution.getProcessInstanceId(), getInfo(tc), vars);
		} else {
			dsl = new GroovyTaskDsl(null, tc.getTenantId(), tc.getProcessDefinitionKey(), tc.getPid(), tc.getHint(), vars);
		}
		return dsl;
	}

	private Object getValue(String processvar, Map<String, Object> vars) {
		try {
			Object val = eval(processvar, vars);
			log("getValue(" + processvar + "):" + val + "\tvars:" + vars);
			return val;
		} catch (Throwable t) {
			throw new RuntimeException("TaskBaseExecutor:Processvariable \"" + processvar + "\" not found", t);
		}
	}

	protected Map<String, Object> getParams(DelegateExecution execution, Expression variablesmapping, String taskVarName) throws Exception {
		if (variablesmapping == null) {
			return new HashMap();
		}
		String vm = variablesmapping.getValue(execution).toString();
		if (vm.trim().length() == 0)
			return new HashMap();
		Map map = (Map) m_ds.deserialize(vm);
		List<Map> varmap = (List<Map>) map.get("items");
		Map<String, Object> values = new HashMap();
		Map<String, Object> vars = execution.getVariables();
		for (Map<String, String> m : varmap) {
			String processvar = m.get("processvar");
			Object o = getValue(processvar, vars);
			String pvar = m.get(taskVarName);
			values.put(pvar, o);
		}
		return values;
	}

	protected void setProcessDefinition(TaskContext tc, VariableScope execution) {
		if (execution instanceof DelegateExecution) {
			Map beans = Context.getProcessEngineConfiguration().getBeans();
			ProcessEngine pe = (ProcessEngine) beans.get(ProcessService.PROCESS_ENGINE);
			String processDefinitionId = ((ExecutionEntity) execution).getProcessDefinitionId();
			RepositoryService repositoryService = pe.getRepositoryService();
			ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
			tc.setProcessDefinitionKey(processDefinition.getKey());
			tc.setProcessDefinitionName(processDefinition.getName());
			tc.setTenantId(processDefinition.getTenantId());
			tc.setPE(pe);
			tc.setPermissionService(getPermissionService());
		}
	}

	private static class ScriptContext {
		public GroovyShell shell = new GroovyShell();;
		public Map<String, Script> scripCache = new HashMap();
	}

	private static ScriptContext scriptContext = new ScriptContext();

	protected static String evalstr(String expr, Map<String, Object> vars) {
		try {
			return String.valueOf(eval(expr, vars));
		} catch (Throwable e) {
			return expr;
		}
	}

	protected static Object eval(String expr, Map<String, Object> vars) {
		Script script = scriptContext.scripCache.get(expr);
		if (script == null) {
			script = scriptContext.shell.parse(expr);
			scriptContext.scripCache.put(expr, script);
		}
		Binding binding = new Binding(vars);
		script.setBinding(binding);
		return script.run();
	}

	private String getString(TaskContext tc, String key, String expr) {
		return expr;
	}

	private Pattern _splitSearchPattern = Pattern.compile("[\",]");

	private List<String> splitByCommasNotInQuotes(String s) {
		if (s == null) {
			return Collections.emptyList();
		}

		List<String> list = new ArrayList<String>();
		Matcher m = _splitSearchPattern.matcher(s);
		int pos = 0;
		boolean quoteMode = false;
		while (m.find()) {
			String sep = m.group();
			if ("\"".equals(sep)) {
				quoteMode = !quoteMode;
			} else if (!quoteMode && ",".equals(sep)) {
				int toPos = m.start();
				list.add(s.substring(pos, toPos));
				pos = m.end();
			}
		}
		if (pos < s.length()) {
			list.add(s.substring(pos));
		}
		return list;
	}

	protected boolean isEmpty(Object s) {
		if (s == null || "".equals(((String) s).trim())) {
			return true;
		}
		return false;
	}

	protected BigInteger checksum(Object obj) {
		try {
			if (obj == null) {
				return BigInteger.ZERO;
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.close();
			MessageDigest m = MessageDigest.getInstance("SHA1");
			m.update(bos.toByteArray());
			return new BigInteger(1, m.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return BigInteger.ZERO;
	}

	protected String getValueFromRegistry(String key) {
		return getRegistryService().get(key);
	}

	protected List<ProcessInstance> getProcessInstances(TaskContext tc, Map<String, String> processCriteria, boolean exception) {
		boolean hasCriteria = false;
		Map<String, Object> vars = tc.getExecution().getVariables();
		log("findExecution:processCriteria:" + processCriteria);
		ExecutionQuery eq = tc.getPE().getRuntimeService().createExecutionQuery();
		String processDefinitionId = getString(tc, PROCESS_DEFINITION_ID, processCriteria.get(PROCESS_DEFINITION_ID));
		if (!isEmpty(processDefinitionId)) {
			String val = trimToEmpty(evalstr(processDefinitionId, vars));
			eq.processDefinitionId(val);
			log("getProcessInstances.processDefinitionId:" + val);
			hasCriteria = true;
		}
		String processDefinitionKey = getString(tc, PROCESS_DEFINITION_KEY, processCriteria.get(PROCESS_DEFINITION_KEY));
		if (!isEmpty(processDefinitionKey)) {
			String key = trimToEmpty(evalstr(processDefinitionKey, vars));
			eq.processDefinitionKey(key);
			log("getProcessInstances.processDefinitionKey:" + key);
			hasCriteria = true;
		}
		String processInstanceId = getString(tc, PROCESS_INSTANCE_ID, processCriteria.get(PROCESS_INSTANCE_ID));
		if (!isEmpty(processInstanceId)) {
			String pid = trimToEmpty(evalstr(processInstanceId, vars));
			eq.processInstanceId(trimToEmpty(pid));
			log("getProcessInstances.processInstanceId:" + pid);
			hasCriteria = true;
		}
		String businessKey = getString(tc, BUSINESS_KEY, processCriteria.get(BUSINESS_KEY));
		if (!isEmpty(businessKey)) {
			String bkey = trimToEmpty(evalstr(businessKey, vars));
			//@@@MS eq.processInstanceBusinessKey(bkey, false);
			log("getProcessInstances.businessKey:" + bkey);
			hasCriteria = true;
		}

		String activityId = getString(tc, ACTIVITY_ID, processCriteria.get(ACTIVITY_ID));
		if (!isEmpty(activityId)) {
			String aid = trimToEmpty(evalstr(activityId, vars));
			eq.activityId(aid);
			log("getProcessInstances.activityId:" + aid);
			hasCriteria = true;
		}
		String executionId = getString(tc, EXECUTION_ID, processCriteria.get(EXECUTION_ID));
		if (!isEmpty(executionId)) {
			eq.executionId(trimToEmpty(evalstr(executionId, vars)));
			hasCriteria = true;
		}
		String processVariable = processCriteria.get(PROCESSVARIABLE);
		if (!isEmpty(processVariable)) {
			processVariable = trimToEmpty(processVariable);
			List<String> tokens = splitByCommasNotInQuotes(processVariable);
			if (tokens.size() == 1) {
				//@@@MS eq.processVariableValueEquals(trimToEmpty(evalstr(tokens.get(0), vars)));
			} else {
				log("p1eval:" + evalstr(tokens.get(1), vars));
				eq.processVariableValueEquals(trimToEmpty(tokens.get(0)), trimToEmpty(evalstr(tokens.get(1), vars)));
			}
			hasCriteria = true;
		}

		if (hasCriteria) {
			log("getProcessInstances.namespace:" + tc.getTenantId());
			//@@@MS eq.executionTenantId(trimToEmpty(tc.getTenantId()));
			List<ProcessInstance> executionList = (List) eq.list();
			log("getProcessInstances:" + executionList);
			if (exception && (executionList == null || executionList.size() == 0)) {
				throw new RuntimeException("TaskBaseExecutor.findProcessInstance:Could not find processInstance with criteria " + processCriteria);
			}
			return executionList;
		}
		return null;
	}

	protected class TaskContext {
		protected VariableScope m_execution;
		protected String m_tenantId;
		protected String m_processDefinitionKey;
		protected String m_processDefinitionName;
		protected String m_hint;
		protected String m_pid;
		protected PermissionService m_ps;
		protected ProcessEngine m_pe;
		protected String m_script;

		public TaskContext() {
		}

		public TaskContext(DelegateExecution execution) {
			m_execution = execution;
			setProcessDefinition(this, execution);
		}

		public void setExecution(VariableScope vs) {
			m_execution = vs;
		}

		public void setPE(ProcessEngine pe) {
			m_pe = pe;
		}

		public ProcessEngine getPE() {
			return m_pe;
		}

		public void setPermissionService(PermissionService ps) {
			m_ps = ps;
		}

		public PermissionService getPermissionService() {
			return m_ps;
		}

		public void setScript(String s) {
			m_script = s;
		}

		public void setPid(String pid) {
			m_pid = pid;
		}

		public void setHint(String hint) {
			m_hint = hint;
		}

		public void setProcessDefinitionName(String pd) {
			m_processDefinitionName = pd;
		}

		public void setProcessDefinitionKey(String pd) {
			m_processDefinitionKey = pd;
		}

		public void setTenantId(String c) {
			m_tenantId = c;
		}

		public VariableScope getExecution() {
			return m_execution;
		}

		public String getScript() {
			return m_script;
		}

		public String getPid() {
			return m_pid;
		}

		public String getHint() {
			return m_hint;
		}

		public String getProcessDefinitionKey() {
			return m_processDefinitionKey;
		}

		public String getProcessDefinitionName() {
			return m_processDefinitionName;
		}

		public String getTenantId() {
			return m_tenantId;
		}
	}

	protected void log(String message) {
		info(this, message);
		System.err.println(message);
	}

	protected void debug(String message) {
		com.jcabi.log.Logger.debug(this, message);
		System.err.println(message);
	}

	protected void log(TaskContext tc, String message) {
		message = getlogMessage(tc, message);
		info(this, message);
		System.err.println(message);
	}

	protected void debug(TaskContext tc, String message) {
		message = getlogMessage(tc, message);
		com.jcabi.log.Logger.debug(this, message);
		System.err.println(message);
	}

	protected void error(TaskContext tc, String message, Exception e) {
		message = getlogMessage(tc, message);
		com.jcabi.log.Logger.error(this, message, e);
		System.err.println(message);
		e.printStackTrace();
	}

	protected String getlogMessage(TaskContext tc, String message) {
		VariableScope execution = tc.getExecution();
		if ((execution instanceof DelegateExecution)) {
			DelegateExecution d = (DelegateExecution) execution;
			message = "(pid:" + d.getProcessInstanceId() + ",eid:" + d.getId() + ",parentId:" + d.getParentId() + "):" + message;
		}
		return message;
	}
}

