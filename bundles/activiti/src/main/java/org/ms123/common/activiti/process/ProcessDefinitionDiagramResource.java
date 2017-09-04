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
package org.ms123.common.activiti.process;

import java.io.InputStream;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.activiti.BaseResource;
import org.ms123.common.activiti.Util;
import org.ms123.common.libhelper.Base64;

public class ProcessDefinitionDiagramResource extends BaseResource {

	String m_processDefinitionId;

	public ProcessDefinitionDiagramResource(ActivitiService as, String processDefinitionId) {
		super(as, null);
		m_processDefinitionId = processDefinitionId;
	}

	public String getDiagram() {
		if (m_processDefinitionId == null) {
			throw new RuntimeException("No process definition id provided");
		}
		RepositoryService repositoryService = getPE().getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(m_processDefinitionId).singleResult();
		if (processDefinition == null) {
			throw new RuntimeException("Process definition " + m_processDefinitionId + " could not be found");
		}
		if (processDefinition.getDiagramResourceName() == null) {
			throw new RuntimeException("Diagram resource could not be found");
		}
		final InputStream definitionImageStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName());
		if (definitionImageStream == null) {
			throw new RuntimeException("Diagram resource could not be found");
		}
		return "data:image/png;base64," + Base64.encode(definitionImageStream);
	}
}
