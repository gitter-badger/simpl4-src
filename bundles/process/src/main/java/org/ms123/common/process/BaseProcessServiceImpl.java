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
package org.ms123.common.process;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import flexjson.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Component;
import org.camunda.bpm.engine.impl.cfg.orientdb.VariableListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.orientdb.OrientdbProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.form.FormService;
import org.ms123.common.git.GitService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.system.registry.RegistryService;
import org.ms123.common.process.converter.Simpl4BpmnJsonConverter;
import org.ms123.common.process.expressions.GroovyExpressionManager;
import org.ms123.common.process.jdbc.ProcessengineJDBC;
import org.ms123.common.process.jobs.Simpl4JobExecutor;
import org.ms123.common.process.listener.OSGiEventDistributor;
import org.ms123.common.process.listener.OSGiVariableEventDistributor;
import org.ms123.common.process.listener.RegisterAllBpmnParseListener;
import org.ms123.common.system.orientdb.OrientDBService;
import org.ms123.common.system.thread.ThreadContext;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
class BaseProcessServiceImpl {

	private static String BPM_DB = "bpmDB";
	protected BundleContext bundleContext;
	protected OrientDBService orientdbService;
	protected Map<String, ProcessEngine> userProcessEngineMap = new HashMap<String, ProcessEngine>();
	protected ProcessEngine rootProcessEngine;
	protected PermissionService permissionService;
	protected GitService gitService;
	protected RegistryService registryService;
	protected DataLayer dataLayer;
	protected static EventAdmin eventAdmin;
	protected FormService formService;

	protected JSONDeserializer ds = new JSONDeserializer();
	protected ProcessengineJDBC peJdbc = new ProcessengineJDBC();

	protected JSONSerializer js = new JSONSerializer();
	private boolean isJdbc = false;

	public synchronized ProcessEngine getRootProcessEngine() {
		info(this,"BaseProcessServiceImpl.getRootProcessEngine");
		if (isJdbc) {
			return getProcessengineJDBC();
		}
		if (this.rootProcessEngine != null) {
			return this.rootProcessEngine;
		}

		OrientGraphFactory f = this.orientdbService.getFactory(BPM_DB);
		f.setStandardElementConstraints(false);

		OrientdbProcessEngineConfiguration c = new OrientdbProcessEngineConfiguration(f);
		//c.setHistory(ProcessEngineConfiguration.HISTORY_NONE);
		c.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
		c.setDatabaseSchemaUpdate("true");
		c.setAuthorizationEnabled(false);
		c.setTenantCheckEnabled(false);
		c.setEnableExpressionsInAdhocQueries(true);
		Simpl4JobExecutor simpl4JobExecutor = new Simpl4JobExecutor(c.getBeans());
		c.setJobExecutor(simpl4JobExecutor);
		c.setJobExecutorActivate(true);
		simpl4JobExecutor.setAutoActivate(true);

		GroovyExpressionManager groovyExpressionManager = new GroovyExpressionManager();
		c.setExpressionManager(groovyExpressionManager);
		this.rootProcessEngine = c.buildProcessEngine();
		c.getBeans().put(ProcessService.PROCESS_ENGINE, this.rootProcessEngine);
		c.getBeans().put("bundleContext", this.bundleContext);
		c.getBeans().put(PermissionService.PERMISSION_SERVICE, this.permissionService);
		c.getBeans().put(RegistryService.REGISTRY_SERVICE, this.registryService);
		c.getBeans().put("gitService", this.gitService);
		simpl4JobExecutor.setProcessEngine(this.rootProcessEngine);

		return this.rootProcessEngine;
	}

	public synchronized ProcessEngine getProcessEngine() {
		if (isJdbc) {
			return getProcessengineJDBC();
		}
		String username = ThreadContext.getThreadContext().getUserName();
		ProcessEngine pe = this.userProcessEngineMap.get(username);
		info(this,"BaseProcessServiceImpl.getProcessEngine("+username+")");
		if (pe != null) {
			return pe;
		}
		OrientGraphFactory f = this.orientdbService.getUserFactory(BPM_DB);
		f.setStandardElementConstraints(false);

		OrientdbProcessEngineConfiguration c = new OrientdbProcessEngineConfiguration(f);
		//c.setHistory(ProcessEngineConfiguration.HISTORY_NONE);
		c.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
		c.setAuthorizationEnabled(false);
		c.setTenantCheckEnabled(false);
		c.setEnableExpressionsInAdhocQueries(true);
		addEventDistributor(c, username);
		addVariableEventDistributor(c, username);
		Simpl4JobExecutor simpl4JobExecutor = new Simpl4JobExecutor(c.getBeans());
		c.setJobExecutor(simpl4JobExecutor);
		c.setJobExecutorActivate(true);
		simpl4JobExecutor.setAutoActivate(true);

		GroovyExpressionManager groovyExpressionManager = new GroovyExpressionManager();
		c.setExpressionManager(groovyExpressionManager);

		pe = c.buildProcessEngine();
		simpl4JobExecutor.setProcessEngine(pe);
		c.getBeans().put(ProcessService.PROCESS_ENGINE, pe);
		c.getBeans().put("bundleContext", this.bundleContext);
		c.getBeans().put(PermissionService.PERMISSION_SERVICE, this.permissionService);
		c.getBeans().put(RegistryService.REGISTRY_SERVICE, this.registryService);
		c.getBeans().put("gitService", this.gitService);

		this.userProcessEngineMap.put(username, pe);
		return pe;
	}

	private void addEventDistributor(OrientdbProcessEngineConfiguration c, String tenant) {
		OSGiEventDistributor dis1 = new OSGiEventDistributor(this.eventAdmin, tenant);
		OSGiEventDistributor dis2 = new OSGiEventDistributor(this.eventAdmin, tenant);
		RegisterAllBpmnParseListener allParseListener = new RegisterAllBpmnParseListener(dis1, dis2);

		List<BpmnParseListener> preParseListeners = c.getCustomPreBPMNParseListeners();
		if (preParseListeners == null) {
			preParseListeners = new ArrayList<BpmnParseListener>();
		}
		preParseListeners.add(allParseListener);
		c.setCustomPreBPMNParseListeners(preParseListeners);
	}

	private void addVariableEventDistributor(OrientdbProcessEngineConfiguration c, String tenant) {
		OSGiVariableEventDistributor dis = new OSGiVariableEventDistributor(this.eventAdmin, tenant);

		List<VariableListener> variableListeners = c.getVariableListeners();
		if (variableListeners == null) {
			variableListeners = new ArrayList<VariableListener>();
		}
		variableListeners.add(dis);
		c.setVariableListeners(variableListeners);
	}

	public Component getProcessComponent() {
		info(this, "BaseProcessServiceImpl.getProcessComponent");
		return new org.ms123.common.process.camel.ProcessComponent();
	}

	private ProcessEngine getProcessengineJDBC() {
		if (this.rootProcessEngine != null) {
			return this.rootProcessEngine;
		}
		this.rootProcessEngine = peJdbc.getRootProcessEngine((ProcessService)this, this.bundleContext);
		return this.rootProcessEngine;
	}
}

