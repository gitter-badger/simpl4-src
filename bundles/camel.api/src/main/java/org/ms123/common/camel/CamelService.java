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
package org.ms123.common.camel.api;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Endpoint;
import org.apache.camel.Route;
import org.ms123.common.rpc.RpcException;
import java.util.*;

public interface CamelService {
	public final String CAMEL_SERVICE = "camelService";
	public final String PROPERTIES = "properties";
	public final String PROCEDURENAME = "urivalue_name";
	public final String RPC = "rpc";
	public final String OVERRIDEID = "overrideid";
	public final String RESOURCEID = "resourceId";
	public final String CAMEL_TYPE = "sw.camel";
	public CamelContext getCamelContext(String namespace,String routeId);
	public Map getRootShapeByBaseRouteId(String namespace, String baseRouteId);
	public List<Map<String,Object>> getProcedureShapesForPrefix(String prefix);
	public Map getProcedureShape(String namespace, String serviceName, String procedureName);
	public void saveHistory(Exchange exchange);
	public Object camelSend(String ns, Endpoint endpoint, final Object body, final Map<String, Object> headers, final Map<String, Object> properties);
	public Object camelSend(String ns, Endpoint endpoint, final Object body, final Map<String, Object> headers, final Map<String, Object> properties, String returnSpec,List<String> returnHeaderList);
	public Object camelSend(String ns, String routeName,Map<String, Object> properties);
	public Object camelSend(String ns, String routeName,Object body, Map<String, Object> headers, Map<String, Object> properties);
	public void  createRoutesFromJson( String namespace ) throws RpcException;
	public String evaluate(String expr, Exchange exchange);
	public void newGroovyClassLoader();
	public void newGroovyClassLoader(String namespace);
}
