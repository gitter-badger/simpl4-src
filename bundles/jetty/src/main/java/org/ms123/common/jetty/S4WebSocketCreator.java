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
package org.ms123.common.jetty;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import java.lang.reflect.Method;
import org.ms123.common.libhelper.Inflector;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Route;
import org.apache.camel.Consumer;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static com.jcabi.log.Logger.info;

/**
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class S4WebSocketCreator implements WebSocketCreator {

	protected Inflector m_inflector = Inflector.getInstance();
	private Map m_config = null;
	private Map<String, CamelContext> m_camelContextMap = new ConcurrentHashMap();
	private BundleContext m_bundleContext;

	private String getServiceClassName(String serviceName) {
		String serviceClassName = null;
		int dot = serviceName.lastIndexOf(".");
		if (dot != -1) {
			String part1 = serviceName.substring(0, dot);
			String part2 = serviceName.substring(dot + 1);
			info(this, "serviceName:" + serviceName);
			serviceClassName = "org.ms123.common." + part1 + "." + m_inflector.upperCamelCase(part2, '-') + "Service";
		} else {
			String s = m_inflector.upperCamelCase(serviceName, '-');
			serviceClassName = "org.ms123.common." + s.toLowerCase() + "." + s + "Service";
		}
		info(this, "ServiceClassName:" + serviceClassName);
		return serviceClassName;
	}

	private synchronized CamelContext getCamelContext(Map<String, String> parameterMap, String uri) throws Exception {
		String namespace = getParameter("namespace", parameterMap);
		CamelContext cc = m_camelContextMap.get(namespace);
		if (cc != null) {
			return cc;
		}
		Object service = null;
		ServiceReference sr = m_bundleContext.getServiceReference("org.ms123.common.camel.api.CamelService");
		if (sr != null) {
			service = m_bundleContext.getService(sr);
		}
		if (service == null) {
			throw new Exception("WebSocketCreator.Cannot resolve service:org.ms123.common.camel.api.CamelService");
		}
		Class[] cargs = new Class[2];
		cargs[0] = String.class;
		cargs[1] = String.class;
		try {
			Method meth = service.getClass().getMethod("getCamelContext", cargs);
			Object[] args = new Object[2];
			args[0] = namespace;
			args[1] = uri;
			cc = (CamelContext) meth.invoke(service, args);
			m_camelContextMap.put(namespace, cc);
			return cc;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("WebSocketCreator.Cannot create WebSocket:" + e.getMessage());
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

	private Object getCamelWebSocket(Map<String, String> parameterMap) throws Throwable {
		String uri = null;
		String name = getParameter("name", parameterMap);
		if (name.indexOf("://") != -1) {
			uri = name;
		} else {
			uri = "websocket://" + name;
		}
		CamelContext cc = getCamelContext(parameterMap, uri);

		Endpoint ep = getEndpoint(uri, cc);
		if (ep == null) {
			throw new Exception("WebSocketCreator.Endpoint not exists:" + uri);
		}
		info(this, "S4WebSocketCreator(" + uri + ").ep:" + ep);
		Class[] cargs = new Class[1];
		cargs[0] = Map.class;
		try {
			Method meth = ep.getClass().getDeclaredMethod("createWebsocket", cargs);
			Object[] args = new Object[1];
			args[0] = parameterMap;
			Object ws = meth.invoke(ep, args);
			info(this, "S4WebSocketCreator.ws:" + ws);
			return ws;
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw e.getCause();
		}
	}

	private Object getWebSocket(String className, Map<String, String> parameterMap) throws Throwable {
		Object service = null;
		ServiceReference sr = m_bundleContext.getServiceReference(className);
		if (sr != null) {
			service = m_bundleContext.getService(sr);
		}
		if (service == null) {
			throw new Exception("WebSocketCreator.Cannot resolve service:" + className);
		}
		Class[] cargs = new Class[2];
		cargs[0] = Map.class;
		cargs[1] = Map.class;
		try {
			Method meth = service.getClass().getDeclaredMethod("createWebSocket", cargs);
			Object[] args = new Object[2];
			args[0] = m_config;
			args[1] = parameterMap;
			return meth.invoke(service, args);
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw e.getCause();
		}
	}

	private String getParameter(String paramName, Map<String, String> map) throws Exception {
		String param = map.get(paramName);
		if (param == null || param.length() == 0) {
			info(this, "WebSocketCreator.Cannot get \"" + paramName + "\" parameter from querystring");
			throw new Exception("WebSocketCreator.Cannot get \"" + paramName + "\" parameter from querystring");
		}
		return param;
	}

	private Map<String, String> convertMap(Map<String, List<String>> inMap) {
		Map<String, String> outMap = new HashMap();
		for (Map.Entry<String, List<String>> entry : inMap.entrySet()) {
			outMap.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
		}
		return outMap;
	}

	public S4WebSocketCreator(Map config) {
		m_bundleContext = (BundleContext) config.get("bundleContext");
		m_config = config;
	}

	@Override
	public synchronized Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
		try {
			Object socket = null;
			Map<String, String> parameterMap = convertMap(req.getParameterMap());
			String serviceName = parameterMap.get("osgiService");
			if( req.hasSubProtocol("wamp.2.json")){
				socket = getWebSocket("org.ms123.common.wamp.WampService", parameterMap);
			}else  if (isEmpty(serviceName) || "camel".equals(serviceName)) {
				socket = getCamelWebSocket(parameterMap);
			} else {
				socket = getWebSocket(getServiceClassName(serviceName), parameterMap);
			}
			info(this, "createWebSocket:" + socket);
			List<String> subProtos = req.getSubProtocols();
			info(this, "createWebSocket:subProtos:" + subProtos);
			info(this, "createWebSocket:hasSubProtocol:" + req.hasSubProtocol("wamp.2.json"));
			if (req.hasSubProtocol("wamp.2.json")) {
				resp.setAcceptedSubProtocol("wamp.2.json");
			}
			return socket;
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				String message = e.getMessage();
				if (message != null && message.startsWith("6:")) {
					resp.sendError(403, message);
				} else {
					resp.sendError(400, message);
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return null;
	}
}

