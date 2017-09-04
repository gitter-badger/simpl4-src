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
package org.ms123.common.workflow.stencil;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.io.*;
import java.util.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.git.GitService;
import org.ms123.common.docbook.DocbookService;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.permission.api.PermissionException;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.utils.UtilsService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.Event;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

/** StencilService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=stencil" })
public class StencilServiceImpl extends BaseStencilServiceImpl implements org.ms123.common.stencil.api.StencilService {

	private static final Logger m_logger = LoggerFactory.getLogger(StencilServiceImpl.class);

	private static final String STENCIL_ENTITY = "stencil";

	private static final String NAME = "name";

	public StencilServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		System.out.println("StencilServiceImpl.activate.props:" + props);
	}

	protected void deactivate() throws Exception {
		System.out.println("StencilServiceImpl deactivate");
	}

	/* BEGIN JSON-RPC-API*/
	public List<Map> getAddonStencils(
			@PName(StoreDesc.NAMESPACE) String namespace) throws RpcException {
		try {
			return m_gitMetaData.getAddonStencils(namespace);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "StencilServiceImpl.getStencils:", e);
		} finally {
		}
	}

	public Map<String, Object> getAddonStencil(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(NAME)               String name) throws RpcException {
		try {
			return m_gitMetaData.getAddonStencil(namespace, name);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "StencilServiceImpl.getStencil:", e);
		}
	}

	public void saveAddonStencil(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(NAME)               String name, 
			@PName("data")             Map data) throws RpcException {
		try {
			m_gitMetaData.saveAddonStencil(namespace, name, data);
			List sList = new ArrayList();
			sList.add( name);
			_generateClasses(namespace,sList);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "StencilServiceImpl.saveStencil:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public void deleteAddonStencil(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(NAME)               String name) throws RpcException {
		try {
			m_gitMetaData.deleteAddonStencil(namespace, name);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "StencilServiceImpl.deleteStencil:", e);
		} finally {
		}
	}

	public void generateClasses(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName("stencilList")         @POptional List<String> stencilList) throws RpcException {
		try {
			_generateClasses(namespace, stencilList);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "StencilServiceImpl.generateClasses:", e);
		} finally {
		}
	}

	public String getStencilSet(
			@PName("namespace")  @POptional String namespace, 
			@PName("name")             String name) throws RpcException {
		try {
			return _getStencilSet(namespace, name);
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "StencilService.getStencilSet:", e);
		}
	}
	public String getStencilView(
			@PName("stencilsetName")             String stencilsetName,
			@PName("path")             String path
					) throws RpcException {
		try {
			return m_gitMetaData.getStencilView(stencilsetName,path);
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "StencilService.getStencilView:", e);
		}
	}
	public String getStencilIcon(
			@PName("stencilsetName")             String stencilsetName,
			@PName("path")             String path
					) throws RpcException {
		try {
			return m_gitMetaData.getStencilIcon(stencilsetName,path);
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "StencilService.getStencilIcon:", e);
		}
	}
	/* END JSON-RPC-API*/
	@Reference(dynamic = true)
	public void setEventAdmin(EventAdmin paramEventAdmin) {
		info("NamespaceServiceImpl.setEventAdmin:" + paramEventAdmin);
		this.m_eventAdmin = paramEventAdmin;
	}
	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		System.out.println("StencilServiceImpl.setGitService:" + gitService);
		m_gitMetaData = new GitMetaDataImpl(gitService);
	}
	@Reference(dynamic = true, optional = true)
	public void setDocbookService(DocbookService docbookService) {
		System.out.println("StencilServiceImpl.setDocbookService:" + docbookService);
		m_docbookService = docbookService;
	}
}
