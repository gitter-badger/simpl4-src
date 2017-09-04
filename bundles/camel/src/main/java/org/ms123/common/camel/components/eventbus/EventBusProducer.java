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
import org.apache.camel.InvalidPayloadRuntimeException;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.Message;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked","deprecation"})
public class EventBusProducer extends DefaultAsyncProducer {

	private static final Logger LOG = LoggerFactory.getLogger(EventBusProducer.class);

	public EventBusProducer(EventBusEndpoint endpoint) {
		super(endpoint);
	}

	@Override
	public EventBusEndpoint getEndpoint() {
		return (EventBusEndpoint) super.getEndpoint();
	}

	@Override
	public boolean process(Exchange exchange, AsyncCallback callback) {
		String address = getEndpoint().getAddress();
		boolean reply = ExchangeHelper.isOutCapable(exchange);
		Message msg = exchange.hasOut() ? exchange.getOut() : exchange.getIn();
		Map properties = new HashMap();
		properties.put("msg", msg);
		debug("Producer.process:" + msg);
		Event event = new Event("eventbus/" + address, properties);
		getEndpoint().getEventAdmin().postEvent(event);
		return true;
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
