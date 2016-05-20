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
