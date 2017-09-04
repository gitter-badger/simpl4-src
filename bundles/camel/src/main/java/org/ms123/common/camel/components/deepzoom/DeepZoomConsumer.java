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
package org.ms123.common.camel.components.deepzoom;

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
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings({ "unchecked", "deprecation" })
public class DeepZoomConsumer extends DefaultConsumer implements EventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DeepZoomConsumer.class);
	private final DeepZoomEndpoint endpoint;
	private ServiceRegistration m_register;

	public DeepZoomConsumer(DeepZoomEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	public void handleEvent(Event event) {
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
		super.doStart();
	}

	protected void doStop() throws Exception {
		super.doStop();
	}

}

