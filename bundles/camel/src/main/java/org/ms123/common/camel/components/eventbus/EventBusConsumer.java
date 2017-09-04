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

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.apache.camel.Message;
import org.osgi.framework.ServiceRegistration;
import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.helpers.MessageFormatter;
import java.util.Map;

@SuppressWarnings({"unchecked","deprecation"})
public class EventBusConsumer extends DefaultConsumer implements EventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(EventBusConsumer.class);
	private final EventBusEndpoint endpoint;
	private ServiceRegistration m_register;

	public EventBusConsumer(EventBusEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	public void handleEvent(Event event) {
		debug("HandleEvent.onEvent {}", event);
		final boolean reply = false;
		final Exchange exchange = endpoint.createExchange(reply ? ExchangePattern.InOut : ExchangePattern.InOnly);
		exchange.setIn((Message) event.getProperty("msg"));
		try {
			getAsyncProcessor().process(exchange, new AsyncCallback() {

				@Override
				public void done(boolean doneSync) {
				}
			});
		} catch (Exception e) {
			getExceptionHandler().handleException("Error processing EventAdmin event: " + event, exchange, e);
		}
	}

	protected void doStart() throws Exception {
		debug("Registering EventHandler handler on address {}", endpoint.getAddress());
		String[] topics = new String[] { "eventbus/" + endpoint.getAddress() };
		Dictionary d = new Hashtable();
		d.put(EventConstants.EVENT_TOPIC, topics);
		m_register = endpoint.getBundleContext().registerService(EventHandler.class.getName(), this, d);
		super.doStart();
	}

	protected void doStop() throws Exception {
		debug("Unregistering EventBus handler on address {}", endpoint.getAddress());
		m_register.unregister();
		super.doStop();
	}

	protected void debug(String msg, Object... args) {
		System.out.println(MessageFormatter.arrayFormat(msg, varargsToArray(args)).getMessage());
		LOG.debug(msg, args);
	}

	protected void info(String msg, Object... args) {
		System.out.println(MessageFormatter.arrayFormat(msg, varargsToArray(args)).getMessage());
		LOG.info(msg, args);
	}

	private Object[] varargsToArray(Object... args) {
		Object[] ret = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			ret[i] = args[i];
		}
		return ret;
	}
}
