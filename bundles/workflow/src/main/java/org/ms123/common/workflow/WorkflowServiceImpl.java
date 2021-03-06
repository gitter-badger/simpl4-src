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
package org.ms123.common.workflow;

import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Collection;
//import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.engine.impl.scripting.BeansResolverFactory;
import org.activiti.engine.impl.scripting.ResolverFactory;
import org.activiti.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.scripting.VariableScopeResolverFactory;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.Deployment;
import org.ms123.common.workflow.processengine.ProcessEngineFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.script.ScriptEngineManager;
import org.ms123.common.data.api.DataLayer;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.ms123.common.system.script.ScriptEngineService;
import org.ms123.common.libhelper.FileSystemClassLoader;
import org.ms123.common.utils.Utils;
import org.ms123.common.git.GitService;
import org.ms123.common.system.tm.TransactionService;
import org.ms123.common.workflow.converter.Simpl4BpmnJsonConverter;
import org.ms123.common.workflow.tasks.TaskScriptExecutor;
import javax.servlet.http.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.dmn.DmnService;
import org.ms123.common.system.registry.RegistryService;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.activiti.process.ProcessDefinitionResponse;
import org.ms123.common.docbook.DocbookService;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.utils.IOUtils;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.RpcException;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import flexjson.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.Process;
import org.apache.camel.CamelContext;
import javax.sql.DataSource;
import org.ms123.common.workflow.api.WorkflowService;
import org.activiti.engine.impl.interceptor.CommandContextFactory;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/** WorkflowService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=workflow" })
public class WorkflowServiceImpl implements org.ms123.common.workflow.api.WorkflowService,EventHandler {

	private static final Logger m_logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);

	protected JSONSerializer m_js = new JSONSerializer();

	protected JSONDeserializer m_ds = new JSONDeserializer();
	protected ProcessEngineFactory m_processEngineFactory;
	private  ServiceRegistration m_serviceRegistration;
	private DataSource m_dataSource;

	private String m_namespace;

	private String m_workspace;

	private ProcessEngine m_processEngine = null;
	private ShiroJobExecutor m_shiroJobExecutor;

	private EventAdmin m_eventAdmin;

	private SpringProcessEngineConfiguration m_processEngineConfiguration = new SpringProcessEngineConfiguration();

	private ScriptEngineService m_scriptEngineService;

	protected PermissionService m_permissionService;
	protected DmnService m_dmnService;
	protected RegistryService m_registryService;

	protected ActivitiService m_activitiService;
	protected TransactionService m_transactionService;

	protected AuthService m_authService;

	protected GitService m_gitService;

	private DataLayer m_dataLayer;

	private BundleContext m_bundleContext;

	final static String[] topics = new String[] {
		"task/classes_generated"
	};
	public void handleEvent(Event event) {
		info("WorkflowServiceImpl1.Event: " + event);
		try{
			if( "task/classes_generated".equals(event.getTopic())){
				info("WorkflowServiceImpl2.Event: " + event);
				m_processEngineFactory.setFsClassLoader(createFsClassLoader2());
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
	}
	public WorkflowServiceImpl() {
		m_processEngineConfiguration.setBeans(new HashMap());
		m_js.prettyPrint(true);
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		info("WorkflowServiceImpl.activate");
		//initProcessEngine(bundleContext);
		m_bundleContext = bundleContext;
		Dictionary d = new Hashtable();
		d.put(EventConstants.EVENT_TOPIC, topics);
		m_serviceRegistration = m_bundleContext.registerService(EventHandler.class.getName(), this, d);
	}

	protected void deactivate() {
		info("WorkflowServiceImpl.deactivate");
		m_shiroJobExecutor.shutdown();
		if( m_processEngine != null){
			m_processEngine.close();
		}
		((DataSourceWrapper)m_dataSource).destroy();
		h2Close(m_dataSource);
		m_dataSource = null;
		m_serviceRegistration.unregister();
	}

	private ClassLoader createFsClassLoader1(){
		String sh = System.getProperty("workspace");
		File[] locations = new File[1];
		locations[0] = new File(sh + "/java", "classes");
		return new FileSystemClassLoader(org.activiti.engine.impl.javax.el.ExpressionFactory.class.getClassLoader(), locations);
	}
	private ClassLoader createFsClassLoader2(){
		String sh = System.getProperty("workspace");
		File[] locations = new File[1];
		locations[0] = new File(sh + "/java", "classes");
		return new FileSystemClassLoader(locations);
	}
	public ProcessEngine getProcessEngine() {
		if (m_processEngine == null) {
			initProcessEngine(m_bundleContext);
		}
		return m_processEngine;
	}

	private DataSource getDataSource(String url){
		if( m_dataSource!=null) return m_dataSource;

		DataSource _ds = null;
		if( m_transactionService.getJtaLocator().equals("bitronix")){
			_ds = getPoolingDataSource(url);
		}else{
			org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
			ds.setUser("sa");
			ds.setPassword("");
			ds.setURL(url);
			_ds = ds;
		}
		m_dataSource = new DataSourceWrapper(_ds);
		return m_dataSource;
	}
	private PoolingDataSource getPoolingDataSource(String url){
		PoolingDataSource ds = new PoolingDataSource();
		ds.setClassName("org.h2.jdbcx.JdbcDataSource");
		ds.setUniqueName("activiti");
		ds.setMaxPoolSize(15);
		ds.setAllowLocalTransactions(true);
		ds.setTestQuery("SELECT 1");
		ds.getDriverProperties().setProperty("user", "sa");
		ds.getDriverProperties().setProperty("password", "");
		ds.getDriverProperties().setProperty("URL", url);    
		return ds;
	}
	private SpringProcessEngineConfiguration initProcessEngine(BundleContext bundleContext) {
		SpringProcessEngineConfiguration c = m_processEngineConfiguration;
		ScriptingEngines se = new ScriptingEngines(m_scriptEngineService.getScriptEngineManager());
		List resolverFactories = new ArrayList<ResolverFactory>();
		resolverFactories.add(new VariableScopeResolverFactory());
		resolverFactories.add(new BeansResolverFactory());
		se.setScriptBindingsFactory(new ScriptBindingsFactory(resolverFactories));
		c.setScriptingEngines(se);
		CommandContextFactory ccf = createDefaultCommandContextFactory();
		ccf.setProcessEngineConfiguration(c);
		c.setCommandContextFactory(ccf);

		c.setDatabaseType("h2");
		String sh = System.getProperty("workspace");
		c.setDataSource(getDataSource("jdbc:h2:file:" + sh + "/activiti/h2;DB_CLOSE_DELAY=1000"));
		c.setDatabaseSchemaUpdate("true");
		c.setJdbcMaxActiveConnections(100);
		c.setJdbcMaxIdleConnections(25);
		m_shiroJobExecutor = new ShiroJobExecutor(c.getBeans());
		c.setJobExecutor(m_shiroJobExecutor);

		c.setClassLoader(createFsClassLoader1());
		c.setJobExecutorActivate(true);
		c.setTransactionManager( m_transactionService.getPlatformTransactionManager());
		c.setTransactionsExternallyManaged(true);
		c.setHistory("full");
		c.setMailServerHost("127.0.0.1");
		GroovyExpressionManager exManager = new GroovyExpressionManager();
		c.setExpressionManager(exManager);
		c.setIdentityService(new IdentityServiceImpl());
		c.getBeans().put("bundleContext", bundleContext);
		List<SessionFactory> customSessionFactories = c.getCustomSessionFactories();
		if (customSessionFactories == null) {
			customSessionFactories = new ArrayList<SessionFactory>();
		}
		customSessionFactories.add(new Simpl4GroupManagerFactory(m_authService, m_permissionService));
		customSessionFactories.add(new Simpl4UserManagerFactory(m_authService, m_permissionService));
		c.setCustomSessionFactories(customSessionFactories);
		ProcessEngineFactory pef = new ProcessEngineFactory();
		pef.setBundle(bundleContext.getBundle());
		pef.setProcessEngineConfiguration(c);
		try {
			pef.init(createFsClassLoader2());
			m_processEngine = pef.getObject();
			c.getBeans().put(PROCESS_ENGINE, m_processEngine);
			c.getBeans().put(WORKFLOW_SERVICE, this);
			//c.getBeans().put(CamelService.CAMEL_SERVICE, m_camelService);
			c.getBeans().put(PermissionService.PERMISSION_SERVICE, m_permissionService);
			c.getBeans().put(DmnService.DMN_SERVICE, m_dmnService);
			c.getBeans().put(RegistryService.REGISTRY_SERVICE, m_registryService);
			exManager.setProcessEngine(m_processEngine);
			m_shiroJobExecutor.setProcessEngine(m_processEngine);
		} catch (Exception e) {
			m_logger.error("WorkflowServiceImpl.activate.initProcessEngine", e);
			e.printStackTrace();
		}
		m_processEngineFactory = pef;
		return c;
	}

 	private CommandContextFactory createDefaultCommandContextFactory() {
    return new CommandContextFactory();
  }

	public CamelContext getCamelContextForProcess(String namespace, String processname) {
		throw new RuntimeException("WorkflowServiceImpl.getCamelContextForProcess not allowed");
	}

	public Object lookupServiceByName(String name) {
		BundleContext bc = m_bundleContext;
		Object service = null;
		ServiceReference sr = bc.getServiceReference(name);
		if (sr != null) {
			service = bc.getService(sr);
		}
		if (service == null) {
			throw new RuntimeException("CamelBehaviorDefaultImpl.Cannot resolve service:" + name);
		}
		return service;
	}

	public void executeScriptTask( String executionId, String tenantId, String processDefinitionKey, String pid, String script, Map newVariables, String taskName ){
		TaskScriptExecutor sce = new TaskScriptExecutor();
		VariableScope vs = new RuntimeVariableScope(m_processEngine.getRuntimeService(), executionId);
		sce.execute(tenantId,processDefinitionKey, pid, script, newVariables, vs,taskName, m_dataLayer,(WorkflowService)this);
	}

	public Map testRules(
			@PName("namespace")        String namespace, 
			@PName("name")             String name, 
			@PName("values")           Map values) throws RpcException {
		try {
			Map rules = getRules(name, namespace);
			RulesProcessor rp = new RulesProcessor(rules, values);
			Map ret = rp.execute();
			return ret;
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "WorkflowService.testRules:", e);
		}
	}

	private Map getRules(String name, String namespace) {
		String filterJson = m_gitService.searchContent(namespace, name, "sw.rule");
		Map contentMap = (Map) m_ds.deserialize(filterJson);
		return contentMap;
	}

	@RequiresRoles("admin")
	public void getBpmn(
			@PName("namespace")        String namespace, 
			@PName("path")             String path, HttpServletResponse response) throws RpcException {
		try {
			String processJson = m_gitService.getFileContent(namespace, path);
			byte[] bpmnBytes = Simpl4BpmnJsonConverter.getBpmnXML(processJson, namespace, path);
			response.setContentType("application/xml");
			response.addHeader("Content-Disposition", "inline;filename=xxx.bpmn20.xml");
			IOUtils.write(bpmnBytes, response.getOutputStream());
			response.setStatus(HttpServletResponse.SC_OK);
			response.getOutputStream().close();
		} catch (Exception e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "WorkflowService.getBpmn:", e);
		}
	}

	@RequiresRoles("admin")
	private Object deployProcess(String namespace, String path, boolean deploy, boolean all) throws Exception {
		info("deployProcess:"+namespace+"/path:"+path);
		String processJson = m_gitService.getFileContent(namespace, path);
		String deploymentId = null;
		String basename = getBasename(path);
		RepositoryService rs = getProcessEngine().getRepositoryService();
		List<Deployment> dl = null;
		if (all) {
			dl = rs.createDeploymentQuery().deploymentName(basename).list();
		} else {
			dl = rs.createDeploymentQuery().deploymentName(basename).deploymentTenantId(namespace).list();
		}
		info("Deployment:" + dl);
	/*	if (dl != null && dl.size() > 0) {
			for (Deployment dm : dl) {
				info("Deployment:" + dm.getName() + "/" + dm.getId());
				rs.deleteDeployment(dm.getId(), true);
			}
		}*/
		if (deploy) {
			Map shape = (Map) m_ds.deserialize(processJson);
			Map<String, Object> properties = (Map) shape.get("properties");
			Object m = properties.get("initialparameter");
			String initialParameter = null;
			if (m instanceof Map) {
				initialParameter = m_js.deepSerialize(m);
			} else {
				initialParameter = (String) properties.get("initialparameter");
				if (initialParameter == null) {
					initialParameter = m_js.deepSerialize(new HashMap());
				}
			}
			byte[] bpmnBytes = Simpl4BpmnJsonConverter.getBpmnXML(processJson, namespace, path);
			InputStream bais = new ByteArrayInputStream(bpmnBytes);
			DeploymentBuilder deployment = rs.createDeployment();
			deployment.name(basename);
			deployment.tenantId(namespace);
			deployment.addString("initialParameter", initialParameter);
			deploymentId = deployment.addInputStream(basename + ".bpmn20.xml", bais).deploy().getId();
			info("deploymentId:" + deploymentId);
			Map pdefs = m_activitiService.getProcessDefinitions(namespace, basename, null, -1, null, null, null);
			List<ProcessDefinitionResponse> pList = (List) pdefs.get("data");
			m_js.prettyPrint(true);
			info("PList:" + m_js.deepSerialize(pList));
			if (pList.size() != 1) {
				throw new RuntimeException("WorkflowService.deployProcess(" + namespace + "," + basename + "):not " + (pList.size() == 0 ? "found" : "uniqe"));
			}
			String processdefinitionId = pList.get(0).getId();
			String groups = (String) properties.get("startablegroups");
			List groupList = null;
			if (groups != null && groups.trim().length() > 0) {
				groupList = new ArrayList<String>(Arrays.asList(groups.split(",")));
			}
			List userList = null;
			String users = (String) properties.get("startableusers");
			if (users != null && users.trim().length() > 0) {
				userList = new ArrayList<String>(Arrays.asList(users.split(",")));
			}
			info("userList:" + userList + "/grList:" + groupList);
			m_activitiService.setProcessDefinitionCandidates(processdefinitionId, userList, groupList);
		} else {
			deploymentId = null;
		}
		Map map = new HashMap();
		map.put("deploymentId", deploymentId);
		return map;
	}
	public synchronized void deployAll(){
		List<Map> repos = m_gitService.getRepositories(new ArrayList(),false);
		for(Map<String,String> repo : repos){
			String namespace = repo.get("name");
			deployNamespace(namespace);
		}
	}
	private final String PATH = "path";
	public synchronized void deployNamespace(String namespace){
		List<String> types = new ArrayList();
		types.add(PROCESS_TYPE);
		types.add(DIRECTORY_TYPE);
		List<String> typesProcess = new ArrayList();
		typesProcess.add(PROCESS_TYPE);

		Map map= m_gitService.getWorkingTree(namespace, null, 100, types, null, null,null);
		List<Map> pathList = new ArrayList();
		toFlatList(map,typesProcess,pathList);

		for( Map pathMap : pathList){
			String path = (String)pathMap.get(PATH);
			deployProcess(namespace,path);
		}
	}

	private String getBasename(String path) {
		String e[] = path.split("/");
		return e[e.length - 1];
	}

	protected Object checkNull(Object o, String msg) {
		if (o == null) {
			throw new RuntimeException(msg);
		}
		return o;
	}

	private void toFlatList(Map<String,Object> fileMap,List<String> types,List<Map> result){
		String type = (String)fileMap.get("type");
		if( types.indexOf(type) != -1){
			result.add(fileMap);
		}
		List<Map> childList = (List)fileMap.get("children");
		for( Map child : childList){
			toFlatList(child,types,result);
		}
	}

	public synchronized void h2Close(DataSource ds) {
		info("h2Close.jta:"+m_transactionService.getJtaLocator());
		if( m_transactionService.getJtaLocator().equals("bitronix")){
			((PoolingDataSource)((DataSourceWrapper)m_dataSource).getDataSource()).close();
		}else{
			java.sql.Connection conn = null;
			try {
				conn = ds.getConnection();
				java.sql.Statement stat = conn.createStatement();
				stat.execute("shutdown compact");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/* BEGIN JSON-RPC-API*/
	@RequiresRoles("admin")
	public Object deployProcess(
			@PName("namespace")        String namespace, 
			@PName("path")             String path) throws RpcException {
		try {
			return deployProcess(namespace, path, true, false);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "WorkflowService.deployProcess:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Object undeployProcess(
			@PName("namespace")        String namespace, 
			@PName("path")             String path, 
			@PName("all")              @PDefaultBool(false) @POptional Boolean all) throws RpcException {
		try {
			return deployProcess(namespace, path, false, all);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "WorkflowService.undeployProcess:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Object saveProcess(
			@PName("namespace")        String namespace, 
			@PName("path")             String path, 
			@PName("data")             Map data) throws RpcException {
		try {
			m_gitService.putContent(namespace, path, "sw.process", m_js.deepSerialize(data));
			return deployProcess(namespace, path, true, false);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "WorkflowService.saveProcess:", e);
		} finally {
		}
	}

	/* END JSON-RPC-API*/
	private static void debug(String msg) {
		m_logger.debug(msg);
	}
	private static void info(String msg) {
		m_logger.info(msg);
	}

	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		this.m_gitService = gitService;
		info("WorkflowServiceImpl.setGitService:" + gitService);
		m_processEngineConfiguration.getBeans().put("gitService", gitService);
	}

	@Reference(target = "(kind=jdo)", dynamic = true, optional = true)
	public void setDataLayer(DataLayer dataLayer) {
		info("WorkflowServiceImpl.setDataLayer:" + dataLayer);
		m_dataLayer = dataLayer;
		m_processEngineConfiguration.getBeans().put(DataLayer.DATA_LAYER, dataLayer);
	}

	@Reference(dynamic = true)
	public void setEventAdmin(EventAdmin paramEventAdmin) {
		info("WorkflowServiceImpl.setEventAdmin:" + paramEventAdmin);
		this.m_eventAdmin = paramEventAdmin;
		m_processEngineConfiguration.getBeans().put("eventAdmin", paramEventAdmin);
	}

	@Reference
	public void setScriptEngineService(ScriptEngineService paramService) {
		m_scriptEngineService = paramService;
		info("WorkflowServiceImpl.setScriptEngineService:" + paramService);
	}

	@Reference(multiple = false, dynamic = true, optional = true)
	public void setAuthService(AuthService paramAuthService) {
		this.m_authService = paramAuthService;
		info("WorkflowServiceImpl.setAuthService:" + paramAuthService);
	}

	@Reference(multiple = false, dynamic = true, optional = true)
	public void setDocbookService(DocbookService paramDocbookService) {
		info("WorkflowServiceImpl.setDocbookService:" + paramDocbookService);
		m_processEngineConfiguration.getBeans().put("docbookService", paramDocbookService);
	}

	@Reference(multiple = false, dynamic = true, optional=true)
	public void setPermissionService(PermissionService paramPermissionService) {
		this.m_permissionService = paramPermissionService;
		info("WorkflowServiceImpl.setPermissionService:" + paramPermissionService);
	}
	@Reference(multiple = false, dynamic = true, optional=true)
	public void setDmnService(DmnService paramDmnService) {
		this.m_dmnService = paramDmnService;
		info("WorkflowServiceImpl.setDmnService:" + paramDmnService);
	}

	@Reference(multiple = false, dynamic = true, optional=true)
	public void setRegistryService(RegistryService paramRegistryService) {
		this.m_registryService = paramRegistryService;
		info("WorkflowServiceImpl.setRegistryService:" + paramRegistryService);
	}

	@Reference(multiple = false, dynamic = true, optional = true)
	public void setActivitiService(ActivitiService paramActivitiService) {
		this.m_activitiService = paramActivitiService;
		info("WorkflowServiceImpl.setActivitiService:" + paramActivitiService);
	}
	@Reference(multiple = false, dynamic = true, optional = true)
	public void setTransactionService(TransactionService paramActivitiService) {
		this.m_transactionService = paramActivitiService;
		info("WorkflowServiceImpl.setTransactionService:" + paramActivitiService);
		m_processEngineConfiguration.getBeans().put("transactionService", paramActivitiService);
	}
}
