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
package org.ms123.common.activiti;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ProcessEngine;
import org.ms123.common.form.FormService;
import org.ms123.common.workflow.api.WorkflowService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.git.GitService;
import org.osgi.service.event.EventAdmin;
import flexjson.*;

/**
 */
@SuppressWarnings("unchecked")
public class BaseResource {

	protected ActivitiService m_as;
	protected JSONSerializer m_js = new JSONSerializer();

	protected Map<String, Object> m_listParams;

	public BaseResource(ActivitiService as, Map<String, Object> lp) {
		m_as = as;
		m_listParams = lp != null ? lp : new HashMap();
		m_js.prettyPrint(true);
	}

	public ProcessEngine getPE() {
		return m_as.getPE();
	}
	public PermissionService getPermissionService() {
		return m_as.getPermissionService();
	}
	public GitService getGitService() {
		return m_as.getGitService();
	}
	public FormService getFormService() {
		return m_as.getFormService();
	}
	public DataLayer getDataLayer() {
		return m_as.getDataLayer();
	}
	public WorkflowService getWorkflowService() {
		return m_as.getWorkflowService();
	}
	public EventAdmin getEventAdmin() {
		return m_as.getEventAdmin();
	}
	protected boolean isUser( String user){
		return getPermissionService().isUserThis(user);
	}
	protected boolean hasRole( String role){
		return getPermissionService().hasUserRole(role);
	}
	protected void checkUser(String user){
		if(getPermissionService().hasAdminRole()){
			return;
		}
		if(!getPermissionService().isUserThis(user)){
			throw new RuntimeException("TasksResource.checkUser:no permission:" + user);
		}
	}
	protected void checkRole(String role){
		if(getPermissionService().hasAdminRole()){
			return;
		}
		if(!getPermissionService().hasUserRole(role)){
			throw new RuntimeException("TasksResource.checkRole:no permission:" + role);
		}
	}
}
