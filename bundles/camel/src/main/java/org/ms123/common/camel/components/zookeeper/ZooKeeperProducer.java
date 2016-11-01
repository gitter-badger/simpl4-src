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

import static java.lang.String.format;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.zookeeper.operations.CreateOperation;
import org.apache.camel.component.zookeeper.operations.DeleteOperation;
import org.apache.camel.component.zookeeper.operations.GetChildrenOperation;
import org.apache.camel.component.zookeeper.operations.OperationResult;
import org.apache.camel.component.zookeeper.operations.SetDataOperation;
import org.apache.camel.component.zookeeper.operations.GetDataOperation;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;

import static org.apache.camel.component.zookeeper.ZooKeeperUtils.getAclListFromMessage;
import static org.apache.camel.component.zookeeper.ZooKeeperUtils.getCreateMode;
import static org.apache.camel.component.zookeeper.ZooKeeperUtils.getCreateModeFromString;
import static org.apache.camel.component.zookeeper.ZooKeeperUtils.getNodeFromMessage;
import static org.apache.camel.component.zookeeper.ZooKeeperUtils.getPayloadFromExchange;
import static org.apache.camel.component.zookeeper.ZooKeeperUtils.getVersionFromMessage;
import org.apache.camel.component.zookeeper.ZooKeeperConnectionManager;
import org.apache.camel.component.zookeeper.ZooKeeperMessage;
import org.ms123.common.camel.api.ExchangeUtils;

/**
 * <code>ZooKeeperProducer</code> attempts to set the content of nodes in the
 * {@link ZooKeeper} cluster with the payloads of the of the exchanges it
 * receives.
 */
@SuppressWarnings("rawtypes")
public class ZooKeeperProducer extends DefaultProducer{
    public static final String ZK_OPERATION_WRITE  = "WRITE";
    public static final String ZK_OPERATION_GET  = "GET";
    public static final String ZK_OPERATION_DELETE = "DELETE";
    public static final String ZK_OPERATION_LIST = "LIST";

    private final ZooKeeperConfiguration configuration;
    private ZooKeeperConnectionManager zkm;
    private ZooKeeper connection;

    public ZooKeeperProducer(ZooKeeperEndpoint endpoint) {
        super(endpoint);
        this.configuration = endpoint.getConfiguration();
        this.zkm = endpoint.getConnectionManager();
    }

    public void process(Exchange exchange) throws Exception {

        ProductionContext context = new ProductionContext(connection, exchange, configuration.getMode());

        boolean isDelete = configuration.getMode().equals(ZK_OPERATION_DELETE);
        boolean isGet = configuration.getMode().equals(ZK_OPERATION_GET);
        boolean isList = configuration.getMode().equals(ZK_OPERATION_LIST);

        if (true/*ExchangeHelper.isOutCapable(exchange)*/) {
            if (isDelete) {
                if (log.isDebugEnabled()) {
                    log.debug(format("Deleting znode '%s', waiting for confirmation", context.node));
                }
                OperationResult result = synchronouslyDelete(context);
                updateExchangeWithResult(context, result);
            }else if (isList) {
                if (log.isDebugEnabled()) {
                    log.debug(format("List childs of znode '%s'", context.node));
                }
                OperationResult result = listChildren(context);
                updateExchangeWithResult(context, result);
            }else if (isGet) {
                if (log.isDebugEnabled()) {
                    log.debug(format("Get znode '%s', waiting for confirmation", context.node));
                }
                OperationResult result = synchronouslyGetData(context);
								log.info("result:"+result);
                updateExchangeWithResult(context, result);
            } else {
                if (log.isDebugEnabled()) {
                    log.info(format("Storing data to znode '%s', waiting for confirmation", context.node));
                }
                OperationResult result = synchronouslySetData(context);
                updateExchangeWithResult(context, result);
            }
        } else {
            if (isDelete) {
                asynchronouslyDeleteNode(connection, context);
            } else {
                asynchronouslySetDataOnNode(connection, context);
            }
        }

    }

    @Override
    protected void doStart() throws Exception {
        connection = zkm.getConnection();
        if (log.isTraceEnabled()) {
            log.trace(String.format("Starting zookeeper producer of '%s'", configuration.getPath()));
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (log.isTraceEnabled()) {
            log.trace(String.format("Shutting down zookeeper producer of '%s'", configuration.getPath()));
        }
        zkm.shutdown();
    }

    private void asynchronouslyDeleteNode(ZooKeeper connection, ProductionContext context) {
        if (log.isDebugEnabled()) {
            log.debug(format("Deleting node '%s', not waiting for confirmation", context.node));
        }
        connection.delete(context.node, context.version, new AsyncDeleteCallback(), context);

    }

    private void asynchronouslySetDataOnNode(ZooKeeper connection, ProductionContext context) {
        if (log.isDebugEnabled()) {
            log.debug(format("Storing data to node '%s', not waiting for confirmation", context.node));
        }
        connection.setData(context.node, context.payload, context.version, new AsyncSetDataCallback(), context);
    }

		private void updateExchangeWithResult(ProductionContext context, OperationResult result) {
			boolean isGet = configuration.getMode().equals(ZK_OPERATION_GET);
			boolean isWrite = configuration.getMode().equals(ZK_OPERATION_WRITE);
			log.info("isOk:"+result.isOk());
			if (result.isOk()) {
				log.info("updateExchangeWithResult("+result.isOk()+"):"+result.getResult()+"\tdest:"+configuration.getDestination());
				if( isGet || isWrite){
					Object obj = deserialize((byte[])result.getResult());
					ExchangeUtils.setDestination(configuration.getDestination(), obj, context.exchange);
				}else{
					ExchangeUtils.setDestination(configuration.getDestination(), result.getResult(), context.exchange);
				}
			} else {
				context.exchange.setException(result.getException());
			}
		}

    private OperationResult listChildren(ProductionContext context) throws Exception {
			String path  = ExchangeUtils.getParameter(configuration.getPath(), context.exchange, String.class, "path");
			return new GetChildrenOperation(context.connection, path).get();
    }

    /** Simple container to avoid passing all these around as parameters */
    private class ProductionContext {
        ZooKeeper connection;
        Exchange exchange;
        Message in;
        byte[] payload;
        int version;
        String node;

        ProductionContext(ZooKeeper connection, Exchange exchange, String mode) {
            this.connection = connection;
            this.exchange = exchange;
            this.in = exchange.getIn();
            //this.node = getNodeFromMessage(in, configuration.getPath());
            this.version = getVersionFromMessage(in);
            //this.payload = getPayloadFromExchange(exchange);

        		boolean isWrite = configuration.getMode().equals(ZK_OPERATION_WRITE);
						this.node  = ExchangeUtils.getParameter(configuration.getPath(), exchange, String.class, "path");
							log.info("node:"+node);
						if( isWrite){
							Object pay= ExchangeUtils.getSource(configuration.getSource(), exchange, Object.class);
							this.payload = serialize((java.io.Serializable)pay);;
							log.info("payload:"+pay);
						}
        }
    }

    private class AsyncSetDataCallback implements StatCallback {

        public void processResult(int rc, String node, Object ctx, Stat statistics) {
            if (Code.NONODE.equals(Code.get(rc))) {
                if (configuration.isCreate()) {
                    log.warn(format("Node '%s' did not exist, creating it...", node));
                    ProductionContext context = (ProductionContext)ctx;
                    OperationResult<String> result = null;
                    try {
                        result = createNode(context);
                    } catch (Exception e) {
                        log.error(format("Error trying to create node '%s'", node), e);
                    }

                    if (result == null || !result.isOk()) {
                        log.error(format("Error creating node '%s'", node), result.getException());
                    }
                }
            } else {
                logStoreComplete(node, statistics);
            }
        }
    }

    private class AsyncDeleteCallback implements VoidCallback {
        @Override
        public void processResult(int rc, String path, Object ctx) {
            if (log.isDebugEnabled()) {
                if (log.isTraceEnabled()) {
                    log.trace(format("Removed data node '%s'", path));
                } else {
                    log.debug(format("Removed data node '%s'", path));
                }
            }
        }
    }

    private OperationResult<String> createNode(ProductionContext ctx) throws Exception {
        CreateOperation create = new CreateOperation(ctx.connection, ctx.node);
        create.setPermissions(getAclListFromMessage(ctx.exchange.getIn()));

        CreateMode mode = null;
        String modeString = configuration.getCreateMode();
        if (modeString != null) {
            try {
                mode = getCreateModeFromString(modeString, CreateMode.EPHEMERAL);
            } catch (Exception e) { }
        } else {
            mode = getCreateMode(ctx.exchange.getIn(), CreateMode.EPHEMERAL);
        }
        create.setCreateMode(mode == null ? CreateMode.EPHEMERAL : mode);
        create.setData(ctx.payload);
        return create.get();
    }

    /**
     * Tries to set the data first and if a no node error is received then an
     * attempt will be made to create it instead.
     */
    private OperationResult synchronouslySetData(ProductionContext ctx) throws Exception {

        SetDataOperation setData = new SetDataOperation(ctx.connection, ctx.node, ctx.payload);
        setData.setVersion(ctx.version);

        OperationResult result = setData.get();

        if (!result.isOk() && configuration.isCreate() && result.failedDueTo(Code.NONODE)) {
            log.warn(format("Node '%s' did not exist, creating it.", ctx.node));
            result = createNode(ctx);
        }
        return result;
    }

    private OperationResult synchronouslyGetData(ProductionContext ctx) throws Exception {

        GetDataOperation getData = new GetDataOperation(ctx.connection, ctx.node);

        OperationResult result = getData.get();

        return result;
    }

    private OperationResult synchronouslyDelete(ProductionContext ctx) throws Exception {
        DeleteOperation setData = new DeleteOperation(ctx.connection, ctx.node);
        setData.setVersion(ctx.version);

        OperationResult result = setData.get();

        if (!result.isOk() && configuration.isCreate() && result.failedDueTo(Code.NONODE)) {
            log.warn(format("Node '%s' did not exist, creating it.", ctx.node));
            result = createNode(ctx);
        }
        return result;
    }


    private void logStoreComplete(String path, Stat statistics) {
        if (log.isDebugEnabled()) {
            if (log.isTraceEnabled()) {
                log.trace(format("Stored data to node '%s', and receive statistics %s", path, statistics));
            } else {
                log.debug(format("Stored data to node '%s'", path));
            }
        }
    }
}
