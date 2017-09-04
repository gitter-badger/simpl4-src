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
@SuppressWarnings("unchecked")
public class Simpl4RulesTaskJsonConverter extends BaseBpmnJsonConverter {

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();

	private final String RULESNAME_PROP = "rulesname";

	private final String RULESNAME = "rulesname";

	private final String RULESKEY_PROP = "ruleskey";

	private final String RULESKEY = "ruleskey";

	private final String VARMAPPING_PROP = "variablesmapping";

	private final String VARMAPPING = "variablesmapping";

	protected String getStencilId(BaseElement flowElement) {
		return "RulesTask";
	}

	protected void convertElementToJson(ObjectNode propertiesNode, BaseElement flowElement) {
		ServiceTask serviceTask = (ServiceTask) flowElement;
	}

	protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
		m_js.prettyPrint(true);
		ServiceTask task = new ServiceTask();
		Map elementMap = (Map) m_ds.deserialize(elementNode.toString());
		Map<String, Object> propMap = (Map) elementMap.get("properties");
		String clazz = Simpl4BpmnJsonConverter.getFullnameForTask("TaskRulesExecutor");
		task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		task.setImplementation(clazz);

		String variablesmapping = checkNull(VARMAPPING, propMap.get(VARMAPPING_PROP));
		FieldExtension field = new FieldExtension();
		field.setFieldName(VARMAPPING);
		field.setExpression(variablesmapping);
		task.getFieldExtensions().add(field);

		field = new FieldExtension();
		field.setFieldName(RULESNAME);
		field.setExpression(getValue(RULESNAME, propMap.get(RULESNAME_PROP)));
		task.getFieldExtensions().add(field);

		field = new FieldExtension();
		field.setFieldName(RULESKEY);
		field.setExpression(getValue(RULESKEY, propMap.get(RULESKEY_PROP)));
		task.getFieldExtensions().add(field);
		return task;
	}

	public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
		fillJsonTypes(convertersToBpmnMap);
	}

	public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
		convertersToBpmnMap.put("RulesTask", Simpl4RulesTaskJsonConverter.class);
	}

	private String checkNull(String name, Object value) {
		if (value == null)
			throw new RuntimeException("Simpl4RulesTaskJsonConverter:" + name + " is null");
		return value.toString();
	}
	private String getValue(String name, Object value) {
		if (value == null){
			return null;
		}
		return value.toString();
	}
}
