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
package org.ms123.common.process.converter;

import java.util.Map;
import java.util.Arrays;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.FormProperty;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.editor.language.json.converter.*;
import flexjson.*;

/**
 */
@SuppressWarnings("unchecked")
public class Simpl4UserTaskJsonConverter extends UserTaskJsonConverter {

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_jsPretty = new JSONSerializer();

	protected JSONSerializer m_js = new JSONSerializer();

	private final String FORMKEY_PROP = "formkey";
	private final String FORMKEY = "formkey";

	private final String CANDIDATEGROUPS_PROP = "candidategroups";
	private final String CANDIDATEGROUPS = "candidategroups";

	private final String CANDIDATEUSERS_PROP = "candidateusers";
	private final String CANDIDATEUSERS = "candidateusers";

	private final String ASSIGNEE_PROP = "assignee";
	private final String ASSIGNEE = "assignee";

	private final String VARMAPPING_PROP = "variablesmapping";
	private final String VARMAPPING = "variablesmapping";

	private final String FORMVARNAME_PROP = "formvarname";
	private final String FORMVARNAME = "formvarname";

	protected String getStencilId(FlowElement flowElement) {
		return "UserTask";
	}

	protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
		UserTask task = (UserTask) super.convertJsonToElement(elementNode, modelNode, shapeMap);
		m_jsPretty.prettyPrint(true);
		Map elementMap = (Map) m_ds.deserialize(elementNode.toString());
		Map<String, Object> propMap = (Map) elementMap.get("properties");
		String formkey = getString(propMap.get(FORMKEY_PROP));
		task.setFormKey("~"+formkey);
		String assignee = getString(propMap.get(ASSIGNEE_PROP));
		if( assignee != null){
			task.setAssignee(assignee);
		}
		String candidategroups = getString(propMap.get(CANDIDATEGROUPS_PROP));
		if (candidategroups != null) {
			task.setCandidateGroups(Arrays.asList(candidategroups.split(",")));
		}
		String candidateusers = getString(propMap.get(CANDIDATEUSERS_PROP));
		if (candidateusers != null) {
			task.setCandidateUsers(Arrays.asList(candidateusers.split(",")));
		}
		String variablesmapping = getVarMapping(propMap.get(VARMAPPING_PROP));
		if (variablesmapping != null) {
			FormProperty formProperty = new FormProperty();
			formProperty.setId(VARMAPPING);
			formProperty.setName(VARMAPPING);
			formProperty.setDefaultExpression("~" + variablesmapping);
			formProperty.setVariable("~" + variablesmapping);
			task.getFormProperties().add(formProperty);
		}
		String formVarname = getString(propMap.get(FORMVARNAME_PROP));
		if (formVarname != null) {
			FormProperty formProperty = new FormProperty();
			formProperty.setId(FORMVARNAME);
			formProperty.setName(FORMVARNAME);
			formProperty.setDefaultExpression("~"+formVarname);
			formProperty.setVariable("~"+formVarname);
			task.getFormProperties().add(formProperty);
		}
		return task;
	}

	public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
		fillJsonTypes(convertersToBpmnMap);
	}

	public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
		convertersToBpmnMap.put("UserTask", Simpl4UserTaskJsonConverter.class);
	}

	private String checkNull(String name, Object value) {
		if (value == null)
			throw new RuntimeException("Simpl4UserTaskJsonConverter:" + name + " is null");
		return value.toString();
	}

	private String getVarMapping(Object value) {
		if (value == null || value.toString().trim().length() == 0) {
			return null;
		}
		return m_js.deepSerialize(value);
	}

	private String getString(Object value) {
		if (value == null)
			return null;
		return value.toString();
	}
}
