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
package org.ms123.common.process.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.process.expressions.GroovyExpressionManager;
import org.ms123.common.process.jobs.Simpl4JobExecutor;
import org.ms123.common.system.registry.RegistryService;
import org.osgi.framework.BundleContext;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 * Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class ProcessengineJDBC {

	protected Map<String, ProcessEngine> userProcessEngineMap = new HashMap<String, ProcessEngine>();
	protected ProcessEngine rootProcessEngine;
	protected ProcessService processService;
	protected BundleContext bundleContext;
	protected DataSource dataSource;

	public synchronized ProcessEngine getRootProcessEngine(ProcessService ps, BundleContext bc) {
		if (this.rootProcessEngine != null) {
			return this.rootProcessEngine;
		}
		this.processService = ps;
		this.bundleContext = bc;

		ProcessEngineConfigurationImpl c = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
		c.setEnableExpressionsInAdhocQueries(true);
		c.setHistory(ProcessEngineConfiguration.HISTORY_NONE);
		//c.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
		c.setDatabaseSchemaUpdate("true");
		c.setDataSource(getDataSource("jdbc:h2:file:/opt/simpl4/workspace/camunda/h2;DB_CLOSE_DELAY=1000;TRACE_LEVEL_FILE=2"));
		//		Simpl4JobExecutor simpl4JobExecutor = new Simpl4JobExecutor(c.getBeans());
		//		c.setJobExecutor(simpl4JobExecutor);
		//		c.setJobExecutorActivate(true);
		//		simpl4JobExecutor.setAutoActivate(true);

		c.setAuthorizationEnabled(false);
		c.setTenantCheckEnabled(false);

		GroovyExpressionManager groovyExpressionManager = new GroovyExpressionManager();
		c.setExpressionManager(groovyExpressionManager);
		this.rootProcessEngine = c.buildProcessEngine();
		c.getBeans().put(ProcessService.PROCESS_ENGINE, this.rootProcessEngine);
		c.getBeans().put("bundleContext", this.bundleContext);
		c.getBeans().put(PermissionService.PERMISSION_SERVICE, this.processService.getPermissionService());
		c.getBeans().put(RegistryService.REGISTRY_SERVICE, this.processService.getRegistryService());
		//simpl4JobExecutor.setProcessEngine(this.rootProcessEngine);

		return this.rootProcessEngine;
	}

	private DataSource getDataSource(String url) {
		if (this.dataSource != null){
			return this.dataSource;
		}

		org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
		ds.setUser("sa");
		ds.setPassword("");
		ds.setURL(url);
		this.dataSource = ds;
		return this.dataSource;
	}

}

