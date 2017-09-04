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
package org.ms123.common.process.engineapi.task;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.form.FormProperty;
import org.ms123.common.libhelper.Utils;
import groovy.lang.*;
import org.codehaus.groovy.control.*;
import flexjson.*;
import org.ms123.common.process.api.ProcessService;
import org.apache.commons.beanutils.PropertyUtils;
import org.ms123.common.process.engineapi.BaseResource;
import org.ms123.common.process.engineapi.Util;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
@SuppressWarnings("unchecked")
public class TaskFormPropertiesResource extends BaseResource {

	JSONDeserializer ds = new JSONDeserializer();
	JSONSerializer js = new JSONSerializer();

	private String m_executionId;

	private String m_taskId;

	/**
	 */
	public TaskFormPropertiesResource(ProcessService ps, String executionId, String taskId) {
		super(ps, null);
		m_executionId = executionId;
		m_taskId = taskId;
	}

	public Map getTaskFormProperties() {
		Map<String, Object> taskFormMap = new HashMap();
		Map pv = null;
		if (m_executionId != null) {
			pv = getPE().getRuntimeService().getVariables(m_executionId);
			js.prettyPrint(true);
			info(this,"getTaskFormProperties.pv:"+js.deepSerialize(pv));
		}
		TaskFormData formData = getPE().getFormService().getTaskFormData(m_taskId);
		Map values = new HashMap();
		if (formData != null) {
			List<FormProperty> formProperties = formData.getFormProperties();
			for (FormProperty fp : formProperties) {
				info(this,"fp::" + fp.getName() + "/" + fp.getValue());
				String value = fp.getValue();
				if( value !=null && value.length() > 0 && value.startsWith("~")){
					value = value.substring(1);
				}
				if (pv != null && "variablesmapping".equals(fp.getName())) {
					Map v = (Map) ds.deserialize(value);
					List<Map> items = (List) v.get("items");
					for (Map item : items) {
						String direction = (String)item.get("direction");
						if (direction == null || direction.equals("incoming")) {
							String processvar = (String) item.get("processvar");
							String formvar = (String) item.get("formvar");
							Object val = getProcessVariableValue(pv, processvar);
							values.put(formvar, val);
						}
					}
				} else {
					taskFormMap.put(fp.getName(), value);
				}
				taskFormMap.put("values", values);
			}
		}
		info(this,"<<<---TaskFormProperties:"+taskFormMap);
		return taskFormMap;
	}

	private Object eval(String scriptStr, Map vars) {
		info(this,"eval:" + scriptStr);
		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(org.ms123.common.workflow.api.GroovyTaskDslBase.class.getName());
		Binding binding = new MyBinding(vars);
		GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), binding, config);
		return shell.evaluate(scriptStr);
	}

	private Object getProcessVariableValue(Map processVariables, String processvar) {
		if (processvar.indexOf("${") == 0) {
			String expr = processvar.substring(2, processvar.length() - 1);
			try {
				Object val = eval(expr, processVariables);
				info(this,"val:" + val);
				return val;
			} catch (Throwable e) {
				e.printStackTrace();
				error(this, "getProcessVariableValue:%[exception]s",e);
				String msg = Utils.formatGroovyException(e,expr);
				throw new RuntimeException(msg);
			}
		} else {
			info(this,"_getProcessVariableValue:" + processvar);
			if (processvar.indexOf(".") == -1) {
				return processVariables.get(processvar);
			}
			String[] parts = processvar.split("\\.");
			Object o = processVariables;
			for (int i = 0; i < parts.length; i++) {
				try {
					o = PropertyUtils.getProperty(o, parts[i]);
				} catch (Exception e) {
					throw new RuntimeException("TaskFormMapping.Exception:" + e.getMessage());
				}
				if (i < (parts.length - 1) && o == null) {
					throw new RuntimeException("TaskFormMapping:processvar.not_exists: " + processvar + " (" + parts[i] + ")");
				}
			}
			return o;
		}
	}

	private class MyBinding extends Binding {

		public MyBinding(Map vars) {
			super(vars);
		}

		public Object getVariable(String name) {
			if (super.hasVariable(name)) {
				return super.getVariable(name);
			}
			info(this,"getVariable.not_defined:" + name);
			return null;
		}
	}

	public abstract static class ScriptBase extends Script {

		public ScriptBase() {
			info(this,"ScriptBase");
		}
	}
}
