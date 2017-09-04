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
package org.ms123.common.system.compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.ms123.common.git.GitService;
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

/** CompileService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=compile" })
public class CompileServiceImpl extends BaseCompileServiceImpl implements CompileService {

	public CompileServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		try {
			Bundle b = bundleContext.getBundle();
			m_bundleContext = bundleContext;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(Map<String, Object> props) {
		info(this,"CompileServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this,"CompileServiceImpl.deactivate");
	}


	@RequiresRoles("admin")
	public void  compileGroovyAll(
		 ) throws RpcException {
		try {
			_compileGroovyAll();
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "CompileServiceImpl.groovycompile:",e);
		}
	}

	@RequiresRoles("admin")
	public void  compileJavaAll(
		 ) throws RpcException {
		try {
			_compileJavaAll();
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "CompileServiceImpl.javacompile:",e);
		}
	}

	@RequiresRoles("admin")
	public List<Map> compileJavaNamespace( @PName("namespace") String namespace) throws RpcException {
		try {
			return _compileJavaNamespace(namespace);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "CompileServiceImpl.javacompile:",e);
		}
	}
	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		this.m_gitService = gitService;
	}
}
