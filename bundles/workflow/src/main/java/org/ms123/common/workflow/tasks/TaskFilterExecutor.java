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

public class TaskFilterExecutor extends TaskBaseExecutor implements JavaDelegate {

	private Expression filtername;
	private Expression filterobject;

	private Expression filtervarname;

	private Expression variablesmapping;

	@Override
	public void execute(DelegateExecution execution) {
		TaskContext tc = new TaskContext(execution);
		showVariablenNames(tc);
		SessionContext sc = getSessionContext(tc);

		Object fno =  filtername != null ? filtername.getValue(execution) : null;
		Object fvo =  filterobject != null ? filterobject.getValue(execution) : null;
		Object fvn =  filtervarname != null ? filtervarname.getValue(execution) : null;

		String fn = null;
		String fv = null;
		if( !isEmpty(fno)){
			fn = getName(fno.toString());
		}else if( !isEmpty(fvo)){
			fv = getName(fvo.toString());
		}else{
			throw new RuntimeException(getExceptionInfo(tc)+"\nTaskFilterExecutor.filtername and filterobject  is null");
		}

		String ln = checkAndGet(fvn,"Filtervarname");
		UserTransaction tx = sc.getUserTransaction();
		List ret = null;
		try {
			Map<String, Object> fparams = getParams(execution, variablesmapping, "filtervar");
			if( fn != null){
				Map retMap = sc.executeNamedFilter(fn, fparams);
				if (retMap == null) {
					ret = new ArrayList();
				} else {
					ret = (List) retMap.get("rows");
				}
			}
			if( fv != null){
				Map filterMap = (Map)execution.getVariable(fv);
				if( filterMap == null ){
					throw new RuntimeException(getExceptionInfo(tc)+"\nTaskFilterExecutor.filter is null");
				}
				Map retMap = sc.executeFilter(filterMap, fparams);
				if (retMap == null) {
					ret = new ArrayList();
				} else {
					ret = (List) retMap.get("rows");
				}
			}
			System.out.println("TaskFilterExecutor.setting:" + ln + "=" + ret);
			execution.setVariable(ln, ret);
		} catch (Exception e) {
			sc.handleException(tx, e);
		} finally {
			sc.handleFinally(tx);
		}
	}
	private String checkAndGet(Object s, String error) {
		if (isEmpty(s)) {
			throw new RuntimeException("TaskFilterExecutor."+error+" is required");
		}
		return s.toString();
	}

	private String getName(String s) {
		if (s == null) {
			throw new RuntimeException("TaskFilterExecutor.filtername or filterobject is required");
		}
		if (s.indexOf(",") == -1){
			return s;
		}
		return s.split(",")[1];
	}
}

