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
package org.ms123.common.process.converter;

import java.util.Map;
import java.util.List;
import java.util.Date;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.editor.language.json.converter.*;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;

/**
 */
@SuppressWarnings("unchecked")
public class Simpl4BpmnJsonConverter extends BpmnJsonConverter {

	public Simpl4BpmnJsonConverter(String namespace) {
		Simpl4SequenceFlowJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		Simpl4FilterTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		Simpl4CamelTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		Simpl4MessageTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		Simpl4DocumentTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		Simpl4UserTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		Simpl4ScriptTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		Simpl4RulesTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		Simpl4MailTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		Simpl4StartEventJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
		DI_RECTANGLES.add("MailTask");
		DI_RECTANGLES.add("FilterTask");
		DI_RECTANGLES.add("CamelTask");
		DI_RECTANGLES.add("MessageTask");
		DI_RECTANGLES.add("RulesTask");
		DI_RECTANGLES.add("DocumentTask");
	}

	private static String TASKPACKAGE="org.ms123.common.workflow.tasks";
	public  static String getFullnameForTask(String clazz){
		return TASKPACKAGE + "." + clazz;
	}
	public static  byte[] getBpmnXML(String processJson, String ns, String path) throws Exception {
		Simpl4BpmnJsonConverter jsonConverter = new Simpl4BpmnJsonConverter(ns);
		JsonNode editorNode = new ObjectMapper().readTree(processJson);
		BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
		bpmnModel.setTargetNamespace(ns);
		for (Process process : bpmnModel.getProcesses()) {
			if (process.getId() == null) {
				process.setId(getBasename(path));
			}
			process.getExecutionListeners().add(createListener("start", "org.ms123.common.workflow.ProcessStartExecutionListener"));
			process.getExecutionListeners().add(createListener("end", "org.ms123.common.workflow.ProcessEndExecutionListener"));
		}
		BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
		byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);
		return bpmnBytes;
	}
	private static ActivitiListener createListener(String event, String clazz) {
		ActivitiListener listener = new ActivitiListener();
		listener.setEvent(event);
		listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		listener.setImplementation(clazz);
		return listener;
	}
	private static String getBasename(String path) {
		String e[] = path.split("/");
		return e[e.length - 1];
	}
}
