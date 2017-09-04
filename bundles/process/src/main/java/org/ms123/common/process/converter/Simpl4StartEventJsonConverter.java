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
import org.activiti.bpmn.model.StartEvent;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.editor.language.json.converter.*;
import flexjson.*;

/**
 */
@SuppressWarnings("unchecked")
public class Simpl4StartEventJsonConverter extends StartEventJsonConverter {

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();

	private final String FORMKEY_PROP = "formkey";

	public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
		fillJsonTypes(convertersToBpmnMap);
	}

	public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
		convertersToBpmnMap.put(STENCIL_EVENT_START_NONE, Simpl4StartEventJsonConverter.class);
		convertersToBpmnMap.put(STENCIL_EVENT_START_TIMER, Simpl4StartEventJsonConverter.class);
		convertersToBpmnMap.put(STENCIL_EVENT_START_ERROR, Simpl4StartEventJsonConverter.class);
		convertersToBpmnMap.put(STENCIL_EVENT_START_MESSAGE, Simpl4StartEventJsonConverter.class);
		convertersToBpmnMap.put(STENCIL_EVENT_START_SIGNAL, Simpl4StartEventJsonConverter.class);
	}

	protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
		StartEvent event = (StartEvent) super.convertJsonToElement(elementNode, modelNode, shapeMap);
		m_js.prettyPrint(true);
		Map elementMap = (Map) m_ds.deserialize(elementNode.toString());
		Map<String, Object> propMap = (Map) elementMap.get("properties");
		String formkey = getString(propMap.get(FORMKEY_PROP));
		event.setFormKey(formkey);
		return event;
	}

	private String getString(Object value) {
		if (value == null)
			return null;
		return value.toString();
	}
}
