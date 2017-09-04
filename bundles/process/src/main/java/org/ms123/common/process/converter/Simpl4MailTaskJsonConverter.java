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
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.editor.language.json.converter.*;
import flexjson.*;

/**
 */
public class Simpl4MailTaskJsonConverter extends BaseBpmnJsonConverter {

	private final String PROPERTY_MAILTASK_TO = "to";

	private final String PROPERTY_MAILTASK_FROM = "from";

	private final String PROPERTY_MAILTASK_SUBJECT = "subject";
	private final String PROPERTY_MAILTASK_ATTACHMENT = "attachment";

	private final String PROPERTY_MAILTASK_CC = "cc";

	private final String PROPERTY_MAILTASK_BCC = "bcc";

	private final String PROPERTY_MAILTASK_TEXT = "text";

	private final String PROPERTY_MAILTASK_HTML = "html";

	public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
		fillJsonTypes(convertersToBpmnMap);
		fillBpmnTypes(convertersToJsonMap);
	}

	public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
		convertersToBpmnMap.put("MailTask", Simpl4MailTaskJsonConverter.class);
	}

	public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
	}

	protected String getStencilId(BaseElement flowElement) {
		return "MailTask";
	}

	protected void convertElementToJson(ObjectNode propertiesNode, BaseElement flowElement) {
	}

	protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
		ServiceTask task = new ServiceTask();
		//task.setType(ServiceTask.MAIL_TASK);

		String clazz = Simpl4BpmnJsonConverter.getFullnameForTask("TaskMailExecutor");
		task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		task.setImplementation(clazz);

		addField(PROPERTY_MAILTASK_TO, elementNode, task);
		addField(PROPERTY_MAILTASK_FROM, elementNode, task);
		addField(PROPERTY_MAILTASK_SUBJECT, elementNode, task);
		addField(PROPERTY_MAILTASK_ATTACHMENT, elementNode, task);
		addField(PROPERTY_MAILTASK_CC, elementNode, task);
		addField(PROPERTY_MAILTASK_BCC, elementNode, task);
		addField(PROPERTY_MAILTASK_TEXT, elementNode, task);
		addField(PROPERTY_MAILTASK_HTML, elementNode, task);
		return task;
	}

	protected void addField(String name, JsonNode elementNode, ServiceTask task) {
		FieldExtension field = new FieldExtension();
		field.setFieldName(name);
		String value = getPropertyValueAsString(name, elementNode);
		if (StringUtils.isNotEmpty(value)) {
			if ((value.contains("${") || value.contains("#{")) && value.contains("}")) {
				field.setExpression(value);
			} else {
				field.setExpression(value);
			}
		}
		task.getFieldExtensions().add(field);
	}
}
