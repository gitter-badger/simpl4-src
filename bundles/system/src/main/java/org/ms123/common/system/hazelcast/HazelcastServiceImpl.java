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
import org.ms123.common.system.orientdb.OrientDBService;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;


import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/** HazelcastService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=hazelcast" })
public class HazelcastServiceImpl  implements HazelcastService, MapStoreFactory{


	private static String HAZELCAST_DATABASE = "hzstore";
	private BundleContext bundleContext;
	private Map<String, HazelcastInstance> instances = new HashMap<String,HazelcastInstance>();
	private OrientDBService orientdbService;
	private OrientGraph orientGraph;


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
		this.initOrientdb();
		return new OrientDBMapStore(this.orientdbService,this.orientGraph,mapName);
	}


	private void initOrientdb() {
		if (orientGraph != null) {
			return;
		}
		try {
			OrientGraphFactory factory = this.orientdbService.getFactory(HAZELCAST_DATABASE);
			this.orientGraph = factory.getTx();
			createClassAndIndex();
		} catch (Exception e) {
			info(this,"OrientDBAccessImpl.initHistory:" + e.getMessage());
			orientGraph = null;
			e.printStackTrace();
		}
	}

	private void createClassAndIndex(){
		try{
			OSchemaProxy schema = this.orientGraph.getRawGraph().getMetadata().getSchema();
			if( schema.getClass("Store") != null){
				return;
			}
			orientdbService.executeUpdate(this.orientGraph, "CREATE CLASS Store EXTENDS V");
			orientdbService.executeUpdate(this.orientGraph, "CREATE PROPERTY Store.map STRING");
			orientdbService.executeUpdate(this.orientGraph, "CREATE PROPERTY Store.key STRING");
			orientdbService.executeUpdate(this.orientGraph, "CREATE INDEX Store.mapkey ON Store ( map,key ) UNIQUE");
			orientdbService.executeUpdate(this.orientGraph, "CREATE INDEX Store.map ON Store ( map ) NOTUNIQUE");
		}catch( Exception e){
			error(this, "createClassAndIndex:%[exception]s",e);
			e.printStackTrace();
		}
	}

	@Reference(dynamic = true, optional = true)
	public void setOrientDBService(OrientDBService os) {
		info(this,"HazelcastServiceImpl.setOrientDBService:" + os);
		this.orientdbService = os;
	}
}

