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
package org.ms123.common.dmn;

import flexjson.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.apache.commons.io.IOUtils;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.git.GitService;
import org.ms123.common.stencil.api.StencilService;
import org.ms123.common.store.StoreDesc;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.*;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
class BaseDmnServiceImpl {

	protected BundleContext bc;

	protected DataLayer dataLayer;

	protected GitService gitService;

	protected JSONDeserializer ds = new JSONDeserializer();

	protected JSONSerializer js = new JSONSerializer();

	private Map<String,DmnDecision> dmnCache = new HashMap<String,DmnDecision>();

	private DmnEngine dmnEngine;

	protected void initDmnEngine() {
		this.dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
	}

	protected Map _deployDMN( String namespace, String name ){
		String cname = getCleanName(name);
		dmnCache.remove( cname );
		return null;
	}

	protected List<Map> _executeDecision(String namespace, String name, Map variables) throws Exception {
		DmnDecision decision = getDecision( namespace, name );
		String cname = getCleanName(name);
		info(this, "Dmn._executeDecision(" + cname + ").input:" + variables);
		DmnDecisionResult	result = this.dmnEngine.evaluateDecision(decision, variables);
		info(this, "Dmn._executeDecision(" + cname + ").resultVars:" + result.getResultList());
		return new ArrayList<Map>(result.getResultList());
	}

	private DmnDecision getDecision(String namespace, String name) throws Exception{
		String cname = getCleanName(name);
		String fullName = getFullName(namespace,cname);
		DmnDecision dmnDecision = this.dmnCache.get(fullName);
		if( dmnDecision == null){
			Map rulesMap = 	getRules(namespace,name);
			RulesConverter rc = new RulesConverter( rulesMap);
			dmnDecision = rc.convert(cname);
			this.dmnCache.put( fullName, dmnDecision);
		}
		return dmnDecision;
	}

	private Map getRules(String namespace, String name) {
		String filterJson = this.gitService.searchContent(namespace, name, "sw.rule");
		Map contentMap = (Map) this.ds.deserialize(filterJson);
		return contentMap;
	}

	private String getCleanName( String name){
		String cname = name.replace(".","_");
		return cname;
	}
	private String getFullName( String namespace, String name){
		return namespace + "." + name;
	}
}

