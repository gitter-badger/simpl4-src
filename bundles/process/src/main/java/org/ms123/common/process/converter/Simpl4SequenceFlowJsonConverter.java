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
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.activiti.editor.language.json.converter.*;
import org.activiti.bpmn.model.SequenceFlow;
import flexjson.*;

/**
 */
public class Simpl4SequenceFlowJsonConverter extends SequenceFlowJsonConverter {

	private String PROPERTY_SEQUENCEFLOW_CONDITIONALFLOW = "conditionalflow";

	protected String getStencilId(FlowElement flowElement) {
    return STENCIL_SEQUENCE_FLOW;
	}
  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    fillJsonTypes(convertersToBpmnMap);
  }
  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_SEQUENCE_FLOW, Simpl4SequenceFlowJsonConverter.class);
  }

  @Override
  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    SequenceFlow flow = new SequenceFlow();
    
    String sourceRef = lookForSourceRef(elementNode.get(EDITOR_SHAPE_ID).asText(), modelNode.get(EDITOR_CHILD_SHAPES));
    
    if (sourceRef != null) {
      flow.setSourceRef(sourceRef);
      String targetId = elementNode.get("target").get(EDITOR_SHAPE_ID).asText();
      flow.setTargetRef(BpmnJsonConverterUtil.getElementId(shapeMap.get(targetId)));
    }
    String conditionalFlow = getPropertyValueAsString(PROPERTY_SEQUENCEFLOW_CONDITIONALFLOW, elementNode); 
		if( conditionalFlow==null || !conditionalFlow.toLowerCase().equals("none")){
    	flow.setConditionExpression(getPropertyValueAsString(PROPERTY_SEQUENCEFLOW_CONDITION, elementNode));
		}
    
    return flow;
  }
  private String lookForSourceRef(String flowId, JsonNode childShapesNode) {
    String sourceRef = null;
    
    if (childShapesNode != null) {
    
      for (JsonNode childNode : childShapesNode) {
        ArrayNode outgoingNode = (ArrayNode) childNode.get("outgoing");
        if (outgoingNode != null && outgoingNode.size() > 0) {
          for (JsonNode outgoingChildNode : outgoingNode) {
            JsonNode resourceNode = outgoingChildNode.get(EDITOR_SHAPE_ID);
            if (resourceNode != null && flowId.equals(resourceNode.asText())) {
              sourceRef = BpmnJsonConverterUtil.getElementId(childNode);
              break;
            }
          }
          
          if (sourceRef != null) {
            break;
          }
        }
        sourceRef = lookForSourceRef(flowId, childNode.get(EDITOR_CHILD_SHAPES));
        
        if (sourceRef != null) {
          break;
        }
      }
    }
    return sourceRef;
  }
}
