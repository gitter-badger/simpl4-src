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

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Properties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.MapStoreFactory;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapLoader;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.HazelcastInstance;


import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/** HazelcastService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=hazelcast" })
public class HazelcastServiceImpl  implements HazelcastService, MapStoreFactory{


	private BundleContext bundleContext;
	private Map<String, HazelcastInstance> instances = new HashMap<String,HazelcastInstance>();


	public HazelcastServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bundleContext=bundleContext;
	}

	public void update(Map<String, Object> props) {
		info(this,"HazelcastServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this,"HazelcastServiceImpl.deactivate");
	}


	public synchronized HazelcastInstance getInstance(String name){
		HazelcastInstance hi = instances.get(name);
		if( hi == null){
			hi = createInstance();
			instances.put(name,hi);
		}
		return hi;
	}

	private HazelcastInstance createInstance() {
		Config config = new XmlConfigBuilder().build();
		NetworkConfig nconfig = config.getNetworkConfig();
		JoinConfig join = nconfig.getJoin();
		join.getMulticastConfig().setEnabled(false);
		join.getTcpIpConfig().setEnabled(false);
		config.getProperties().setProperty("hazelcast.version.check.enabled", "false");

		MapConfig mapCfg = config.getMapConfig("p-*");
		MapStoreConfig mapStoreCfg = new MapStoreConfig();
		mapStoreCfg.setEnabled(true);
		mapStoreCfg.setWriteDelaySeconds(0);
		mapStoreCfg.setFactoryImplementation( this);
		mapCfg.setMapStoreConfig(mapStoreCfg);

		HazelcastInstance hi = Hazelcast.newHazelcastInstance(config);
		info(this, "createInstance:"+hi);
		return hi;
	}

	public MapLoader newMapStore(String mapName, Properties properties) {
		info(this, "newMapStore:"+mapName+"/"+properties);
		return new OrientDBMapStore();
	}
}

