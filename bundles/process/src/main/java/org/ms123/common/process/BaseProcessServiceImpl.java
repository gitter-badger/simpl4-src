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
import org.camunda.bpm.engine.impl.cfg.orientdb.OrientdbProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.ms123.common.git.GitService;
import org.ms123.common.process.converter.Simpl4BpmnJsonConverter;
import org.ms123.common.system.orientdb.OrientDBService;
import org.ms123.common.system.thread.ThreadContext;
import org.osgi.framework.BundleContext;
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
	protected Map<String,ProcessEngine> userProcessEngineMap = new HashMap<String,ProcessEngine>();
	protected ProcessEngine rootProcessEngine;
	protected GitService gitService;

	protected JSONDeserializer ds = new JSONDeserializer();

	protected JSONSerializer js = new JSONSerializer();

	public synchronized ProcessEngine getRootProcessEngine(){
		if( this.rootProcessEngine != null){
			return this.rootProcessEngine;
		}
		OrientGraphFactory f = this.orientdbService.getFactory(BPM_DB);
		this.rootProcessEngine = new OrientdbProcessEngineConfiguration(f).buildProcessEngine();
		return this.rootProcessEngine;
	}

	public synchronized ProcessEngine getProcessEngine(){
		String username = ThreadContext.getThreadContext().getUserName();
		ProcessEngine pe = this.userProcessEngineMap.get(username);
		if( pe != null){
			return pe;
		}
		OrientGraphFactory f = this.orientdbService.getUserFactory(BPM_DB);
		pe = new OrientdbProcessEngineConfiguration(f).buildProcessEngine();
		this.userProcessEngineMap.put(username,pe);
		return pe;
	}
}

