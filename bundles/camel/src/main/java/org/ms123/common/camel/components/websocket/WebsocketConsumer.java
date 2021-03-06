/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ms123.common.camel.components.websocket;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import java.util.Map;
import java.util.HashMap;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.CloseStatus;
import flexjson.*;

@SuppressWarnings({"unchecked","deprecation"})
public class WebsocketConsumer extends DefaultConsumer {

	private final WebsocketEndpoint endpoint;
	private JSONSerializer m_js = new JSONSerializer();

	public WebsocketConsumer(WebsocketEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	@Override
	public void start() throws Exception {
		super.start();
		endpoint.setConsumer(this);
	}

	@Override
	public void stop() throws Exception {
		endpoint.setConsumer(this);
		super.stop();
	}

	public void sendMessage(final String connectionKey, final Object body, Map<String, String> headers, final Session session) {
		final Exchange exchange = getEndpoint().createExchange();
		if (body != null) {
			exchange.getIn().setBody(body);
		}
		if (headers != null) {
			exchange.getIn().getHeaders().putAll(headers);
		}
		exchange.getIn().setHeader(WebsocketConstants.CONNECTION_KEY, connectionKey);
		// send exchange using the async routing engine
		getAsyncProcessor().process(exchange, new AsyncCallback() {

			public void done(boolean doneSync) {
				if (exchange.getException() != null) {
					getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
					Throwable e = exchange.getException();
					e.printStackTrace();
					String msg = e.getMessage();
					while (e.getCause() != null) {
						e = e.getCause();
						msg = e.getMessage();
					}
					if (msg == null) {
						msg = e.toString();
					}
					Map map = new HashMap();
					map.put("request", body);
					map.put("errorMessage", msg);
					String sendString = m_js.deepSerialize(map);
					session.getRemote().sendStringByFuture(sendString);
				}
			}
		});
	}
}
