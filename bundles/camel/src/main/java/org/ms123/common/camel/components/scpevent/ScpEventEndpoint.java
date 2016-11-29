/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
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
@SuppressWarnings({"unchecked","deprecation"})
@UriEndpoint(scheme = "scpevent", title = "ScpEvent", syntax = "scpevent:pattern", consumerClass = ScpEventConsumer.class)
public class ScpEventEndpoint extends DefaultEndpoint {

	private SshService sshService;
	private BundleContext bundleContext;

	@UriParam
	private String filepattern;

	@UriParam
	private String pathDestination;

	public ScpEventEndpoint(String uri, ScpEventComponent component, String remaining) {
		super(uri, component);
//		this.filepattern = filepattern;
		this.bundleContext = BundleContextUtils.getBundleContext(ScpEventEndpoint.class);
		info(this, "bundleContext:"+this.bundleContext);
		this.sshService = lookupServiceByClass(SshService.class);
		info(this, "SshService:"+this.sshService);
	}

	@Override
	public ScpEventComponent getComponent() {
		return (ScpEventComponent) super.getComponent();
	}

	public Producer createProducer() throws Exception {
		return new ScpEventProducer(this);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		ScpEventConsumer consumer = new ScpEventConsumer(this, processor,this.sshService);
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
	public String getPathDestination() {
		return this.pathDestination;
	}

	public void setPathDestination(String pathDestination) {
		this.pathDestination = pathDestination;
	}


	public String getFilepattern() {
		return this.filepattern;
	}

	public void setFilepattern(String filepattern) {
		this.filepattern = filepattern;
	}
}
