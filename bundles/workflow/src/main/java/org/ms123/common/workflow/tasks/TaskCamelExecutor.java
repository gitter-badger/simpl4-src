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
package org.ms123.common.workflow.tasks;

import java.util.*;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.ms123.common.data.api.DataLayer;
import javax.transaction.UserTransaction;
import org.ms123.common.data.api.SessionContext;
import org.apache.commons.beanutils.*;
import org.ms123.common.store.StoreDesc;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.apache.commons.beanutils.PropertyUtils;
import flexjson.*;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_PROCESS_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_ACTIVITY_KEY;
import static org.ms123.common.rpc.CallService.ACTIVITI_CAMEL_PROPERTIES;

import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_NAME;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_EXECUTION_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_BUSINESS_KEY;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_DEFINITION_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_DEFINITION_NAME;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_INSTANCE_ID;

@SuppressWarnings({"unchecked","deprecation"})
public class TaskCamelExecutor extends TaskBaseExecutor implements JavaDelegate {

	private Expression namespace;
	private Expression routename;
	private Expression routevarname;

	private Expression returnvariable;
	private Expression returnmapping;

	private Expression variablesmapping;

	@Override
	public void execute(DelegateExecution execution) {
		TaskContext tc = new TaskContext(execution);
		showVariablenNames(tc);

		Object rno =  routename != null ? routename.getValue(execution) : null;
		Object rvo =  routevarname != null ? routevarname.getValue(execution) : null;

		String rn = null;
		String rv = null;
		if( !isEmpty(rno)){
			rn = getName(rno.toString());
		}else if( !isEmpty(rvo)){
			rv = getName(rvo.toString());
		}else{
			throw new RuntimeException(getExceptionInfo(tc)+"\nTaskServiceExecutor: servicename and servicevarname  not set");
		}

		try {
			String methodname = null;
			Map<String, Object> fparams = getParams(execution, variablesmapping, "routevar");
			if( rn != null){
				methodname = rn;
			}
			if( rv != null){
				methodname = (String)execution.getVariable(rv);
				if( methodname == null ){
					throw new RuntimeException(getExceptionInfo(tc)+"\nTaskServiceExecutor: servicename not set");
				}
			}
			
			Map<String,String> activitiProperties = new TreeMap<String,String>();
			activitiProperties.put(HISTORY_ACTIVITI_PROCESS_KEY, tc.getTenantId() +"/"+tc.getProcessDefinitionName()+"/"+execution.getProcessInstanceId());
			activitiProperties.put(HISTORY_ACTIVITI_ACTIVITY_KEY, tc.getTenantId() +"/"+tc.getProcessDefinitionName()+"/"+execution.getId()+"/"+execution.getCurrentActivityId());
			activitiProperties.put(WORKFLOW_ACTIVITY_ID, execution.getCurrentActivityId());
			activitiProperties.put(WORKFLOW_ACTIVITY_ID, execution.getCurrentActivityId());
			activitiProperties.put(WORKFLOW_ACTIVITY_NAME, execution.getCurrentActivityName());
			activitiProperties.put(WORKFLOW_EXECUTION_ID, execution.getId());
			activitiProperties.put(WORKFLOW_PROCESS_BUSINESS_KEY, execution.getProcessBusinessKey());
			activitiProperties.put(WORKFLOW_PROCESS_DEFINITION_ID, execution.getProcessDefinitionId());
			activitiProperties.put(WORKFLOW_PROCESS_DEFINITION_NAME, tc.getProcessDefinitionName());
			activitiProperties.put(WORKFLOW_PROCESS_INSTANCE_ID, execution.getProcessInstanceId());
			fparams.put(ACTIVITI_CAMEL_PROPERTIES, activitiProperties);
			log("TaskCamelExecutor("+methodname+"):"+fparams);


			String ns = namespace.getValue(execution).toString();
			if( "-".equals(ns)){
				ns = tc.getTenantId();
			}
			Object answer = getCallService().callCamel( ns+"."+methodname, fparams);
			if( returnvariable != null){
				String rvar = returnvariable.getValue(execution).toString();
				execution.setVariable(rvar, answer);
			}
			if( returnmapping != null && answer instanceof Map){
				String s = returnmapping.getValue(execution).toString();
				Object obj = m_ds.deserialize(s);
				List<Map<String,String>> mappingList = null;
				if( obj instanceof Map){
					mappingList = (List)((Map)obj).get("items");
				}else{
					mappingList = (List)obj;
				}
				Map<String,Object> answerMap = (Map)answer;
				for( Map<String,String> mapping: mappingList){
					String pvar = mapping.get("processvar");
					String svar = mapping.get("routevar");
					Object value = answerMap.get(svar);
					log("TaskCamelExecutor.returnsetting:"+pvar+" -> "+ value);
					execution.setVariable(pvar, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private String getName(String s) {
		if (s == null) {
			throw new RuntimeException("TaskCamelExecutor.routename is null");
		}
		return s;
	}
}

