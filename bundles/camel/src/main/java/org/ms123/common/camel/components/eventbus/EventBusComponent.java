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

import java.util.List;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.ComponentConfiguration;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.spi.EndpointCompleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
public class EventBusComponent extends UriEndpointComponent implements EndpointCompleter {

	private static final Logger LOG = LoggerFactory.getLogger(EventBusComponent.class);

	private String host;
	private int port;

	public EventBusComponent() {
		super(EventBusEndpoint.class);
	}

	public EventBusComponent(CamelContext context) {
		super(context, EventBusEndpoint.class);
	}

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		EventBusEndpoint endpoint = new EventBusEndpoint(uri, this, remaining);
		setProperties(endpoint, parameters);
		return endpoint;
	}

	public List<String> completeEndpointPath(ComponentConfiguration componentConfiguration, String text) {
		return null;
	}

	@Override
	protected void doStart() throws Exception {
		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
	}
}
