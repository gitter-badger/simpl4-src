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
package org.ms123.common.activiti.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.ms123.common.permission.api.PermissionService;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.activiti.BaseResource;
import org.ms123.common.activiti.Util;
import org.activiti.engine.form.TaskFormData;
import org.ms123.common.form.FormService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.repository.ProcessDefinition;
import flexjson.*;
import org.apache.commons.beanutils.*;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
@SuppressWarnings("unchecked")
public class TaskOperationResource extends BaseResource {

	private String m_taskId;
	JSONDeserializer ds = new JSONDeserializer();
	JSONSerializer js = new JSONSerializer();

	private String m_operation;

	private Map<String, Object> m_startParams;

	public TaskOperationResource(ActivitiService as, String taskId, String operation, Map<String, Object> startParams) {
		super(as, null);
		m_taskId = taskId;
		m_operation = operation;
		m_startParams = startParams;
	}

	public Map executeTaskOperation() {
		checkPermission(m_taskId);
		if ("claim".equals(m_operation)) {
			String userId = org.ms123.common.system.thread.ThreadContext.getThreadContext().getUserName();
			getPE().getTaskService().claim(m_taskId, userId);
		} else if ("unclaim".equals(m_operation)) {
			getPE().getTaskService().claim(m_taskId, null);
		} else if ("complete".equals(m_operation)) {
			if( m_startParams==null) m_startParams= new HashMap();
			Map<String, Object> variables = m_startParams;
			variables.remove("taskId");
			//getPE().getFormService().submitTaskFormData(m_taskId, variables);

			TaskFormData taskFormData = getPE().getFormService().getTaskFormData(m_taskId);
			List<FormProperty> userProperties = taskFormData.getFormProperties();
			String formVar = null;
			String variablesMapping = null;
			for(FormProperty fp : userProperties){
				info(this,"TaskOperationResource.FormProperty:"+fp.getId()+"="+fp.getValue());
				if( "formvarname".equals(fp.getId())){
					formVar = fp.getValue();
				}
				if( "variablesmapping".equals(fp.getId())){
					variablesMapping = fp.getValue();
				}
			}
			Map<String, Object> newVariables = new HashMap();
			if (taskFormData != null) {
				String formKey = taskFormData.getFormKey();

				Task task = getPE().getTaskService().createTaskQuery().taskId(m_taskId).singleResult();
				String taskName = task.getName();
				String executionId = task.getExecutionId();
				String pid = task.getProcessInstanceId();
				String processDefinitionId = task.getProcessDefinitionId();
				ProcessDefinition processDefinition = getPE().getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
				String tenantId = processDefinition.getTenantId();
				String processDefinitionKey = processDefinition.getKey();

				String namespace=tenantId;
				info(this,"TaskOperationResource.formVar:"+formVar);
				if( formVar == null || formVar.length()==0 ){
					formVar = getFormVar(namespace,formKey);
				}
				Map data = (Map)variables.get(getFormKeyWithoutExtension(formKey));
				info(this,"formKey:"+formKey+"/formVar:" + formVar +"/"+data+"/"+taskName);
				if( data != null){
					List<String> assetList = getGitService().assetList(namespace,formKey, "sw.form", true);
					List<Map> errors=null;
					Map ret = null;
					if( assetList.size() > 0){
						ret  = getFormService().validateForm(namespace,formKey,data,true);				
						errors = (List)ret.get("errors");
					}else{
						errors = new ArrayList<Map>();
						ret = new HashMap();
						ret.put("cleanData", data);
					}
					info(this,"data:"+ret);
					if( errors.size()>0){
						Map successNode = new HashMap();
						successNode.put("success", false);
						successNode.put("errors", errors);
						return successNode;
					}else{
						data = (Map)ret.get("cleanData");
						if( !"-".equals(formVar)){
							newVariables.put(formVar,data);
						}
						setMapping(newVariables,  data, variablesMapping, executionId);
						String script = (String)ret.get("postProcess");
						if( script!=null && script.trim().length()> 2){
							getWorkflowService().executeScriptTask( executionId, tenantId, processDefinitionKey, pid, script, newVariables, taskName );
							if( data.get("errors") != null ){
								Object _errors = data.get("errors");
								Map successNode = new HashMap();
								successNode.put("success", false);
								if( _errors instanceof List){
									successNode.put("errors", _errors);
								}else{
									List errorList = new ArrayList();
									Map error = new HashMap();
									if( _errors instanceof String){
										error.put("message", _errors);
									}else{
										error.put("message", "Unknown error");
									}
									errorList.add(error);
									successNode.put("errors", errorList);
								}
								return successNode;
							}
						}
					}
				}
			}

			info(this,"newVariables:"+js.deepSerialize(newVariables));
			getPE().getTaskService().complete(m_taskId, newVariables);
		} else if ("assign".equals(m_operation)) {
			String userId = org.ms123.common.system.thread.ThreadContext.getThreadContext().getUserName();
			getPE().getTaskService().setAssignee(m_taskId, userId);
		} else {
			throw new RuntimeException("'" + m_operation + "' is not a valid operation");
		}
		Map successNode = new HashMap();
		successNode.put("success", true);
		return successNode;
	}
	private void setMapping(Map<String,Object> newVariables, Map formData, String variablesMapping, String executionId){
		if( executionId == null) return;
		Map v = (Map) ds.deserialize(variablesMapping);
		List<Map> items = (List) v.get("items");
		for (Map item : items) {
			String direction = (String)item.get("direction");
			if (direction != null && direction.equals("outgoing")) {
				String processvar = (String) item.get("processvar");
				String formvar = (String) item.get("formvar");
				try{
					Object o = PropertyUtils.getProperty(formData, formvar);
					setValue(executionId, newVariables, processvar, o);
				}catch(Exception e){
					e.printStackTrace();
					throw new RuntimeException("TaskOperationResource.setMapping:", e);
				}
			}
		}
	}
	protected void setValue(String executionId, Map<String,Object> newVariables, String processvar, Object value) throws Exception {
		if (processvar.indexOf(".") == -1) {
			newVariables.put(processvar, value);
			return;
		}
		String[] parts = processvar.split("\\.");
		Object o = getPE().getRuntimeService().getVariable(executionId,parts[0]);
		if (o == null) {
			o = new HashMap();
		}
		newVariables.put(parts[0], o);
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
	private void checkPermission(String taskId){
		Task task = getPE().getTaskService().createTaskQuery().taskId(m_taskId).singleResult();
		String assignee = task.getAssignee();
		String owner = task.getOwner();
		if( owner != null){
			checkUser( owner );
		}else if( assignee != null){
			checkUser( assignee );
		}else{
			throw new RuntimeException("TaskOperationResource.checkPermission:assignee and owner are null");
		}
	}

	private String getFormKeyWithoutExtension(String formKey){
		if( formKey.endsWith(".form")){
			return formKey.substring(0,formKey.length()-5);
		}
		return formKey;
	}
	private String getFormVar(String namespace,String formKey){
		String formVar=null;
		try{
			formVar = getFormService().getFormName(namespace,formKey);				
		}catch(Exception e){
			throw new RuntimeException("TaskOperationResource:cannot get formVar:",e);
		}
		info(this,"TaskOperationResource:"+formVar);
		if( formVar == null){
			throw new RuntimeException("TaskOperationResource:formVar is null");
		}
		return formVar;
	}
}
