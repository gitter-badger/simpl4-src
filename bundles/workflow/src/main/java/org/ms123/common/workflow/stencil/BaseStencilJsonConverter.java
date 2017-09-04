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
package org.ms123.common.workflow.stencil;

import java.util.Map;
import java.util.List;
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
public abstract  class BaseStencilJsonConverter extends BaseBpmnJsonConverter {
	protected JSONSerializer m_js = new JSONSerializer();
	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected abstract String getStencilId(FlowElement flowElement);
	protected abstract void createFields(Map properties, ServiceTask task);
	protected abstract String getClassName();

	protected void convertElementToJson(ObjectNode propertiesNode, FlowElement flowElement) {
		ServiceTask serviceTask = (ServiceTask) flowElement;
	}

	protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
		ServiceTask task = new ServiceTask();
		//String clazz = getPropertyValueAsString(CLASSNAME, elementNode);
		String clazz = "org.ms123.common.camel.components.CamelBehaviorDefaultImpl";
		task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		task.setImplementation(getClassName());
		Map elementMap = (Map) m_ds.deserialize(elementNode.toString());
		Map<String, Object> propMap = (Map) elementMap.get("properties");
		createFields(propMap,task);
		return task;
	}


	public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
		//fillJsonTypes(convertersToBpmnMap);
	}

	public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
		//convertersToBpmnMap.put(m_taskName, m_clazz);
	}

	private String checkNull(String name, Object value) {
		if (value == null)
			throw new RuntimeException(getStencilId(null)+"JsonConverter:" + name + " is null");
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
			throw new RuntimeException(getStencilId(null)+"StencilJsonConverter:" + name + " is null");
		String val=value.toString();
		if( val.trim().length() ==0){
			throw new RuntimeException(getStencilId(null)+"StencilJsonConverter:" + name + " is empty");
		}
		return val;
	}
	protected void addField(String name, Map properties, ServiceTask task) {
		FieldExtension field = new FieldExtension();
		field.setFieldName(name);
		String value = getValue(name, properties);
		if (StringUtils.isNotEmpty(value)) {
			if ((value.contains("${") || value.contains("#{")) && value.contains("}")) {
				field.setExpression(value);
			} else {
				field.setStringValue(value);
			}
		}
		task.getFieldExtensions().add(field);
	}
	private String getValue(String name, Map<String,Object> properties) {
		Object value = properties.get(name);
		if (value == null){
			return null;
		}
		if( value instanceof String){
			return value.toString();
		}
		return m_js.deepSerialize(value);
	}
}
