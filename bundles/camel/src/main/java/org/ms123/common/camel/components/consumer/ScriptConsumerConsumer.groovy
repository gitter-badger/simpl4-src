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
import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import groovy.transform.TypeCheckingMode;
import java.io.File;
import java.net.URL;
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

@CompileStatic
@TypeChecked
public class ScriptConsumerConsumer extends DefaultConsumer {
	String namespace;
	File scriptFile;
	String scriptName;
	Class scriptClazz;
	Object scriptInstance;
	String scriptSource;
	GroovyClassLoader classLoader;
	ScriptConsumerEndpoint endpoint;

	private final ScriptConsumerEndpoint endpoint;

	public ScriptConsumerConsumer(ScriptConsumerEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	protected void doStart() throws Exception {
		this.namespace = endpoint.getNamespace();
		this.scriptFile = endpoint.getScriptfile();
		this.scriptName = this.scriptFile.getName();
		this.scriptSource = readFileToString(this.scriptFile);
		parse();
		execute();
		super.doStart();
	}

	protected void doStop() throws Exception {
		callingStartStop(false);
		super.doStop();
	}

	private void parse() {
		info(this,"GroovyProcessor.parse("+this.scriptName+")");

		def parentLoader = new URLClassLoader( endpoint.getClassPath(this.namespace) as URL[], this.getClass().getClassLoader() )

		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(GroovyBase.class.getName());
		def importCustomizer = new ImportCustomizer();
		importCustomizer.addStarImports("org.apache.camel");
		importCustomizer.addStarImports("groovy.transform");
		config.addCompilationCustomizers(importCustomizer);
		this.classLoader =  new GroovyClassLoader(parentLoader,config);

		try{
			GroovyCodeSource gcs = new GroovyCodeSource( this.scriptSource, "Script_"+this.scriptName, "/groovy/shell");
			this.scriptClazz = this.classLoader.parseClass(gcs,false);
		}catch(Throwable e){
			String msg = Utils.formatGroovyException(e,this.scriptSource);
			throw new RuntimeException("GroovyProcessor.parse("+scriptName+"):"+msg);
		}
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	private void callingStartStop( start) {
		info(this,"GroovyProcessor.callingStartStop("+start+","+this.scriptName+")");

		Thread.currentThread().setContextClassLoader(classLoader);//needed orientdb-groovy/OrientGraphHelper.groovy

		try{
			if( start ){
				this.scriptInstance.start();
			}else{
				this.scriptInstance.stop();
			}
		}catch(groovy.lang.MissingMethodException e){
			e.printStackTrace();
			Object[] args = e.getArguments();
			String a = "";
			String k = "";
			for(int i=0; i< args.length;i++){
				a += k+args[i];
				k = ",";
			}
			throw new RuntimeException("GroovyProcessor.callingStartStop("+this.scriptName+"):"+e.getMethod() + "("+ a + ") not found");
		}catch(Exception ex){
			ex.printStackTrace();
			String msg = Utils.formatGroovyException(ex,scriptSource);
			throw new RuntimeException("GroovyProcessor.callingStartStop("+this.scriptFile+"):"+msg );
		}
	}

	private void execute() {
		def env = [
			gitRepos: System.getProperty("git.repos"),
			simpl4Dir: System.getProperty("simpl4.dir"),
			homeDir: System.getProperty("git.repos")+ "/" + this.namespace,
			homeDataDir: System.getProperty("git.repos")+ "/" + this.namespace+"_data",
			namespace: this.namespace,
			hostname: InetAddress.getLocalHost().getHostName()
		]
		this.scriptInstance = 	this.scriptClazz.newInstance();
		if( endpoint.fieldExists(this.scriptClazz,"env")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "env", env );
		}
		if( endpoint.fieldExists(this.scriptClazz,"entityService")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "entityService", endpoint.lookupServiceByString( "org.ms123.common.entity.api.EntityService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"permissionService")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "permissionService", endpoint.lookupServiceByString( "org.ms123.common.permission.api.PermissionService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"processService")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "processService", endpoint.lookupServiceByString( "org.ms123.common.process.api.ProcessService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"authService")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "authService", endpoint.lookupServiceByString( "org.ms123.common.auth.api.AuthService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"dataLayer")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "dataLayer", endpoint.getDataLayer())
		}
		if( endpoint.fieldExists(this.scriptClazz,"dataLayerJdo")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "dataLayerJdo", endpoint.getDataLayerJdo())
		}
		if( endpoint.fieldExists(this.scriptClazz,"messageService")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "messageService", endpoint.lookupServiceByString( "org.ms123.common.message.MessageService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"settingService")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "settingService", endpoint.lookupServiceByString( "org.ms123.common.setting.api.SettingService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"registryService")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "registryService", endpoint.lookupServiceByString( "org.ms123.common.system.registry.RegistryService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"callService")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "callService", endpoint.lookupServiceByString( "org.ms123.common.rpc.CallService"))
		}
		if( endpoint.fieldExists(this.scriptClazz,"orientGraph")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "orientGraph", endpoint.getOrientGraph( this.namespace))
		}
		if( endpoint.fieldExists(this.scriptClazz,"orientGraphRoot")){
			endpoint.injectField( this.scriptClazz, this.scriptInstance, "orientGraphRoot", endpoint.getOrientGraphRoot( this.namespace))
		}
		callingStartStop(true);
	}
}
