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
package org.ms123.launcher;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.util.*;
import java.net.*;
import java.io.File;
import java.io.FileInputStream;
import org.apache.felix.main.Main;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.FrameworkEvent;
import java.util.concurrent.Callable;
import static org.joor.Reflect.*;
import java.io.*;
import java.net.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.concurrent.ExecutorService;
import org.unix4j.Unix4j;
import org.unix4j.builder.Unix4jCommandBuilder;
import org.unix4j.io.Output;
import org.unix4j.io.StreamOutput;
import org.unix4j.unix.Ls;
import org.unix4j.unix.Sort;
import org.unix4j.unix.grep.GrepOption;
import org.unix4j.variable.Arg;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.util.*;
import org.eclipse.jgit.lib.*;
import org.apache.commons.io.FileUtils;


@SuppressWarnings({"unchecked","deprecation"})
public class OsgiStarter implements ServletContextListener {

	public static int jettyPort = -1;

	private static boolean isWS = false;
	public static String jettyHost = null;
	public static String simpl4BaseUrl = null;
	public static String simpl4Dir;
	private static String FELIX_VERSION="4.6.1";

	private static Object osgiFrameworkObj = null;

	public void contextInitialized(ServletContextEvent sce) {
		if (jettyPort != -1) {
			return;
		}
		InetAddress loopBack = null;
		try {
			loopBack = InetAddress.getLoopbackAddress();
			ServerSocket socket = new ServerSocket(0, 1, loopBack);
			jettyPort = 11111;//socket.getLocalPort();
			jettyHost = loopBack.getHostAddress();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
			jettyPort = 10000;
			jettyHost = "127.0.0.1";
		}
		simpl4BaseUrl = "http://" + loopBack.getHostAddress() + ":" + jettyPort;
		info("contextInitialized:" + jettyPort + "|" + loopBack + "|" + simpl4BaseUrl);
		ExecutorService executor = getExecutorService();
		executor.submit(new MyCallable(sce));
	}

	public class MyCallable implements Callable<String> {

		ServletContextEvent sce;

		public MyCallable(ServletContextEvent _sce) {
			this.sce = _sce;
		}

		@Override
		public String call() throws Exception {
			try {
				Thread.sleep(5500);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			String cp = this.sce.getServletContext().getRealPath("/WEB-INF");
			simpl4Dir = cp.substring(0, cp.length() - 8);
			start();
			return null;
		}
	}

	private ExecutorService getExecutorService() {
		try {
			Context ctx = new InitialContext();
			if (ctx == null){
				throw new Exception("OsgiStarter.JNDI could not create InitalContext ");
			}
			ExecutorService es = (ExecutorService)ctx.lookup("wm/simpl4WorkManager");
			info("OsgiStarter.getExecutorService:"+ es);
			return es;
		} catch (Throwable e) {
			info("OsgiStarter.getExecutorService:"+ e);
		}
		return Executors.newSingleThreadExecutor();
	}
	public static void main(String[] args) {
		jettyPort = 10000;
		jettyHost = "127.0.0.1";
		simpl4Dir = (String) System.getProperty("simpl4.dir");
		System.out.println("simpl4Dir:" + simpl4Dir);
		start();
	}

	public void contextDestroyed(ServletContextEvent sce) {
		info("contextDestroyed" );
		terminate();
		jettyPort = -1;
	}

	private static void start() {
		if (osgiFrameworkObj == null) {
			info("Starting the osgi-framework");
			startFramework();
		}
	}

	private static void terminate() {
		if (osgiFrameworkObj != null) {
			info("Stopping the osgi-framework");
			try {
				on(osgiFrameworkObj).call("stop");
				on(osgiFrameworkObj).call("waitForStop", 0L).get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			info("osgi-framework stopped");
		}
	}

	private static void initLogger(){
		if( !isFirstRun() ){
			return;
		}
		String simpl4Dir = (String) System.getProperty("simpl4.dir");
		String loggingConfigTpl = new File(simpl4Dir, "etc/logging.config.tpl").toString();
		info("doSetup.loggingConfigTpl:"+loggingConfigTpl);
		String basedir = getBaseDir().replaceAll("\\\\", "/");
		String basedir2 = simpl4Dir.replaceAll("\\\\", "/");
		info("doSetup.basedir:"+basedir);
		File logDir = new File(getBaseDir(),"log");
		if( !logDir.exists()){
			logDir.mkdir();
		}
		info("doSetup.logDir:"+logDir);
		File loggingConfig = new File(simpl4Dir, "etc/logging.config");
		Unix4j.cat(loggingConfigTpl).sed("s!_BASEDIR_!"+basedir2+"!g").sed("s!_LOGDIR_!"+logDir.toString().replaceAll("\\\\", "/")+"!g").toFile(loggingConfig);

		File logBackTpl = new File(simpl4Dir, "etc/logback.xml.tpl");
		File logBack = new File(simpl4Dir,"etc/logback.xml");
		Unix4j.cat(logBackTpl).sed("s!_BASEDIR_!"+basedir+"!g").sed("s!_LOGDIR_!"+logDir.toString().replaceAll("\\\\", "/")+"!g").toFile(logBack);

		File logConfigTpl = new File(simpl4Dir, "etc/config/org/ops4j/pax/logging.config.tpl");
		File logConfig = new File(simpl4Dir,"etc/config/org/ops4j/pax/logging.config");
		Unix4j.cat(logConfigTpl).sed("s!_BASEDIR_!"+basedir2+"!g").toFile(logConfig);
	}

	private static boolean isFirstRun(){
		String simpl4Dir = (String) System.getProperty("simpl4.dir");
		File initFile = new File(simpl4Dir, "etc/initialized");
		return !initFile.exists();
	}
	private static String getBaseDir(){
		String baseDir = getVarDir();
		if( baseDir == null){
			return (String) System.getProperty("simpl4.dir");
		}
		return baseDir;
	}
	private static String getVarDir(){
		String varDir = (String) System.getProperty("tpso.web.vardir");
		if( varDir == null){
			return (String) System.getProperty("simpl4.vardir");
		}
		return varDir;
	}
	private static void setProperties() {
		String af = new File(".").getAbsolutePath();
		String vardir = getVarDir();
		String varSimpl4Dir = null;
		if( vardir != null){
			varSimpl4Dir = new File(vardir, "simpl4").toString();
		}
		info("setProperties.currentDir:" + af);
		info("setProperties.simpl4Dir:" + simpl4Dir);
		info("setProperties.varSimpl4Dir:" + varSimpl4Dir);
		setUserDir(simpl4Dir + "/server");
		System.setProperty("activemq.data", simpl4Dir + "/etc/activemq/data");
		System.setProperty("cassandra.boot_without_jna", "true");
		System.setProperty("cassandra.storagedir", ((vardir!=null) ? varSimpl4Dir : simpl4Dir) + "/gitrepos/global_data/store/cassandra");
		System.setProperty("disableCheckForReferencesInContentException", "true");
		System.setProperty("drools.dialect.java.compiler", "JANINO");
		System.setProperty("etc.dir", simpl4Dir + "/etc");
		System.setProperty("felix.cm.dir", simpl4Dir + "/etc/config");
		System.setProperty("felix.config.properties", "file:" + simpl4Dir + "/server/felix/config.ini");
		System.setProperty("felix.fileinstall.dir", ((vardir!=null) ? varSimpl4Dir : simpl4Dir) + "/gitrepos/.bundles");
		System.setProperty("file.encoding", "UTF-8");
		System.setProperty("git.repos", ((vardir!=null) ? varSimpl4Dir : simpl4Dir) + "/gitrepos");
		System.setProperty("groovy.target.indy", "false");
		System.setProperty("jetty.host", jettyHost);
//		System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG");
		System.setProperty("jetty.port", String.valueOf(jettyPort));
		System.setProperty("karaf.etc", simpl4Dir + "/etc/activemq/etc");
		System.setProperty("karaf.local.roles", "admin,manager");
		System.setProperty("karaf.shell.init.script", simpl4Dir + "/etc/shell.init.script");
		System.setProperty("karaf.shell.init.script", simpl4Dir + "/etc/shell.init.script");
		System.setProperty("karaf.startLocalConsole", "false");
		System.setProperty("karaf.startRemoteShell", "false");
		System.setProperty("karaf.systemBundlesStartLevel", "0");
		System.setProperty("openfireHome", simpl4Dir + "/etc/openfire");
		System.setProperty("org.ops4j.pax.logging.DefaultServiceLog.level", "ERROR");
		if( vardir != null){
			System.setProperty("org.osgi.framework.storage", varSimpl4Dir + "/server/felix/cache");
		}else{
			System.setProperty("org.osgi.framework.storage", simpl4Dir + "/server/felix/cache/runner");
		}
		System.setProperty("org.osgi.service.http.port", "7170");
		System.setProperty("server.root", simpl4Dir + "/server");
		System.setProperty("simpl4.dir", simpl4Dir);
		System.setProperty("webconsole.jms.password", "admin");
		System.setProperty("webconsole.jms.url", "tcp://localhost:61616");
		System.setProperty("webconsole.jms.user", "admin");
		System.setProperty("webconsole.jmx.password", "admin");
		System.setProperty("webconsole.jmx.url", "service:jmx:rmi:///jndi/rmi://localhost:1098/jmxrmi");
		System.setProperty("webconsole.jmx.user", "admin");
		System.setProperty("webconsole.type", "properties");
		System.setProperty("workspace", ((vardir!=null) ? varSimpl4Dir : simpl4Dir) + "/workspace");
		if( vardir != null){
			System.setProperty("bitronix.tm.journal.disk.logPart1Filename", varSimpl4Dir + "/server/btm1.tlog");
			System.setProperty("bitronix.tm.journal.disk.logPart2Filename", varSimpl4Dir + "/server/btm2.tlog");
		}else{
			System.setProperty("bitronix.tm.journal.disk.logPart1Filename", simpl4Dir + "/server/btm1.tlog");
			System.setProperty("bitronix.tm.journal.disk.logPart2Filename", simpl4Dir + "/server/btm2.tlog");
		}
		Double version = Double.parseDouble(System.getProperty("java.specification.version"));
		System.out.println("JavaVersion:"+version);
		if( version < 1.8){
			System.setProperty("simpl4.wamp.disabled", "true");
		}
		isWS = System.getProperty("com.ibm.oti.jcl.build")!=null ||  System.getProperty("ibm.system.encoding")!=null;
		System.out.println("isWS:"+isWS);
		if( isWS || version < 1.8){
			System.setProperty("simpl4.activemq.disabled", "true");
			System.setProperty("simpl4.openfire.disabled", "true");
		}
		System.setProperty("simpl4.in_servlet", "true");
		
	}

	private static void setUserDir(String directory_name) {
		File directory = new File(directory_name).getAbsoluteFile();
		if (directory.exists()) {
			System.setProperty("user.dir", directory.getAbsolutePath());
		}
	}

	private static void copySystemProperties(Map configProps) {
		for (Enumeration e = System.getProperties().propertyNames(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			if (key.startsWith("felix.") || key.equals("org.osgi.framework.storage")) {
				System.out.println("copySystemProperties:" + key + "=" + System.getProperty(key));
				configProps.put(key, System.getProperty(key));
			}
		}
	}

	private static void startFramework() {
		URL felixURL = null;
		setProperties();
		Main.loadSystemProperties();
		Map<String, String> configProps = Main.loadConfigProperties();
		if (configProps == null) {
			info("No " + Main.CONFIG_PROPERTIES_FILE_VALUE + " found.");
			configProps = new HashMap<String, String>();
		}
		copySystemProperties(configProps);
		String enableHook = configProps.get(Main.SHUTDOWN_HOOK_PROP);
		if ((enableHook == null) || !enableHook.equalsIgnoreCase("false")) {
			Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {

				public void run() {
					try {
						if (osgiFrameworkObj != null) {
							on(osgiFrameworkObj).call("stop");
							on(osgiFrameworkObj).call("waitForStop", 0L).get();
						}
					} catch (Exception ex) {
						System.err.println("Error stopping osgi-framework: " + ex);
					}
				}
			});
		}
		try {
			initLogger();
			List<URL> classLoaderUrls = new ArrayList<URL>();
			classLoaderUrls.add(new URL("file:" + simpl4Dir + "/WEB-INF/lib/org.apache.felix.main-"+FELIX_VERSION+".jar"));
			classLoaderUrls.add(new URL("file:" + simpl4Dir + "/WEB-INF/lib/xml-w3c.jar"));
			info("classLoaderUrls:" + classLoaderUrls);
			CustomClassLoader ccl = new CustomClassLoader(classLoaderUrls);
			Thread.currentThread().setContextClassLoader(ccl);
			Object frameworkFactoryObj = ccl.loadClass("org.apache.felix.framework.FrameworkFactory").newInstance();
			info("frameworkFactoryObj:" + frameworkFactoryObj);
			osgiFrameworkObj = on(frameworkFactoryObj).call("newFramework", configProps).get();
			info("osgiFrameworkObj:" + osgiFrameworkObj);
			on(osgiFrameworkObj).call("init");
			Object bundleContextObj = on(osgiFrameworkObj).call("getBundleContext").get();
			info("bundleContextObj:" + bundleContextObj);

			Object autoProcessorObj = ccl.loadClass("org.apache.felix.main.AutoProcessor").newInstance();
			info("autoProcessorObj:" + autoProcessorObj);
			on(autoProcessorObj).call("process", configProps, bundleContextObj);
			FrameworkEvent event;
			Object eventObj;
			do {
				on(osgiFrameworkObj).call("start");
				info("waitForStop");
				eventObj = on(osgiFrameworkObj).call("waitForStop", 0L).get();
				info("waitForStop2:"+eventObj);
			} while ((int) on(eventObj).call("getType").get() == FrameworkEvent.STOPPED_UPDATE);
			osgiFrameworkObj = null;
			info("osgiFrameworkObj:null");
		} catch (Exception ex) {
			info("Could not create osgi-framework: " + ex);
			ex.printStackTrace();
		}
	}
	private static void info(String msg) {
		System.out.println("OsgiStarter:" + msg);
//		System.err.println("OsgiStarter:" + msg);
	}
}
