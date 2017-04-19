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

