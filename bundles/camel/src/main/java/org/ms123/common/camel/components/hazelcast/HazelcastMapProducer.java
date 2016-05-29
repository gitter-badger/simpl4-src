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

@SuppressWarnings({ "unchecked", "deprecation" })
public class HazelcastMapProducer extends HazelcastDefaultProducer implements HazelcastConstantsOwn{

	private final IMap<Object, Object> cache;
	private HazelcastMapEndpoint endpoint;
	private String objectId;
	private String source;
	private String destination;

	public HazelcastMapProducer(HazelcastInstance hazelcastInstance, HazelcastMapEndpoint endpoint, String cacheName) {
		super(endpoint);
		this.cache = hazelcastInstance.getMap(cacheName);
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
		info(this, "OID:" + oid);


		String soperation = endpoint.parameters.get(OPERATION);
		info(this, "OPERATION:" + soperation);
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

	/**
	 * query map with a sql like syntax (see http://www.hazelcast.com/)
	 */
	private void query(String query, Exchange exchange) {
		Collection<Object> result = this.cache.values(new SqlPredicate(query));
		exchange.getOut().setBody(result);
	}

	/**
	 * update an object in your cache (the whole object will be replaced)
	 */
	private void update(Object oid, Exchange exchange) {
		Object body = exchange.getIn().getBody();
		this.cache.lock(oid);
		this.cache.replace(oid, body);
		this.cache.unlock(oid);
	}

	/**
	 * remove an object from the cache
	 */
	private void delete(Object oid) {
		this.cache.remove(oid);
	}

	/**
	 * find an object by the given id and give it back
	 */
	private void get(Object oid, Exchange exchange) {
		Object obj = this.cache.get(oid);
		info(this,"Get("+oid+").obj:"+obj);
		ExchangeUtils.setDestination(this.destination,obj , exchange);
	}

	/**
	 * put a new object into the cache
	 */
	private void put(Object oid, Exchange exchange) {
		Object obj = ExchangeUtils.getSource(this.source, exchange, Object.class);
		info(this,"Put("+oid+").obj:"+obj);
		this.cache.put(oid, obj);
	}
}

