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
package org.ms123.common.camel.components.zookeeper;

import java.util.List;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedOperation;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.component.zookeeper.ZooKeeperConnectionManager;
//import org.apache.camel.component.zookeeper.ZooKeeperProducer;
import org.apache.camel.component.zookeeper.ZooKeeperConsumer;

/**
 * The zookeeper component allows interaction with a ZooKeeper cluster.
 */
@ManagedResource(description = "ZooKeeper Endpoint")
@UriEndpoint(scheme = "zookeeper", title = "ZooKeeper", syntax = "zookeeper:serverUrls/path", consumerClass = ZooKeeperConsumer.class, label = "clustering")
public class ZooKeeperEndpoint extends org.apache.camel.component.zookeeper.ZooKeeperEndpoint {
    @UriParam
    private ZooKeeperConfiguration configuration;
    private ZooKeeperConnectionManager connectionManager;

    public ZooKeeperEndpoint(String uri, ZooKeeperComponent component, ZooKeeperConfiguration configuration) {
        super(uri, component,configuration);
        this.configuration = configuration;
        this.connectionManager = new ZooKeeperConnectionManager(this);
    }
    ZooKeeperConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public Producer createProducer() throws Exception {
        return new ZooKeeperProducer(this);
    }

    public void setConfiguration(ZooKeeperConfiguration configuration) {
        this.configuration = configuration;
    }

    public ZooKeeperConfiguration getConfiguration() {
        return configuration;
    }
    public void setMode(String mode) {
        getConfiguration().setMode(mode);
    }

    @ManagedAttribute
    public String getMode() {
        return getConfiguration().getMode();
    }

    public void setSource(String source) {
        getConfiguration().setSource(source);
    }

    @ManagedAttribute
    public String getSource() {
        return getConfiguration().getSource();
    }
    public void setDestination(String source) {
        getConfiguration().setDestination(source);
    }

    @ManagedAttribute
    public String getDestination() {
        return getConfiguration().getDestination();
    }

}
