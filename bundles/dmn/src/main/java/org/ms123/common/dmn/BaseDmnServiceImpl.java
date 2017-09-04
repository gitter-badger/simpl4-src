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
package org.ms123.common.dmn;


import flexjson.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.spi.el.DmnScriptEngineResolver;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.git.GitService;
import org.ms123.common.stencil.api.StencilService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.script.ScriptEngineService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.*;
import java.security.MessageDigest;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
class BaseDmnServiceImpl {

	protected BundleContext bc;

	protected DataLayer dataLayer;

	protected GitService gitService;
	protected ScriptEngineService scriptEngineService;

	protected JSONDeserializer ds = new JSONDeserializer();

	protected JSONSerializer js = new JSONSerializer();

	private Map<String, DmnDecision> dmnCache = new HashMap<String, DmnDecision>();

	private DmnEngine dmnEngine;

	protected void initDmnEngine() {
		DefaultDmnEngineConfiguration config = new DefaultDmnEngineConfiguration();
		DmnScriptEngineResolver scriptResolver = new DmnScriptEngineResolver() {
			@Override
			public ScriptEngine getScriptEngineForLanguage(String language) {
				info(this, "getScriptEngineForLanguage:" + language);
				return scriptEngineService.getEngineByName(language);
			}
		};
		config.setScriptEngineResolver(scriptResolver);
		this.dmnEngine = config.buildEngine();
	}

	protected Map _deployDMN(String namespace, String name) {
		return null;
	}

	protected List<Map> _executeDecision(String namespace, String decisionString, Map variables) throws Exception {
		String md5 = getMD5OfUTF8(decisionString);
		DmnDecision decision = getDecision(namespace, md5, decisionString);
		info(this, "Dmn.executeDecision(" + md5 + ").variables:" + variables);
		DmnDecisionResult result = this.dmnEngine.evaluateDecision(decision, variables);
		info(this, "Dmn.executeDecision(" + md5 + ").result:" + result.getResultList());
		return new ArrayList<Map>(result.getResultList());
	}

	private DmnDecision getDecision(String namespace, String md5, String decisionString) throws Exception {
		DmnDecision dmnDecision = this.dmnCache.get(md5);
		if( dmnDecision != null){
			return dmnDecision;
		}
		Map rulesMap = (Map)ds.deserialize( decisionString);
		if( rulesMap.get("decision") != null){
			rulesMap = (Map)rulesMap.get("decision");
		}
		RulesConverter rc = new RulesConverter(rulesMap);
		dmnDecision = rc.convert("MD5"+ md5);
		this.dmnCache.put( md5, dmnDecision);
		return dmnDecision;
	}

	private String getMD5OfUTF8(String text) {
		try {
			MessageDigest msgDigest = MessageDigest.getInstance("MD5");
			byte[] mdbytes = msgDigest.digest(text.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
				String hex = Integer.toHexString(0xff & mdbytes[i]);
				if (hex.length() == 1){
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception ex) {
			throw new RuntimeException("BaseDmnServiceImpl.getMD5OfUTF8",ex);
		}
	}
}

