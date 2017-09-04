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
package org.ms123.common.camel.components.eventbus;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.core.osgi.utils.BundleContextUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
@UriEndpoint(scheme = "eventbus", title = "Eventbus", syntax = "eventbus:topic", consumerClass = EventBusConsumer.class)
public class EventBusEndpoint extends DefaultEndpoint {

	@UriParam
	private String address;

	private BundleContext m_bundleContext;

	private EventAdmin m_eventAdmin;

	public EventBusEndpoint(String uri, EventBusComponent component, String address) {
		super(uri, component);
		this.address = address;
		m_bundleContext = BundleContextUtils.getBundleContext(EventBusEndpoint.class);
		m_eventAdmin = lookupServiceByClass(EventAdmin.class);
	}

	@Override
	public EventBusComponent getComponent() {
		return (EventBusComponent) super.getComponent();
	}

	public Producer createProducer() throws Exception {
		return new EventBusProducer(this);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		EventBusConsumer consumer = new EventBusConsumer(this, processor);
		configureConsumer(consumer);
		return consumer;
	}

	public <T> T lookupServiceByClass(Class<T> clazz) {
		T service = null;
		ServiceReference sr = m_bundleContext.getServiceReference(clazz);
		if (sr != null) {
			service = (T) m_bundleContext.getService(sr);
		}
		if (service == null) {
			throw new RuntimeException("EventBusEndpoint.Cannot resolve service:" + clazz);
		}
		return service;
	}

	public boolean isSingleton() {
		return true;
	}

	public EventAdmin getEventAdmin() {
		return m_eventAdmin;
	}

	public BundleContext getBundleContext() {
		return m_bundleContext;
	}

	public String getAddress() {
		return address;
	}

	/**
     * Sets the event bus address used to communicate
     */
	public void setAddress(String address) {
		this.address = address;
	}
}
