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
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.delegate.Expression;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.scripting.ScriptingEngines;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.ms123.common.data.api.DataLayer;
import javax.transaction.UserTransaction;
import org.ms123.common.data.api.SessionContext;
import org.apache.commons.beanutils.*;
import org.ms123.common.store.StoreDesc;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.history.HistoricProcessInstance;
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
//@@@MS			activitiProperties.put(WORKFLOW_ACTIVITY_NAME, execution.getCurrentActivityName());
			activitiProperties.put(WORKFLOW_EXECUTION_ID, execution.getId());
			activitiProperties.put(WORKFLOW_PROCESS_BUSINESS_KEY, execution.getProcessInstanceBusinessKey());
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

