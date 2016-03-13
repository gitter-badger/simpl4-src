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
