/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014-2017] [Manfred Sattler] <manfred@ms123.org>
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
package org.ms123.common.process.api;

import org.camunda.bpm.engine.ProcessEngine;
import org.ms123.common.form.FormService;
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