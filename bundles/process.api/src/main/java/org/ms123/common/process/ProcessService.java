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
package org.ms123.common.process.api;

import org.camunda.bpm.engine.ProcessEngine;
import org.ms123.common.form.FormService;
import org.ms123.common.system.registry.RegistryService;
import org.ms123.common.git.GitService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.data.api.DataLayer;
import org.osgi.service.event.EventAdmin;
import org.ms123.common.rpc.RpcException;
import java.util.Map;
import java.util.List;
import org.apache.camel.Component;

public interface ProcessService {
	public static final String PROCESS_ENGINE = "processEngine";
	public static final String PROCESS_SERVICE = "processService";

	public ProcessEngine getRootProcessEngine();
	public ProcessEngine getProcessEngine();
	public FormService getFormService();
	public RegistryService getRegistryService();
	public GitService getGitService();
	public PermissionService getPermissionService();
	public DataLayer getDataLayer();
	public EventAdmin getEventAdmin();
	public Map startProcessInstance(
			String namespace, 
			Integer  version,
			String processDefinitionId, 
			String processDefinitionKey, 
			String processDefinitionName, 
			String messageName, 
			String businessKey, 
			Map<String, Object> startParams) throws RpcException;

	public Map getProcessDefinitions(
			String namespace, 
			String key, 
			String name, 
			Integer version, 
			String user, 
			String group, 
			Map<String, Object> listParams) throws RpcException;

	public void setProcessDefinitionCandidates(
			String processDefinitionId, 
			List<String> userList, 
			List<String> groupList) throws RpcException;

	public Map executeTaskOperation(
			String taskId, 
			String operation, 
			Map<String, Object> startParams) throws RpcException;

	public Map executeTaskOperation(
			String taskId, 
			String operation, 
			Map<String, Object> startParams, boolean check) throws RpcException;

	public Component getProcessComponent();
}
