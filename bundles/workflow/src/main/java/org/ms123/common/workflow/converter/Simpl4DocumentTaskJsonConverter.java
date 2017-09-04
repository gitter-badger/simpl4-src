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
package org.ms123.common.workflow.converter;

import java.util.Map;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.editor.language.json.converter.*;
import flexjson.*;

/**
 */
@SuppressWarnings("unchecked")
public class Simpl4DocumentTaskJsonConverter extends BaseBpmnJsonConverter {

	protected JSONDeserializer m_ds = new JSONDeserializer();

	private final String DOCUMENTNAME_PROP = "documentname";
	private final String FILENAME_PROP = "filename";
	private final String VARMAPPING_PROP = "variablesmapping";



	protected String getStencilId(BaseElement flowElement) {
		return "DocumentTask";
	}

	protected void convertElementToJson(ObjectNode propertiesNode, BaseElement flowElement) {
		ServiceTask serviceTask = (ServiceTask) flowElement;
	}

	protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
		ServiceTask task = new ServiceTask();

		String clazz = Simpl4BpmnJsonConverter.getFullnameForTask("TaskDocumentExecutor");
		task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		task.setImplementation(clazz);

		addField(VARMAPPING_PROP, elementNode, task);
		addField(DOCUMENTNAME_PROP, elementNode, task);
		addField(FILENAME_PROP, elementNode, task);
		return task;
	}

	public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
		fillJsonTypes(convertersToBpmnMap);
	}

	public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
		convertersToBpmnMap.put("DocumentTask", Simpl4DocumentTaskJsonConverter.class);
	}

	private String checkNull(String name, Object value) {
		if (value == null)
			throw new RuntimeException("Simpl4DocumentTaskJsonConverter:" + name + " is null");
		return value.toString();
	}
	private String getValue(String name, Object value) {
		if (value == null){
			return null;
		}
		return value.toString();
	}
	private String checkEmpty(String name, Object value) {
		if (value == null)
			throw new RuntimeException("Simpl4DocumentTaskJsonConverter:" + name + " is null");
		String val=value.toString();
		if( val.trim().length() ==0){
			throw new RuntimeException("Simpl4DocumentTaskJsonConverter:" + name + " is empty");
		}
		return val;
	}
	protected void addField(String name, JsonNode elementNode, ServiceTask task) {
		FieldExtension field = new FieldExtension();
		field.setFieldName(name);
		String value = getPropertyValueAsString(name, elementNode);
		if (StringUtils.isNotEmpty(value)) {
			if ((value.contains("${") || value.contains("#{")) && value.contains("}")) {
				field.setExpression(value);
			} else {
				field.setStringValue(value);
			}
		}
		task.getFieldExtensions().add(field);
	}
}
