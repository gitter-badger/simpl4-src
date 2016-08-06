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
package org.ms123.common.camel.components.websocket;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.system.thread.ThreadContext;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

public class WebsocketEndpoint extends DefaultEndpoint {

	private NodeSynchronization m_sync;
	private WebsocketStore m_memoryStore;
	private WebsocketComponent m_component;
	private WebsocketConsumer m_consumer;
	private Boolean sendToAll;
	private String startableGroups;
	private String startableUsers;
	private PermissionService permissionService;

	public WebsocketEndpoint(WebsocketComponent component, String uri, String remaining, Map<String, Object> parameters) {
		super(uri, component);
		this.m_memoryStore = new MemoryWebsocketStore();
		this.m_sync = new DefaultNodeSynchronization(m_memoryStore);
		this.m_component = component;
		CamelContext cc = component.getCamelContext();
		debug(this, "CamelContext:" + cc);
		this.permissionService = getByType(cc, PermissionService.class);
		debug(this, "PermissionService:" + this.permissionService);
	}

	@Override
	public WebsocketComponent getComponent() {
		ObjectHelper.notNull(m_component, "component");
		return (WebsocketComponent) super.getComponent();
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		ObjectHelper.notNull(m_component, "component");
		WebsocketConsumer consumer = new WebsocketConsumer(this, processor);
		configureConsumer(consumer);
		return consumer;
	}

	@Override
	public Producer createProducer() throws Exception {
		return new WebsocketProducer(this, m_memoryStore);
	}

	public void setConsumer(WebsocketConsumer consumer) {
		this.m_consumer = consumer;
	}

	public Object createWebsocket(Map<String, String> parameterMap) {
		List<String> permittedRoleList = getPermittedRoles();
		List<String> permittedUserList = getPermittedUsers();
		String userName = getUserName();
		List<String> userRoleList = getUserRoles(userName);
		debug(this, "WebsocketEndpoint.createWebsocket.userName:" + userName);
		debug(this, "WebsocketEndpoint.createWebsocket.userRoleList:" + userRoleList);
		debug(this, "WebsocketEndpoint.createWebsocket.permittedRoleList:" + permittedRoleList);
		debug(this, "WebsocketEndpoint.createWebsocket.permittedUserList:" + permittedUserList);
		if (!isPermitted(userName, userRoleList, permittedUserList, permittedRoleList)) {
			throw new RuntimeException(PERMISSION_DENIED + ":User(" + userName + ") has no permission");
		}
		return new DefaultWebsocket(parameterMap, m_sync, m_consumer);
	}

	public void connect(WebsocketProducer producer) throws Exception {
	}

	public void disconnect(WebsocketProducer producer) throws Exception {
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public Boolean getSendToAll() {
		return sendToAll;
	}

	public void setSendToAll(Boolean sendToAll) {
		this.sendToAll = sendToAll;
	}

	@Override
	protected void doStart() throws Exception {
		ServiceHelper.startService(m_memoryStore);
		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		ServiceHelper.stopService(m_memoryStore);
		super.doStop();
	}

	private <T> T getByType(CamelContext ctx, Class<T> kls) {
		return kls.cast(ctx.getRegistry().lookupByName(kls.getName()));
	}

	public String getStartableUsers() {
		return this.startableUsers;
	}

	public void setStartableUsers(String s) {
		this.startableUsers = s;
	}

	public String getStartableGroups() {
		return this.startableGroups;
	}

	public void setStartableGroups(String s) {
		this.startableGroups = s;
	}

	public List<String> getPermittedUsers() {
		return getStringList(this.startableUsers);
	}

	public List<String> getPermittedRoles() {
		return getStringList(this.startableGroups);
	}

	protected List<String> getStringList(String s) {
		if (s == null)
			return new ArrayList<String>();
		return Arrays.asList(s.split(","));
	}

	protected String getUserName() {
		return ThreadContext.getThreadContext().getUserName();
	}

	protected boolean isPermitted(String userName, List<String> userRoleList, List<String> permittedUserList, List<String> permittedRoleList) {
		if (permittedUserList.contains(userName)) {
			return true;
		}
		for (String userRole : userRoleList) {
			if (permittedRoleList.contains(userRole)) {
				return true;
			}
		}
		return false;
	}

	protected List<String> getUserRoles(String userName) {
		List<String> userRoleList = null;
		try {
			userRoleList = this.permissionService.getUserRoles(userName);
		} catch (Exception e) {
			userRoleList = new ArrayList<>();
		}
		return userRoleList;
	}
}

