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
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.form.FormService;
import org.ms123.common.git.GitService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.process.converter.Simpl4BpmnJsonConverter;
import org.ms123.common.process.engineapi.process.ProcessDefinitionCandidateResource;
import org.ms123.common.process.engineapi.process.ProcessDefinitionDiagramResource;
import org.ms123.common.process.engineapi.process.ProcessDefinitionsResource;
import org.ms123.common.process.engineapi.process.ProcessInstanceDiagramResource;
import org.ms123.common.process.engineapi.process.ProcessInstanceResource;
import org.ms123.common.process.engineapi.process.ProcessInstancesResource;
import org.ms123.common.process.engineapi.process.StartProcessInstanceResource;
import org.ms123.common.process.engineapi.repository.DeploymentsDeleteResource;
import org.ms123.common.process.engineapi.task.TaskFormPropertiesResource;
import org.ms123.common.process.engineapi.task.TaskOperationResource;
import org.ms123.common.process.engineapi.task.TasksResource;
import org.ms123.common.process.engineapi.repository.DeploymentResource;
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
import org.osgi.service.event.EventAdmin;
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


	public ProcessServiceImpl() {
		this.js.prettyPrint(true);
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bc = bundleContext;
	}

	protected void deactivate() throws Exception {
		System.out.println("ProcessServiceImpl deactivate");
	}

	public ProcessEngine getPE() {
		return null;//@@@MS;
	}
	public PermissionService getPermissionService(){
		return this.permissionService;
	}

	public FormService getFormService(){
		return this.formService;
	}
	public GitService getGitService(){
		return this.gitService;
	}
	public DataLayer getDataLayer(){
		return this.dataLayer;
	}
	public EventAdmin getEventAdmin(){
		return this.eventAdmin;
	}

	public Map executeTaskOperation( String taskId, String operation, Map<String, Object> startParams, boolean check) {
		try {
			TaskOperationResource tr = new TaskOperationResource(this, taskId, operation, startParams, check);
			return tr.executeTaskOperation();
		} catch (Exception e) {
			throw new RuntimeException( "ProcessService.executeTaskOperation:", e);
		}
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
	public Map getProcessDefinitions(
			@PName("namespace")  @POptional String namespace, 
			@PName("key")  @POptional String key, 
			@PName("name")  @POptional String name, 
			@PName("version")  @POptional Integer version, 
			@PName("startableByUser")  @POptional String user, 
			@PName("startableByGroup")  @POptional String group, 
			@PName("listParams")       @POptional Map<String, Object> listParams) throws RpcException {
		try {
			ProcessDefinitionsResource pdr = new ProcessDefinitionsResource(this, listParams, namespace,key, name,version, user,group);
			return pdr.getProcessDefinitions();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.getProcessDefinitions:", e);
		}
	}

	public void setProcessDefinitionCandidates(
			@PName("processDefinitionId")  @POptional String processDefinitionId, 
			@PName("candidateUsers")  @POptional List<String> userList, 
			@PName("candidateGroups")  @POptional List<String> groupList
			) throws RpcException {
		try {
			ProcessDefinitionCandidateResource pdc = new ProcessDefinitionCandidateResource(this,processDefinitionId, userList,groupList);
			pdc.execute();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.setProcessDefinitionCandidates:", e);
		}
	}

	public Map getProcessInstance(
			@PName("processInstanceId") String processInstanceId) throws RpcException {
		try {
			ProcessInstanceResource pir = new ProcessInstanceResource(this, processInstanceId);
			return pir.getProcessInstance();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.getProcessInstance:", e);
		}finally{
		}
	}

	@RequiresRoles("admin")
	public Map deleteProcessInstance(
			@PName("processInstanceId") String processInstanceId,
			@PName("reason")  @POptional String reason 
				) throws RpcException {
		try {
			ProcessInstanceResource pir = new ProcessInstanceResource(this, processInstanceId, reason);
			return pir.deleteProcessInstance();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.deleteProcessInstance:", e);
		}finally{
		}
	}

	public Map getProcessInstances(
			@PName("namespace") String namespace, 
			@PName("processDefinitionId") @POptional String processDefinitionId, 
			@PName("processDefinitionKey") @POptional String processDefinitionKey, 
			@PName("businessKey")      @POptional String businessKey, 
			@PName("unfinished")       @POptional Boolean unfinished, 
			@PName("finished")         @POptional Boolean finished, 
			@PName("listParams")       @POptional Map<String, Object> listParams) throws RpcException {
		try {
			if (processDefinitionId == null && processDefinitionKey == null) {
				throw new RuntimeException("getProcessInstance.no processDefinition{Id,Key}");
			}
			ProcessInstancesResource pir = new ProcessInstancesResource(this, listParams, processDefinitionId, processDefinitionKey, businessKey, unfinished, finished,namespace);
			return pir.getProcessInstances();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.getProcessInstances:", e);
		}
	}

	public Map startProcessInstance(
			//@PName(StoreDesc.STORE_ID) @POptional String storeId, 
			@PName("namespace") @POptional String namespace, 
			@PName("version") @POptional Integer version, 
			@PName("processDefinitionId") @POptional String processDefinitionId, 
			@PName("processDefinitionKey") @POptional String processDefinitionKey, 
			@PName("processDefinitionName") @POptional String processDefinitionName, 
			@PName("messageName") @POptional String messageName, 
			@PName("businessKey")      @POptional String businessKey, 
			@PName("startParams")      @POptional Map<String, Object> startParams) throws RpcException {
		try {
			if (processDefinitionId == null && processDefinitionKey == null && processDefinitionName == null && messageName == null) {
				throw new RuntimeException("startProcessInstance.no processDefinition{Id,Key,Name,MessageName}");
			}
			StartProcessInstanceResource spir = new StartProcessInstanceResource(this, namespace,version, processDefinitionId, processDefinitionKey, processDefinitionName,messageName, businessKey, startParams);
			return spir.startProcessInstance();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.startProcessInstance:", e);
		}
	}

	public Map getVariables(
			@PName("namespace")      String namespace,
			@PName("formId")      String formId,
			@PName("executionId")      String executionId) throws RpcException {
		try {
			Set<String> vars = this.formService.getFormInputVariables(namespace,formId);
			return this.getPE().getRuntimeService().getVariables(executionId,vars);
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.getVariables:", e);
		}finally{
		}
	}

	public Map getTasks(
			@PName("queryParams")      @POptional Map<String, Object> queryParams, 
			@PName("listParams")       @POptional Map<String, Object> listParams) throws RpcException {
		try {
			TasksResource tr = new TasksResource(this, listParams, queryParams);
			return tr.getTasks();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.getTasks:", e);
		}finally{
		}
	}

	public Map getTaskFormProperties(
			@PName("executionId")      String executionId, 
			@PName("taskId")           String taskId) throws RpcException {
		try {
			TaskFormPropertiesResource tr = new TaskFormPropertiesResource(this, executionId, taskId);
			return tr.getTaskFormProperties();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.getTaskFormProperties:", e);
		}finally{
		}
	}

	public Map executeTaskOperation(
			@PName("taskId")           String taskId, 
			@PName("operation")        String operation, 
			@PName("startParams")      @POptional Map<String, Object> startParams) throws RpcException {
		try {
			TaskOperationResource tr = new TaskOperationResource(this, taskId, operation, startParams);
			return tr.executeTaskOperation();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.executeTaskOperation:", e);
		}
	}

	public String getDefinitionDiagram(
			@PName("processDefinitionId") String processDefinitionId) throws RpcException {
		try {
			ProcessDefinitionDiagramResource pir = new ProcessDefinitionDiagramResource(this, processDefinitionId);
			return pir.getDiagram();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.getDefintionDiagram:", e);
		}
	}

	public String getInstanceDiagram(
			@PName("processInstanceId") String processInstanceId) throws RpcException {
		try {
			ProcessInstanceDiagramResource pir = new ProcessInstanceDiagramResource(this, processInstanceId);
			return pir.getDiagram();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.getInstanceDiagram:", e);
		}
	}

	@RequiresRoles("admin")
	public Map deleteDeployments(
			@PName("deploymentIds") List<String> deploymentIds,
			@PName("cascade")         @POptional @PDefaultBool(false) Boolean cascade 
				) throws RpcException {
		try {
			DeploymentsDeleteResource ddr = new DeploymentsDeleteResource(this, deploymentIds, cascade);
			return ddr.execute();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.deleteDeployments:", e);
		}
	}

	@RequiresRoles("admin")
	public Object deployProcess(
			@PName("namespace")        String namespace, 
			@PName("path")             String path) throws RpcException {
		try {
			DeploymentResource dp = new DeploymentResource(this);
			return dp.deployProcess(namespace, path, true, false);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.deployProcess:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Object undeployProcess(
			@PName("namespace")        String namespace, 
			@PName("path")             String path, 
			@PName("all")              @PDefaultBool(false) @POptional Boolean all) throws RpcException {
		try {
			DeploymentResource dp = new DeploymentResource(this);
			return dp.deployProcess(namespace, path, false, all);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.undeployProcess:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Object saveProcess(
			@PName("namespace")        String namespace, 
			@PName("path")             String path, 
			@PName("data")             Map data) throws RpcException {
		try {
			this.gitService.putContent(namespace, path, "sw.process", this.js.deepSerialize(data));
			DeploymentResource dp = new DeploymentResource(this);
			return dp.deployProcess(namespace, path, true, false);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ProcessService.saveProcess:", e);
		} finally {
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
	@Reference(target = "(kind=jdo)", dynamic = true, optional = true)
	public void setDataLayer(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}

	@Reference(dynamic = true,optional=true)
	public void setFormService(FormService paramFormService) {
		this.formService = paramFormService;
	}

	@Reference(dynamic = true,optional=true)
	public void setEventAdmin(EventAdmin paramEventAdmin) {
		this.eventAdmin = paramEventAdmin;
	}
}
