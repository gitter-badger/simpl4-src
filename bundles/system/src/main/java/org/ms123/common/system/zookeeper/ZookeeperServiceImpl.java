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
package org.ms123.common.system.zookeeper;


import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;


/** ZookeeperService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=zookeeper" })
public class ZookeeperServiceImpl extends ZooKeeperServerMain implements ZookeeperService {
	private Thread thread;
	private ServerConfig config;

	private BundleContext bundleContext;

	public ZookeeperServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bundleContext = bundleContext;
		try {
			info(this,"ZookeeperService activate");
			config = getConfig();
			thread = new Thread(this::zk, "org.simpl4.addons.zookeeper");
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
			error(this, "ZookeeperServiceImpl.activate.error:%[exception]s", e);
		}
	}

	public void update(Map<String, Object> props) {
		info(this, "ZookeeperServiceImpl.updated:" + props);
	}

	protected void deactivate() throws Exception {
		info(this, "ZookeeperServiceImpl.deactivate");
		shutdown();
		thread.interrupt();
	}


	private ServerConfig getConfig() {
		Properties properties = new Properties();
		try {
			properties.load(new FileReader("../etc/zookeeper.properties"));
		} catch (Exception e) {
			throw new RuntimeException("ZookeeperServiceImpl.getProperties:", e);
		}
		QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
		try {
			quorumConfiguration.parseProperties(properties);
		} catch (Exception e) {
			throw new RuntimeException("ZookeeperServiceImpl.getConfig:", e);
		}
		ServerConfig config = new ServerConfig();
		config.readFrom(quorumConfiguration);
		return config;
	}

	private void zk() {
		try {
			info(this,"ZookeeperService starting");
			runFromConfig(config);
		} catch (Exception e) {
			e.printStackTrace();
			error(this, "ZookeeperServiceImpl.zk.error:%[exception]s", e);
		}
		info(this,"ZookeeperService exiting");
	}

}

