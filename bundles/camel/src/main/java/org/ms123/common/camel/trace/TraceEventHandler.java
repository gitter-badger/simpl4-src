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
package org.ms123.common.camel.trace;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Service;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.RouteNode;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.TracedRouteNodes;
import org.apache.camel.model.ProcessorDefinitionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import flexjson.*;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;
import static org.ms123.common.system.history.HistoryService.HISTORY_HINT;
import static org.ms123.common.system.history.HistoryService.HISTORY_CAMEL_TRACE;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;
import static org.apache.camel.util.StringHelper.xmlEncode;
import javax.xml.namespace.QName;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;

@SuppressWarnings("unchecked")
public class TraceEventHandler implements org.apache.camel.processor.interceptor.TraceEventHandler, Service {

	public TraceEventHandler(boolean persist) {
	}

	@Override
	public void traceExchange(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange) throws Exception {
		logEntry(node, target, traceInterceptor, exchange, "--");
	}

	@Override
	public Object traceExchangeIn(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange) throws Exception {
		logEntry(node, target, traceInterceptor, exchange, "in");
		return null;
	}

	@Override
	public void traceExchangeOut(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange, Object traceState) throws Exception {
		logEntry(node, target, traceInterceptor, exchange, "out");
	}

	public void logEntry(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange, String direction) throws Exception {
		boolean loggingOff = getPropertyBoolean("__loggingOff", exchange);
		boolean logExceptionsOnly = getPropertyBoolean("__logExceptionsOnly", exchange);
		Date timestamp = new Date();
		boolean hasException = extractCausedByException(exchange) != null;
		TraceEventMessage msg = new TraceEventMessage(timestamp, node, exchange);
		String previousNode = msg.getPreviousNode();
		Map<String, Object> props = new HashMap<String, Object>();
		IntrospectionSupport.getProperties(msg, props, null);
		props.put("body", checkLength((String)props.get("body"), 9128));
		props.put("outBody", checkLength((String)props.get("outBody"), 9128));
		props.put("toNode", null);
		String contextName = exchange.getContext().getName();
		props.put("contextName", contextName);
		props.put("direction", direction);
		String routeId = getRouteId(exchange);
		props.put("routeId", routeId);
		props.put("node", node.toString());
		props.put("resourceId", node.getId());
		JSONSerializer js = new JSONSerializer();
		js.prettyPrint(true);
		debug(this, "logEntry:" + routeId + "/" + node.toString()+"/"+loggingOff+"/"+logExceptionsOnly);
		String breadcrumbId = (String) exchange.getIn().getHeaders().get(Exchange.BREADCRUMB_ID);
		if( (loggingOff == false && logExceptionsOnly==false)|| hasException){
			createLogEntry(exchange, contextName + "/" + routeId +"|"+  breadcrumbId, props, hasException, js);
		}
	}

	private void createLogEntry(Exchange exchange, String key, Map msg, boolean hasException, JSONSerializer js) {
		EventAdmin eventAdmin = (EventAdmin) exchange.getContext().getRegistry().lookupByName(EventAdmin.class.getName());
		Map props = new HashMap();
		props.put(HISTORY_KEY, key);
		props.put(HISTORY_HINT, hasException ? "error" : "ok");
		props.put(HISTORY_TYPE, HISTORY_CAMEL_TRACE);
		props.put(HISTORY_MSG, js.exclude("tracedExchange").deepSerialize(msg));
		eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}

	private boolean getPropertyBoolean(String name, Exchange exchange){
		Object ret = exchange.getProperty(name);
		if( ret instanceof String){
			return new Boolean((String)ret).booleanValue();
		}
			
		Boolean b = (Boolean)exchange.getProperty(name);
		return b != null ? b.booleanValue() : false;
	}
	private String getRouteId(Exchange exchange) {
		String routeId = null;
		try {
			routeId = exchange.getUnitOfWork().getRouteContext().getRoute().getId();
		} catch (Exception e) {
			routeId = exchange.getFromRouteId();
		}
		return routeId;
	}

	private String checkLength(String s,int len){
		if( s==null) return s;
		if( s.length()>len){
			return s.substring(0,len) + " ...";
		}
		return s;
	}
	private Throwable extractCausedByException(Exchange exchange) {
		Throwable cause = exchange.getException();
		if (cause == null) {
			cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
		}
		return cause;
	}

	private String extractRoute(ProcessorDefinition<?> node) {
		RouteDefinition route = ProcessorDefinitionHelper.getRoute(node);
		if (route != null) {
			return route.getId();
		} else {
			return null;
		}
	}

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}
}
