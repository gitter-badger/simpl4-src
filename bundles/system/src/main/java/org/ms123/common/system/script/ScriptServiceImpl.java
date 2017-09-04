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
package org.ms123.common.system.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.ms123.common.git.GitService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.apache.shiro.authz.annotation.RequiresRoles;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;

/** ScriptService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=script" })
public class ScriptServiceImpl extends BaseScriptServiceImpl implements ScriptService {

	public ScriptServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		System.out.println("CompileEventHandlerService.activate.props:" + props);
		try {
			Bundle b = bundleContext.getBundle();
			m_bundleContext = bundleContext;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(Map<String, Object> props) {
		info(this,"ScriptServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this,"ScriptServiceImpl.deactivate");
	}


	@RequiresRoles("admin")
	public void  compileScript(
			@PName(StoreDesc.NAMESPACE) String namespace,
			@PName("path") String path,
			@PName("content") String content,
			@PName("type") String type
		 ) throws RpcException {
		try {
			m_gitService.putContent(namespace,path, type, content);
			compileScript(m_bundleContext, namespace, path,content, type);
		} catch (Throwable e) {
			e.printStackTrace();
			String msg = checkNull(e.getMessage());
			while (e.getCause() != null) {
				e = e.getCause();
				if( !(e.getClass().toString().equals("class jdk.nashorn.internal.runtime.ParserException"))){
					msg += "\n"+checkNull(e.getMessage());
				}
			}
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ScriptService.compileScript:"+msg);
		}
	}

	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		System.out.println("ScriptServiceImpl.setGitService:" + gitService);
		this.m_gitService = gitService;
	}
	@Reference(dynamic = true, optional = true)
	public void setCamelService(CamelService camelService) {
		System.out.println("ScriptServiceImpl.setCamelService:" + camelService);
		this.m_camelService = camelService;
	}
}
