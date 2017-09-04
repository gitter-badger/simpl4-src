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
package org.ms123.common.management;

import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.apache.commons.beanutils.PropertyUtils;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.workflow.api.WorkflowService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.domainobjects.api.DomainObjectsService;
import org.ms123.common.system.compile.CompileService;
import org.ms123.common.system.dbmeta.DbMetaService;
import org.ms123.common.git.GitService;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;

/** ManagementService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=management" })
public class ManagementServiceImpl extends BaseManagementServiceImpl implements ManagementService {

	private static final Logger m_logger = LoggerFactory.getLogger(ManagementServiceImpl.class);

	private DataLayer m_dataLayer;

	public ManagementServiceImpl() {
		info(this,"ManagementServiceImpl construct");
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		info(this,"ManagementServiceImpl.activate.props:" + props);
		try {
			info(this,"ManagementServiceImpl.activate");
			m_bundleContext = bundleContext;
			registerEventHandler();
			m_bundleContext.addFrameworkListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(Map<String, Object> props) {
		info(this,"ManagementServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this,"ManagementServiceImpl.deactivate");
		m_serviceRegistration.unregister();
	}

	/* BEGIN JSON-RPC-API*/
	@RequiresRoles("admin")
	public Map managementCmd(
			@PName("host")             String host, 
			@PName("cmd")              String cmd) throws RpcException {
		try {
			return null;
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ManagementService.managementCmd:", e);
		} finally {
		}
	}

	@Reference(target = "(kind=jdo)", dynamic = true, optional = true)
	public void setDataLayer(DataLayer dataLayer) {
		info(this,"ManagementServiceImpl.setDataLayer:" + dataLayer);
		m_dataLayer = dataLayer;
	}
	@Reference(dynamic = true, optional=true)
	public void setDomainObjectsService(DomainObjectsService paramService) {
		this.m_domainobjectsService = paramService;
		info(this,"ManagementServiceImpl.setDomainObjectsService:" + paramService);
	}
	@Reference(dynamic = true, optional=true)
	public void setCompileService(CompileService paramService) {
		this.m_compileService = paramService;
		info(this,"ManagementServiceImpl.setCompileService:" + paramService);
	}
	@Reference(dynamic = true, optional=true)
	public void setDbMetaService(DbMetaService paramService) {
		this.m_dbmetaService = paramService;
		info(this,"ManagementServiceImpl.setDbMetaService:" + paramService);
	}
	@Reference(dynamic = true, optional=true)
	public void setWorkflowService(WorkflowService paramService) {
		this.m_workflowService = paramService;
		info(this,"ManagementServiceImpl.setWorkflowService:" + paramService);
	}
	@Reference(dynamic = true, optional=true)
	public void setCamelService(CamelService paramService) {
		this.m_camelService = paramService;
		info(this,"ManagementServiceImpl.setCamelService:" + paramService);
	}
	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		info(this,"ManagementServiceImpl.setGitService:" + gitService);
		m_gitService = gitService;
	}
	@Reference(dynamic = true, optional = true)
	public void setPermissionService(PermissionService paramService) {
		info(this, "ManagementServiceImpl:" + paramService);
		this.m_permissionService = paramService;
	}
}
