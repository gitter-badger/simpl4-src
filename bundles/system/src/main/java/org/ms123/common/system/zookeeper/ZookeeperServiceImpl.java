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

