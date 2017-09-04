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

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.Map;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.git.GitService;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.script.ScriptEngineService;
import org.osgi.framework.BundleContext;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

/** DmnService implementation
 */
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=dmn" })
public class DmnServiceImpl extends BaseDmnServiceImpl implements DmnService {

	private static final String NAME = "name";
	private static final String NAMESPACE = "namespace";

	public DmnServiceImpl() {
		this.js.prettyPrint(true);
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bc = bundleContext;
		this.initDmnEngine();
	}

	protected void deactivate() throws Exception {
		System.out.println("DmnServiceImpl deactivate");
	}

	/* BEGIN JSON-RPC-API*/
	//	@RequiresRoles("admin")
	public Map deployDMN(@PName(NAMESPACE) String namespace, @PName(NAME) String name) throws RpcException {
		try {
			return _deployDMN(namespace, name);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "DmnServiceImpl.deployDMN:", e);
		} finally {
		}
	}

	public List<Map> executeDecision(
			@PName(NAMESPACE) String namespace, 
			@PName("decisionString") @POptional String decisionString, 
			@PName("variables") Map variables) throws RpcException {
		try {
			return _executeDecision(namespace, decisionString, variables);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "DmnServiceImpl.executeDecision:", e);
		} finally {
		}
	}

	/* END JSON-RPC-API*/
	@Reference(target = "(kind=jdo)", dynamic = true, optional = true)
	public void setDataLayer(DataLayer dataLayer) {
		System.out.println("DmnServiceImpl.setDataLayer:" + dataLayer);
		this.dataLayer = dataLayer;
	}

	@Reference(dynamic = true, optional = true)
	public void setScriptEngineService(ScriptEngineService scriptEngine) {
		System.out.println("DmnServiceImpl.setScriptEngineService:" + scriptEngine);
		this.scriptEngineService = scriptEngine;
	}

	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		System.out.println("DmnServiceImpl.setGitService:" + gitService);
		this.gitService = gitService;
	}
}

