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
package org.ms123.common.flowable;

import flexjson.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.lang.reflect.*;
import org.osgi.framework.wiring.*;
import org.ms123.common.git.GitService;
import org.ms123.common.stencil.api.StencilService;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.store.StoreDesc;
import org.osgi.framework.BundleContext;
import org.flowable.dmn.api.DmnManagementService;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnRuleService;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.engine.DmnEngineConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.flowable.dmn.model.Decision;
import org.flowable.dmn.model.DecisionRule;
import org.flowable.dmn.model.DecisionTable;
import org.flowable.dmn.model.DecisionTableOrientation;
import org.flowable.dmn.model.DmnDefinition;
import org.flowable.editor.dmn.converter.DmnJsonConverter;
import org.flowable.dmn.api.RuleEngineExecutionResult;
import org.flowable.dmn.api.DmnDeployment;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
class BaseFlowableServiceImpl {

	protected BundleContext bc;

	protected DataLayer dataLayer;

	protected GitService gitService;

	protected JSONDeserializer ds = new JSONDeserializer();

	protected JSONSerializer js = new JSONSerializer();
	private Map<String, DmnEngine> dmnEngines = new HashMap<String, DmnEngine>();

	protected DmnEngine cachedDmnEngine;
	protected DmnEngineConfiguration dmnEngineConfiguration;
	protected DmnRepositoryService repositoryService;
	protected DmnRuleService ruleService;
	protected DmnManagementService managementService;
	private ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private DmnEngine getDmnEngine(String configurationResource) {
		DmnEngine dmnEngine = dmnEngines.get(configurationResource);
		if (dmnEngine == null) {
			info(this, "==== BUILDING DMN ENGINE ====");

			String sh = System.getProperty("workspace");
			dmnEngine = DmnEngineConfiguration.createStandaloneDmnEngineConfiguration().setDatabaseSchemaUpdate("true").setJdbcUrl("jdbc:h2:file:" + sh + "/flowable/h2;DB_CLOSE_DELAY=1000").buildDmnEngine();

			info(this, "==== DMN ENGINE CREATED ====");
			dmnEngines.put(configurationResource, dmnEngine);
		}
		return dmnEngine;
	}

	private void closeDmnEngines() {
		for (DmnEngine dmnEngine : dmnEngines.values()) {
			dmnEngine.close();
		}
		dmnEngines.clear();
	}

	protected void initDmnEngine() {
		if (cachedDmnEngine == null) {
			cachedDmnEngine = getDmnEngine("default");
		}
		this.dmnEngineConfiguration = cachedDmnEngine.getDmnEngineConfiguration();
		this.repositoryService = cachedDmnEngine.getDmnRepositoryService();
		this.ruleService = cachedDmnEngine.getDmnRuleService();
		this.managementService = cachedDmnEngine.getDmnManagementService();
		info(this, "Flowable.dmnEngineConfiguration:" + this.dmnEngineConfiguration);
		info(this, "Flowable.repositoryService:" + this.repositoryService);
		info(this, "Flowable.ruleService:" + this.ruleService);
	}

	protected Map _executeDecision(String namespace, String name, Map variables) {
		String cname = name.replace(".","_");
		List<DmnDeployment> dmnDeployments = this.repositoryService.createDeploymentQuery().deploymentName(cname).list();
		for (DmnDeployment dep : dmnDeployments) {
			info(this, "Flowable.dmnDeployments:" + dep.getId() + "/" + dep.getName());
		}
		RuleEngineExecutionResult result = this.ruleService.executeDecisionByKeyAndTenantId(cname, variables, namespace);
		info(this, "Flowable._executeDecision(" + cname + ").input:" + variables);
		Map resultVars = result.getResultVariables();
		info(this, "Flowable._executeDecision(" + cname + ").resultVars:" + resultVars);
		return resultVars;
	}

	private Map getRules(String namespace, String name) {
		String filterJson = this.gitService.searchContent(namespace, name, "sw.rule");
		Map contentMap = (Map) this.ds.deserialize(filterJson);
		return contentMap;
	}
	protected Map _deployDMN(String namespace, String name, String jsonString) throws Exception{
		String cname = name.replace(".","_");
		List<DmnDeployment> dmnDeployments = this.repositoryService.createDeploymentQuery().deploymentName(cname).list();
		info(this, "Flowable.dmnDeployments:" + dmnDeployments);
		for (DmnDeployment dep : dmnDeployments) {
			this.repositoryService.deleteDeployment(dep.getId());
		}

		DmnDefinition dmnDefinition = null;
		if( jsonString != null && !jsonString.trim().equals("")){
			JsonNode node = parseJson(jsonString);
			dmnDefinition = new DmnJsonConverter().convertToDmn(node, "abc", 1, new Date());
		}else{
			Map rulesMap = 	getRules(namespace,name);
			RulesConverter rc = new RulesConverter( rulesMap);
			dmnDefinition = rc.convert(cname);
		}

		info(this, "Flowable.dmnDefinition:" + dmnDefinition);
		for (Decision dec : dmnDefinition.getDecisions()) {
			info(this, "Flowable.Decision(" + dec.getId() + "):" + dec);
		}
		this.repositoryService.createDeployment().tenantId(namespace).name(cname).addDmnModel(cname + ".dmn", dmnDefinition).deploy();
		return new HashMap();
	}

	protected JsonNode parseJson(String jsonString) {
		try {
			return OBJECT_MAPPER.readTree(jsonString);
		} catch (IOException e) {
			throw new RuntimeException("Could not parse " + jsonString + " : " + e.getMessage());
		}
	}

}

