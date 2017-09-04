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
public class Simpl4CamelTaskJsonConverter extends BaseBpmnJsonConverter {

	protected JSONDeserializer ds = new JSONDeserializer();
	protected JSONSerializer js = new JSONSerializer();


	private final String RETURNVARIABLE_PROP = "returnvariable";
	private final String RETURNVARIABLE = "returnvariable";

	private final String RETURNMAPPING_PROP = "returnmapping";
	private final String RETURNMAPPING = "returnmapping";

	private final String NAMESPACE_PROP = "namespace";
	private final String NAMESPACE = "namespace";

	private final String ROUTENAME_PROP = "routename";
	private final String ROUTENAME = "routename";

	private final String ROUTEVARNAME_PROP = "routevarname";
	private final String ROUTEVARNAME = "routevarname";

	private final String VARMAPPING_PROP = "variablesmapping";
	private final String VARMAPPING = "variablesmapping";

	protected String getStencilId(BaseElement flowElement) {
		return "CamelTask";
	}

	protected void convertElementToJson(ObjectNode propertiesNode, BaseElement flowElement) {
		ServiceTask serviceTask = (ServiceTask) flowElement;
	}

	protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
		ServiceTask task = new ServiceTask();
		Map elementMap = (Map) ds.deserialize(elementNode.toString());
		Map<String, Object> propMap = (Map) elementMap.get("properties");

		String clazz = Simpl4BpmnJsonConverter.getFullnameForTask("TaskCamelExecutor");
		task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		task.setImplementation(clazz);

		FieldExtension field = new FieldExtension();
		field.setFieldName(VARMAPPING);
		String variablesmapping = getValue(VARMAPPING, js.deepSerialize(propMap.get(VARMAPPING_PROP)));
		field.setExpression(variablesmapping);
		task.getFieldExtensions().add(field);

		field = new FieldExtension();
		field.setFieldName(NAMESPACE);
		field.setStringValue(getValue(NAMESPACE, propMap.get(NAMESPACE_PROP)));
		task.getFieldExtensions().add(field);

		field = new FieldExtension();
		field.setFieldName(ROUTENAME);
		field.setStringValue(getValue(ROUTENAME, propMap.get(ROUTENAME_PROP)));
		task.getFieldExtensions().add(field);

		field = new FieldExtension();
		field.setFieldName(ROUTEVARNAME);
		field.setStringValue(getValue(ROUTEVARNAME, propMap.get(ROUTEVARNAME_PROP)));
		task.getFieldExtensions().add(field);

		field = new FieldExtension();
		field.setFieldName(RETURNVARIABLE);
		field.setStringValue(checkNull(RETURNVARIABLE, propMap.get(RETURNVARIABLE_PROP)));
		task.getFieldExtensions().add(field);

		field = new FieldExtension();
		field.setFieldName(RETURNMAPPING);
		String returnmapping = getValue(RETURNMAPPING, js.deepSerialize(propMap.get(RETURNMAPPING_PROP)));
		field.setExpression(returnmapping);
		task.getFieldExtensions().add(field);
		return task;
	}

	public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
		fillJsonTypes(convertersToBpmnMap);
	}

	public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
		convertersToBpmnMap.put("CamelTask", Simpl4CamelTaskJsonConverter.class);
	}
	private String checkEmpty(String name, Object value) {
		if (value == null)
			throw new RuntimeException("Simpl4CamelTaskJsonConverter:" + name + " is null");
		String val=value.toString();
		if( val.trim().length() ==0){
			throw new RuntimeException("Simpl4CamelTaskJsonConverter:" + name + " is empty");
		}
		return val;
	}

	private String checkNull(String name, Object value) {
		if (value == null)
			throw new RuntimeException("Simpl4CamelTaskJsonConverter:" + name + " is null");
		return value.toString();
	}
	private String getValue(String name, Object value) {
		if (value == null){
			return null;
		}
		return value.toString();
	}
}








































