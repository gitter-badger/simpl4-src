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
package org.ms123.common.camel.components.scpevent;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.ms123.common.system.ssh.SshService;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.BundleContext;
import org.apache.camel.core.osgi.utils.BundleContextUtils;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
@SuppressWarnings({ "unchecked", "deprecation" })
@UriEndpoint(scheme = "scpevent", title = "ScpEvent", syntax = "scpevent:pattern", consumerClass = ScpEventConsumer.class)
public class ScpEventEndpoint extends DefaultEndpoint {

	private SshService sshService;
	private BundleContext bundleContext;

	@UriParam
	private String filepattern;

	@UriParam
	private String fileDestination;

	@UriParam
	private String vfsrootDestination;

	public ScpEventEndpoint(String uri, ScpEventComponent component, String remaining) {
		super(uri, component);
		//		this.filepattern = filepattern;
		this.bundleContext = BundleContextUtils.getBundleContext(ScpEventEndpoint.class);
		info(this, "bundleContext:" + this.bundleContext);
		this.sshService = lookupServiceByClass(SshService.class);
		info(this, "SshService:" + this.sshService);
	}

	@Override
	public ScpEventComponent getComponent() {
		return (ScpEventComponent) super.getComponent();
	}

	public Producer createProducer() throws Exception {
		return new ScpEventProducer(this);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		ScpEventConsumer consumer = new ScpEventConsumer(this, processor, this.sshService);
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
			throw new RuntimeException("EventBusEndpoint.Cannot resolve service:" + clazz);
		}
		return service;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getFileDestination() {
		return this.fileDestination;
	}

	public void setFileDestination(String fileDestination) {
		this.fileDestination = fileDestination;
	}

	public String getVfsRootDestination() {
		return this.vfsrootDestination;
	}

	public void setVfsRootDestination(String v) {
		this.vfsrootDestination = v;
	}

	public String getFilePattern() {
		return this.filepattern;
	}

	public void setFilePattern(String filepattern) {
		this.filepattern = filepattern;
	}
}

