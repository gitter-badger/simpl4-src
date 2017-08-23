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
package org.ms123.common.process.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.process.expressions.GroovyExpressionManager;
import org.ms123.common.process.jobs.Simpl4JobExecutor;
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
	protected DataSource dataSource;

	public synchronized ProcessEngine getRootProcessEngine() {
		if (this.rootProcessEngine != null) {
			return this.rootProcessEngine;
		}

		ProcessEngineConfigurationImpl c = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
		c.setDatabaseSchemaUpdate("true");
		c.setDataSource(getDataSource("jdbc:h2:file:/opt/simpl4/workspace/camunda/h2;DB_CLOSE_DELAY=1000;TRACE_LEVEL_FILE=2"));
		//		Simpl4JobExecutor simpl4JobExecutor = new Simpl4JobExecutor(c.getBeans());
		//		c.setJobExecutor(simpl4JobExecutor);
		//		c.setJobExecutorActivate(true);
		//		simpl4JobExecutor.setAutoActivate(true);

		c.setAuthorizationEnabled(false);
		c.setTenantCheckEnabled(false);
		c.setHistory(ProcessEngineConfiguration.HISTORY_FULL);

		GroovyExpressionManager groovyExpressionManager = new GroovyExpressionManager();
		c.setExpressionManager(groovyExpressionManager);
		this.rootProcessEngine = c.buildProcessEngine();
		c.getBeans().put(ProcessService.PROCESS_ENGINE, this.rootProcessEngine);
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

