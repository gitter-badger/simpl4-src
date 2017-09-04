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
package org.ms123.common.form;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.*;
import java.util.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.git.GitService;
import org.ms123.common.stencil.api.StencilService;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.permission.api.PermissionService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

/** FormService implementation
 */
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=form" })
public class FormServiceImpl extends BaseFormServiceImpl implements FormService {

	private static final Logger m_logger = LoggerFactory.getLogger(FormServiceImpl.class);

	private static final String NAME = "name";

	protected JSONSerializer m_js = new JSONSerializer();

	private PermissionService m_permissionService;

	public FormServiceImpl() {
		m_js.prettyPrint(true);
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		m_bc = bundleContext;
	}

	protected void deactivate() throws Exception {
		System.out.println("FormServiceImpl deactivate");
	}

	/* BEGIN JSON-RPC-API*/
	public Map validateForm(
			@PName("namespace")        String namespace, 
			@PName(NAME)               String name, 
			@PName("data")             Map data,
			@PName("cleanData")        Boolean cleanData
				) throws RpcException {
		try {
			return _validateForm(namespace, name, data,cleanData);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "FormServiceImpl.validateForm:", e);
		} finally {
		}
	}

	/* END JSON-RPC-API*/
	@Reference(target = "(kind=jdo)", dynamic = true, optional = true)
	public void setDataLayer(DataLayer dataLayer) {
		System.out.println("FormServiceImpl.setDataLayer:" + dataLayer);
		m_dataLayer = dataLayer;
	}
	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		System.out.println("FormServiceImpl.setGitService:" + gitService);
		m_gitService = gitService;
	}
	@Reference(dynamic = true, optional = true)
	public void setStencilService(StencilService stencilService) {
		System.out.println("FormServiceImpl.setStencilService:" + stencilService);
		m_stencilService = stencilService;
	}
}
