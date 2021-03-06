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
package org.ms123.common.system.call;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.Writer;
import java.io.OutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import org.apache.camel.Route;
import org.apache.camel.CamelContext;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.git.GitService;
import org.osgi.framework.BundleContext;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.rpc.JsonRpcServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.io.IOUtils.copy;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_PROCESS_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_ACTIVITY_KEY;
import static org.ms123.common.system.history.HistoryService.CAMEL_ROUTE_DEFINITION_KEY;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/** CallService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=call" })
public class CallServiceImpl extends BaseCallServiceImpl implements org.ms123.common.rpc.CallService {

	public CallServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
	}

	public void update(Map<String, Object> props) {
	}

	protected void deactivate() throws Exception {
	}

	public Object callCamel(String methodName, Object _methodParams) {
			return callCamel(methodName,_methodParams,null,null);
	}

	public Object callCamel(String methodName, Object _methodParams, HttpServletRequest request, HttpServletResponse response) {
		Map methodParams = (Map) _methodParams;
		int dot = countMatches( methodName, ".");
		String namespace = null;
		String serviceName = null;
		String fqMethodName = methodName;
		if( dot == 1){
			String a[] = methodName.split("\\.");
			namespace = a[0];
			methodName = a[1];
		}
		if( dot == 2){
			String a[] = methodName.split("\\.");
			namespace = a[0];
			serviceName = a[1];
			methodName = a[2];
		}
		if( namespace == null){
			namespace = getNamespace(methodParams);
			if (namespace == null) {
				throw new RpcException(JsonRpcServlet.ERROR_FROM_SERVER, JsonRpcServlet.PARAMETER_MISMATCH, "Method("+methodName+"):Namespace not found");
			}
			fqMethodName = namespace+"."+methodName;
		}
		info(this,"procedure:"+namespace+"."+methodName);
		info(this,"methodParams:"+methodParams);
		Map shape  = this.getProcedureShape(namespace,serviceName,methodName );
		if( shape == null){
			info(this,"getProcedureShape is null:"+fqMethodName);
			shape = getRootShape(namespace, methodName);
		}
		if (shape == null) {
			throw new RpcException(JsonRpcServlet.ERROR_FROM_SERVER, JsonRpcServlet.METHOD_NOT_FOUND, "Method \"" + fqMethodName + "\" not found");
		}
		if(!isRPC(shape)){
			info(this,"Shape.isRPC:"+shape);
			throw new RpcException(JsonRpcServlet.ERROR_FROM_SERVER, JsonRpcServlet.METHOD_NOT_FOUND, "RPC in \"" + fqMethodName + "\" not enabled");
		}
		List<String> permittedRoleList = getStringList(shape, "startableGroups");
		List<String> permittedUserList = getStringList(shape, "startableUsers");
		String userName = getUserName();
		List<String> userRoleList = getUserRoles(userName);
		debug(this,"userName:" + userName);
		info(this,"userRoleList:" + userRoleList);
		info(this,"permittedRoleList:" + permittedRoleList);
		info(this,"permittedUserList:" + permittedUserList);
		if (!isPermitted(userName, userRoleList, permittedUserList, permittedRoleList)) {
			throw new RpcException(JsonRpcServlet.ERROR_FROM_METHOD, JsonRpcServlet.PERMISSION_DENIED, "Method("+fqMethodName+"):User(" + userName + ") has no permission");
		}

		Map<String, Object> properties = new TreeMap();
		Map<String, Object> headers = new HashMap();
		Map<String, Object> bodyMap = new HashMap();
		Object bodyObj=null;
		List<Map> paramList = getItemList(shape, "rpcParameter");
		int bodyCount = countBodyParams(paramList);
		for (Map param : paramList) {
			String destination = (String) param.get("destination");
			String name = (String) param.get("name");
			String destname = (String) param.get("destname");
			if( isEmpty(destname)){
				destname = name;
			}	
			Object def = param.get("defaultvalue");
			def = deserializeDefaultvalue( def, param.get("type"));
			Class type = m_types.get((String) param.get("type"));
			Boolean opt = (Boolean) param.get("optional");
			if ("property".equals(destination)) {
				properties.put(destname, getValue(name, methodParams.get(name), def, opt, type,fqMethodName));
			} else if ("header".equals(destination)) {
				headers.put(destname, getValue(name, methodParams.get(name), def, opt, type,fqMethodName));
			} else if ("body".equals(destination)) {
				bodyObj = getValue(name, methodParams.get(name), def, opt, type,fqMethodName);
				bodyMap.put(destname, bodyObj);
			}
		}

		Map acp =(Map) methodParams.get(ACTIVITI_CAMEL_PROPERTIES);
		if( acp != null){
				properties.putAll(acp);
		}
		if( bodyCount != 1){
			if( bodyMap.keySet().size()>0){
				bodyObj = bodyMap;
			}else{
				bodyObj = null;
			}
		}
		properties.put("__logExceptionsOnly", getBoolean(shape, "logExceptionsOnly", false));
		properties.put("_namespace",namespace);
		properties.put("_method",methodName);
		debug(this,"methodParams:" + methodParams);
		debug(this,"paramList:" + paramList);
		debug(this,"properties:" + properties);
		debug(this,"headers:" + headers);
	
		debugCall( namespace, methodName, methodParams);

		String returnSpec = getString(shape, "rpcReturn", "body");
		List<String> returnHeaderList = new ArrayList();
		List<Map> rh = getItemList(shape, "rpcReturnHeaders");
		if( rh!=null){
			for( Map<String,String> m : rh){
				returnHeaderList.add( m.get("name"));
			}
		}

		String routeId = getId(shape);
		CamelContext cc = m_camelService.getCamelContext(namespace,routeId);
		Route route = cc.getRoute(routeId);
		if( route == null){ //Maybe multiple routes
			route= getRouteWithDirectConsumer(cc, routeId);
			if( route == null){
				throw new RpcException(JsonRpcServlet.ERROR_FROM_METHOD, JsonRpcServlet.INTERNAL_SERVER_ERROR, "CamelRouteService:route for '"+routeId+"' not found");
			}
		}
		properties.put(CAMEL_ROUTE_DEFINITION_KEY, namespace+"/"+routeId );
		debug(this,"Endpoint:" + route.getEndpoint());
		Object answer = null;
		try {
			answer = m_camelService.camelSend(namespace, route.getEndpoint(), bodyObj, headers, properties,returnSpec, returnHeaderList);
			debug(this,"CallServiceImpl.Answer:" + answer);
			if( answer != null){
				debug(this,"CallServiceImpl.Answer.type:" + answer.getClass());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RpcException(JsonRpcServlet.ERROR_FROM_METHOD, JsonRpcServlet.INTERNAL_SERVER_ERROR, "CamelRouteService", e);
		}
		if( "bodyWithMime".equals(returnSpec)){
			if( response == null){
				throw new RpcException(JsonRpcServlet.ERROR_FROM_METHOD, JsonRpcServlet.INTERNAL_SERVER_ERROR, "CamelRouteService:returnSpec is \"bodyWithMime\" and response is null");
			}
			String mime = getString(shape, "mimetype", "text/html");
			response.setContentType(mime);
			boolean bytes=false;
			if( answer instanceof byte[] ){
				bytes=true;
			}
			try {
				if( bytes || mime.startsWith("image/") || mime.endsWith("pdf")){
					OutputStream os = response.getOutputStream();
					if( bytes ){
						copy( new ByteArrayInputStream((byte[])answer) , os );
						os.close();
					}else{
						copy( new FileInputStream((File)answer) , os );
						os.close();
					}
				}else{
					response.setCharacterEncoding( "UTF-8" );
					final Writer responseWriter = response.getWriter();
					response.setStatus(HttpServletResponse.SC_OK);
					if( answer instanceof String ){
						responseWriter.write(String.valueOf(answer));
					}else{
						copy( new FileReader((File)answer), responseWriter); 
					}
					responseWriter.close();
				}
			} catch (Exception e) {
				throw new RpcException(JsonRpcServlet.ERROR_FROM_METHOD, JsonRpcServlet.INTERNAL_SERVER_ERROR, "CamelRouteService:response to method \"" + methodName + "\":",e);
			}
			return null;
		}else{
			return answer;
		}
	}

	public void callHooks(Map props) {
		String serviceName = (String) props.get(SERVICENAME);
		String methodName = (String) props.get(METHODNAME);
		Object methodParams = props.get(METHODPARAMS);
		Object at = props.get(AT);
		Object result = props.get(METHODRESULT);
		String ns = getNamespace(methodParams);
		if (ns == null) {
			return;
		}
		List<Map> hookList = getHooks(ns);
		if (hookList == null)
			return;
		for (Map<String, Object> call : hookList) {
			if (at.equals(call.get(AT)) && ns.equals(call.get(StoreDesc.NAMESPACE)) && serviceName.equals(call.get(SERVICENAME)) && methodName.equals(call.get(METHODNAME))) {
				String preCondition = (String) call.get(PRECONDITION);
				if (preCondition != null) {
					boolean isok = isHookPreConditionOk(preCondition, methodParams);
					info(this,"preCondition:" + preCondition + ":" + isok);
					if (!isok)
						return;
				}
				String action = (String) call.get(ACTION);
				Boolean sync = (Boolean) call.get(SYNC);
				info(this,"CallServiceImpl.camelAction: service:" + serviceName + ",Method:" + methodName + "/params:" + methodParams);
				try {
					camelHook(ns, getUserName(), serviceName, methodName, action, sync, methodParams, result);
				} catch (Exception e) {
					System.err.println("callHooks:" + e);
					throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "CallRemote:", e);
				}
			}
		}
	}

	@Reference(dynamic = true, optional = true)
	public void setCamelService(CamelService paramCamelService) {
		this.m_camelService = paramCamelService;
		System.out.println("CallServiceImpl.setCamelService:" + paramCamelService);
	}

	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		System.out.println("CallServiceImpl.setGitService:" + gitService);
		this.m_gitService = gitService;
	}

	@Reference(dynamic = true, optional = true)
	public void setPermissionService(PermissionService paramPermissionService) {
		this.m_permissionService = paramPermissionService;
		System.out.println("CallServiceImpl.setPermissionService:" + paramPermissionService);
	}
}
