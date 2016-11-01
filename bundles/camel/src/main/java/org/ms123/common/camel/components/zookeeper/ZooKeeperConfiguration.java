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

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.apache.camel.spi.UriPath;
import org.apache.camel.util.CollectionStringBuffer;

/**
 * <code>ZookeeperConfiguration</code> encapsulates the configuration used to
 * interact with a ZooKeeper cluster. Most typically it is parsed from endpoint
 * uri but may also be configured programatically and applied to a
 * {@link ZooKeeperComponent}. A copy of this component's configuration will be
 * injected into any {@link ZooKeeperEndpoint}s the component creates.
 */
@UriParams
public class ZooKeeperConfiguration extends org.apache.camel.component.zookeeper.ZooKeeperConfiguration implements Cloneable {

    @UriPath @Metadata(required = "true")
    private String mode;
    private String source;
    private String destination;

    public ZooKeeperConfiguration copy() {
        try {
            return (ZooKeeperConfiguration)clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeCamelException(e);
        }
    }
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
    public String getSource() {
        return source;
    }

    public void setSource(String s) {
        this.source = s;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String d) {
        this.destination = d;
    }
}
