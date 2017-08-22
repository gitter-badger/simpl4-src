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
package org.ms123.common.process;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import flexjson.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Component;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.orientdb.OrientdbProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.form.FormService;
import org.ms123.common.git.GitService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.process.converter.Simpl4BpmnJsonConverter;
import org.ms123.common.process.expressions.GroovyExpressionManager;
import org.ms123.common.process.jobs.Simpl4JobExecutor;
import org.ms123.common.process.listener.OSGiEventDistributor;
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
	protected BundleContext bc;
	protected OrientDBService orientdbService;
	protected Map<String, ProcessEngine> userProcessEngineMap = new HashMap<String, ProcessEngine>();
	protected ProcessEngine rootProcessEngine;
	protected PermissionService permissionService;
	protected GitService gitService;
	protected DataLayer dataLayer;
	protected static EventAdmin eventAdmin;
	protected FormService formService;

	protected JSONDeserializer ds = new JSONDeserializer();

	protected JSONSerializer js = new JSONSerializer();

	public synchronized ProcessEngine getRootProcessEngine() {
		if (this.rootProcessEngine != null) {
			return this.rootProcessEngine;
		}
		OrientGraphFactory f = this.orientdbService.getFactory(BPM_DB);
		f.setStandardElementConstraints(false);

		OrientdbProcessEngineConfiguration c = new OrientdbProcessEngineConfiguration(f);
		Simpl4JobExecutor simpl4JobExecutor = new Simpl4JobExecutor(c.getBeans());
		c.setJobExecutor(simpl4JobExecutor);
		c.setJobExecutorActivate(true);
		simpl4JobExecutor.setAutoActivate(true);

		GroovyExpressionManager groovyExpressionManager = new GroovyExpressionManager();
		c.setExpressionManager(groovyExpressionManager);
		this.rootProcessEngine = c.buildProcessEngine();
		c.getBeans().put(ProcessService.PROCESS_ENGINE, this.rootProcessEngine);
		simpl4JobExecutor.setProcessEngine( this.rootProcessEngine);

		return this.rootProcessEngine;
	}

	public synchronized ProcessEngine getProcessEngine() {
		String username = ThreadContext.getThreadContext().getUserName();
		ProcessEngine pe = this.userProcessEngineMap.get(username);
		if (pe != null) {
			return pe;
		}
		OrientGraphFactory f = this.orientdbService.getUserFactory(BPM_DB);
		f.setStandardElementConstraints(false);

		OrientdbProcessEngineConfiguration c = new OrientdbProcessEngineConfiguration(f);
		addEventDistributor( c, username);
		Simpl4JobExecutor simpl4JobExecutor = new Simpl4JobExecutor(c.getBeans());
		c.setJobExecutor(simpl4JobExecutor);
		c.setJobExecutorActivate(true);
		simpl4JobExecutor.setAutoActivate(true);

		GroovyExpressionManager groovyExpressionManager = new GroovyExpressionManager();
		c.setExpressionManager(groovyExpressionManager);

		pe  = c.buildProcessEngine();
		simpl4JobExecutor.setProcessEngine( pe);
		c.getBeans().put(ProcessService.PROCESS_ENGINE, pe);

		this.userProcessEngineMap.put(username, pe);
		return pe;
	}

	private void addEventDistributor(OrientdbProcessEngineConfiguration c, String tenant){
		OSGiEventDistributor dis1 = new OSGiEventDistributor(this.eventAdmin, tenant);
		OSGiEventDistributor dis2 = new OSGiEventDistributor(this.eventAdmin, tenant);
		RegisterAllBpmnParseListener allParseListener = new RegisterAllBpmnParseListener( dis1, dis2);

  	List<BpmnParseListener> preParseListeners = c.getCustomPreBPMNParseListeners();
    if(preParseListeners == null) {
      preParseListeners = new ArrayList<BpmnParseListener>();
    }
    preParseListeners.add(allParseListener);
    c.setCustomPreBPMNParseListeners(preParseListeners);
	}

	public Component getProcessComponent(){
		info(this,"BaseProcessServiceImpl.getProcessComponent");
		return new org.ms123.common.process.camel.ProcessComponent();
	}
}

