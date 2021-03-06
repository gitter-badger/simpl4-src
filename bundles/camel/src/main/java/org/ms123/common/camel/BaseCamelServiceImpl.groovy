/*
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
package org.ms123.common.camel;

import flexjson.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.Collections;
import java.security.MessageDigest;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.utils.UtilsService;
import org.ms123.common.git.GitService;
import org.ms123.common.git.FileHolderApi;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.namespace.NamespaceService;
import org.ms123.common.datamapper.DatamapperService;
import org.ms123.common.utils.Inflector;
import static org.ms123.common.libhelper.Utils.formatGroovyException;
import org.ms123.common.system.compile.java.JavaCompiler;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.jndi.JNDIContextManager;

import javax.jdo.PersistenceManager;
import javax.jdo.Extent;
import javax.jdo.Query;
import javax.transaction.UserTransaction;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.impl.CompositeRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Producer;
import org.apache.camel.Endpoint;
import org.apache.camel.Route;
import org.apache.camel.Exchange;
import org.apache.camel.Consumer;
import org.apache.camel.spi.Registry;
import org.apache.camel.spi.CamelContextNameStrategy;
import org.apache.camel.core.osgi.OsgiDefaultCamelContext;
import org.apache.camel.core.osgi.OsgiServiceRegistry;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.MessageHistory;
import org.apache.camel.FailedToCreateRouteException;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.ModelHelper;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.tools.FileSystemCompiler;
import org.codehaus.groovy.control.CompilerConfiguration;
import java.io.File;
import  org.ms123.common.camel.components.*;
import  org.ms123.common.camel.api.GroovyExpression;
import  org.ms123.common.camel.trace.*;
import  org.ms123.common.camel.view.VisGenerator;
import org.ms123.common.camel.jsonconverter.CamelRouteJsonConverter;
import static org.ms123.common.permission.api.PermissionService.PERMISSION_SERVICE;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.io.FilenameUtils;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;
import static org.ms123.common.system.history.HistoryService.HISTORY_HINT;
import static org.ms123.common.system.history.HistoryService.HISTORY_TIME;
import static org.ms123.common.system.history.HistoryService.HISTORY_CAMEL_HISTORY;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_PROCESS_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_ACTIVITY_KEY;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_NAME;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.CompiledScript;
import javax.script.Compilable;

/**
 *
 */
@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
abstract class BaseCamelServiceImpl implements Constants,org.ms123.common.camel.api.CamelService,EventHandler {
	protected Inflector m_inflector = Inflector.getInstance();
	protected  ServiceRegistration m_serviceRegistration;

	protected DataLayer m_dataLayer;

	protected AuthService m_authService;

	protected GitService m_gitService;

	protected PermissionService m_permissionService;
	protected NamespaceService m_namespaceService;

	protected DatamapperService m_datamapperService;

	protected UtilsService m_utilsService;
	protected EventAdmin m_eventAdmin;
	protected JNDIContextManager m_jdniContextManager;


	protected BundleContext m_bundleContext;
	protected JSONDeserializer m_ds = new JSONDeserializer();
	protected JSONSerializer m_js = new JSONSerializer();

	private Map<String, ContextCacheEntry> m_contextCache = new LinkedHashMap();
	private Map<String, List<Route>> m_routeCache = new LinkedHashMap();
	private Map<String, Map<String,Object>> m_procedureCache = new LinkedHashMap();

	final static String[] topics = [
		"namespace/installed",
		"namespace/created",
		"namespace/preCommit",
		"namespace/preUpdate",
		"namespace/postUpdate",
		"namespace/preGet",
		"namespace/pull",
		"namespace/deleted"
	];

	protected void registerEventHandler(BundleContext bundleContext) {
		try {
			Bundle b = bundleContext.getBundle();
			Dictionary d = new Hashtable();
			d.put(EventConstants.EVENT_TOPIC, topics);
			m_serviceRegistration = b.getBundleContext().registerService(EventHandler.class.getName(), this, d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void handleEvent(Event event) {
		debug(this,"BaseCamelServiceImpl.Event: " + event);
		try{
			if( "namespace/installed".equals(event.getTopic())){
				String namespace= (String)event.getProperty("namespace")
				debug(this,"BaseCamelServiceImpl.handleEvent:"+namespace);
				_createRoutesFromJson( namespace);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
	}

	public String evaluate(String expr, Exchange exchange){
		return GroovyExpression.evaluate( expr, exchange);
	}

	public CamelContext getCamelContext(String namespace, String routeName) {
		info(this,"getCamelContext: " + namespace+"|"+routeName);
		try{
			List<ContextCacheEntry> cceList = getContextCacheEntryList(namespace);
			for( ContextCacheEntry cce : cceList){	
				if( routeName.startsWith("websocket")){
					if( getEndpoint(routeName, cce.context) != null){
						info(this,"getCamelContext found: " + cce);
						return cce.context;
					}
				}else{
					Route route = cce.context.getRoute( routeName);
					if( route != null){
						info(this,"getCamelContext found: " + cce);
						return cce.context;
					}
				}
			}
			throw new RuntimeException("");
		}catch(Exception e){
			throw new RuntimeException("BaseCamelServiceImpl.getCamelContext("+namespace+"): not found");
		}
	}

	private Endpoint getEndpoint(String uri, CamelContext cc) {
		Map<String, Endpoint> endpoints = cc.getEndpointMap();
		for (Map.Entry<String, Endpoint> entry : endpoints.entrySet()) {
			String key = entry.getKey();
			if (key.equals(uri) || key.startsWith(uri + ":") || key.startsWith(uri + "?")) {
				return entry.getValue();
			}
		}
		//@@@MS Sometimes (sharedEndpoints?) is the Endpoint not in the map;
		List<Route> routes = cc.getRoutes();
		for (Route r : routes) {
			Consumer c = r.getConsumer();
			String key = c.getEndpoint().getEndpointUri();
			if (key.equals(uri) || key.startsWith(uri + ":") || key.startsWith(uri + "?")) {
				return c.getEndpoint();
			}
		}
		return null;
	}
	public Map<String,Object> getProcedureShape(String namespace, String serviceName, String procedureName) {
		info(this,"getProcedureShape:"+serviceName+"|"+procedureName);
		Iterator<Map.Entry<String,Map>> iter = m_procedureCache.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,Map> entry = iter.next();
			String key = entry.getKey();
			if( serviceName != null){
				if(key.equals(namespace+"/"+serviceName+"/"+procedureName) ){
					info(this,"\tHit:"+key);
					return entry.value;
				}
			}else{
				if(key.startsWith(namespace+"/") && key.endsWith("/"+procedureName)){
					return entry.value;
				}
			}
		}
	}
	public List<Map<String,Object>> _getProcedureShapesForPrefix(String prefix) {
		List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();
		Iterator<Map.Entry<String,Map>> iter = m_procedureCache.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,Map> entry = iter.next();
			String key = entry.getKey();
			if(entry.getKey().startsWith(prefix)){
				ret.add(entry.value);
			}
		}
		return ret;
	}
	private void addProcedureShape(String namespace, String baseRouteId,Map shape) {
		if( shape == null) return;
		String procedureName = getProcedureName(shape);
		m_procedureCache.put(namespace+"/"+baseRouteId + "/" +  procedureName, shape);
	}
	private void removeProcedureShape(String prefix){
		Iterator<Map.Entry<String,Map>> iter = m_procedureCache.entrySet().iterator();
		while (iter.hasNext()) {
				Map.Entry<String,Map> entry = iter.next();
				if(entry.getKey().startsWith(prefix)){
						iter.remove();
				}
		}
	}

	private ContextCacheEntry getContextCacheEntry(String namespace, String routeId){
		for( String key : m_contextCache.keySet()){
			if( key.startsWith(namespace+"/")){
				ContextCacheEntry cce = m_contextCache.get(key);
				if( cce.routeEntryMap[routeId] != null){
					return cce;
				}
			}
		}
		return null;
	}
	private List<ContextCacheEntry> getContextCacheEntryList(String namespace){
		List<ContextCacheEntry> list = new ArrayList<ContextCacheEntry>();
		for( String key : m_contextCache.keySet()){
			if( key.startsWith(namespace+"/")){
				ContextCacheEntry cce = m_contextCache.get(key);
				list.add( cce);
			}
		}
		return list;
	}

	public Map getRootShapeByBaseRouteId(String namespace,String routeId){
		//ContextCacheEntry cce  = m_contextCache.get(getContextKey(namespace));
		ContextCacheEntry cce  = getContextCacheEntry(namespace, routeId);
		if( cce == null){
			return null;
		}
		RouteCacheEntry re = cce.routeEntryMap[routeId];
		if( re == null) return null;
		return re.rootShape;
	}

	protected List<Map> _getRouteInfoList(String contextKey){
		List<Map> resultList = new ArrayList();
		ContextCacheEntry cce  = m_contextCache.get(contextKey);
		if( cce == null){
			//throw new RuntimeException("_getRouteDefinitions:context:"+contextKey+" not found");
			return resultList;
		}
		CamelContext cc = cce.context;
		debug(this,"Def:"+cc.getRouteDefinitions());
		List<RouteDefinition> rdList =  cc.getRouteDefinitions();
		for( RouteDefinition rd : rdList){
			Map routeMap = new HashMap();
			routeMap.put("id", rd.getId());
			routeMap.put("route", rd.toString());
			resultList.add(routeMap);
		}
		return resultList;
	}
	protected List<String> _getContextNames(String namespace){
		List<String> retList = new ArrayList();
		for( String key : m_contextCache.keySet()){
			if( namespace != null && key.startsWith(namespace+"/")){
				retList.add(key);
			}
		}
		info(this, "_getContextNames:"+retList);
		return retList;
	}
	protected Map<String,List> _getRouteVisGraph(String contextKey, String routeId){
		ContextCacheEntry cce  = m_contextCache.get(contextKey);
		if( cce == null){
			throw new RuntimeException("_getVisGraph:context:"+contextKey+" not found");
		}
		CamelContext cc = cce.context;
		List<RouteDefinition> routeDefinitions = new ArrayList();
		RouteDefinition routeDefinition =  cc.getRouteDefinition(routeId);
		if( routeDefinition != null ){
			routeDefinitions.add( routeDefinition);
		}else{
			int i=1;
			while(true){
				routeDefinition =  cc.getRouteDefinition(Utils.createRouteId(routeId,i));
				if( routeDefinition==null){
					break;
				}
				routeDefinitions.add(routeDefinition);
			}
		}

		VisGenerator vg = new VisGenerator();
		return vg.getGraph(routeDefinitions);
	}

	protected synchronized void _createRoutesFromJson(){
		List<Map> repos = m_gitService.getRepositories(new ArrayList(),false);
		for(Map<String,String> repo : repos){
			String namespace = repo.get("name");
			_createRoutesFromJson(namespace);
		}
	}


	private CamelRouteJsonConverter createRoutesDefinitionFromJson(RouteCacheEntry re, String path, ModelCamelContext context, Map rootShape) {
		try{
			return new CamelRouteJsonConverter(path, context, rootShape,m_namespaceService.getBranding(),null,m_bundleContext);
		}catch(Exception e){
			re.lastError=e.getMessage();
			throw e;
		}
	}
	protected synchronized void _createRoutesFromJson(String namespace){
		_createRoutesFromJson(namespace,null);
	}
	protected synchronized void _createRoutesFromJson(String namespace,String path){
		Map<String, List> routesJsonMap = getRoutesJsonMap(namespace);
		if( routesJsonMap.size() == 0){
			List<ContextCacheEntry> cceList = getContextCacheEntryList(namespace);
			for( ContextCacheEntry cce : cceList){	
				stopNotActiveRoutes( namespace, cce, []);
			}
			removeProcedureShape(namespace+"/");
		}
		for( String  contextKey : routesJsonMap.keySet()){
			List<Map> list = routesJsonMap.get(contextKey);
			ContextCacheEntry cce = m_contextCache.get(contextKey);
			if( cce == null){
				if(list.size() == 0) {
					//No enabled route
					stopNotActiveRoutes(namespace, contextKey,[]);
					continue;
				}
				cce = new ContextCacheEntry();
				cce.groovyRegistry = new GroovyRegistry( BaseCamelServiceImpl.class.getClassLoader(), m_bundleContext, namespace);
				cce.context = CamelContextBuilder.createCamelContext(namespace,cce.groovyRegistry, m_bundleContext,true);
				cce.context.setNameStrategy( new FixedCamelContextNameStrategy(contextKey));
				cce.context.start();
				m_contextCache.put(contextKey, cce);
			}
			List<String> okList = [];
			for( Map map : list){
				Map rootShape = (Map)map.rootShape;
				String md5 = (String)map.md5;
				String _path = (String)map.path;
				String routeBaseId = Utils.getId(rootShape);
				if( path != null && _path != path ){
					okList.add( routeBaseId);
					continue;
				}
				boolean enabled = getBoolean(rootShape, ENABLED, true);
				if( enabled == false){
					stopNotActiveRoutes( namespace, contextKey, []);
					continue;
				}
				debug(this,"routeBaseId:"+routeBaseId);
				boolean autoStart = getBoolean(rootShape, AUTOSTART, false);
				RouteCacheEntry re = cce.routeEntryMap[routeBaseId];
				if( re == null){
					//new Route
					re = new RouteCacheEntry( rootShape:rootShape,md5:md5,routeId:routeBaseId);
					debug(this,"Add route:"+routeBaseId);
					def c  = createRoutesDefinitionFromJson( re, _path, cce.context, rootShape);
					RoutesDefinition routesDef = c.getRoutesDefinition();
					Map<String,Map> procedureShapes = c.getProcedureShapes();					
					debug(this,"createRoutesDefinitionFromJson.routesDef:"+ModelHelper.dumpModelAsXml(cce.context, routesDef));

					int i=1;
					int size = routesDef.getRoutes().size();
					routesDef.getRoutes().each(){RouteDefinition routeDef->
						String routeId = Utils.createRouteId(routeBaseId,i);
						routeDef.routeId(routeId);
						routeDef.setGroup(namespace);
						routeDef.autoStartup( autoStart);
						addRouteDefinition(cce.context, routeDef,re, routeBaseId);
						addProcedureShape( namespace, routeBaseId, procedureShapes[routeId]);
						if( autoStart){
							cce.context.startRoute(routeId);
						}
						i++;
					}
					cce.routeEntryMap[routeBaseId] = re;
					okList.add( routeBaseId);
				}else{
					debug(this,"lastError:"+re.lastError+"/"+re.md5+"/"+md5+"/"+(re.md5==md5));
					if( re.lastError == null  && re.md5 == md5 ){
						//Nothing changed.
						okList.add( routeBaseId);
						continue;
					}else{
						//exchange route
						debug(this,"Exchange route:"+routeBaseId+"/"+autoStart);
						re.md5 = md5;
						re.rootShape = rootShape;
						def c  = createRoutesDefinitionFromJson( re, _path, cce.context, rootShape);
						RoutesDefinition routesDef = c.getRoutesDefinition();
						Map<String,Map> procedureShapes = c.getProcedureShapes();					
						debug(this,"createRoutesDefinitionFromJson.routesDef:"+ModelHelper.dumpModelAsXml(cce.context,routesDef));

						removeProcedureShape( namespace+"/"+routeBaseId+"/" );
						stopAndRemoveRoutesForShape(cce.context, routeBaseId);
						int i=1;
						int size = routesDef.getRoutes().size();
						routesDef.getRoutes().each(){RouteDefinition routeDef->
							String routeId = Utils.createRouteId(routeBaseId,i);
							if( i==1 && size > 1)
							routeDef.routeId(routeId);
							routeDef.autoStartup( autoStart);
							addRouteDefinition(cce.context, routeDef,re, routeBaseId);
							addProcedureShape( namespace, routeBaseId, procedureShapes[routeId]);
							if( autoStart){
								cce.context.startRoute(routeId);
							}
							i++;
						}
						okList.add( routeBaseId);
					}
				}
			}
			stopNotActiveRoutes(namespace, contextKey,okList);
		}
	}

	private void stopAndRemoveRoutesForShape( CamelContext cc, String baseRouteId){
		RouteDefinition routeDefinition =  cc.getRouteDefinition(baseRouteId);
		if( routeDefinition != null ){
			info(this,"stopAndRemoveRoute:"+baseRouteId);
			cc.stopRoute(baseRouteId);
			cc.removeRoute(baseRouteId);
		}
		for(int i=1; i < 100; i++){
			String routeId = Utils.createRouteId(baseRouteId,i);	
			routeDefinition =  cc.getRouteDefinition(routeId);
			if( routeDefinition != null){
				info(this,"stopAndRemoveRoute:"+routeId);
				cc.stopRoute(routeId);
				cc.removeRoute(routeId);
			}
		}
	}

	private void stopNotActiveRoutes(String namespace, String contextKey, List okList){
		ContextCacheEntry cce = m_contextCache.get(contextKey);
		stopNotActiveRoutes(namespace,cce, okList);
	}
	private void stopNotActiveRoutes(String namespace, ContextCacheEntry cce, List okList){
		debug(this,"stopNotActiveRoutes:"+cce+"/"+okList);
		if( cce == null) return;
		List<String> ridList = [];
		cce.context.getRouteDefinitions().each(){RouteDefinition rdef->
			ridList.add(rdef.getId());
		}
		for( String rid in ridList){
			if( !containsRid(okList,rid)){
				info(this,"Remove route:"+rid);
				cce.context.stopRoute(rid);
				cce.context.removeRoute(rid);
				def baseRouteId = Utils.getBaseRouteId(rid);
				cce.routeEntryMap.remove(baseRouteId);
				removeProcedureShape(namespace+"/"+baseRouteId+"/");
			}
		}
		debug(this,"-->Context("+cce+"):status:"+cce.context.getStatus());
		cce.context.getRouteDefinitions().each(){RouteDefinition rdef->
			String rid = rdef.getId();
			debug(this,"\tRoute("+rid+"):status:"+cce.context.getRouteStatus(rid));
		}
	}

	private boolean containsRid( List<String> okList, String rid){
		for( String ok : okList){
			if( rid.startsWith( ok ) ){
				return true;
			}
		}
		return false;
	}

	private void addRouteDefinition(CamelContext context, RouteDefinition rd, RouteCacheEntry re, String baseRouteId) throws Exception{
		debug(this,"addRouteDefinition.routeDef:"+ModelHelper.dumpModelAsXml(context,rd));
		try{
			context.addRouteDefinition(rd );
			if( re != null){
				re.lastError = null;
			}
		}catch(Exception e){
			e.printStackTrace();
			context.removeRouteDefinition(rd);
			try{
				stopAndRemoveRoutesForShape(context, baseRouteId);
			}catch(Exception e1){
				info(this,"stopAndRemoveRoutesForShape.error:"+e1.getMessage());
				e1.printStackTrace();
			}
			if( re != null){
				re.lastError = e.getMessage();
			}
			if( e instanceof FailedToCreateRouteException){
				String msg = e.getMessage();
				System.err.println("msg:"+msg);
				int ind;
				if( (ind=msg.indexOf("<<<")) != -1){
					msg = msg.substring(ind+4);
				}
				throw new RuntimeException("<br/>"+msg);
			}
			throw e;
		}
	}



	private Map<String,List> getRoutesJsonMap(String namespace){
		List<String> types = new ArrayList();
		types.add(CAMEL_TYPE);
		types.add(DIRECTORY_TYPE);
		List<String> typesCamel = new ArrayList();
		typesCamel.add(CAMEL_TYPE);

		Map map= m_gitService.getWorkingTree(namespace, null, 100, types, null, null,null);
		List<Map> pathList = new ArrayList();
		toFlatList(map,typesCamel,pathList);

		Map<String,List> routesJsonMap = [:];
		for( Map pathMap : pathList){
			String path = (String)pathMap.get(PATH);
			String  routeString = m_gitService.getContent(namespace, path);
			String md5 = getMD5OfUTF8(routeString);
			Map rootShape=null;
			try{
				rootShape = (Map)m_ds.deserialize(routeString);
			}catch(Exception e){
				e.printStackTrace();
				info(this,"Cannot deserialize:"+path);
				continue;
			}
			String contextKey = getContextKey(namespace, Utils.getBaseName(path));
//			boolean enabled = getBoolean(rootShape, ENABLED, true);
//			if( !enabled) continue;
			if( routesJsonMap[contextKey] == null){
				routesJsonMap[contextKey] = [];
			}
			routesJsonMap[contextKey].add([md5:md5, path:path, rootShape: rootShape]);
		}
		return routesJsonMap;
	}


	public void newGroovyClassLoader(){
		for( String key : m_contextCache.keySet()){
			GroovyRegistry gr = m_contextCache.get(key).groovyRegistry;
			gr.newClassLoader();
			CamelContext co = m_contextCache.get(key).context;
			co.stop();
			co.start();
		}
	}

	public void newGroovyClassLoader(String namespace){
		for( String key : m_contextCache.keySet()){
			if( key.startsWith(namespace+"/")){
				ContextCacheEntry  cce = m_contextCache.get(key);
				CamelContext co = cce.context;
				co.stop();
				List<RouteDefinition> rdList = co.getRouteDefinitions();
				cce = new ContextCacheEntry();
				m_contextCache.put(key, cce);
				cce.groovyRegistry = new GroovyRegistry( BaseCamelServiceImpl.class.getClassLoader(), m_bundleContext, namespace);
				cce.context = CamelContextBuilder.createCamelContext(namespace,cce.groovyRegistry, m_bundleContext,true);
				cce.context.setNameStrategy( new FixedCamelContextNameStrategy(key));
				cce.context.addRouteDefinitions( rdList);
				cce.context.start();
			}
		}
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
	public void saveHistory(Exchange exchange) {
		String activitikey = (String)exchange.getProperty("activitikey");
		if( activitikey == null){
			activitikey = (String)exchange.getProperty(HISTORY_ACTIVITI_PROCESS_KEY);
		}
		if( activitikey == null){
			return;
		}

		List<MessageHistory> list = exchange.getProperty(Exchange.MESSAGE_HISTORY, List.class);
		ExchangeFormatter formatter = new ExchangeFormatter();
		formatter.setShowExchangeId(true);
		formatter.setMultiline(true);
		formatter.setShowHeaders(true);
		formatter.setStyle(ExchangeFormatter.OutputStyle.Fixed);
		String routeStackTrace = MessageHelper.dumpMessageHistoryStacktrace(exchange, formatter, true);
		boolean hasException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class) != null;

		Map props = new HashMap();
		props.put(HISTORY_TYPE, HISTORY_CAMEL_HISTORY);
		String key = activitikey;
		props.put(HISTORY_KEY, key);
		
		Map hint = new HashMap();
		hint.put("status", hasException ? "error": "ok");
		if( exchange.getProperty(WORKFLOW_ACTIVITY_ID) != null){
			hint.put(WORKFLOW_ACTIVITY_ID, (String)exchange.getProperty(WORKFLOW_ACTIVITY_ID));
			hint.put(WORKFLOW_ACTIVITY_NAME, (String)exchange.getProperty(WORKFLOW_ACTIVITY_NAME));
		}

		props.put(HISTORY_HINT, m_js.deepSerialize(hint));
		props.put(HISTORY_MSG, routeStackTrace);
		m_eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}

	public static class SleepBean {
		public void sleep(String body, Exchange exchange) throws Exception {
			debug(this,"SleepBean.start");
			Thread.sleep(500);
			debug(this,"SleepBean.end");
		}
	}

	protected void printRoutes(CamelContext camelContextObj) {
		for (Endpoint e : camelContextObj.getEndpoints()) {
			debug(this,"Endpoint:" + e + "/" + e.getEndpointKey());
		}
	}

	private static String getMD5OfUTF8(String text) {
		try {
			MessageDigest msgDigest = MessageDigest.getInstance("MD5");
			byte[] mdbytes = msgDigest.digest(text.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<mdbytes.length;i++) {
				String hex=Integer.toHexString(0xff & mdbytes[i]);
				if(hex.length()==1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception ex) {
			throw new RuntimeException("BaseCamelServiceImpl.getMD5OfUTF8");
		}
	}

	private String getContextKey(String namespace, String base){
		return namespace+"/"+base;
	}

	protected boolean getBoolean(Map shape, String name,boolean _default) {
		Map properties = (Map) shape.get(PROPERTIES);

		Object value  = properties.get(name);
		if( value == null) return _default;
		return (boolean)value;
	}

	protected String getString(Map shape, String name,String _default) {
		Map properties = (Map) shape.get(PROPERTIES);

		Object value  = properties.get(name);
		if( value == null) return _default;
		return (String)value;
	}


	protected String getProcedureName(Map shape) {
		Map<String,String> properties = (Map) shape.get(PROPERTIES);
		return properties.get(PROCEDURENAME);
	}

	private static class RouteCacheEntry {
		String lastError;
		String routeId;
		String md5;
		Map rootShape;
	}

	private static class ContextCacheEntry {
		GroovyRegistry groovyRegistry;
		String key;
		Map<String,RouteCacheEntry> routeEntryMap = [:];
		ModelCamelContext context;
	}

	private static  class FixedCamelContextNameStrategy implements CamelContextNameStrategy {
		private String name;

		public FixedCamelContextNameStrategy(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getNextName() {
			throw new RuntimeException("FixedCamelContextNameStrategy: not allowed");
		}

		@Override
		public boolean isFixedName() {
			return true;
		}
	}

	private List<String> getUserRoles(String userName){
		List<String> userRoleList = null;
		try {
			userRoleList = m_permissionService.getUserRoles(userName);
		} catch (Exception e) {
			userRoleList = new ArrayList();
		}
		return userRoleList;
	}
	private boolean isPermitted(String userName, List<String> userRoleList, List<String> permittedUserList, List<String> permittedRoleList) {
		if (permittedUserList.contains(userName)) {
			info(this,"userName(" + userName + " is allowed:" + permittedUserList);
			return true;
		}
		for (String userRole : userRoleList) {
			if (permittedRoleList.contains(userRole)) {
				info(this,"userRole(" + userRole + " is allowed:" + permittedRoleList);
				return true;
			}
		}
		return false;
	}
	private List<String> getStringList(Map shape, String name) {
		String s = getString(shape, name, "");
		return Arrays.asList(s.split(","));
	}

	protected void addMailcap(){
		javax.activation.MailcapCommandMap  cm = (javax.activation.MailcapCommandMap)javax.activation.CommandMap.getDefaultCommandMap();
		cm.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		cm.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		cm.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		cm.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		cm.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
		javax.activation.CommandMap.setDefaultCommandMap(cm);
	}

}

