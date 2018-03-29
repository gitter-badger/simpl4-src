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
package org.ms123.common.camel.components.consumer;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import java.io.File;
import java.net.URL;
import java.util.Map;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.ms123.common.libhelper.Utils;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.io.FileUtils.readFileToString;

@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
public class ScriptConsumerConsumer extends DefaultConsumer {
	File file;
	def main;
	String namespace;
	Long lastMod = 0L;
	Class scriptClazz;
	String scriptSource;
	ClassLoader classLoader;
	ScriptConsumerEndpoint endpoint;
	/*public GroovyProcessor(script, f, ns, main ){
		this.lastMod = f.lastModified();
		this.namespace = ns;
		this.main = main;
		parse(script, this.file.getName());
	}*/

	private final ScriptConsumerEndpoint endpoint;

	public ScriptConsumerConsumer(ScriptConsumerEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	protected void doStart() throws Exception {
		if( this.file == null){
			this.file = endpoint.getScriptfile();
			this.namespace = endpoint.getNamespace();
		}
		execute();
		super.doStart();
	}

	protected void doStop() throws Exception {
		super.doStop();
	}


	def testModified(){
		if( this.file == null) return;
		def curMod = this.file.lastModified();
		if( curMod > this.lastMod){
			def script = readFileToString(this.file);
			parse(script, this.file.getName());
			this.lastMod = curMod;
		}
	}

	private void parse(String scriptStr,String scriptName) {
		info(this,"GroovyProcessor.parse("+scriptName+"):"+scriptStr);
		if( scriptStr == null) return null;
		this.scriptSource = scriptStr;

		def parentLoader = new URLClassLoader( endpoint.getClassPath(this.namespace) as URL[], this.getClass().getClassLoader() )

		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(org.ms123.common.camel.jsonconverter.GroovyBase.class.getName());
		def importCustomizer = new ImportCustomizer();
		importCustomizer.addStarImports("org.apache.camel");
		importCustomizer.addStarImports("groovy.transform");
		config.addCompilationCustomizers(importCustomizer);
		//GroovyClassLoader loader =  new CollectorClassLoader(parentLoader,config);
		GroovyClassLoader loader =  new GroovyClassLoader(parentLoader,config);
		this.classLoader = loader;

		try{
			GroovyCodeSource gcs = new GroovyCodeSource( scriptStr, "Script_"+scriptName, "/groovy/shell");
			this.scriptClazz = loader.parseClass(gcs,false);
		}catch(Throwable e){
			String msg = Utils.formatGroovyException(e,scriptStr);
			throw new RuntimeException("GroovyProcessor.parse("+scriptName+"):"+msg);
		}
	}

	private Object run(Script script, Map vars, String scriptName) {
		println("GroovyProcessor.run("+scriptName+"):"+vars);

		Thread.currentThread().setContextClassLoader(classLoader);//needed orientdb-groovy/OrientGraphHelper.groovy

		script.setBinding(new Binding(vars));
		try{
			return script.run();
		}catch(groovy.lang.MissingMethodException e){
			e.printStackTrace();
			Object[] args = e.getArguments();
			String a = "";
			String k = "";
			for(int i=0; i< args.length;i++){
				a += k+args[i];
				k = ",";
			}
			throw new RuntimeException("GroovyProcessor.run("+scriptName+"):"+e.getMethod() + "("+ a + ") not found");
		}catch(Exception ex){
			ex.printStackTrace();
			String msg = Utils.formatGroovyException(ex,scriptSource);
			throw new RuntimeException("GroovyProcessor.run("+scriptName+"):"+msg );
		}
	}

	public void execute() {
		testModified();

		def params = [:];
		/*params.put("exchange", ex);
		def ctx = ex.getContext();
		def registry = ctx.getRegistry();
		params.put("headers", ex.in.headers);
		params.put("h", ex.in.headers);
		params.put("min", ex.in);
		params.put("msg", ex.in);
		params.put("registry", registry);
		params.put("properties", ex.properties);
		params.put("p", ex.properties);*/
		def env = [
			gitRepos: System.getProperty("git.repos"),
			simpl4Dir: System.getProperty("simpl4.dir"),
			homeDir: System.getProperty("git.repos")+ "/" + this.namespace,
			homeDataDir: System.getProperty("git.repos")+ "/" + this.namespace+"_data",
			namespace: this.namespace,
			hostname: InetAddress.getLocalHost().getHostName()
		]
		def script = 	this.scriptClazz.newInstance() as Script;
		params.put("env", env);
		if( endpoint.fieldExists(this.scriptClazz,"entityService")){
			endpoint.injectField( this.scriptClazz, script, "entityService", endpoint.lookupServiceByString( "org.ms123.common.entity.api.EntityService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"permissionService")){
			endpoint.injectField( this.scriptClazz, script, "permissionService", endpoint.lookupServiceByString( "org.ms123.common.permission.api.PermissionService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"processService")){
			endpoint.injectField( this.scriptClazz, script, "processService", endpoint.lookupServiceByString( "org.ms123.common.process.api.ProcessService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"authService")){
			endpoint.injectField( this.scriptClazz, script, "authService", endpoint.lookupServiceByString( "org.ms123.common.auth.api.AuthService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"dataLayer")){
			endpoint.injectField( this.scriptClazz, script, "dataLayer", endpoint.getDataLayer())
		}
		if( endpoint.fieldExists(this.scriptClazz,"dataLayerJdo")){
			endpoint.injectField( this.scriptClazz, script, "dataLayerJdo", endpoint.getDataLayerJdo())
		}
		if( endpoint.fieldExists(this.scriptClazz,"messageService")){
			endpoint.injectField( this.scriptClazz, script, "messageService", endpoint.lookupServiceByString( "org.ms123.common.message.MessageService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"settingService")){
			endpoint.injectField( this.scriptClazz, script, "settingService", endpoint.lookupServiceByString( "org.ms123.common.setting.api.SettingService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"registryService")){
			endpoint.injectField( this.scriptClazz, script, "registryService", endpoint.lookupServiceByString( "org.ms123.common.system.registry.RegistryService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"callService")){
			endpoint.injectField( this.scriptClazz, script, "callService", endpoint.lookupServiceByString( "org.ms123.common.rpc.CallService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"orientGraph")){
			endpoint.injectField( this.scriptClazz, script, "orientGraph", endpoint.getOrientGraph( this.namespace))
		}
		if( endpoint.fieldExists(this.scriptClazz,"orientGraphRoot")){
			endpoint.injectField( this.scriptClazz, script, "orientGraphRoot", endpoint.getOrientGraphRoot( this.namespace))
		}
		run(script,params,this.file.getName());
	}
}
