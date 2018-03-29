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
package org.ms123.common.camel;

import java.util.EventObject;
import java.util.Hashtable;
import java.util.Collection;
import org.apache.camel.builder.DefaultErrorHandlerBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.core.osgi.OsgiDefaultCamelContext;
import org.apache.camel.core.osgi.OsgiServiceRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.impl.CompositeRegistry;
import org.apache.camel.Component;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeCreatedEvent;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.management.event.ExchangeSendingEvent;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.LoggingLevel;
import org.apache.camel.spi.Registry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.support.EventNotifierSupport;
import org.ms123.common.camel.components.*;
import org.ms123.common.camel.components.activiti.*;
import org.ms123.common.camel.components.localdata.*;
import org.ms123.common.camel.components.template.*;
import org.ms123.common.camel.components.asciidoctor.*;
import org.ms123.common.camel.components.docbook.*;
import org.ms123.common.camel.components.xdocreport.*;
import org.ms123.common.camel.components.wawidoc.*;
import org.ms123.common.camel.components.repo.*;
import org.ms123.common.camel.components.direct.*;
import org.ms123.common.camel.components.hazelcast.*;
import org.ms123.common.camel.components.zookeeper.*;
import org.ms123.common.camel.components.deepzoom.*;
import org.ms123.common.camel.components.scpevent.*;
import org.ms123.common.camel.components.consumer.*;
import org.ms123.common.camel.trace.*;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.datamapper.DatamapperService;
import org.ms123.common.libhelper.ClassLoaderWrapper;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.system.thread.ThreadContext;
import org.ms123.common.system.tm.TransactionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.Event;
import org.springframework.transaction.PlatformTransactionManager;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;
import static org.ms123.common.system.history.HistoryService.ACTIVITI_CAMEL_CORRELATION_TYPE;
import static org.ms123.common.system.history.HistoryService.ACC_ACTIVITI_ID;
import static org.ms123.common.system.history.HistoryService.ACC_ROUTE_INSTANCE_ID;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_ACTIVITY_KEY;
import static org.ms123.common.system.history.HistoryService.CAMEL_ROUTE_DEFINITION_KEY;
import org.osgi.service.jndi.JNDIContextManager;
import javax.naming.Context;

/**
 *
 */
@SuppressWarnings({"unchecked","deprecation"})
public class CamelContextBuilder {

	private static final Logger m_logger = LoggerFactory.getLogger(CamelContextBuilder.class);

	public static synchronized ModelCamelContext createCamelContext(String namespace, Registry groovyRegistry, BundleContext bc, boolean trace) throws Exception {
		SimpleRegistry sr = new SimpleRegistry();
		OsgiServiceRegistry or = new OsgiServiceRegistry(bc);
		PermissionService permissionService = (PermissionService) or.lookupByName(PermissionService.class.getName());
		ProcessService processService = (ProcessService) or.lookupByName(ProcessService.class.getName());
		info("createCamelContext.processService:"+processService);
		Component processComponent = processService.getProcessComponent();
		info("createCamelContext.processComponent:"+processComponent);

		JNDIContextManager jndiContextManager = (JNDIContextManager) or.lookupByName(JNDIContextManager.class.getName());
		info("createCamelContext.JNDIContextManager:"+jndiContextManager);
		Hashtable env=new Hashtable();
		Context jndiContext=jndiContextManager.newInitialContext(env);
		info("CamelContextBuilder.jndiContext:"+jndiContext);	

		sr.put(PermissionService.PERMISSION_SERVICE, permissionService);
		sr.put(ProcessService.PROCESS_SERVICE, processService);
		sr.put(DataLayer.DATA_LAYER, or.lookupByNameAndType("dataLayer", DataLayer.class));
		sr.put("datamapper", or.lookupByName(DatamapperService.class.getName()));
		sr.put("namespace", namespace);
		sr.put("activiti", new ActivitiComponent());
		sr.put("localdata", new LocalDataComponent());
		sr.put("repo", new RepoComponent());
		sr.put("xmpp", new org.ms123.common.xmpp.camel.XmppComponent());
		sr.put("wamp", new org.ms123.common.wamp.camel.WampClientComponent());
		sr.put("eventbus", new org.ms123.common.camel.components.eventbus.EventBusComponent());
		sr.put("websocket", new org.ms123.common.camel.components.websocket.WebsocketComponent());
		sr.put("vfs", new org.ms123.common.camel.components.vfs.VfsComponent());
		sr.put("process", processComponent);
		sr.put("direct", new DirectComponent());
		sr.put("xdocreport", new XDocReportComponent());
		sr.put("wawidoc", new WawiDocComponent());
		sr.put("template", new TemplateComponent());
		sr.put("asciidoctor", new AsciidoctorComponent());
		sr.put("docbook", new DocbookComponent());
		sr.put("hazelcast", new HazelcastComponent());
		sr.put("zookeeper", new ZooKeeperComponent());
		sr.put("deepzoom", new DeepZoomComponent());
		sr.put("scpevent", new ScpEventComponent());
		sr.put("scriptconsumer", new ScriptConsumerComponent());
		sr.put("jndiContext", jndiContext);

		Collection<ServiceReference<DataLayer>> srList	=	bc.getServiceReferences(DataLayer.class, "(kind=orientdb)");
		info("dataLayerOrientdb.srList:"+srList);
		DataLayer orient = null;
		if (srList != null && srList.size()>=1) {
			orient = bc.getService(srList.iterator().next());
		}
		info("dataLayerOrientdb:"+orient);
		if (orient == null) {
			throw new RuntimeException("CamelContextBuilder.Cannot resolve service:org.ms123.common.camel.api.DataLayer(Orientdb)");
		}
		sr.put("dataLayerOrientdb", orient);

		TransactionService ts = (TransactionService) or.lookupByName(TransactionService.class.getName());
		sr.put(org.springframework.transaction.PlatformTransactionManager.class.getName(), ts.getPlatformTransactionManager());
		createTransactionPolicies(ts.getPlatformTransactionManager(), sr);
		ModelCamelContext camelContext;
		camelContext = new OsgiDefaultCamelContext(bc);
		Registry r = camelContext.getRegistry();
		CompositeRegistry cr = new CompositeRegistry();
		cr.addRegistry(or);
		cr.addRegistry(sr);
		cr.addRegistry(r);
		if (groovyRegistry != null) {
			cr.addRegistry(groovyRegistry);
		}
		camelContext = new OsgiDefaultCamelContext(bc, cr);
		camelContext.setApplicationContextClassLoader(new ClassLoaderWrapper(CamelContextBuilder.class.getClassLoader(), groovy.lang.Script.class.getClassLoader()));
		DefaultErrorHandlerBuilder dehb = new DefaultErrorHandlerBuilder();
		dehb.logExhaustedMessageHistory(false);
		dehb.disableRedelivery();
		camelContext.setErrorHandlerBuilder(dehb);
		Tracer tracer = new Tracer();
		tracer.setTraceOutExchanges(true);
		TraceFormatter formatter = new TraceFormatter();
		formatter.setShowBreadCrumb(false);
		formatter.setShowProperties(false);
		formatter.setShowOutBody(false);
		formatter.setShowBody(false);
		formatter.setShowOutHeaders(false);
		formatter.setShowHeaders(false);
		formatter.setShowBodyType(false);
		formatter.setShowExchangePattern(false);
		formatter.setShowNode(true);
		tracer.setFormatter(formatter);
		tracer.setTraceExceptions(true);
		tracer.setLogLevel(LoggingLevel.DEBUG);
		tracer.setTraceHandler(new TraceEventHandler(true));
		if (trace) {
			camelContext.addInterceptStrategy(tracer);
			camelContext.setTracing(true);
		}
		camelContext.getProperties().put(Exchange.LOG_DEBUG_BODY_STREAMS, "true");
		camelContext.setMessageHistory(true);
		camelContext.getManagementStrategy().addEventNotifier(new ExchangeEventNotifer(namespace, permissionService));
		camelContext.getShutdownStrategy().setSuppressLoggingOnTimeout(true);
		camelContext.getShutdownStrategy().setTimeout(10);
		return camelContext;
	}

	private static void createTransactionPolicies(PlatformTransactionManager ptm, SimpleRegistry sr) {
		String[] names = { "PROPAGATION_MANDATORY", "PROPAGATION_NESTED", "PROPAGATION_NEVER", "PROPAGATION_NOT_SUPPORTED", "PROPAGATION_REQUIRED", "PROPAGATION_REQUIRES_NEW", "PROPAGATION_SUPPORTS" };
		for (String name : names) {
			SpringTransactionPolicy stp = new SpringTransactionPolicy(ptm);
			stp.setPropagationBehaviorName(name);
			sr.put(name, stp);
		}
	}


	public static class ExchangeEventNotifer extends EventNotifierSupport {
		PermissionService m_permissionService;
		String m_namespace;

		public ExchangeEventNotifer(String ns, PermissionService ps) {
			m_namespace = ns;
			m_permissionService = ps;
		}

		public void notify(EventObject event) throws Exception {
			if (event instanceof ExchangeCreatedEvent) {
				ExchangeCreatedEvent ev = (ExchangeCreatedEvent) event;
				if( true || ev.getExchange().getProperty(Exchange.CORRELATION_ID )==null){
					EventAdmin eventAdmin = (EventAdmin)ev.getExchange().getContext().getRegistry().lookupByName(EventAdmin.class.getName());

					String fr = (String)ev.getExchange().getFromRouteId();
					String aci = (String)ev.getExchange().getProperty( HISTORY_ACTIVITI_ACTIVITY_KEY );
					if( aci != null){
						String bc = (String)ev.getExchange().getIn().getHeader( Exchange.BREADCRUMB_ID  );
						String routeDef = (String)ev.getExchange().getProperty(CAMEL_ROUTE_DEFINITION_KEY );
						if( fr != null){
							int  slash = routeDef.indexOf("/");
							routeDef = routeDef.substring(0,slash+1) + fr;
							ev.getExchange().setProperty(CAMEL_ROUTE_DEFINITION_KEY, routeDef );
						}
						Map props = new HashMap();
						props.put(HISTORY_TYPE, ACTIVITI_CAMEL_CORRELATION_TYPE);
						props.put(ACC_ACTIVITI_ID, aci);
						props.put(ACC_ROUTE_INSTANCE_ID, routeDef + "|" + bc );
						eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
					}
					ThreadContext tc = ThreadContext.getThreadContext();
					debug("------>EventNotifierSupportStart:" + ev +"/"+tc);
					if( tc == null){
						m_permissionService.loginInternal(m_namespace);
						ThreadContext.getThreadContext().put(ev.getExchange().getExchangeId(),true);
					}
				}
			}
			if (event instanceof ExchangeCompletedEvent) {
				ExchangeCompletedEvent ev = (ExchangeCompletedEvent) event;
				if( ev.getExchange().getProperty(Exchange.CORRELATION_ID )==null){
					debug("<-----EventNotifierSupportComplete:" + ev );
					CamelService camelService = (CamelService)ev.getExchange().getContext().getRegistry().lookupByName(CamelService.class.getName());
					camelService.saveHistory(ev.getExchange());
					if(ThreadContext.getThreadContext().get(ev.getExchange().getExchangeId()) != null){
						ThreadContext.getThreadContext().finalize(null);
					}
				}
			}
		}

		public boolean isEnabled(EventObject event) {
			if (event instanceof ExchangeCreatedEvent) return true;
			if (event instanceof ExchangeCompletedEvent) return true;
			return false;
		}

		protected void doStart() throws Exception {
		}

		protected void doStop() throws Exception {
		}
	}
	private static void debug(String msg) {
		//System.out.println(msg);
		m_logger.debug(msg);
	}

	private static void info(String msg) {
		m_logger.info(msg);
	}
}
