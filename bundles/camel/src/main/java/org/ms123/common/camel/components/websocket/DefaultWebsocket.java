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
package org.ms123.common.camel.components.websocket;

import java.io.Serializable;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.apache.camel.Exchange;
import flexjson.*;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
public class DefaultWebsocket implements WebSocketListener {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultWebsocket.class);

	private final WebsocketConsumer m_consumer;
	private final NodeSynchronization m_sync;
	private volatile Session m_session;
	private Map<String, String> m_parameterMap;
	private Map<String, String> m_headers = new HashMap();
	private Map<String, String> m_closeCommandBody = new HashMap();
	private JSONDeserializer m_ds = new JSONDeserializer();
	private JSONSerializer m_js = new JSONSerializer();

	public DefaultWebsocket(Map<String, String> parameterMap, NodeSynchronization sync, WebsocketConsumer consumer) {
		this.m_parameterMap = parameterMap;
		this.m_sync = sync;
		this.m_consumer = consumer;
		extractHeaders();
		extractCloseCommandBody();
		m_js.prettyPrint(true);
	}

	public RemoteEndpoint getRemote() {
		Session sess = this.m_session;
		return sess == null ? null : m_session.getRemote();
	}

	public Session getSession() {
		return m_session;
	}

	public boolean isConnected() {
		Session sess = this.m_session;
		return (sess != null) && (sess.isOpen());
	}

	public boolean isNotConnected() {
		Session sess = this.m_session;
		return (sess == null) || (!sess.isOpen());
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		this.m_session = null;
		debug("onClose {} {}", statusCode, reason);
		if( m_closeCommandBody != null){
			this.m_consumer.sendMessage(getConnectionKey(), m_closeCommandBody, m_headers,m_session);
		}
		m_sync.removeSocket(this);
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		this.m_session = sess;
		debug("onOpen {}", sess);
		m_sync.addSocket(this);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		cause.printStackTrace(System.err);
	}

	@Override
	public void onWebSocketText(String message) {
		debug("onMessage: {}", message);
		debug("\theaders: {}", m_headers);
		Object body = null;
		try {
			body = m_ds.deserialize(message);
		} catch (Exception e) {
			body = message;
		}
		if (this.m_consumer != null) {
			this.m_consumer.sendMessage(getConnectionKey(), body, m_headers,m_session);
		} else {
			debug("No consumer to handle message received: {}", message);
		}
	}

	public void sendMessage(String message) {
		debug("-> Websocket:" + message);
		m_session.getRemote().sendStringByFuture(message);
	}

	public String getConnectionKey() {
		return m_parameterMap.get("connectionKey");
	}

	private void extractHeaders() {
		String headersString = m_parameterMap.get("camelHeaders");
		if( headersString != null){
			Map<String,Object> headers = (Map)m_ds.deserialize(headersString);
			for (Map.Entry<String, Object> e : headers.entrySet()) {
				m_headers.put(e.getKey(), String.valueOf(e.getValue()));
			}
		}
	}

	private void extractCloseCommandBody() {
		String closeString = m_parameterMap.get("closeCommandBody");
		if( closeString != null){
			Map<String,Object> map = (Map)m_ds.deserialize(closeString);
			m_closeCommandBody = new HashMap();
			for (Map.Entry<String, Object> e : map.entrySet()) {
				m_closeCommandBody.put(e.getKey(), String.valueOf(e.getValue()));
			}
		}
	}

	protected void debug(String msg, Object... args) {
		System.out.println(MessageFormatter.arrayFormat(msg, varargsToArray(args)).getMessage());
		LOG.debug(msg, args);
	}

	protected void info(String msg, Object... args) {
		System.out.println(MessageFormatter.arrayFormat(msg, varargsToArray(args)).getMessage());
		LOG.info(msg, args);
	}

	private Object[] varargsToArray(Object... args) {
		Object[] ret = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			ret[i] = args[i];
		}
		return ret;
	}
}
