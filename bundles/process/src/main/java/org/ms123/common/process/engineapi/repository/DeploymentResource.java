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
package org.ms123.common.process.engineapi.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.process.engineapi.BaseResource;
import org.ms123.common.process.converter.Simpl4BpmnJsonConverter;
import org.ms123.common.process.engineapi.Util;
import org.ms123.common.permission.api.PermissionService;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.ms123.common.process.engineapi.process.ProcessDefinitionResponse;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.RepositoryService;
import org.ms123.common.process.Constants;
import static com.jcabi.log.Logger.info;



/**
 */
@SuppressWarnings({"unchecked","deprecation"})
public class DeploymentResource extends BaseResource implements Constants{

	public DeploymentResource(ProcessService ps) {
		super(ps, null);
	}

	public Object deployProcess(String namespace, String path, boolean deploy, boolean all) throws Exception {
		if (!deploy) {
			throw new RuntimeException("DeploymentResource.deployProcess:unDeploy not supported");
		}
		info(this,"deployProcess:"+namespace+"/path:"+path);
		String processJson = getGitService().getFileContent(namespace, path);
		String deploymentId = null;
		String deployName = getDeploymentName(namespace,getBasename(path));
		RepositoryService rs = getRootPE().getRepositoryService();
		List<Deployment> dl = rs.createDeploymentQuery().deploymentName(deployName).list();
		info(this,"Deployment("+deployName+"):" + dl);
		Map shape = (Map) m_ds.deserialize(processJson);
		Map<String, Object> properties = (Map) shape.get("properties");
		Object m = properties.get("initialparameter");
		String initialParameter = null;
		if (m instanceof Map) {
			initialParameter = m_js.deepSerialize(m);
		} else {
			initialParameter = (String) properties.get("initialparameter");
			if (initialParameter == null) {
				initialParameter = m_js.deepSerialize(new HashMap());
			}
		}
		byte[] bpmnBytes = Simpl4BpmnJsonConverter.getBpmnXML(processJson, namespace, path);
		InputStream bais = new ByteArrayInputStream(bpmnBytes);
		DeploymentBuilder deployment = rs.createDeployment();
		deployment.name(deployName);
		deployment.addString("initialParameter", initialParameter);
		deploymentId = deployment.addInputStream(deployName + ".bpmn20.xml", bais).deploy().getId();
		info(this,"deploymentId:" + deploymentId);
		Map pdefs = getProcessService().getProcessDefinitions(namespace, deployName, null, -1, null, null, null);
		List<ProcessDefinitionResponse> pList = (List) pdefs.get("data");
		m_js.prettyPrint(true);
		info(this,"PList:" + m_js.deepSerialize(pList));
		if (pList.size() != 1) {
			throw new RuntimeException("ProcessService.deployProcess(" + deployName + "):not " + (pList.size() == 0 ? "found" : "uniqe"));
		}
		String processdefinitionId = pList.get(0).getId();
		Map<String,String> attr = new HashMap<String,String>();
		attr.put("subject", "namespaceProcDefAssoc");
		//@@@MS getRegistryService().set(processdefinitionId,namespace, attr);
		String groups = (String) properties.get("startablegroups");
		List groupList = null;
		if (groups != null && groups.trim().length() > 0) {
			groupList = new ArrayList<String>(Arrays.asList(groups.split(",")));
		}
		List userList = null;
		String users = (String) properties.get("startableusers");
		if (users != null && users.trim().length() > 0) {
			userList = new ArrayList<String>(Arrays.asList(users.split(",")));
		}
		info(this,"userList:" + userList + "/grList:" + groupList);
		getProcessService().setProcessDefinitionCandidates(processdefinitionId, userList, groupList);
		Map retMap = new HashMap();
		retMap.put("deploymentId", deploymentId);
		return retMap;
	}
	private String getBasename(String path) {
		String e[] = path.split("/");
		return e[e.length - 1];
	}
	private String getDeploymentName( String ns, String name){
		return ns + NAMESPACE_DELIMITER + name;
	}
}
