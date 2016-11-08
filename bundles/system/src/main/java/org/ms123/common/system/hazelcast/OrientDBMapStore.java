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
package org.ms123.common.system.hazelcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.MapStore;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.HazelcastInstance;
import org.ms123.common.system.orientdb.OrientDBService;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import org.ms123.common.system.orientdb.OrientDBService;


import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/** OrientDBMapStore implementation
 */
@SuppressWarnings("unchecked")
public class OrientDBMapStore  implements MapStore<String,Object> {

	private OrientDBService orientdbService;
	private OrientGraph orientGraph;
	private String mapName;

	public OrientDBMapStore(OrientDBService dbser, OrientGraph graph,String mapName) {
		this.orientGraph = graph;
		this.mapName = mapName;
		this.orientdbService = dbser;
	}

	public void store(final String key, final Object value) {
		info(this, "hazel.store("+key+"):"+value);
		try{
			this.orientdbService.executeUpdate(this.orientGraph, "UPDATE Store SET value = ?, map=?,key=? UPSERT WHERE map = ? and key = ?", value, this.mapName, key,this.mapName, key);
		}catch( Exception e){
			error(this, "store:%[exception]s",e);
			throw new RuntimeException("Hazelcast.store("+key+","+value+") failed");
		}
	}

	public void storeAll(final Map<String,Object> map) {
		info(this, "hazel.storeAll:"+map);
		for (Map.Entry<String, Object> entry : map.entrySet()) store(entry.getKey(), entry.getValue());
	}

	public void delete(final String key) {
		info(this, "hazel.delete("+key+")");
		try{
			this.orientdbService.executeUpdate(this.orientGraph, "DELETE VERTEX Store WHERE map = ? and key = ?", this.mapName, key);
		}catch( Exception e){
			error(this, "delete:%[exception]s",e);
			throw new RuntimeException("Hazelcast.delete("+key+") failed");
		}
	}

	public void deleteAll(final Collection<String> keys) {
		 for (String key : keys) delete(key);
	}

	public Object load(final String key) {
		OCommandRequest query = new OSQLSynchQuery("select value from Store where key=? and map=?");
		Iterable<Element> result = this.orientGraph.command(query).execute(key, this.mapName);
		for (Element elem : result) {
			Object value = elem.getProperty("value");
			info(this, "hazel.load("+key+"):"+value);
			return value;
		}
		info(this, "hazel.load("+key+"):null");
		return null;
	}

	public Map<String,Object> loadAll(final Collection<String> keys) {
		info(this, "hazel.loadAll:"+keys);
		Map<String, Object> result = new HashMap<String, Object>();
		for (String key : keys) result.put(key, load(key));
		return result;
	}

	public Set loadAllKeys() {
		info(this, "hazel.loadAllKey");
		return null;
	}


}

