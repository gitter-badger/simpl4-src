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
//import liquibase.servicelocator.CustomResolverServiceLocator;
//import liquibase.servicelocator.PackageScanClassResolver;
//import liquibase.servicelocator.ServiceLocator;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;


/**
 *
 */
@SuppressWarnings({"unchecked","deprecation"})
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

	private  DmnEngine getDmnEngine(String configurationResource) {
		DmnEngine dmnEngine = dmnEngines.get(configurationResource);
		if (dmnEngine == null) {
			info(this,"==== BUILDING DMN ENGINE ====");

			String sh = System.getProperty("workspace");
			dmnEngine = DmnEngineConfiguration.createStandaloneDmnEngineConfiguration()
				.setDatabaseSchemaUpdate("true")
				.setJdbcUrl("jdbc:h2:file:" + sh + "/flowable/h2;DB_CLOSE_DELAY=1000")
				.buildDmnEngine();

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
		//initLiquibase();
		if (cachedDmnEngine == null) {
			cachedDmnEngine = getDmnEngine("default");
		}
		this.dmnEngineConfiguration = cachedDmnEngine.getDmnEngineConfiguration();
		this.repositoryService = cachedDmnEngine.getDmnRepositoryService();
		this.ruleService = cachedDmnEngine.getDmnRuleService();
		this.managementService = cachedDmnEngine.getDmnManagementService();
		info(this, "Flowable.dmnEngineConfiguration:"+this.dmnEngineConfiguration);
		info(this, "Flowable.repositoryService:"+this.repositoryService);
		info(this, "Flowable.ruleService:"+this.ruleService);
	}

	protected DmnDefinition _deployDMN( String namespace,String name, String jsonString ){
		JsonNode node = parseJson( jsonString );
		DmnDefinition dmnDefinition = new DmnJsonConverter().convertToDmn(node, "abc", 1, new Date());
		info(this,"dmnDefinition:"+dmnDefinition);
		this.repositoryService.createDeployment()
			.name(name)
			.category(namespace)
			.addDmnModel(name, dmnDefinition)
			.deploy();
		return dmnDefinition;
	}

	protected String readJsonToString(String resource) {
		InputStream is = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream(resource);
			return IOUtils.toString(is);
		} catch (IOException e) {
			throw new RuntimeException("Could not read " + resource + " : " + e.getMessage());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	protected JsonNode parseJson(String jsonString) {
//		String jsonString = readJsonToString(resource);
		try {
			return OBJECT_MAPPER.readTree(jsonString);
		} catch (IOException e) {
			throw new RuntimeException("Could not parse " + jsonString + " : " + e.getMessage());
		}
	}

	private void initLiquibase(){
		//PackageScanClassResolver resolver = new OSGIPackageScanClassResolver(this.bc.getBundle());

		//ServiceLocator.setInstance(new CustomResolverServiceLocator(resolver));
		//ServiceLocator.reset();
	}

}
