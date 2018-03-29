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

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.io.File;
import java.net.URL;
import org.apache.camel.Consumer;
import org.apache.camel.core.osgi.utils.BundleContextUtils;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.git.GitService;
import org.ms123.common.system.orientdb.OrientDBService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
@UriEndpoint(scheme = "scriptConsumer", title = "ScriptConsumer", syntax = "scriptConsumer:topic", consumerClass = ScriptConsumerConsumer.class)
public class ScriptConsumerEndpoint extends DefaultEndpoint {

	@UriParam
	private String scriptfile;
	private String namespace;

	private BundleContext bundleContext;

	public ScriptConsumerEndpoint(String uri, ScriptConsumerComponent component, String address) {
		super(uri, component);
		bundleContext = BundleContextUtils.getBundleContext(ScriptConsumerEndpoint.class);
	}

	@Override
	public ScriptConsumerComponent getComponent() {
		return (ScriptConsumerComponent) super.getComponent();
	}

	public Producer createProducer() throws Exception {
		//return new ScriptConsumerProducer(this);
		throw new RuntimeException("ScriptConsumerEndpoint.createProducer: not implemented");
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		ScriptConsumerConsumer consumer = new ScriptConsumerConsumer(this, processor);
		configureConsumer(consumer);
		return consumer;
	}

	public <T> T lookupServiceByClass(Class<T> clazz) {
		T service = null;
		ServiceReference sr = bundleContext.getServiceReference(clazz);
		if (sr != null) {
			service = (T) bundleContext.getService(sr);
		}
		if (service == null) {
			throw new RuntimeException("ScriptConsumerEndpoint.Cannot resolve service:" + clazz);
		}
		return service;
	}

	public Object lookupServiceByString(String clazz) {
		Object service = null;
		ServiceReference sr = bundleContext.getServiceReference(clazz);
		if (sr != null) {
			service = bundleContext.getService(sr);
		}
		if (service == null) {
			throw new RuntimeException("ScriptConsumerEndpoint.Cannot resolve service:" + clazz);
		}
		return service;
	}

	protected DataLayer getDataLayer() throws Exception{
		ServiceReference[] srList	=	bundleContext.getServiceReferences("org.ms123.common.data.api.DataLayer", "(kind=orientdb)");
		DataLayer dataLayer = null;
		if (srList != null && srList.length>=1) {
			dataLayer = (DataLayer)bundleContext.getService(srList[0]);
		}
		if (dataLayer == null) {
			throw new RuntimeException("ScriptConsumerEndpoint.Cannot resolve service:org.ms123.common.camel.api.DataLayer(Orientdb)");
		}
		return dataLayer;
	}
	protected DataLayer getDataLayerJdo() throws Exception{
		ServiceReference[] srList	=	bundleContext.getServiceReferences("org.ms123.common.data.api.DataLayer", "(kind=jdo)");
		DataLayer dataLayer = null;
		if (srList != null && srList.length>=1) {
			dataLayer = (DataLayer)bundleContext.getService(srList[0]);
		}
		if (dataLayer == null) {
			throw new RuntimeException("ScriptConsumerEndpoint.Cannot resolve service:org.ms123.common.camel.api.DataLayer(JDO)");
		}
		return dataLayer;
	}

	protected OrientGraph getOrientGraph(String namespace){
		OrientDBService oService=null;
		ServiceReference sr = bundleContext.getServiceReference("org.ms123.common.system.orientdb.OrientDBService");
		if (sr != null) {
			oService = (OrientDBService)bundleContext.getService(sr);
		}
		if (oService == null) {
			throw new RuntimeException("ScriptConsumerEndpoint.Cannot resolve OrientDBService");
		}
		return oService.getOrientGraph(namespace);
	}
	protected OrientGraph getOrientGraphRoot(String namespace){
		OrientDBService oService=null;
		ServiceReference sr = bundleContext.getServiceReference("org.ms123.common.system.orientdb.OrientDBService");
		if (sr != null) {
			oService = (OrientDBService)bundleContext.getService(sr);
		}
		if (oService == null) {
			throw new RuntimeException("ScriptConsumerEndpoint.Cannot resolve OrientDBService");
		}
		return oService.getOrientGraphRoot(namespace);
	}

	public boolean isSingleton() {
		return true;
	}
  protected boolean  fieldExists(Class clazz, String fieldname) {
    try{
      clazz.getDeclaredField(fieldname);
      return true;
    }catch(Exception e){
      return false;
    }
  }

	protected void  injectField(Class clazz,Object obj, String fieldname, Object value) {
		try{
			java.lang.reflect.Field field = clazz.getDeclaredField(fieldname);
			field.setAccessible(true);
			field.set(obj, value );
		}catch(Exception e){
		}
	}

	protected URL[] getClassPath(String namespace) throws Exception{
		URL url1 = 	new URL( "file:"+System.getProperty("workspace") + "/java/" + namespace+"/");
		URL url2 = 	new URL( "file:"+System.getProperty("workspace") + "/java/global/");
		URL url3 = 	new URL( "file:"+System.getProperty("workspace") + "/groovy/" + namespace+"/");
		URL url4 = 	new URL( "file:"+System.getProperty("git.repos") + "/"+namespace+"/.etc/jooq/build/");
		URL[] urls = new URL[4];
		urls[0] = url1;
		urls[1] = url2;
		urls[2] = url3;
		urls[3] = url4;
		return urls;
	}

	protected File getRepoFile(String namespace,String name){
		GitService gitService=null;
		ServiceReference sr = bundleContext.getServiceReference("org.ms123.common.git.GitService");
		if (sr != null) {
			gitService = (GitService)bundleContext.getService(sr);
		}
		if (gitService == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve GitService");
		}
		return gitService.searchFile(namespace, name, "sw.groovy");
	}
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public File getScriptfile() {
		return getRepoFile(getNamespace(),this.scriptfile);
	}

	public void setScriptfile(String scriptfile) {
		this.scriptfile = scriptfile;
	}
	public void setNamespace(String ns) {
		this.namespace = ns;
	}
	public String getNamespace() {
		return this.namespace;
	}
}
