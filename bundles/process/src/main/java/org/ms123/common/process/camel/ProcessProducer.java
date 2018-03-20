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
package org.ms123.common.process.camel;


import flexjson.*;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.MessageHistory;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.camel.api.ExchangeUtils;
import org.ms123.common.camel.trace.ExchangeFormatter;
import org.ms123.common.camel.trace.MessageHelper;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.system.thread.ThreadContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.ms123.common.system.history.HistoryService.ACC_ACTIVITI_ID;
import static org.ms123.common.system.history.HistoryService.ACC_ROUTE_INSTANCE_ID;
import static org.ms123.common.system.history.HistoryService.ACTIVITI_CAMEL_CORRELATION_TYPE;
import static org.ms123.common.system.history.HistoryService.CAMEL_ROUTE_DEFINITION_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_ACTIVITY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_JOB_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_START_PROCESS_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_HINT;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;

@SuppressWarnings({"unchecked", "deprecation"})
public class ProcessProducer extends DefaultProducer implements ProcessConstants,org.ms123.common.process.Constants {

	protected JSONSerializer js = new JSONSerializer();
	protected JSONDeserializer ds = new JSONDeserializer();
	private RuntimeService runtimeService;
	private HistoryService historyService;
	private TaskService taskService;
	private FormService formService;
	private RepositoryService repositoryService;
	private PermissionService permissionService;
	private CamelService camelService;
	private ProcessService processService;

	private ProcessOperation operation;
	private ProcessEndpoint endpoint;

	private Map<String, String> processCriteria;
	private Map<String, String> taskCriteria;
	private String taskOperation;
	private String taskId;
	private String activityId;
	private String namespace;
	private String headerFields;
	private String destination;
	private List<Map<String,String>> assignments;
	private String variableNames;
	private String businessKey;
	private String signalName;
	private String messageName;
	private String deleteReason;
	private boolean withMetadata;
	private boolean isSendSignal;
	private boolean isSendMessage;
	private boolean isCheckAssignments;

	private Map options;
	private String activitiKey;

	public ProcessProducer(ProcessEndpoint endpoint, ProcessService processService, PermissionService permissionService) {
		super(endpoint);
		this.endpoint = endpoint;
		this.permissionService = permissionService;
		this.processService = processService;
		this.camelService = (CamelService) endpoint.getCamelContext().getRegistry().lookupByName(CamelService.class.getName());
		String[] path = endpoint.getEndpointKey().split("(:|\\?)");
		info(this,"ProcessProducer.operation:" + (path[1].replace("//", "")));
		this.operation = ProcessOperation.valueOf(path[1].replace("//", ""));
		this.namespace = endpoint.getNamespace();
		this.signalName = endpoint.getSignalName();
		this.headerFields = endpoint.getHeaderFields();
		this.destination = endpoint.getDestination();
		this.assignments = endpoint.getAssignments();
		this.variableNames = endpoint.getVariableNames();
		this.businessKey = endpoint.getBusinessKey();
		this.messageName = endpoint.getMessageName();
		this.deleteReason = endpoint.getDeleteReason();
		this.isSendSignal = endpoint.isSendSignal();
		this.withMetadata = endpoint.withMetadata();
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
		this.runtimeService = getRuntimeService();
		this.historyService = getHistoryService();
		this.repositoryService = getRepositoryService();
		this.taskService = getTaskService();
		this.formService = getFormService();
		this.activityId = getString(exchange, ACTIVITY_ID, this.activityId);
		this.namespace = getString(exchange, NAMESPACE, this.namespace);
		if (isEmpty(this.namespace)) {
			this.namespace = (String) exchange.getContext().getRegistry().lookupByName("namespace");
		}
		if (isEmpty(this.namespace)) {
			this.namespace = (String) exchange.getProperty("_namespace");
		}
		this.namespace = trimToEmpty( this.namespace);
		info(this,"ProcessProducer("+isSingleton()+").operation:" + this.operation+"/namespace:"+this.namespace);
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
	private void invokeOperation(ProcessOperation operation, Exchange exchange) throws Exception {
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
			doQueryTasks(exchange, false);
			break;
		case getTaskVariables:
			doQueryTasks(exchange, true );
			break;
		case executeTaskOperation:
			doExecuteTaskOperation(exchange);
			break;
		case deleteProcessInstance:
			doDeleteProcessInstance(exchange);
			break;
		default:
			throw new RuntimeException("ProcessProducer.Operation not supported. Value: " + operation);
		}
	}

	private void doGetProcessVariables(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange, false, false );
		boolean withMetadata = getBoolean(exchange, "metadata", this.withMetadata);

		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();
		for( ProcessInstance pi : processInstanceList){
			pi = 	runtimeService.createProcessInstanceQuery().processInstanceId( pi.getId()).singleResult();
			if( pi == null){
				continue;
			}
			info(this,"doGetProcessVariables.processInstance:" + pi);
			Map<String, Object> processVariables = runtimeService.getVariables(pi.getProcessInstanceId());
			info(this,"doGetProcessVariables.variables:" + processVariables);
			String variableNames = getString(exchange, "variableNames", this.variableNames);
			if( isEmpty(variableNames)){
				Map<String,Object> m = new HashMap<String,Object>();
				m.putAll( processVariables );
				if( withMetadata){
					m.put("_processInstanceId", pi.getProcessInstanceId());
					m.put("_processDefinitionId", pi.getProcessDefinitionId());
					m.put("_businessKey", pi.getBusinessKey());
				}
				retList.add( m );
			}else{
				List<String> nameList = Arrays.asList(variableNames.split(","));
				debug(this,"doGetProcessVariables:nameList:" + nameList);
				Map<String,Object> vars = processVariables;
				Map<String,Object> ret = new HashMap();
				for (Map.Entry<String, Object> entry : vars.entrySet()) {
					if( nameList.indexOf( entry.getKey()) > -1){
						debug(this,"doGetProcessVariables:var:" + entry.getKey()+"="+entry.getValue());
						ret.put( entry.getKey(), entry.getValue());
					}
				}
				if( withMetadata){
					ret.put("_processInstanceId", pi.getProcessInstanceId());
					ret.put("_processDefinitionId", pi.getProcessDefinitionId());
					ret.put("_businessKey", pi.getBusinessKey());
				}
				retList.add( ret);
			}
		}
		//exchange.getIn().setBody(retList);
		ExchangeUtils.setDestination(this.destination, retList, exchange);
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
			String variableNames = getString(exchange, "variableNames", this.variableNames);
			if( !isEmpty(variableNames)){
				List<String> nameList = Arrays.asList(variableNames.split(","));
				List<HistoricVariableInstance> variableList = this.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(pi.getId()).list();
				Map<String,Object> varsSelected = new HashMap();
				for( HistoricVariableInstance vi : variableList){
					String name = vi.getName();
					Object value = vi.getValue();
					if( nameList.indexOf( name ) > -1){
						varsSelected.put( name, value);
					}
				}
				piMap.put("variables", varsSelected);
			}
			ret.add(piMap);
		}
		//exchange.getIn().setBody(ret);
		ExchangeUtils.setDestination(this.destination, ret, exchange);
	}

	private void doQueryTasks(Exchange exchange, boolean varsOnly) {
		TaskQuery taskQuery = this.taskService.createTaskQuery();
		buildTaskQuery( exchange, taskQuery);
		List<Task> taskList = taskQuery.list();
		info(this,"doQueryTasks.taskQuery.taskList:"+taskList);
		List<Map> ret = new ArrayList<Map>();
		Map<String,Boolean> tasksFound = new HashMap<String,Boolean>();
		for( Task task : taskList){
			ProcessDefinition pd = this.repositoryService.getProcessDefinition(task.getProcessDefinitionId());
			String processInstanceId = task.getProcessInstanceId();
			info(this,"\tTaskName:"+task.getName()+"/taskId"+task.getId()+"/taskDefKey:"+task.getTaskDefinitionKey()+"/processDefId:"+task.getProcessDefinitionId()+"/pdKey:"+pd.getKey());
			if( !pd.getKey().startsWith(this.namespace)){
				continue;
			}
			Map<String, Object> taskMap = createTaskMapFromTask(task, varsOnly);
			if( varsOnly==false){
				taskMap.put("processName", pd.getName());
			}
			
			Map<String,Object> varsSelected = getVariablesSelected(exchange, processInstanceId);
			if( varsSelected!= null){
				taskMap.put("variables", varsSelected);
			}
			ret.add(taskMap);
			tasksFound.put( task.getId(), true );
		}
		//search in history
		HistoricTaskInstanceQuery htaskQuery = this.getHistoryService().createHistoricTaskInstanceQuery();
		buildTaskQuery( exchange, htaskQuery);
		List<HistoricTaskInstance> htaskList = htaskQuery.list();
		info(this,"doQueryTasks.historicTaskQuery.taskList:"+htaskList);
		for( HistoricTaskInstance task : htaskList){
			if( tasksFound.get( task.getId())){
				info(this,"\tTask("+task.getId()+","+task.getName()+") already with TaskEntityQuery found");
				continue;
			}
			String processDefinitionId = task.getProcessDefinitionId();
			String processDefinitionKey = task.getProcessDefinitionKey();
			String processInstanceId = task.getProcessInstanceId();
			info(this,"processInstanceKey:"+processDefinitionKey);
			if( !processDefinitionKey.startsWith(this.namespace)){
				continue;
			}
			Map<String, Object> taskMap = createTaskMapFromHistoricTaskInstance(task, varsOnly);
			if( varsOnly==false){
				taskMap.put("processName", processDefinitionKey);
			}
			
			Map<String,Object> varsSelected = getVariablesSelected(exchange, processInstanceId);
			if( varsSelected!= null){
				taskMap.put("variables", varsSelected);
			}
			ret.add(taskMap);
		}
		ExchangeUtils.setDestination(this.destination, ret, exchange);
	}


	private Map<String, Object> createTaskMapFromTask( Task task,boolean varsOnly){
		Map<String, Object> taskMap = new HashMap();
		taskMap.put("id", task.getId());
		taskMap.put("name", task.getName());
		if( varsOnly) return taskMap;
		taskMap.put("assignee", task.getAssignee());
		taskMap.put("createTime", task.getCreateTime());
		taskMap.put("delegationState", task.getDelegationState());
		taskMap.put("description", task.getDescription());
		taskMap.put("dueDate", task.getDueDate());
		taskMap.put("executionId", task.getExecutionId());
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
		return taskMap;
	}
	private Map<String, Object> createTaskMapFromHistoricTaskInstance( HistoricTaskInstance task,boolean varsOnly){
		Map<String, Object> taskMap = new HashMap();
		taskMap.put("id", task.getId());
		taskMap.put("name", task.getName());
		if( varsOnly) return taskMap;
		taskMap.put("assignee", task.getAssignee());
		taskMap.put("description", task.getDescription());
		taskMap.put("dueDate", task.getDueDate());
		taskMap.put("executionId", task.getExecutionId());
		taskMap.put("owner", task.getOwner());
		taskMap.put("parentTaskId", task.getParentTaskId());
		taskMap.put("priority", task.getPriority());
		taskMap.put("processDefinitionId", task.getProcessDefinitionId());
		taskMap.put("processInstanceId", task.getProcessInstanceId());
		taskMap.put("taskDefinitionId", task.getTaskDefinitionKey());
		return taskMap;
	}
	private Map<String,Object> getVariablesSelected( Exchange exchange, String processInstanceId){
		String variableNames = getString(exchange, "variableNames", this.variableNames);
		if( !isEmpty(variableNames)){
			Map<String,Object> varsSelected = new HashMap();
			List<String> nameList = Arrays.asList(variableNames.split(","));
			List<HistoricVariableInstance> variableList = this.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
			for( HistoricVariableInstance vi : variableList){
				String name = vi.getName();
				Object value = vi.getValue();
				if( nameList.indexOf( name ) > -1){
					varsSelected.put( name, value);
				}
			}
			return varsSelected;
		}
		return null;
	}

	private void buildTaskQuery( Exchange exchange, Object taskQuery ){
		Class taskQueryClass = taskQuery.getClass();
		Method methods[] = taskQueryClass.getMethods();
		for (Map.Entry<String, String> entry : this.taskCriteria.entrySet()) {
			String  key = entry.getKey();
			if( "processVariable".equals(key)){
				key = "processVariableValueEquals";
			}
			if( "taskVariable".equals(key)){
				key = "taskVariableValueEquals";
			}
			String  val = entry.getValue();
			info(this,"doQueryTasks("+key+"):"+val);

			if( isEmpty(val)){
				Method m = getMethod(methods,key,0);
				if( m != null){
					setQueryValue( taskQuery, m, null);	
				}
			}else{
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
						setQueryValue(taskQuery, m, eval(trimToEmpty(tokens.get(0)),exchange,String.class), eval(tokens.get(1),exchange,m.getParameterTypes()[1]));
					}
				}
			}
		}
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
			Object[] args = null;
			if( val == null){
				info(this,"setQueryValue("+m.getName()+")");
				args = new Object[0];
			}else{
				info(this,"setQueryValue("+m.getName()+"):"+val);
				args = new Object[1];
				args[0] = val;
			}
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

	private void doDeleteProcessInstance(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange);
		doDeleteProcessInstance(exchange, processInstanceList);
	}
	private void doDeleteProcessInstance(Exchange exchange,List<ProcessInstance> processInstanceList) {
		String deleteReason = getString(exchange, "deleteReason", this.deleteReason);
		info(this,"processInstanceList:" + processInstanceList);
		info(this,"deleteReason:" + deleteReason);
		Map<String,Object> processVariables = getProcessVariables(exchange);
		if( processInstanceList != null){
			for( ProcessInstance pi : processInstanceList){
				info(this,"PI:"+pi.getId());
				Execution execution = runtimeService.createExecutionQuery() .executionId(pi.getId()).singleResult(); 
				info(this,"\tExecution:"+execution);
				if( execution != null){
					info(this,"doDeleteProcess:"+messageName+"/"+execution.getId());
					if( processVariables != null){
						this.runtimeService.setVariables( execution.getId(), processVariables);
					}
					this.runtimeService.deleteProcessInstance( execution.getId(), deleteReason);
				}
			}
		}
	}


	private void doExecuteTaskOperation(Exchange exchange) {
		String taskOperation = getString(exchange, "taskOperation", this.taskOperation);
		String taskId = getString(exchange, "taskId", this.taskId);
		Map<String,Object> pv = getProcessVariables(exchange);
		info(this,"doExecuteTaskOperation("+taskOperation+","+taskId+".variables:"+pv);
		
		Map<String,Object> tv = getTaskAssignments(exchange);
		if( tv != null && tv.size() > 0){
			getTaskService().setVariablesLocal( taskId, tv );
		}
		if( !"complete".equals(taskOperation)){ //This becames in "complete" handled
			if( pv != null && pv.size() > 0){
				getTaskService().setVariables( taskId, pv );
			}
		}
		if( !"setVariables".equals(taskOperation)){ 
			this.processService.executeTaskOperation( taskId, taskOperation, pv, this.isCheckAssignments );
		}
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
				String tenant = ThreadContext.getThreadContext().getUserName();
				info(this,"ExecutionEntity("+tenant+"):" + (pi instanceof ExecutionEntity));
				if( pi instanceof ExecutionEntity ){
					((ExecutionEntity)pi).setTenantId(tenant);
				}
				this.activitiKey += "/" + pi.getId();
				debug(this,"m_activitiKey:" + this.activitiKey);
				ExchangeUtils.setDestination(this.destination, pi.getId(), exchange);
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
				/*@@@MS} catch (org.camunda.bpm.engine.ActivitiOptimisticLockingException e) {
					info(this,"SignalThread:" + e);
					try {
						Thread.sleep(100L);
					} catch (Exception x) {
					}
					continue;*/
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
		String user = ThreadContext.getThreadContext().getUserName();
		info(this,"ExecuteProcess:user:" + user);
		//ThreadContext.loadThreadContext(this.namespace, "admin");
		//this.permissionService.loginInternal(this.namespace);
		try {
			ProcessInstantiationBuilder pib = null;
			String processDefinitionId = getString(exchange, PROCESS_DEFINITION_ID, this.processCriteria.get(PROCESS_DEFINITION_ID));
			if (!isEmpty(processDefinitionId)) {
				info(this,"ExecuteProcess:processDefinitionId:" + processDefinitionId);
				pib = this.runtimeService.createProcessInstanceById(processDefinitionId);
				processDefinition = this.repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
				if (processDefinition == null) {
					throw new RuntimeException("No process with processDefinitionId(" + processDefinitionId + ")");
				}
			}
			String processDefinitionKey = getString(exchange, PROCESS_DEFINITION_KEY, this.processCriteria.get(PROCESS_DEFINITION_KEY));
			if (!isEmpty(processDefinitionKey)) {
				info(this,"ExecuteProcess:processDefinitionKey:" + processDefinitionKey);
				pib = this.runtimeService.createProcessInstanceByKey(processDefinitionKey);
				processDefinition = this.repositoryService.createProcessDefinitionQuery().latestVersion().processDefinitionKey(processDefinitionKey).singleResult();
				if (processDefinition == null) {
					throw new RuntimeException("No process with processDefinitionKey(" + processDefinitionKey + ")");
				}
			}
			if( pib == null){
					throw new RuntimeException("No process with processDefinitionId("+processDefinitionId+") or processDefinitionKey(" + processDefinitionKey + ")");
			}
			String businessKey = getString(exchange, BUSINESS_KEY, this.businessKey);
			if (!isEmpty(businessKey)) {
				info(this,"ExecuteProcess:businessKey:" + businessKey);
				pib.businessKey(businessKey);
			}

			for (Map.Entry<String, Object> entry : vars.entrySet()) {
				info(this,"ExecuteProcess:var:" + entry.getKey()+"="+entry.getValue());
				pib.setVariable(entry.getKey(), entry.getValue());
			}
			setInitialParameter( processDefinition, pib );
			return pib.execute();
		} finally {
			ThreadContext.getThreadContext().finalize(null);
			info(this,"EndProcess:" + this.processCriteria + "/" + this.namespace);
		}
	}

	private void setInitialParameter(ProcessDefinition processDefinition, ProcessInstantiationBuilder pib){
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
					pib.setVariable(name, value);
				}
			}
			//pib.addVariable("__currentUser", uid);
			//pib.addVariable("__startingUser", uid);
			pib.setVariable("__namespace", this.namespace);
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
			throw new RuntimeException("ProcessProducer.parse:"+e.getMessage()+" -> "+ expr);
		}
	}

	private <T> T eval(String str, Exchange exchange, Class<T> clazz) {
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
		info(this,"getProcessVariables1:"+processVariables);
		processVariables.putAll( getProcessAssignments(exchange) );
		info(this,"getProcessVariables2:"+processVariables);
		return processVariables;
	}

	private Map<String,Object> getProcessAssignments(Exchange exchange){
		return ExchangeUtils.getAssignments( exchange, this.assignments);
	}
	private Map<String,Object> getTaskAssignments(Exchange exchange){
		return ExchangeUtils.getAssignments( exchange, this.assignments, true);
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
			eq.processInstanceBusinessKey(trimToEmpty(eval(businessKey,exchange))/*@@@MS,childProcesses*/);
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
		List<String> processVariables = this.endpoint.getProcessCriteriaVar();
		for( String processVariable : processVariables){
			hasCriteria=true;
			processVariable = trimToEmpty(processVariable);
			List<String> tokens = splitByCommasNotInQuotes( processVariable);
			if( tokens.size() == 1){
				eq.processVariableValueEquals(processVariable, eval(trimToEmpty(tokens.get(0)),exchange,Object.class));
			}else{
				String name = eval(trimToEmpty(tokens.get(0)),exchange,String.class);
				Object value = eval(trimToEmpty(tokens.get(1)),exchange,Object.class);
				if( name == null ) continue;
				if( value == null ) value="";
				info(this,"setProcessVariable("+name+"):"+value);
				eq.processVariableValueEquals( name, value);
			}
		}

		if( true ){
			info(this,"getProcessInstances.namespace:"+this.namespace);
			List<ProcessInstance> executionList = (List) eq.list();
			info(this,"getProcessInstances:" + executionList);
			if (exception && (executionList == null || executionList.size() == 0)) {
				throw new RuntimeException("ProcessProducer.findProcessInstance:Could not find processInstance with criteria " + processCriteria);
			}
			List<ProcessInstance> retList = new ArrayList<ProcessInstance>();
			for( ProcessInstance pi : executionList){
				ProcessDefinition pd = this.repositoryService.createProcessDefinitionQuery().processDefinitionId(pi.getProcessDefinitionId()).singleResult();
				if( !pd.getKey().startsWith(this.namespace)){
					info(this,"getProcessInstances:processDefinitionKey("+pd.getKey()+")  don't starts with namespace(" + this.namespace+")");
					continue;
				}
				retList.add( pi );
			}
			return retList;
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

		List<String> processVariables = this.endpoint.getProcessCriteriaVar();
		for( String processVariable : processVariables){
			processVariable = trimToEmpty(processVariable);
			List<String> tokens = splitByCommasNotInQuotes( processVariable);
			if( tokens.size() == 1){
				hq.variableValueEquals(processVariable,eval(trimToEmpty(tokens.get(0)),exchange,Object.class));
			}else{
				String name = eval(trimToEmpty(tokens.get(0)),exchange,String.class);
				Object value = eval(trimToEmpty(tokens.get(1)),exchange,Object.class);
				if( name == null ) continue;
				if( value == null ) value="";
				info(this,"setProcessVariable("+name+"):"+value);
				hq.variableValueEquals( name, value);
			}
		}

		List<HistoricProcessInstance> piList = (List) hq.list();
		info(this,"getHistoricProcessInstances:" + piList);
		List<HistoricProcessInstance> retList = new ArrayList<HistoricProcessInstance>();
		for( HistoricProcessInstance pi : piList){
			if( !pi.getProcessDefinitionKey().startsWith(trimToEmpty(this.namespace))){
				info(this,"getHistoricProcessInstances:processDefinitionId("+pi.getProcessDefinitionKey()+")  don't starts with namespace(" + trimToEmpty(this.namespace)+")");
				continue;
			}
			retList.add( pi );
		}
		return retList;
	}

	private ProcessDefinition getProcessDefinition(ProcessInstance processInstance) {
		info(this,"getProcessDefinition:" + processInstance.getProcessDefinitionId() + "/ns:" + this.namespace);

		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
		if (processDefinition == null) {
			throw new RuntimeException("ProcessProducer:getProcessDefinition:processDefinition not found:" + processInstance);
		}
		String namespace = processDefinition.getKey().substring(0, processDefinition.getKey().indexOf(NAMESPACE_DELIMITER));
		info(this,"getProcessDefinition:" + processDefinition + "/" + namespace);
		return processDefinition;
	}

	private void saveActivitiCamelCorrelationHistory(Exchange exchange, ProcessInstance processInstance) {
		EventAdmin eventAdmin = (EventAdmin) exchange.getContext().getRegistry().lookupByName(EventAdmin.class.getName());

		String aci = this.namespace + "/" + processInstance.getProcessDefinitionId()/*@@@MS*/ + "/" + processInstance.getId() + "/" + this.activityId;
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
		if (this.operation == ProcessOperation.startProcess) {
			String namespace = processDefinition.getKey().substring(0, processDefinition.getKey().indexOf(NAMESPACE_DELIMITER));
			String key = namespace + "/" + processDefinition.getId();
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

	protected ProcessEndpoint getProcessEndpoint() {
		return (ProcessEndpoint) getEndpoint();
	}
	private FormService getFormService(){
		return this.processService.getProcessEngine().getFormService();
	}
	private TaskService getTaskService(){
		return this.processService.getProcessEngine().getTaskService();
	}
	private RepositoryService getRepositoryService(){
		return this.processService.getProcessEngine().getRepositoryService();
	}
	private RuntimeService getRuntimeService(){
		return this.processService.getProcessEngine().getRuntimeService();
	}
	private HistoryService getHistoryService(){
		return this.processService.getProcessEngine().getHistoryService();
	}
}

