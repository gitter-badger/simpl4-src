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
package org.ms123.common.camel.components.hazelcast;

import java.util.Collection;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

import org.apache.camel.Exchange;
import org.apache.camel.component.hazelcast.HazelcastComponentHelper;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.apache.camel.component.hazelcast.HazelcastDefaultProducer;
import org.ms123.common.camel.api.ExchangeUtils;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;

@SuppressWarnings({ "unchecked", "deprecation" })
public class HazelcastMapProducer extends HazelcastDefaultProducer implements HazelcastConstantsOwn{

	private IMap<Object, Object> cache;
	private HazelcastMapEndpoint endpoint;
	private String objectId;
	private String source;
	private String destination;
	private String cacheName;;
	private HazelcastInstance hazelcastInstance;

	public HazelcastMapProducer(HazelcastInstance hazelcastInstance, HazelcastMapEndpoint endpoint, String cacheName) {
		super(endpoint);
		this.cacheName = cacheName;
		this.hazelcastInstance = hazelcastInstance;
		this.endpoint = endpoint;
		this.objectId = endpoint.parameters.get(OBJECT_ID);
		this.source = endpoint.parameters.get(SOURCE);
		this.destination = endpoint.parameters.get(DESTINATION);
	}

	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headers = exchange.getIn().getHeaders();

		String query = null;

		if (headers.containsKey(HazelcastConstants.QUERY)) {
			query = (String) headers.get(HazelcastConstants.QUERY);
		}


		String oid = ExchangeUtils.getParameter(this.objectId, exchange, String.class, OPERATION);


		String soperation = endpoint.parameters.get(OPERATION);
		debug(this, "OID:" + oid+"\tOperation:"+soperation);
		final int operation = Integer.parseInt(soperation);
		switch (operation) {

		case HazelcastConstants.PUT_OPERATION:
			this.put(oid, exchange);
			break;

		case HazelcastConstants.GET_OPERATION:
			this.get(oid, exchange);
			break;

		case HazelcastConstants.DELETE_OPERATION:
			this.delete(oid);
			break;

		case HazelcastConstants.UPDATE_OPERATION:
			this.update(oid, exchange);
			break;

		case HazelcastConstants.QUERY_OPERATION:
			this.query(query, exchange);
			break;

		default:
			throw new IllegalArgumentException(String.format("The value '%s' is not allowed for parameter '%s' on the MAP cache.", operation, HazelcastConstants.OPERATION));
		}

		// finally copy headers
		HazelcastComponentHelper.copyHeaders(exchange);

	}

	private IMap<Object, Object> getCache(){
		if( this.cache == null){
			this.cache = this.hazelcastInstance.getMap(this.cacheName);
		}
		debug(this,"HazelcastMapProducer.hazelcastInstance:"+this.hazelcastInstance);
		return this.cache;
	}
	/**
	 * query map with a sql like syntax (see http://www.hazelcast.com/)
	 */
	private void query(String query, Exchange exchange) {
		Collection<Object> result = getCache().values(new SqlPredicate(query));
		exchange.getOut().setBody(result);
	}

	/**
	 * update an object in your cache (the whole object will be replaced)
	 */
	private void update(Object oid, Exchange exchange) {
		Object obj = ExchangeUtils.getSource(this.source, exchange, Object.class);
		info(this,"Update("+oid+").obj:"+obj);
		getCache().lock(oid);
		getCache().replace(oid, obj);
		getCache().unlock(oid);
	}

	/**
	 * remove an object from the cache
	 */
	private void delete(Object oid) {
		getCache().remove(oid);
	}

	/**
	 * find an object by the given id and give it back
	 */
	private void get(Object oid, Exchange exchange) {
		Object obj = getCache().get(oid);
		info(this,"Get("+oid+").obj:"+obj);
		ExchangeUtils.setDestination(this.destination,obj , exchange);
	}

	/**
	 * put a new object into the cache
	 */
	private void put(Object oid, Exchange exchange) {
		Object obj = ExchangeUtils.getSource(this.source, exchange, Object.class);
		info(this,"Put("+oid+").obj:"+obj);
		getCache().put(oid, obj);
	}
}

