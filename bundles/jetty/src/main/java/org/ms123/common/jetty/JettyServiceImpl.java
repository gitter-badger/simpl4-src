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

import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Writer;
import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.service.component.ComponentContext;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.*;
import org.eclipse.jetty.util.*;
import org.eclipse.jetty.server.*;
import javax.servlet.http.*;
import javax.servlet.*;
import org.eclipse.jetty.servlet.ServletHolder;
import java.net.*;
import org.ms123.common.docbook.DocbookService;
import org.ms123.common.git.GitService;
import org.ms123.common.libhelper.ClassLoaderWrapper;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.rpc.JsonRpcServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.ms123.common.libhelper.Inflector;
import java.lang.reflect.*;
import org.ms123.common.utils.IOUtils;
import flexjson.*;
import org.yaml.snakeyaml.*;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;
import java.util.concurrent.ExecutorService;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkEvent;
import java.nio.file.Paths;
import java.nio.file.Files;

/** JettyService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true)
public class JettyServiceImpl implements JettyService, ServiceListener,FrameworkListener {

	private final String NAMESPACE = "namespace";

	protected Inflector m_inflector = Inflector.getInstance();

	private PermissionService m_permissionService;
	private DocbookService m_docbookService;
	private GitService m_gitService;

//	private MiltonService m_miltonService;

	private JsonRpcServlet m_rpcServlet;
	private S4WebSocketServlet m_websocketServlet;

	private BundleContext m_bundleContext;

	private Server m_server;

	private boolean m_notStarted = true;

	private String m_basedir;

	public Map<String,String> FILETYPES = createFiletypeMap();

	private static final String HEADER_IFMODSINCE = "If-Modified-Since";

	private static final String HEADER_LASTMOD = "Last-Modified";

	public JettyServiceImpl() {
	}

	private static Map<String, String> createFiletypeMap() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("json", "application/json");
		result.put("pdf", "image/pdf");
		result.put("json.gz", "application/json");
		result.put("yaml", "text/x-yaml");
		result.put("yml", "text/x-yaml");
		result.put("xml", "text/xml");
		result.put("js", "text/javascript");
		result.put("js.gz", "text/javascript");
		result.put("css", "text/css");
		result.put("css.gz", "text/css");
		result.put("adoc", "text/x-asciidoc");
		result.put("html", "text/html");
		result.put("txt", "text/plain");
		result.put("html.gz", "text/html");
		result.put("gif", "image/gif");
		result.put("png", "image/png");
		result.put("jpg", "image/jpeg");
		result.put("jepg", "image/jpeg");
		result.put("woff", "application/x-font-woff");
		result.put("ttf", "application/x-font-ttf");
		result.put("otf", "application/x-font-otf");
		result.put("otf.gz", "application/x-font-otf");
		result.put("woff.gz", "application/x-font-woff");
		result.put("svg", "image/svg+xml");
		result.put("svgz", "image/svg+xml");
		result.put("odt", "application/vnd.oasis.opendocument.text");
		return Collections.unmodifiableMap(result);
	}

	private ExecutorService getExecutorService() {
		try {
			Context ctx = new InitialContext();
			if (ctx == null){
				throw new Exception("JNDI could not create InitalContext ");
			}
			return  (ExecutorService)ctx.lookup("wm/simpl4WorkManager");
		} catch (Throwable e) {
			info("JettyServiceImpl.getExecutorService:"+ e);
		}
		return null;
	}

	//protected void activate(ComponentContext context) {
	protected void __activate() {
		System.out.println("JettyServiceImpl.activate");
		BundleContext bc = m_bundleContext;
		bc.addServiceListener(this);
		try {
			initJetty();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			ServiceReference[] sr = bc.getServiceReferences((String)null, "(rpc.prefix=*)");
			for (int i = 0; i < sr.length; i++) {
				String rpc_prefix = (String) sr[i].getProperty("rpc.prefix");
				Object o = bc.getService(sr[i]);
				if (o != null) {
					String[] objectClass = (String[]) sr[i].getProperty("objectClass");
					m_rpcServlet.putServiceMapping(rpc_prefix, objectClass[0]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		m_bundleContext = bundleContext;
		m_bundleContext.addFrameworkListener(this);
		__activate();
	}

	public void frameworkEvent(FrameworkEvent event) {
		info("JettyServiceImpl.frameworkEvent:"+event);
		if( event.getType() != FrameworkEvent.STARTED){
			return; 
		}
		ClassLoader save = Thread.currentThread().getContextClassLoader();
		try{
			ClassLoaderWrapper clw = new ClassLoaderWrapper(this.getClass().getClassLoader());
			Thread.currentThread().setContextClassLoader(clw);
			m_server.start();
			info("JettyServer.startet");

			System.err.println("\n\n********************************************************************");
			String localAddress = getLocalHostLANAddress();
			System.err.println("* address:   "+localAddress);
			String port = getInt(System.getProperty("jetty.port"), 8075)+"";
			System.err.println("* port:      "+port);
			if( "80".equals(port)){
				System.err.println("* simpl4Url: "+"http://"+localAddress+"/sw/start.html");
			}else{
				System.err.println("* simpl4Url: "+"http://"+localAddress+":"+port+"/sw/start.html");
			}
			System.err.println("********************************************************************\n\n");

		}catch(Exception e){
			error("JettyServer.start.error",e);
		}finally{
			Thread.currentThread().setContextClassLoader(save);
		}
	}
	private void initJetty() throws Exception {
		ExecutorService es = getExecutorService();
		info("Jetty.ExecutorService:"+es);
		ClassLoaderWrapper clw = new ClassLoaderWrapper(this.getClass().getClassLoader(), Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(clw);
		String port = System.getProperty("jetty.port");
		String host = System.getProperty("jetty.host");
		if( es != null ){
			m_server = new Server(new ExecutorThreadPool(es)); 
		}else{
			m_server = new Server();
		}

		ServerConnector connector=new ServerConnector(m_server);
    connector.setPort(getInt(port, 8075));
		if( host != null){
    	connector.setHost(host);        
		}
    m_server.setConnectors(new Connector[]{connector});
		for(Connector y : m_server.getConnectors()) {
			for(ConnectionFactory x  : y.getConnectionFactories()) {
				if(x instanceof HttpConnectionFactory) {
					((HttpConnectionFactory)x).getHttpConfiguration().setSendServerVersion(false);
				}
			}
		}

		String sh = System.getProperty("simpl4.dir");
		m_basedir = new File(sh).getCanonicalFile().toString();
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		ServletContextHandler context0 = new ServletContextHandler(contexts, "/", ServletContextHandler.SESSIONS);
		List<Handler> handlerList = new ArrayList<Handler>();
		handlerList.add( context0);

		boolean isOpenfireDisabled = System.getProperty("simpl4.openfire.disabled") != null && "true".equals(System.getProperty("simpl4.openfire.disabled"));
		boolean isActiveMQDisabled = System.getProperty("simpl4.activemq.disabled") != null && "true".equals(System.getProperty("simpl4.activemq.disabled"));
		boolean isWAMPDisabled = System.getProperty("simpl4.wamp.disabled") != null && "true".equals(System.getProperty("simpl4.wamp.disabled"));
		info("Jetty.isWAMPDisabled:"+isWAMPDisabled);
		info("Jetty.isOpenfireDisabled:"+isOpenfireDisabled);
		info("Jetty.isActiveMQDisabled:"+isActiveMQDisabled);
		WebAppContext webappOpenfire=null;
		if( !isOpenfireDisabled){
			webappOpenfire = new WebAppContext(contexts, m_basedir + "/etc/openfire/web", "/openfire/");
			webappOpenfire.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
			webappOpenfire.setWelcomeFiles(new String[]{"index.jsp"});
			webappOpenfire.setResourceBase(m_basedir + "/etc/openfire/web");
			webappOpenfire.setContextPath("/openfire/");
			handlerList.add( webappOpenfire);
		}
		WebAppContext webAppActivemq = null;
		if( !isActiveMQDisabled){
			webAppActivemq = new WebAppContext(contexts, m_basedir + "/etc/activemq/web", "/activemq/");
			webAppActivemq.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
			webAppActivemq.setWelcomeFiles(new String[]{"index.jsp"});
			webAppActivemq.setResourceBase(m_basedir + "/etc/activemq/web");
			webAppActivemq.setContextPath("/activemq/");
			handlerList.add( webAppActivemq);
		}

		File servlets = new File(m_basedir,"/etc/servlets.json");
		if( servlets.exists()){
			String servletsString = new String(Files.readAllBytes(Paths.get(servlets.toURI())));
			JSONDeserializer ds = new JSONDeserializer();
			List<Map> servletList = (List) ds.deserialize(servletsString);
			for( Map<String,String> map : servletList){
				String basedir = map.get("basedir");
				if( !basedir.startsWith("/")){
					basedir = new File(m_basedir, basedir).toString();
				}
				String contextPath = map.get("contextPath");
				String indexFile = map.get("indexFile");
				info( "Create.webAppContext("+basedir+","+contextPath+","+indexFile+")");
				WebAppContext webAppContext = new WebAppContext(contexts, basedir, contextPath);
				webAppContext.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
				webAppContext.setAttribute("bundleContext", m_bundleContext);
				if( indexFile != null){
					webAppContext.setWelcomeFiles(new String[]{indexFile});
				}
				webAppContext.setResourceBase(basedir);
				webAppContext.setContextPath(contextPath);
				handlerList.add( webAppContext);
			}
		}

		LoginFilter loginFilter = new LoginFilter(m_permissionService);
		FilterHolder loginFilterHolder = new FilterHolder(loginFilter);
		loginFilterHolder.setName("LoginFilter");
		ServletHandler servletHandler = context0.getServletHandler();
		servletHandler.addFilter(loginFilterHolder, createFilterMapping("/*", loginFilterHolder));
		BundleContext bc = m_bundleContext;
		m_rpcServlet = new JsonRpcServlet(bc);
		context0.addServlet(new ServletHolder(m_rpcServlet), "/rpc/*");

		Map<String,Object> config = new HashMap();
		config.put("bundleContext",m_bundleContext);
		m_websocketServlet = new S4WebSocketServlet(config);
		context0.addServlet(new ServletHolder(m_websocketServlet), "/ws/*");
		context0.addServlet(new ServletHolder(new DefaultServlet() {


			public String getInitParameter(String name){
				//System.out.println("getInitParameter:"+name+"="+super.getInitParameter(name));
				if("resourceBase".equals(name)) return m_basedir;
				if("acceptRanges".equals(name)) return "true";
				if("etags".equals(name)) return "true";
				if("cacheControl".equals(name)) return "max-age=604800";
				return super.getInitParameter(name);
			}

			public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
				String method = req.getMethod().toUpperCase();
				if (method.equals("GET") || method.equals("HEAD")) {
					doGet(req, resp);
				} else {
					unknownRequest(req, resp);
				}
			}

			public void doGet(HttpServletRequest req, HttpServletResponse response) {
				try {
					if( req.getPathInfo().endsWith(".__pdf")){
						super.doGet(req,response);
						return;
					}
					if( req.getPathInfo().startsWith("/openfire/")){
						return;
					}
					info("Repo Request:"+req.getPathInfo());
					if( req.getPathInfo().startsWith("/repo/")){
						if(!handleRepo(req,response)){
							unknownRequest(req, response);
						}	
					}else{
						response.setHeader("Access-Control-Allow-Origin", "*");
						boolean handled = handleStatic(req, response);
						if (!handled) {
							unknownRequest(req, response);
						}
					}
				} catch (Exception e) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					e.printStackTrace();
				}
			}

			public void doOptions(HttpServletRequest req, HttpServletResponse response) {
				String origin = req.getHeader("Origin");
				debug("doOptions:" + origin);
				if (origin != null) {
					response.setHeader("Access-Control-Allow-Origin", origin);
					response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
					response.setContentType("text/plain");
					response.setHeader("Connection", "Keep-Alive");
					debug("setHeader");
				} else {
					String allow = "OPTIONS, GET, POST, TRACE, PROPFIND, PROPPATCH, MKCOL, COPY, PUT, DELETE, MOVE, LOCK, UNLOCK, BIND, REBIND, UNBIND, VERSION-CONTROL";
					response.setHeader("Allow", allow);
					response.setHeader("DAV", "1,2,3,bind,bind");
				}
			}
		}), "/*");

		Handler[] handlerArray = new Handler[handlerList.size()];
		handlerArray = handlerList.toArray(handlerArray);
		contexts.setHandlers(handlerArray);
		/*if( webappOpenfire != null && webAppActivemq!=null){
			contexts.setHandlers(new Handler[] { context0, webappOpenfire, webAppActivemq });
		}else if( webappOpenfire != null){
			contexts.setHandlers(new Handler[] { context0, webappOpenfire });
		}else if( webAppActivemq != null){
			contexts.setHandlers(new Handler[] { context0, webAppActivemq });
		}else{
			contexts.setHandlers(new Handler[] { context0 });
		}*/
		m_server.setHandler(contexts);
	}

	private void unknownRequest(HttpServletRequest request, HttpServletResponse response) {
		info("unknown request:" + request.getPathInfo() + "/" + request.getMethod());
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	private boolean handleRepo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String segs[] = request.getPathInfo().split("/");
		if( segs.length != 4 ){
			//throw new RuntimeException("Bad request");
		}
		String namespace = segs[2];
		String fileName = segs[segs.length-1];
		String ext = getExtension(fileName);
		if( segs.length>4){
			//System.out.println("pathInfo:"+request.getPathInfo());
			int i = ("/repo/"+namespace+"/").length();
			fileName = request.getPathInfo().substring(i);
			//System.out.println("Filename:"+fileName);
		}
		String mime =  FILETYPES.get(ext);
		//System.out.println("Mime:"+mime);
		if( mime == null){
			throw new RuntimeException("Unknown Filetype");
		}
		getAsset(namespace,fileName,mime, ext, request, response);
		return true;
	}

	public static String getExtension(String name){
		if( name.lastIndexOf("/")!=-1){
			name = name.substring(name.lastIndexOf("/"));
		}
		String segs[] = name.split("\\.");
		int len = segs.length;
		String ext = segs[len-1];
		if(len < 2 ){
			throw new RuntimeException("Bad filename");
		}
		if( "gz".equals(ext)){
			if(len < 3 ){
				throw new RuntimeException("Bad filename");
			}
			return segs[len-2] + "."+ segs[len-1];
		}
		return segs[len-1];	
	}

	private boolean handleStatic(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String target = request.getPathInfo();
		String namespace = request.getPathInfo().split("/")[1];
		boolean handled = true;
		if (target.startsWith("/robots.txt")){
			return true;
		}
		if (!target.startsWith("/" + namespace + "/")){
			return false;
		}
		target = target.substring(("/" + namespace + "/").length());
		debug("m_basedir:"+m_basedir+"|target:"+target);
		if (target.endsWith("mobile.html")) {
			FileResource fr = new FileResource(new URL("file:" + m_basedir + "/client/mobile.html"));
			response.setContentType("text/html;charset=UTF-8");
			response.addDateHeader("Date", new java.util.Date().getTime());
			response.addDateHeader("Expires", new java.util.Date().getTime() + 1000000000);
			fr.writeTo(response.getOutputStream(), 0, -1);
		}else if (target.endsWith("start.html")) {
			FileResource fr = new FileResource(new URL("file:" + m_basedir + "/client/start.html"));
			response.setContentType("text/html;charset=UTF-8");
			response.addDateHeader("Date", new java.util.Date().getTime());
			response.addDateHeader("Expires", new java.util.Date().getTime() + 1000000000);
			fr.writeTo(response.getOutputStream(), 0, -1);
		}else if (!target.startsWith("surface") && target.endsWith(".html")) {
			int slash = target.indexOf("/");
			if( slash != -1){
				String ns = target.substring(0,slash);	
				String ws = target.substring(slash+1, target.length()-5);	
				debug("ns:"+ns+"|ws:"+ws);
				m_docbookService.website(ns, ws, request, response);
			}else{
				m_docbookService.website("testapp1", "firstweb", request, response);
			}
		}else if ( isAssetRequest(target)) {
			String ns = getNamespace(target);	
			String assetName = getAssetName(target);	
			String ext = getFiletype(assetName);	
			debug("ns:"+ns+"|assetName:"+assetName+"|"+ext);
			m_docbookService.getAsset(ns, assetName, "image/"+ext, request, response);
		} else if (target.endsWith(".html")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource2(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("text/html;charset=UTF-8");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".html.gz")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource2(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("text/html;charset=UTF-8");
			response.setHeader("Content-Encoding","gzip");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".css")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource2(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("text/css;charset=UTF-8");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".css.gz")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource2(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("text/css;charset=UTF-8");
			response.setHeader("Content-Encoding","gzip");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".js.gz")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource2(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("text/javascript;charset=UTF-8");
			response.setHeader("Content-Encoding","gzip");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".js")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource2(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("text/javascript;charset=UTF-8");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".gif")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("image/gif");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".jpg")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("image/jpeg");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".png")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("image/png");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".woff")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("application/x-font-woff");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".ttf")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("application/x-font-ttf");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".pdf")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentLength((int)fr.length());
			response.setContentType("application/pdf");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".xml")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentLength((int)fr.length());
			response.setContentType("application/xml;charset=UTF-8");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".svg")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("image/svg+xml;charset=UTF-8");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else if (target.endsWith(".svgz")) {
			target = removeFirstSegmentInCaseWebsite(target);
			FileResource fr = getFileResource(m_basedir, target);
			if(!isModified(fr, request, response)){
				return true;
			}
			response.setContentType("image/svg+xml;charset=UTF-8");
			response.setHeader("Content-Encoding","gzip");
			setHeaders(response);
			fr.writeTo(response.getOutputStream(), 0, -1);
		} else {
			handled = false;
		}
		return handled;
	}

	private String removeFirstSegmentInCaseWebsite(String s){
		debug("removeFirstSegmentInCaseWebsite1:"+s);
		String segs[] = s.split("/");
		if( s.charAt(0) == '/'){
			if( segs.length > 1 && segs[1].equals("website")){
				return s.substring(s.substring(1).indexOf("/")+1);
			}
		}else{
			if( segs.length > 1 && segs[0].equals("website")){
				return s.substring(s.indexOf("/")+1);
			}
		}
		debug("removeFirstSegmentInCaseWebsite2:"+s);
		return s;
	}
	private FileResource getFileResource(String basedir, String target) throws Exception {
		FileResource fr = new FileResource(new URL("file:" + basedir + "/client/" + target));
		if (fr.exists()) {
			return fr;
		}
		fr = new FileResource(new URL("file:" + basedir + "/client/common/build/" + target));
		if (fr.exists()) {
			return fr;
		}
		fr = new FileResource(new URL("file:" + basedir + "/client/website/build/" + target));
		if (fr.exists()) {
			return fr;
		}
		fr = new FileResource(new URL("file:" + basedir + "/client/mobile/build/" + target));
		if (fr.exists()) {
			return fr;
		}
		fr = new FileResource(new URL("file:" + basedir + "/client/" + target));
		debug("getFileResource:"+fr);
		return fr;
	}
	private FileResource getFileResource2(String basedir, String target) throws Exception {
		FileResource fr = new FileResource(new URL("file:" + basedir + "/client/" + target));
		if (fr.exists()) {
			return fr;
		}
		fr = new FileResource(new URL("file:" + basedir + "/client/mobile/build/" + target));
		debug("getFileResource:"+fr);
		return fr;
	}

	private void handleService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String _meth = request.getMethod().toLowerCase();
		if (!"post".equals(_meth)) {
			throw new RuntimeException("JettyServiceImpl.handleService:only method \"post\" allowed");
		}
		String pathInfo = request.getPathInfo();
		BundleContext bc = m_bundleContext;
		String[] segs = pathInfo.split("/");
		String namespace = segs[1];
		String service = segs[3];
		String method = segs[4];
		int dot = service.lastIndexOf(".");
		String clazzName = null;
		if (dot != -1) {
			String part1 = service.substring(0, dot);
			String part2 = service.substring(dot + 1);
			info("service:" + service);
			clazzName = "org.ms123." + namespace + "." + part1 + "." + m_inflector.upperCamelCase(part2, '-') + "Service";
		} else {
			String s = m_inflector.upperCamelCase(service, '-');
			clazzName = "org.ms123." + namespace + "." + s.toLowerCase() + "." + s + "Service";
		}
		info("=> " + clazzName + "/" + method);
		ServiceReference sr = bc.getServiceReference(clazzName);
		if (sr == null) {
			if (dot != -1) {
				String part1 = service.substring(0, dot);
				String part2 = service.substring(dot + 1);
				clazzName = "org.ms123.common." + part1 + "." + m_inflector.upperCamelCase(part2, '-') + "Service";
			} else {
				String s = m_inflector.upperCamelCase(service, '-');
				clazzName = "org.ms123.common." + s.toLowerCase() + "." + s + "Service";
			}
			info("=> " + clazzName);
			sr = bc.getServiceReference(clazzName);
		}
		if (sr == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String contentType = request.getContentType();
		Map paramMap = null;
		try {
			String postdata = convertStreamToString(request.getInputStream());
			if (postdata != null && postdata.startsWith("{")) {
				info("\tPostdata(" + contentType + "):" + postdata);
				JSONDeserializer ds = new JSONDeserializer();
				paramMap = (Map) ds.deserialize(postdata);
			} else {
				paramMap = new HashMap();
				Map<String, String[]> map = request.getParameterMap();
				Iterator<String> it = map.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					String[] val = map.get(key);
					paramMap.put(key, val.length > 1 ? val : val[0]);
				}
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			info("json-error:" + e);
			return;
		}
		info("\tparamMap:" + paramMap);
		Object o = bc.getService(sr);
		Class[] cargs = new Class[2];
		cargs[0] = Map.class;
		cargs[1] = Map.class;
		if (method.indexOf("-") != -1) {
			method = m_inflector.lowerCamelCase(method, '-');
			info("\tmethodname:" + method);
		}
		Method meth = o.getClass().getDeclaredMethod(method, cargs);
		info("\tDeclaredMethot:" + meth);
		info("\tNamespace:" + namespace);
		Map params = new HashMap();
		Object[] args = new Object[2];
		Map sysMap = new HashMap();
		sysMap.put("username", request.getAttribute("username"));
		sysMap.put(NAMESPACE, namespace);
		sysMap.put("request", request);
		sysMap.put("response", response);
		args[0] = paramMap;
		args[1] = sysMap;
		try {
			Object _ret = meth.invoke(o, args);
			if (_ret != null && !response.isCommitted()) {
				Map retMap = _ret != null ? (Map) _ret : new HashMap();
				boolean pretty = paramMap.get("_prettyPrint") == null ? false : (Boolean) paramMap.get("_prettyPrint");
				JSONSerializer serializer = new JSONSerializer();
				serializer.prettyPrint(pretty);
				Object jsonObject = serializer.deepSerialize(retMap);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter().print(jsonObject);
				info("<= " + jsonObject);
			}
		} catch (Throwable e) {
			info("doPost:"+ e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json;charset=UTF-8");
			Map retMap = new HashMap();
			while (e.getCause() != null) {
				e = e.getCause();
				info("\t:cause:" + e.getMessage());
			}
			retMap.put("msg", e.toString());
			JSONSerializer serializer = new JSONSerializer();
			Object jsonObject = serializer.deepSerialize(retMap);
			response.getWriter().print(jsonObject);
			info("<= " + jsonObject);
		}
	}

	private FilterMapping createFilterMapping(String pathSpec, FilterHolder filterHolder) {
		FilterMapping filterMapping = new FilterMapping();
		filterMapping.setPathSpec(pathSpec);
		filterMapping.setFilterName(filterHolder.getName());
		return filterMapping;
	}

	public void serviceChanged(ServiceEvent event) {
		String[] objectClass = (String[]) event.getServiceReference().getProperty("objectClass");
		ServiceReference sr = event.getServiceReference();
		String rpc_prefix = (String) sr.getProperty("rpc.prefix");
		if (rpc_prefix == null) {
			info("JettyServiceImpl.serviceChanged is null:"+objectClass+"/"+sr);
			return;
		}
		if (event.getType() == ServiceEvent.REGISTERED) {
			debug("Ex1: Service of type " + objectClass[0] + " registered.rpc_prefix:" + rpc_prefix);
			m_rpcServlet.putServiceMapping(rpc_prefix, objectClass[0]);
		} else if (event.getType() == ServiceEvent.UNREGISTERING) {
			m_rpcServlet.putServiceMapping(rpc_prefix, null);
			debug("Ex1: Service of type " + objectClass[0] + " unregistered.rpc_prefix:" + rpc_prefix);
		} else if (event.getType() == ServiceEvent.MODIFIED) {
			m_rpcServlet.putServiceMapping(rpc_prefix, objectClass[0]);
			debug("Ex1: Service of type " + objectClass[0] + " modified.rpc_prefix:" + rpc_prefix);
		}
	}

	protected void deactivate() throws Exception {
		info("JettyServiceImpl deactivate");
		m_server.stop();
	}

	public void destroy() throws Exception {
		info("deactivate");
		m_server.stop();
	}

	@Reference(dynamic = true)
	public void setPermissionService(PermissionService shiroService) {
		info("shiroService:" + shiroService);
		this.m_permissionService = shiroService;
	}
	@Reference(dynamic = true,optional=true)
	public void setDocbookService(DocbookService dbService) {
		info("dbService:" + dbService);
		this.m_docbookService = dbService;
	}
	@Reference(dynamic = true,optional=true)
	public void setGitService(GitService dbService) {
		info("gitService:" + dbService);
		this.m_gitService = dbService;
	}
	private boolean isAssetRequest(String target){
		try{
			String p[] = target.split("/");
			if( p.length!=2) return false;
			if( p[1].startsWith("image:") || p[1].startsWith("repo")){
				debug("isAssetRequest:true");
				return true;
			}
			return false;
		}catch(Exception e){
			return false;
		}
	}
	private String getFiletype(String asset){
		try{
			int dot = asset.lastIndexOf(".");
			String ext = asset.substring(dot+1);	
			if( "jpg".equals(ext)) return "jpeg";
			return ext;
		}catch(Exception e){
			return "unknown_ext";
		}
	}
	private String getNamespace(String target){
		try{
			int slash = target.indexOf("/");
			return target.substring(0,slash);	
		}catch(Exception e){
			return "unknown_ns";
		}
	}
	private String getAssetName(String target){
		try{
			int slash = target.indexOf("/");
			if( target.substring(slash+1).startsWith("image")){
				return target.substring(slash+1+("image:".length()), target.length());	
			}else if( target.substring(slash+1).startsWith("repo")){
				return target.substring(slash+1+("repo:".length()), target.length());	
			}else{
				return "unknown_ns";
			}
		}catch(Exception e){
			return "unknown_ns";
		}
	}

	private String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	private boolean isModified(FileResource f, HttpServletRequest request, HttpServletResponse response){
		Date sinceDate = new Date(request.getDateHeader("If-Modified-Since"));
		long modTime = f.lastModified( ); 
		if( modTime < sinceDate.getTime() ){
			response.setStatus(304);
			return false;
		}else{
			response.setDateHeader("Last-Modified", modTime );
			return true;
		}
	}

	private void setHeaders(HttpServletResponse response){
		response.setHeader("Cache-Control", "must-revalidate, private");
		response.setDateHeader("Last-Modified", new java.util.Date().getTime());
		response.setIntHeader("Expires", -1 );
	}

	private void getAsset(String namespace, String name, String type, String ext, HttpServletRequest request, HttpServletResponse response) throws Exception {
		File asset=null;
		String contentType = type;
		try{
			if( "image/svg".equals(type)){
				type = "image/svg+xml";
				contentType = "image/svg+xml";
			}
			if( "image/swf".equals(type)){
				contentType = "application/x-shockwave-flash";
			}
			if( "image/pdf".equals(type)){
				contentType = "application/pdf";
			}
			if( "image/jpeg".equals(type)){
				type = "image/jpg";
			}
			info("getAsset.FileName:"+name+"/contentType:"+type);
			asset = m_gitService.searchFile(namespace, name, type);
		}catch(Exception e){
			e.printStackTrace();
			response.setStatus(404);
			return;
		}
		
		String rpc = request.getParameter("rpc");
		if( rpc == null){
			Date sinceDate = new Date(request.getDateHeader("If-Modified-Since")+1000);
			long modTime = asset.lastModified( ); 
			if( modTime < sinceDate.getTime() ){
				response.setStatus(304);
				return;
			}else{
				if( name.endsWith(".gz")){
					response.setHeader("Content-Encoding","gzip");
				}
				if( name.endsWith(".svgz")){
					response.setHeader("Content-Encoding","gzip");
				}
				setHeaders(response);
				response.setStatus(HttpServletResponse.SC_OK);
				if( "adoc".equals(ext)){
					response.setContentType( "text/html; charset=UTF-8" );
					Writer w  = response.getWriter();
					String t = request.getParameter("t");
					if( t== null){
						t = request.getParameter("template");
					}
					if( t!= null){
						w.write("<template is=\"dom-bind\" bind>");
					}
					m_docbookService.adocToHtml(asset, w, request.getParameterMap() );
					if( t!= null){
						w.write("</template>");
					}
					w.close();
				}else if( "yaml".equals(ext) || "yml".equals(ext)){
					response.setContentType( "application/json; charset=UTF-8" );
					PrintWriter w  = response.getWriter();
					Yaml yaml = new Yaml();
  				Object yamlObj = yaml.load(new InputStreamReader(new FileInputStream(asset), "UTF8"));
					JSONSerializer serializer = new JSONSerializer();
					Object jsonObj = serializer.deepSerialize(yamlObj);
					w.print(jsonObj);
					w.close();
				}else{
					response.setContentType( contentType );
					response.setContentLength( (int)asset.length() );
					OutputStream os = response.getOutputStream();
					InputStream in = new FileInputStream(asset);
					IOUtils.copy( in, os );
					os.close();
					in.close();
				}
			}
		}else{
			Map<String, Object> rpcMap = null;
			if( rpc.trim().startsWith("{")){
				rpcMap = m_rpcServlet.extractRequestMap(rpc);
			}else{
				Map<String,String>params = splitQuery(request.getQueryString());
				params.put("filename", name);
				String method = rpc;
				if( method.indexOf(".") < 0){
					method = namespace + "."+ method;
				}
				rpcMap = new HashMap<String,Object>();
				rpcMap.put("method", method);
				rpcMap.put("service", "simpl4");
				rpcMap.put("params", params);
			}
			rpcMap.put("_ASSET", asset);
			String result = m_rpcServlet.handleRPC(request,rpcMap,response);
			if (!response.isCommitted()) {
				String origin = request.getHeader("Origin");
				if (origin != null) {
					response.setHeader("Access-Control-Allow-Origin", origin);
				}
				response.setContentType(JsonRpcServlet.DOPOST_RESPONSE_CONTENTTYPE);
				try {
					response.getWriter().write(result);
				} catch (IOException e) {
					throw new ServletException("Cannot write response", e);
				}
			}
		}
	}
	private Map<String, String> splitQuery(String query) {
		final Map<String, String> queryPairs = new HashMap<String, String>();
		final String[] pairs = query.split("&");
		for (String pair : pairs) {
			final int idx = pair.indexOf("=");
			try{
				final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
				final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
				if( !"method".equals(key)){
					queryPairs.put(key,value);
				}
			}catch( Exception e){
				error("splitQuery:"+e.toString(), e );
			}
		}
		return queryPairs;
	}
	private String getLocalHostLANAddress() {
		try{
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {
						if (inetAddr.isSiteLocalAddress()) {
							String s = inetAddr.toString();
							if( s.startsWith("/")){
								return s.substring(1);
							}else{
								return s;
							}
						}
					}
				}
			}
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null) {
				return "The JDK InetAddress.getLocalHost() method unexpectedly returned null.";
			}
			return jdkSuppliedAddress.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		return "unknown";
	}

	private int getInt(String s, int def) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
		}
		return def;
	}

	protected void debug(String msg) {
		m_logger.debug(msg);
	}
	protected void info(String msg) {
		m_logger.info(msg);
	}
	protected void error(String msg,Throwable t) {
		t.printStackTrace();
		m_logger.error(msg,t);
	}
	private static final org.slf4j.Logger m_logger = org.slf4j.LoggerFactory.getLogger(JettyServiceImpl.class);
}
