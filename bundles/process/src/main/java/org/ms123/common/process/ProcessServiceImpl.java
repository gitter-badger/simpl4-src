/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014-2017] [Manfred Sattler] <manfred@ms123.org>
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
package org.ms123.common.process;


import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.camunda.bpm.engine.impl.cfg.orientdb.OrientdbProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.ms123.common.git.GitService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.process.converter.Simpl4BpmnJsonConverter;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.system.orientdb.OrientDBService;
import org.ms123.common.utils.IOUtils;
import org.osgi.framework.BundleContext;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;


/** ProcessService implementation
 */
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=process" })
public class ProcessServiceImpl extends BaseProcessServiceImpl implements ProcessService {
	private static final String NAME = "name";
	private static final String NAMESPACE = "namespace";

	protected PermissionService permissionService;

	public ProcessServiceImpl() {
		this.js.prettyPrint(true);
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bc = bundleContext;
	}

	protected void deactivate() throws Exception {
		System.out.println("ProcessServiceImpl deactivate");
	}

	/* BEGIN JSON-RPC-API*/
	@RequiresRoles("admin")
	public void getBpmn(
			@PName("namespace")        String namespace, 
			@PName("path")             String path, HttpServletResponse response) throws RpcException {
		try {
			String processJson = this.gitService.getFileContent(namespace, path);
			byte[] bpmnBytes = Simpl4BpmnJsonConverter.getBpmnXML(processJson, namespace, path);
			response.setContentType("application/xml");
			response.addHeader("Content-Disposition", "inline;filename=xxx.bpmn20.xml");
			IOUtils.write(bpmnBytes, response.getOutputStream());
			response.setStatus(HttpServletResponse.SC_OK);
			response.getOutputStream().close();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.getBpmn:", e);
		}
	}

	/* END JSON-RPC-API*/
	@Reference(dynamic = true, optional = true)
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		this.gitService = gitService;
		//m_processEngineConfiguration.getBeans().put("gitService", gitService);
	}
	@Reference
	public void setOrientDBService(OrientDBService paramEntityService) {
		orientdbService = paramEntityService;
		info(this, "ProcessServiceImpl.setOrientDBService:" + paramEntityService);
	}
}
