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

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.apache.camel.CamelContext;
import org.apache.camel.ComponentConfiguration;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.spi.EndpointCompleter;
import static com.jcabi.log.Logger.info;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
public class ScriptConsumerComponent extends UriEndpointComponent implements EndpointCompleter {

	private String host;
	private int port;

	public ScriptConsumerComponent() {
		super(ScriptConsumerEndpoint.class);
	}

	public ScriptConsumerComponent(CamelContext context) {
		super(context, ScriptConsumerEndpoint.class);
	}

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		ScriptConsumerEndpoint endpoint = new ScriptConsumerEndpoint(uri, this, new HashMap(parameters));
		parameters.keySet().removeIf(key-> 
			!(key.equals("scriptfile") || key.equals("namespace"))
		);
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
