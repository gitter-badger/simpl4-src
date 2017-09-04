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

import flexjson.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.form.FormService;
import org.ms123.common.git.GitService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.workflow.api.WorkflowService;
import org.ms123.common.permission.api.PermissionService;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.activiti.engine.runtime.*;
import org.activiti.engine.history.*;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.osgi.service.event.EventAdmin;

/**
 *
 */
class BaseActivitiServiceImpl implements Constants {

	protected JSONDeserializer m_ds = new JSONDeserializer();
	protected JSONSerializer m_js = new JSONSerializer();

	protected WorkflowService m_workflowService;

	protected DataLayer m_dataLayer;
	protected EventAdmin m_eventAdmin;

	protected NucleusService m_nucleusService;

	protected GitService m_gitService;
	protected FormService m_formService;
	protected PermissionService m_permissionService;

	protected ProcessEngine m_processEngine;

	protected String _getTenantId(String processInstanceId) {
		ProcessInstance processInstance = m_processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		String processDefinitionId = null;
		String tenantId = null;
		if (processInstance == null) {
			HistoricProcessInstance instance = m_processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			if (instance == null) {
				throw new RuntimeException("BaseActivitiServiceImpl._getTenantId:processInstance not found:" + processInstanceId);
			}
			processDefinitionId = instance.getProcessDefinitionId();
			tenantId = instance.getTenantId();
		} else {
			processDefinitionId = processInstance.getProcessDefinitionId();
			tenantId = processInstance.getTenantId();
		}
		RepositoryService repositoryService = m_processEngine.getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).processDefinitionTenantId(tenantId).singleResult();
		return processDefinition.getTenantId();
	}

	protected ClassLoader _setContextClassLoader(String processInstanceId) {
		String tenantId = _getTenantId(processInstanceId);
		ClassLoader saveCl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(m_nucleusService.getClassLoader(StoreDesc.getNamespaceData(tenantId)));
		return saveCl;
	}
}
