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
package org.ms123.common.camel.components.activiti;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.FormService;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.task.Task;


import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.RepositoryServiceImpl;

import org.activiti.engine.repository.ProcessDefinition;
import org.apache.camel.Exchange;
import org.apache.camel.MessageHistory;
import org.apache.camel.impl.DefaultProducer;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.workflow.api.WorkflowService;
import org.ms123.common.system.thread.ThreadContext;
import org.ms123.common.camel.api.ExchangeUtils;
import org.ms123.common.camel.trace.ExchangeFormatter;
import org.ms123.common.camel.trace.MessageHelper;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import java.io.InputStream;
import java.util.Map;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import flexjson.*;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.beanutils.ConvertUtils;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;
import static org.ms123.common.system.history.HistoryService.HISTORY_HINT;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_START_PROCESS_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_JOB_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;

import static org.ms123.common.system.history.HistoryService.ACTIVITI_CAMEL_CORRELATION_TYPE;
import static org.ms123.common.system.history.HistoryService.ACC_ACTIVITI_ID;
import static org.ms123.common.system.history.HistoryService.ACC_ROUTE_INSTANCE_ID;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_ACTIVITY_KEY;
import static org.ms123.common.system.history.HistoryService.CAMEL_ROUTE_DEFINITION_KEY;

import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_EXECUTION_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_BUSINESS_KEY;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_INSTANCE_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_NAME;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings({"unchecked", "deprecation"})
public class ActivitiProducer extends org.activiti.camel.ActivitiProducer implements ActivitiConstants {

	protected JSONSerializer js = new JSONSerializer();
	protected JSONDeserializer ds = new JSONDeserializer();
	private RuntimeService runtimeService;
	private HistoryService historyService;
	private TaskService taskService;
	private FormService formService;
	private RepositoryService repositoryService;
	private PermissionService permissionService;
	private WorkflowService workflowService;
	private CamelService camelService;
	private ActivitiService activitiService;

	private ActivitiOperation operation;
	private ActivitiEndpoint endpoint;

	private Map<String, String> processCriteria;
	private Map<String, String> taskCriteria;
	private String taskOperation;
	private String taskId;
	private String activityId;
	private String namespace;
	private String headerFields;
	private List<Map<String,String>> assignments;
	private String variableNames;
	private String businessKey;
	private String signalName;
	private String messageName;
	private boolean isSendSignal;
	private boolean isSendMessage;
	private boolean isCheckAssignments;

	private Map options;
	private String activitiKey;

	public ActivitiProducer(ActivitiEndpoint endpoint, WorkflowService workflowService, PermissionService permissionService) {
		super(endpoint, -1, 100);
		this.endpoint = endpoint;
		this.permissionService = permissionService;
		this.workflowService = workflowService;
		this.runtimeService = workflowService.getProcessEngine().getRuntimeService();
		this.historyService = workflowService.getProcessEngine().getHistoryService();
		this.repositoryService = workflowService.getProcessEngine().getRepositoryService();
		this.taskService = workflowService.getProcessEngine().getTaskService();
		this.formService = workflowService.getProcessEngine().getFormService();
		this.camelService = (CamelService) endpoint.getCamelContext().getRegistry().lookupByName(CamelService.class.getName());
		this.activitiService = (ActivitiService) endpoint.getCamelContext().getRegistry().lookupByName(ActivitiService.class.getName());
		info(this,"ActivitiProducer.camelService:" + this.camelService);
		setRuntimeService(this.runtimeService);
		String[] path = endpoint.getEndpointKey().split(":");
		this.operation = ActivitiOperation.valueOf(path[1].replace("//", ""));
		this.namespace = endpoint.getNamespace();
		this.signalName = endpoint.getSignalName();
		this.headerFields = endpoint.getHeaderFields();
		this.assignments = endpoint.getAssignments();
		this.variableNames = endpoint.getVariableNames();
		this.businessKey = endpoint.getBusinessKey();
		this.messageName = endpoint.getMessageName();
		this.isSendSignal = endpoint.isSendSignal();
		this.isSendMessage = endpoint.isSendMessage();
		this.isCheckAssignments = endpoint.isCheckAssignments();
		this.processCriteria = endpoint.getProcessCriteria();
		this.taskCriteria = endpoint.getTaskCriteria();
		this.taskOperation = endpoint.getTaskOperation();
		this.taskId = endpoint.getTaskId();
		this.options = endpoint.getOptions();
		this.js.prettyPrint(true);
	}

	public void process(Exchange exchange) throws Exception {
		this.activityId = getString(exchange, ACTIVITY_ID, this.activityId);
		this.namespace = getString(exchange, NAMESPACE, this.namespace);
		if (isEmpty(this.namespace)) {
			this.namespace = (String) exchange.getContext().getRegistry().lookupByName("namespace");
		}
		if (isEmpty(this.namespace)) {
			this.namespace = (String) exchange.getProperty("_namespace");
		}
		info(this,"ActivitiProducer.operation:" + this.operation+"/namespace:"+this.namespace);
		invokeOperation(this.operation, exchange);
		/* final CamelService camelService = (CamelService) exchange.getContext().getRegistry().lookupByName(CamelService.class.getName());
		 exchange.setProperty(WORKFLOW_ACTIVITY_NAME, this.activityId);
		 camelService.saveHistory(exchange);
		 saveActivitiCamelCorrelationHistory(exchange);
		 printHistory(exchange);*/
	}

	/**
	 * Entry method that selects the appropriate operation and executes it
	 * @param operation
	 * @param exchange
	 * @throws Exception
	 */
	private void invokeOperation(ActivitiOperation operation, Exchange exchange) throws Exception {
		switch (operation) {
		case sendMessageEvent:
			doSendMessageEvent(exchange);
			break;
		case sendSignalEvent:
			doSendSignalEvent(exchange);
			break;
		case sendSignalToReceiveTask:
			doSendSignalToReceiveTask(exchange);
			break;
		case startProcess:
			doStartProcess(exchange);
			break;
		case getProcessVariables:
			doGetProcessVariables(exchange);
			break;
		case queryProcessInstances:
			doQueryProcessInstances(exchange);
			break;
		case queryTasks:
			doQueryTasks(exchange);
			break;
		case executeTaskOperation:
			doExecuteTaskOperation(exchange);
			break;
		default:
			throw new RuntimeException("ActivitiProducer.Operation not supported. Value: " + operation);
		}
	}

	private void doGetProcessVariables(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange, false );
		if( processInstanceList.size() > 1 ){
			//throw new RuntimeException("ActivitiProducer.doGetProcessVariables.more as one process queried: " + processInstanceList);
		}

		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();
		for( ProcessInstance pi : processInstanceList){
			info(this,"doGetProcessVariables.processInstance1:" + pi);
			pi = 	runtimeService.createProcessInstanceQuery().includeProcessVariables().processInstanceId( pi.getId()).singleResult();
			info(this,"doGetProcessVariables.processInstance2:" + pi);
			if( pi == null){
				continue;
			}
			info(this,"doGetProcessVariables.variables:" + pi.getProcessVariables());
			String variableNames = getString(exchange, "variableNames", this.variableNames);
			if( isEmpty(variableNames)){
				//			exchange.getIn().setBody(pi.getProcessVariables());
				retList.add( pi.getProcessVariables());
			}else{
				List<String> nameList = Arrays.asList(variableNames.split(","));
				info(this,"doGetProcessVariables:nameList:" + nameList);
				Map<String,Object> vars = pi.getProcessVariables();
				Map<String,Object> ret = new HashMap();
				for (Map.Entry<String, Object> entry : vars.entrySet()) {
					if( nameList.indexOf( entry.getKey()) > -1){
						info(this,"doGetProcessVariables:var:" + entry.getKey()+"="+entry.getValue());
						ret.put( entry.getKey(), entry.getValue());
					}
				}
				//			exchange.getIn().setBody(ret);
				retList.add( ret);
			}
		}
		if( retList.size() == 1){
			exchange.getIn().setBody(retList.get(0));
		}else{
			exchange.getIn().setBody(retList);
		}
	}

	private void doQueryProcessInstances(Exchange exchange) {
		List<HistoricProcessInstance> processInstanceList = getHistoricProcessInstances(exchange );

		List<Map> ret = new ArrayList<Map>();
		for( HistoricProcessInstance pi : processInstanceList){
			Map piMap = new HashMap();
			piMap.put("id", pi.getId());
			piMap.put("businessKey", pi.getBusinessKey());
			piMap.put("startTime",pi.getStartTime().getTime());
			if( pi.getEndTime()!=null){
				piMap.put("endTime", pi.getEndTime().getTime());
			}
			piMap.put("duration", pi.getDurationInMillis());
			piMap.put("processDefinitionId", pi.getProcessDefinitionId());
			piMap.put("startUserId", pi.getStartUserId());
			ret.add(piMap);
		}
		exchange.getIn().setBody(ret);
	}

	private void doQueryTasks(Exchange exchange) {
		TaskQuery taskQuery = this.taskService.createTaskQuery();
		Class taskQueryClass = taskQuery.getClass();
		Method methods[] = taskQueryClass.getMethods();
		for (Map.Entry<String, String> entry : this.taskCriteria.entrySet()) {
			String  key = entry.getKey();
			String  val = entry.getValue();
			info(this,"doQueryTasks("+key+"):"+val);

			List<String> tokens = splitByCommasNotInQuotes( val);
			if( tokens.size() == 1){
  			Method m = getMethod(methods,key,1);
				if( m != null){
					Object v = eval( trimToEmpty(val), exchange, m.getParameterTypes()[0] );
					setQueryValue( taskQuery, m, v);	
				}
			}else{
  			Method m = getMethod(methods,key,2);
				if( m != null){
					setQueryValue(taskQuery, m, trimToEmpty(tokens.get(0)), eval(tokens.get(1),exchange,m.getParameterTypes()[1]));
				}
			}
		}
		taskQuery.taskTenantId(trimToEmpty(this.namespace));
		List<Task> taskList = taskQuery.list();
		info(this,"taskList:");
		List<Map> ret = new ArrayList<Map>();
		for( Task task : taskList){
			info(this,"\t"+task.getName()+"/"+task.getId()+"/"+task.getTaskDefinitionKey());
			Map<String, Object> taskMap = new HashMap();
			taskMap.put("assignee", task.getAssignee());
			taskMap.put("createTime", task.getCreateTime());
			taskMap.put("delegationState", task.getDelegationState());
			taskMap.put("description", task.getDescription());
			taskMap.put("dueDate", task.getDueDate());
			taskMap.put("executionId", task.getExecutionId());
			taskMap.put("id", task.getId());
			taskMap.put("name", task.getName());
			taskMap.put("owner", task.getOwner());
			taskMap.put("parentTaskId", task.getParentTaskId());
			taskMap.put("priority", task.getPriority());
			taskMap.put("processDefinitionId", task.getProcessDefinitionId());
			taskMap.put("processInstanceId", task.getProcessInstanceId());
			taskMap.put("taskDefinitionId", task.getTaskDefinitionKey());
			TaskFormData taskFormData = this.formService.getTaskFormData(task.getId());
			if (taskFormData != null) {
				taskMap.put("formResourceKey", taskFormData.getFormKey());
			}
			ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) this.repositoryService).getDeployedProcessDefinition(task.getProcessDefinitionId());
			taskMap.put("processName", pde.getName());
			ret.add(taskMap);
		}
		exchange.getIn().setBody(ret);
	}

	private Method getMethod( Method[] methods, String name, int pc){
		for( Method m : methods){
			if( m.getName().equals(name) && m.getParameterCount() == pc){
				info(this,"getMethod:"+m.getName());
				return m;
			}
		}
		return null;
	}
	private void setQueryValue( Object o, Method m, Object val ){
		try {
			info(this,"setQueryValue("+m.getName()+"):"+val);
			Object[] args = new Object[1];
			args[0] = val;
			m.invoke(o, args);
		} catch (Exception e) {
			error(this, "setQueryValue("+m.getName()+","+val+").error:%[exception]s",e);
		}
	}
	private void setQueryValue( Object o, Method m, String val1, Object val2 ){
		try {
			info(this,"setQueryValue("+m.getName()+"):"+val1+","+val2);
			Object[] args = new Object[2];
			args[0] = val1;
			args[1] = val2;
			m.invoke(o, args);
		} catch (Exception e) {
			error(this, "setQueryValue("+m.getName()+","+val1+","+val2+").error:%[exception]s",e);
		}
	}

	private void doExecuteTaskOperation(Exchange exchange) {
		String taskOperation = getString(exchange, "taskOperation", this.taskOperation);
		String taskId = getString(exchange, "taskId", this.taskId);
		Map<String,Object> pv = getProcessVariables(exchange);
//		pv.putAll( getProcessAssignments(exchange) );
		info(this,"doExecuteTaskOperation("+taskOperation+","+taskId+".variables:"+pv);
		this.activitiService.executeTaskOperation( taskId, taskOperation, pv, this.isCheckAssignments );
	}

	private void doSendMessageEvent(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange);
		doSendMessageEvent(exchange, processInstanceList);
	}
	private void doSendMessageEvent(Exchange exchange,List<ProcessInstance> processInstanceList) {
		String messageName = getString(exchange, "messageName", this.messageName);
		info(this,"processInstanceList:" + processInstanceList);
		info(this,"messageName:" + messageName);
		Map<String,Object> processVariables = getProcessVariables(exchange);
		if( processInstanceList != null){
			for( ProcessInstance pi : processInstanceList){
				info(this,"PI:"+pi.getId());
				Execution execution = runtimeService.createExecutionQuery() .executionId(pi.getId()).messageEventSubscriptionName(messageName).singleResult(); 
				info(this,"\tExecution:"+execution);
				if( execution != null){
					info(this,"doSendMessageEvent:"+messageName+"/"+execution.getId());
					if( processVariables == null){
						this.runtimeService.messageEventReceived( messageName, execution.getId());
					}else{
						this.runtimeService.messageEventReceived( messageName, execution.getId(), processVariables);
					}
				}else{
					info(this,"doSendMessageEvent.message("+messageName+") not found in process:"+pi.getId());
					List<Execution> execs = runtimeService.createExecutionQuery() .messageEventSubscriptionName(messageName).list(); 
				}
			}
		}
	}

	private void doSendSignalEvent(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange);
		doSendSignalEvent(exchange, processInstanceList);
	}
	private void doSendSignalEvent(Exchange exchange,List<ProcessInstance> processInstanceList) {
		String signalName = getString(exchange, "signalName", this.signalName);
		info(this,"processInstanceList:" + processInstanceList);
		Map<String,Object> processVariables = getProcessVariables(exchange);
		if( processInstanceList == null){
			info(this,"doSendSignalEvent:"+signalName);
			if( processVariables == null){
				this.runtimeService.signalEventReceived( signalName);
			}else{
				this.runtimeService.signalEventReceived( signalName, processVariables);
			}
		}else{
			for( ProcessInstance pi : processInstanceList){
				Execution execution = runtimeService.createExecutionQuery() .executionId(pi.getId()).signalEventSubscriptionName(signalName).singleResult(); 
				info(this,"\tExecution:"+execution);
				if( execution != null){
					info(this,"doSendSignalEvent:"+signalName+"/"+execution.getId());
					if( processVariables == null){
						this.runtimeService.signalEventReceived( signalName, execution.getId());
					}else{
						this.runtimeService.signalEventReceived( signalName, execution.getId(), processVariables);
					}
				}else{
					info(this,"doSendSignalEvent.signal("+signalName+") not found in process:"+pi.getId());
					List<Execution> execs = runtimeService.createExecutionQuery() .signalEventSubscriptionName(signalName).list(); 
					info(this,"doSendSignalEvent.allExecutions("+signalName+"):"+execs);
				}
			}
		}
	}

	private void doSendSignalToReceiveTask(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange);
		debug(this,"doSendSignalToReceiveTask:"+processInstanceList);
		for( ProcessInstance pi : processInstanceList){
			signal(exchange, pi);
		}
	}

	private void doStartProcess(Exchange exchange) {
		boolean isSendSignal = getBoolean(exchange, "sendSignal", this.isSendSignal);
		boolean isSendMessage = getBoolean(exchange, "sendMessage", this.isSendMessage);
		List<ProcessInstance> processInstanceList=null;
		if( isSendMessage || isSendSignal ){
			processInstanceList = getProcessInstances(exchange, true, false);
		}
		if( !isEmpty(processInstanceList)){
			if( isSendMessage){
				doSendMessageEvent(exchange, processInstanceList);
			}else if( isSendSignal){
				doSendSignalEvent(exchange, processInstanceList);
			}
		}else{
			ProcessInstance pi = executeProcess(exchange);
			debug(this,"ProcessInstance:" + pi);
			if (pi != null) {
				this.activitiKey += "/" + pi.getId();
				debug(this,"m_activitiKey:" + this.activitiKey);
				exchange.getOut().setBody(pi.getId());
			}
		}
	}

	private void signal(Exchange exchange, ProcessInstance execution) {
		String ns = (String) exchange.getContext().getRegistry().lookupByName("namespace");
		this.activitiKey += "/" + execution.getId() + "/" + this.activityId;
		if (execution == null) {
			throw new RuntimeException("Couldn't find activityId " + this.activityId + " for processId " + execution.getId());
		}
		debug(this,"signal.ProcessInstance:"+ this.js.serialize(execution));
		Map<String,Object> pv = getProcessVariables(exchange);
		//pv.putAll( getProcessAssignments(exchange) );
		debug(this,"signal.processAssignment:"+ this.js.serialize(pv));
		new SignalThread(ns, getProcessDefinition(execution), execution, exchange, pv).start();
	}

	private class SignalThread extends Thread {
		Execution execution;
		ProcessDefinition processDefinition;
		Exchange exchange;
		Map variables;

		String ns;

		public SignalThread(String ns, ProcessDefinition pd, ProcessInstance exec, Exchange exchange, Map<String,Object> variables) {
			this.execution = exec;
			this.exchange = exchange;
			this.variables = variables;
			this.ns = ns;
			this.processDefinition = pd;
		}

		public void run() {
			ThreadContext.loadThreadContext(ns, "admin");
			permissionService.loginInternal(ns);
			while (true) {
				try {
					info(this,"SignalThread.sending signal to:" + execution.getId());
					if( this.variables!=null){
						runtimeService.signal(execution.getId(), this.variables);
					}else{
						runtimeService.signal(execution.getId());
					}
				} catch (org.activiti.engine.ActivitiOptimisticLockingException e) {
					info(this,"SignalThread:" + e);
					try {
						Thread.sleep(100L);
					} catch (Exception x) {
					}
					continue;
				} catch (Exception e) {
					createLogEntry(exchange, this.processDefinition, e);
					error(this, "SignalThread::%[exception]s", e);
				} finally {
					ThreadContext.getThreadContext().finalize(null);
				}
				break;
			}
		}
	}

	private ProcessInstance executeProcess(Exchange exchange) {
		ProcessDefinition processDefinition=null;
		Map<String, Object> vars = getProcessVariables(exchange);//getCamelVariablenMap(exchange);
		info(this,"ExecuteProcess:criteria:" + this.processCriteria);
		info(this,"ExecuteProcess:vars:" + vars);
		ThreadContext.loadThreadContext(this.namespace, "admin");
		this.permissionService.loginInternal(this.namespace);
		try {
			ProcessInstanceBuilder pib = this.runtimeService.createProcessInstanceBuilder();
			String processDefinitionId = getString(exchange, PROCESS_DEFINITION_ID, this.processCriteria.get(PROCESS_DEFINITION_ID));
			if (!isEmpty(processDefinitionId)) {
				info(this,"ExecuteProcess:processDefinitionId:" + processDefinitionId);
				pib.processDefinitionId(processDefinitionId);
				processDefinition = this.repositoryService.createProcessDefinitionQuery().latestVersion().processDefinitionId(processDefinitionId).processDefinitionTenantId(this.namespace).singleResult();
				if (processDefinition == null) {
					throw new RuntimeException("No process with processDefinitionId(" + processDefinitionId + ") in namespace(" + this.namespace + ")");
				}
			}
			String processDefinitionKey = getString(exchange, PROCESS_DEFINITION_KEY, this.processCriteria.get(PROCESS_DEFINITION_KEY));
			if (!isEmpty(processDefinitionKey)) {
				info(this,"ExecuteProcess:processDefinitionKey:" + processDefinitionKey);
				pib.processDefinitionKey(processDefinitionKey);
				processDefinition = this.repositoryService.createProcessDefinitionQuery().latestVersion().processDefinitionKey(processDefinitionKey).processDefinitionTenantId(this.namespace).singleResult();
				if (processDefinition == null) {
					throw new RuntimeException("No process with processDefinitionKey(" + processDefinitionKey + ") in namespace(" + this.namespace + ")");
				}
			}
			String businessKey = getString(exchange, BUSINESS_KEY, this.businessKey);
			if (!isEmpty(businessKey)) {
				info(this,"ExecuteProcess:businessKey:" + businessKey);
				pib.businessKey(businessKey);
			}

			for (Map.Entry<String, Object> entry : vars.entrySet()) {
				info(this,"ExecuteProcess:var:" + entry.getKey()+"="+entry.getValue());
				pib.addVariable(entry.getKey(), entry.getValue());
			}
			info(this,"ExecuteProcess:tenant:" + this.namespace);
			pib.tenantId(this.namespace);
			setInitialParameter( processDefinition, pib );
			return pib.start();
		} finally {
			ThreadContext.getThreadContext().finalize(null);
			info(this,"EndProcess:" + this.processCriteria + "/" + this.namespace);
		}
	}

	private void setInitialParameter(ProcessDefinition processDefinition, ProcessInstanceBuilder pib){
		String deploymentId = processDefinition.getDeploymentId();
		RepositoryService rs = this.repositoryService;
		try {
			InputStream is = rs.getResourceAsStream(deploymentId, "initialParameter");
			Map params = (Map) new JSONDeserializer().deserialize(IOUtils.toString(is));
			info(this,"setInitialParameter:"+params);
			if (params.get("items") != null) {
				List<Map> items = (List) params.get("items");
				for (Map<String, String> item : items) {
					String name = item.get("name");
					Class type = ExchangeUtils.assignmentTypes.get( item.get("type") );
					Object value = item.get("value");
					if( value instanceof String ){
						String v = ((String)value).trim();
						if( v.length() > 1 && (v.startsWith("{") || v.startsWith("["))){
							value = new JSONDeserializer().deserialize(v);
						}else{
							value = convertTo( value, type );
						}
					}
					info(this,"ExecuteProcess.initialVar:" + name + "=" + value);
					pib.addVariable(name, value);
				}
			}
			//pib.addVariable("__currentUser", uid);
			//pib.addVariable("__startingUser", uid);
			pib.addVariable("__namespace", this.namespace);
		} catch (Exception e) {
			if (e instanceof NullPointerException) {
				e.printStackTrace();
			}
			info(this,"getResourceAsStream:" + e);
		}
	}
	private GroovyClassLoader groovyClassLoader = null;
	private Map<String,Script> scriptCache = new HashMap();
	private Script parse(String expr) {
		if( groovyClassLoader == null){
			ClassLoader parentLoader = this.getClass().getClassLoader();
			groovyClassLoader =   new GroovyClassLoader(parentLoader,new CompilerConfiguration());
		}
		try{
			GroovyCodeSource gcs = new GroovyCodeSource( expr, "script", "groovy/shell");
			return InvokerHelper.createScript(groovyClassLoader.parseClass(gcs,false), new Binding());
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("ActivitiProducer.parse:"+e.getMessage()+" -> "+ expr);
		}
	}

	private Object eval(String str, Exchange exchange, Class clazz) {
		return ExchangeUtils.getParameter( str, exchange, clazz );
	}
	private String eval(String str, Exchange exchange) {
		return ExchangeUtils.getParameter( str, exchange, String.class );
	}

	private Map<String,Object> getProcessVariables(Exchange exchange){
		List<String> _headerList=null;
		String headerFields = getString(exchange, "headerFields", this.headerFields);
		if( !isEmpty(headerFields)){
			_headerList = Arrays.asList(headerFields.split(","));
		}else{
			_headerList = new ArrayList();
		}
		Map<String,String> modMap = new HashMap<String,String>();
		List<String> headerList = new ArrayList<String>();
		for( String h : _headerList){
			String[]  _tmp = h.split(":");
			String key = _tmp[0];
			String mod = _tmp.length>1 ? _tmp[1] : "";
			headerList.add(key);
			modMap.put(key,mod);
		}
		Map<String,Object> processVariables = new HashMap();
		for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
			if( headerList.size()==0 || headerList.contains( header.getKey())){
				if( header.getValue() instanceof Map  && !"asMap".equals(modMap.get(header.getKey()) )){
					processVariables.putAll((Map)header.getValue());
				}else{
					processVariables.put(header.getKey(), header.getValue());
				}
			}
		}
		processVariables.putAll( getProcessAssignments(exchange) );
		return processVariables;
	}

	private Map<String,Object> getProcessAssignments(Exchange exchange){
		return ExchangeUtils.getAssignments( exchange, this.assignments);
	}

	private List<ProcessInstance> getProcessInstances(Exchange exchange) {
		return getProcessInstances(exchange, true, true );
	}
	private List<ProcessInstance> getProcessInstances(Exchange exchange, boolean childProcesses) {
		return getProcessInstances(exchange, childProcesses, true );
	}
	private List<ProcessInstance> getProcessInstances(Exchange exchange, boolean childProcesses, boolean exception) {
		boolean hasCriteria = false;
		info(this,"findExecution:processCriteria:" + this.processCriteria + "/activityId:" + this.activityId);
		ExecutionQuery eq = this.runtimeService.createExecutionQuery();
		String processDefinitionId = getString(exchange, PROCESS_DEFINITION_ID, this.processCriteria.get(PROCESS_DEFINITION_ID));
		if (!isEmpty(processDefinitionId)) {
			eq.processDefinitionId(trimToEmpty(eval(processDefinitionId, exchange)));
			info(this,"getProcessInstances.processDefinitionId:"+trimToEmpty(eval(processDefinitionId,exchange)));
			hasCriteria=true;
		}
		String processDefinitionKey = getString(exchange, PROCESS_DEFINITION_KEY, this.processCriteria.get(PROCESS_DEFINITION_KEY));
		if (!isEmpty(processDefinitionKey)) {
			eq.processDefinitionKey(trimToEmpty(eval(processDefinitionKey,exchange)));
			info(this,"getProcessInstances.processDefinitionKey:"+trimToEmpty(eval(processDefinitionKey,exchange)));
			hasCriteria=true;
		}
		String processInstanceId = getString(exchange, PROCESS_INSTANCE_ID, this.processCriteria.get(PROCESS_INSTANCE_ID));
		if (!isEmpty(processInstanceId)) {
			eq.processInstanceId(trimToEmpty(eval(processInstanceId,exchange)));
			info(this,"getProcessInstances.processInstanceId:"+trimToEmpty(eval(processInstanceId,exchange)));
			hasCriteria=true;
		}
		String businessKey = getString(exchange, BUSINESS_KEY, this.businessKey);
		if (isEmpty(businessKey)) {
			businessKey = getString(exchange, BUSINESS_KEY, this.processCriteria.get(BUSINESS_KEY));
		}
		if (!isEmpty(businessKey)) {
			eq.processInstanceBusinessKey(trimToEmpty(eval(businessKey,exchange)),childProcesses);
			info(this,"getProcessInstances.businessKey:"+trimToEmpty(eval(businessKey,exchange)));
			hasCriteria=true;
		}

		String activityId = getString(exchange, ACTIVITY_ID, this.processCriteria.get(ACTIVITY_ID));
		if (!isEmpty(activityId)) {
			eq.activityId(trimToEmpty(eval(activityId,exchange)));
			hasCriteria=true;
		}
		String executionId = getString(exchange, EXECUTION_ID, this.processCriteria.get(EXECUTION_ID));
		if (!isEmpty(executionId)) {
			eq.executionId(trimToEmpty(eval(executionId,exchange)));
			hasCriteria=true;
		}
		String processVariable = this.processCriteria.get(PROCESSVARIABLE);
		if (!isEmpty(processVariable)) {
			processVariable = trimToEmpty(processVariable);
			List<String> tokens = splitByCommasNotInQuotes( processVariable);
			if( tokens.size() == 1){
				eq.processVariableValueEquals(trimToEmpty(eval(tokens.get(0),exchange)));
			}else{
				debug(this,"p1eval:"+eval(tokens.get(1),exchange));
				eq.processVariableValueEquals(
					trimToEmpty(tokens.get(0)),
					trimToEmpty(eval(tokens.get(1),exchange))
				);
			}
			hasCriteria=true;
		}

		if( hasCriteria ){
			info(this,"getProcessInstances.namespace:"+this.namespace);
			eq.executionTenantId(trimToEmpty(this.namespace));
			List<ProcessInstance> executionList = (List) eq.list();
			info(this,"getProcessInstances:" + executionList);
			if (exception && (executionList == null || executionList.size() == 0)) {
				throw new RuntimeException("ActivitiProducer.findProcessInstance:Could not find processInstance with criteria " + processCriteria);
			}
			return executionList;
		}
		return null;
	}

	private List<HistoricProcessInstance> getHistoricProcessInstances(Exchange exchange) {
		Map<String, Object> vars = getCamelVariablenMap(exchange);

		info(this,"findExecution:processCriteria:" + this.processCriteria );
		info(this,"findExecution:vars:" + vars);
		HistoricProcessInstanceQuery hq = this.historyService.createHistoricProcessInstanceQuery();
		String processDefinitionId = getString(exchange, PROCESS_DEFINITION_ID, this.processCriteria.get(PROCESS_DEFINITION_ID));
		if (!isEmpty(processDefinitionId)) {
			hq.processDefinitionId(trimToEmpty(eval(processDefinitionId, exchange)));
			info(this,"getProcessInstances.processDefinitionId:"+trimToEmpty(eval(processDefinitionId,exchange)));
		}
		String processDefinitionKey = getString(exchange, PROCESS_DEFINITION_KEY, this.processCriteria.get(PROCESS_DEFINITION_KEY));
		if (!isEmpty(processDefinitionKey)) {
			hq.processDefinitionKey(trimToEmpty(eval(processDefinitionKey,exchange)));
			info(this,"getProcessInstances.processDefinitionKey:"+trimToEmpty(eval(processDefinitionKey,exchange)));
		}
		String processInstanceId = getString(exchange, PROCESS_INSTANCE_ID, this.processCriteria.get(PROCESS_INSTANCE_ID));
		if (!isEmpty(processInstanceId)) {
			hq.processInstanceId(trimToEmpty(eval(processInstanceId,exchange)));
			info(this,"getProcessInstances.processInstanceId:"+trimToEmpty(eval(processInstanceId,exchange)));
		}
		String businessKey = getString(exchange, BUSINESS_KEY, this.businessKey);
		if (isEmpty(businessKey)) {
			businessKey = getString(exchange, BUSINESS_KEY, this.processCriteria.get(BUSINESS_KEY));
		}
		if (!isEmpty(businessKey)) {
			hq.processInstanceBusinessKey(trimToEmpty(eval(businessKey,exchange)));
			info(this,"getProcessInstances.businessKey:"+trimToEmpty(eval(businessKey,exchange)));
		}

		String finished = getString(exchange, FINISHED, this.processCriteria.get(FINISHED));
		if (!isEmpty(finished)) {
			boolean isFinished = toBoolean(finished);
			if( isFinished){
				hq.finished();
			}else{
				hq.unfinished();
			}
		}

		String processVariable = this.processCriteria.get(PROCESSVARIABLE);
		if (!isEmpty(processVariable)) {
			processVariable = trimToEmpty(processVariable);
			List<String> tokens = splitByCommasNotInQuotes( processVariable);
			if( tokens.size() == 1){
				hq.variableValueEquals(trimToEmpty(eval(tokens.get(0),exchange)));
			}else{
				hq.variableValueEquals(
					trimToEmpty(tokens.get(0)),
					trimToEmpty(eval(tokens.get(1),exchange))
				);
			}
		}

		hq.processInstanceTenantId(trimToEmpty(this.namespace));
		List<HistoricProcessInstance> piList = (List) hq.list();
		info(this,"getHistoricProcessInstances:" + piList);
		return piList;
	}

	private ProcessDefinition getProcessDefinition(ProcessInstance processInstance) {
		info(this,"getProcessDefinition:" + processInstance.getProcessDefinitionId() + "/ns:" + this.namespace);

		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
		if (processDefinition == null) {
			throw new RuntimeException("ActivitiProducer:getProcessDefinition:processDefinition not found:" + processInstance);
		}
		info(this,"getProcessDefinition:" + processDefinition + "/" + processDefinition.getTenantId());
		return processDefinition;
	}

	private void saveActivitiCamelCorrelationHistory(Exchange exchange, ProcessInstance processInstance) {
		EventAdmin eventAdmin = (EventAdmin) exchange.getContext().getRegistry().lookupByName(EventAdmin.class.getName());

		String aci = this.namespace + "/" + processInstance.getProcessDefinitionKey() + "/" + processInstance.getId() + "/" + this.activityId;
		exchange.setProperty(HISTORY_ACTIVITI_ACTIVITY_KEY, aci);

		String bc = (String) exchange.getIn().getHeader(Exchange.BREADCRUMB_ID);
		String routeDef = (String) exchange.getProperty(CAMEL_ROUTE_DEFINITION_KEY);
		Map props = new HashMap();
		props.put(HISTORY_TYPE, ACTIVITI_CAMEL_CORRELATION_TYPE);
		props.put(ACC_ACTIVITI_ID, aci);
		props.put(ACC_ROUTE_INSTANCE_ID, routeDef + "|" + bc);
		eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}

	private void printHistory(Exchange exchange) {
		List<MessageHistory> list = exchange.getProperty(Exchange.MESSAGE_HISTORY, List.class);
		ExchangeFormatter formatter = new ExchangeFormatter();
		formatter.setShowExchangeId(true);
		formatter.setMultiline(true);
		formatter.setShowHeaders(true);
		formatter.setStyle(ExchangeFormatter.OutputStyle.Fixed);
		String routeStackTrace = MessageHelper.dumpMessageHistoryStacktrace(exchange, formatter, true);
		info(this,routeStackTrace);
	}

	private void createLogEntry(Exchange exchange, ProcessDefinition processDefinition, Exception e) {
		EventAdmin eventAdmin = (EventAdmin) exchange.getContext().getRegistry().lookupByName(EventAdmin.class.getName());
		Map props = new HashMap();
		if (this.operation == ActivitiOperation.startProcess) {
			String key = processDefinition.getTenantId() + "/" + processDefinition.getId();
			props.put(HISTORY_KEY, key);
			props.put(HISTORY_TYPE, HISTORY_ACTIVITI_START_PROCESS_EXCEPTION);
			Map hint = new HashMap();
			hint.put("processDefinitionId", processDefinition.getId());
			hint.put("processDefinitionKey", processDefinition.getKey());
			hint.put("processDefinitionName", processDefinition.getName());
			hint.put("processDeploymentId", processDefinition.getDeploymentId());
			props.put(HISTORY_HINT, this.js.deepSerialize(hint));
		} else {
			props.put(HISTORY_KEY, this.activitiKey);
			props.put(HISTORY_TYPE, HISTORY_ACTIVITI_JOB_EXCEPTION);
		}
		Throwable r = getRootCause(e);
		props.put(HISTORY_MSG, r != null ? getStackTrace(r) : getStackTrace(e));
		eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}

	protected boolean isProcessStart() {
		return this.activityId == null;
	}

	private boolean isEmpty(String s) {
		return (s == null || "".equals(s.trim()));
	}
	private boolean isEmpty(List s) {
		return (s == null || s.size()==0);
	}

	private Map getCamelVariablenMap(Exchange exchange) {
		Map camelMap = new HashMap();
		Map exVars = ExchangeUtils.prepareVariables(exchange, true, true, true);
		camelMap.putAll(exVars);
		return camelMap;
	}
	private boolean toBoolean(String val) {
		return val.equalsIgnoreCase("true") || val.equalsIgnoreCase("t") || val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("y") || val.equals("1");
	}

	private String getString(Exchange e, String key, String def) {
		String value = ExchangeUtils.getParameter(def,e, String.class);
		info(this,"getString("+key+","+def+"):"+value+"/"+isEmpty(value));
		if( isEmpty(value)){
			value = e.getIn().getHeader(key, String.class);
		}
		return value;
	}

	private boolean getBoolean(Exchange e, String key, boolean def) {
		Boolean value = e.getIn().getHeader(key, Boolean.class);
		debug(this,"getBoolean:" + key + "=" + value + "/def:" + def);
		if (value == null) {
			value = e.getProperty(key, Boolean.class);
		}
		debug(this,"getBoolean2:" + key + "=" + value + "/def:" + def);
		return value != null ? value : def;
	}

	public Object convertTo(Object sourceObject, Class<?> targetClass) {
		if( targetClass==null){
			return sourceObject;
		}
		try {
			return ConvertUtils.convert(sourceObject, targetClass);
		} catch (Exception e) {
			error(this, "ConvertUtils.error:%[exception]s",e);
		}
		return sourceObject;
	}

	private String makeVariableName( String expr ){
		if( expr.indexOf( ".") == -1){
			return expr;
		}
		int dot = expr.lastIndexOf(".");
		String n = expr.substring(dot+1);
		return n.replaceAll("[^\\p{IsAlphabetic}^\\p{IsDigit}]", "");
	}

	private Pattern _splitSearchPattern = Pattern.compile("[\",]"); 
	private List<String> splitByCommasNotInQuotes(String s) {
		if (s == null){
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
		if (pos < s.length()){
			list.add(s.substring(pos));
		}
		return list;
	}

	protected ActivitiEndpoint getActivitiEndpoint() {
		return (ActivitiEndpoint) getEndpoint();
	}
}

