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
package org.ms123.common.camel.components.hazelcast;

import java.util.Map;
import java.util.HashMap;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.hazelcast.atomicnumber.HazelcastAtomicnumberEndpoint;
import org.apache.camel.component.hazelcast.instance.HazelcastInstanceEndpoint;
import org.apache.camel.component.hazelcast.list.HazelcastListEndpoint;
//import org.apache.camel.component.hazelcast.map.HazelcastMapEndpoint;
import org.apache.camel.component.hazelcast.multimap.HazelcastMultimapEndpoint;
import org.apache.camel.component.hazelcast.queue.HazelcastQueueEndpoint;
import org.apache.camel.component.hazelcast.seda.HazelcastSedaConfiguration;
import org.apache.camel.component.hazelcast.seda.HazelcastSedaEndpoint;
import org.apache.camel.component.hazelcast.topic.HazelcastTopicEndpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.apache.camel.component.hazelcast.HazelcastCommand;
import org.apache.camel.component.hazelcast.HazelcastComponentHelper;
import org.apache.camel.component.hazelcast.HazelcastDefaultEndpoint;
import org.ms123.common.system.hazelcast.HazelcastService;
import static com.jcabi.log.Logger.info;

import static org.apache.camel.util.ObjectHelper.removeStartingCharacters;

public class HazelcastComponent extends UriEndpointComponent implements HazelcastConstantsOwn{

	private final HazelcastComponentHelper helper = new HazelcastComponentHelper();

	private HazelcastInstance hazelcastInstance;
	private boolean createOwnInstance;
	private CamelContext camelContext;

	public HazelcastComponent() {
		super(HazelcastDefaultEndpoint.class);
	}

	public HazelcastComponent(final CamelContext context) {
		super(context, HazelcastDefaultEndpoint.class);
		info(this,"HazelcastComponent");
		this.camelContext=context;
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {

		// Query param named 'hazelcastInstance' (if exists) overrides the instance that was set
		// programmatically and cancels local instance creation as well.
		HazelcastInstance hzInstance = resolveAndRemoveReferenceParameter(parameters, "hazelcastInstance", HazelcastInstance.class);
		// Now we use the hazelcastInstance from component
		if (hzInstance == null) {
			hzInstance = hazelcastInstance;
		}

		String operation = getAndRemoveOrResolveReferenceParameter(parameters, HazelcastConstants.OPERATION_PARAM, String.class);
		String objectId = getAndRemoveOrResolveReferenceParameter(parameters, OBJECT_ID, String.class);
		String source = getAndRemoveOrResolveReferenceParameter(parameters, SOURCE, String.class);
		String sql = getAndRemoveOrResolveReferenceParameter(parameters, SQL, String.class);
		String destination = getAndRemoveOrResolveReferenceParameter(parameters, DESTINATION, String.class);

		Map<String, String> params = new HashMap<String, String>();
		params.put(OBJECT_ID, objectId);
		params.put(SOURCE, source);
		params.put(SQL, sql);
		params.put(DESTINATION, destination);
		params.put(OPERATION, operation);

		info(this, "creation HazelcastEndpoint:" + parameters);
		info(this, "creation HazelcastEndpoint:" + remaining);
		HazelcastDefaultEndpoint endpoint = null;

		// check type of endpoint
		if (remaining.startsWith(HazelcastConstants.MAP_PREFIX)) {
			// remaining is the cache name
			remaining = removeStartingCharacters(remaining.substring(HazelcastConstants.MAP_PREFIX.length()), '/');
			endpoint = new HazelcastMapEndpoint(hzInstance, uri, remaining, params, this);
			endpoint.setCommand(HazelcastCommand.map);
		}

		/*        if (remaining.startsWith(HazelcastConstants.MULTIMAP_PREFIX)) {
		 // remaining is the cache name
		 remaining = removeStartingCharacters(remaining.substring(HazelcastConstants.MULTIMAP_PREFIX.length()), '/');
		 endpoint = new HazelcastMultimapEndpoint(hzInstance, uri, remaining, this);
		 endpoint.setCommand(HazelcastCommand.multimap);
		 }

		 if (remaining.startsWith(HazelcastConstants.ATOMICNUMBER_PREFIX)) {
		 // remaining is the name of the atomic value
		 remaining = removeStartingCharacters(remaining.substring(HazelcastConstants.ATOMICNUMBER_PREFIX.length()), '/');
		 endpoint = new HazelcastAtomicnumberEndpoint(hzInstance, uri, this, remaining);
		 endpoint.setCommand(HazelcastCommand.atomicvalue);
		 }

		 if (remaining.startsWith(HazelcastConstants.INSTANCE_PREFIX)) {
		 // remaining is anything (name it foo ;)
		 remaining = removeStartingCharacters(remaining.substring(HazelcastConstants.INSTANCE_PREFIX.length()), '/');
		 endpoint = new HazelcastInstanceEndpoint(hzInstance, uri, this);
		 endpoint.setCommand(HazelcastCommand.instance);
		 }

		 if (remaining.startsWith(HazelcastConstants.QUEUE_PREFIX)) {
		 // remaining is anything (name it foo ;)
		 remaining = removeStartingCharacters(remaining.substring(HazelcastConstants.QUEUE_PREFIX.length()), '/');
		 endpoint = new HazelcastQueueEndpoint(hzInstance, uri, this, remaining);
		 endpoint.setCommand(HazelcastCommand.queue);
		 }

		 if (remaining.startsWith(HazelcastConstants.TOPIC_PREFIX)) {
		 // remaining is anything (name it foo ;)
		 remaining = removeStartingCharacters(remaining.substring(HazelcastConstants.TOPIC_PREFIX.length()), '/');
		 endpoint = new HazelcastTopicEndpoint(hzInstance, uri, this, remaining);
		 endpoint.setCommand(HazelcastCommand.topic);
		 }

		 if (remaining.startsWith(HazelcastConstants.SEDA_PREFIX)) {
		 final HazelcastSedaConfiguration config = new HazelcastSedaConfiguration();
		 setProperties(config, parameters);
		 config.setQueueName(remaining.substring(remaining.indexOf(":") + 1, remaining.length()));

		 endpoint = new HazelcastSedaEndpoint(hzInstance, uri, this, config);
		 endpoint.setCommand(HazelcastCommand.seda);
		 }

		 if (remaining.startsWith(HazelcastConstants.LIST_PREFIX)) {
		 // remaining is anything (name it foo ;)
		 remaining = removeStartingCharacters(remaining.substring(HazelcastConstants.LIST_PREFIX.length()), '/');
		 endpoint = new HazelcastListEndpoint(hzInstance, uri, this, remaining);
		 endpoint.setCommand(HazelcastCommand.list);
		 }*/

		if (endpoint == null) {
			throw new IllegalArgumentException(String.format("Your URI does not provide a correct 'type' prefix. It should be anything like 'hazelcast:[%s|%s|%s|%s|%s|%s|%s]name' but is '%s'.", HazelcastConstants.MAP_PREFIX, HazelcastConstants.MULTIMAP_PREFIX, HazelcastConstants.ATOMICNUMBER_PREFIX, HazelcastConstants.INSTANCE_PREFIX, HazelcastConstants.QUEUE_PREFIX, HazelcastConstants.SEDA_PREFIX, HazelcastConstants.LIST_PREFIX, uri));
		}

		return endpoint;
	}

	@Override
	public void doStart() throws Exception {
		super.doStart();
		info(this, "startHazelcast:"+this.hazelcastInstance);
		this.hazelcastInstance = createInstance();
	}

	@Override
	public void doStop() throws Exception {
		super.doStop();
	}

	public HazelcastService getHazelcastService() {
		return getByType(getCamelContext(), HazelcastService.class);
	}

	private <T> T getByType(CamelContext ctx, Class<T> kls) {
		return kls.cast(ctx.getRegistry().lookupByName(kls.getName()));
	}
	public HazelcastInstance getHazelcastInstance() {
		return this.hazelcastInstance;
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	private HazelcastInstance createInstance() {
		HazelcastService hazelcastService = getHazelcastService();
		info(this, "doStart:hazelcastService:"+hazelcastService);
		HazelcastInstance hi = hazelcastService.getInstance("default");
		info(this, "doStart:createOwnInstance:"+hi);
		return hi;
	}
}

