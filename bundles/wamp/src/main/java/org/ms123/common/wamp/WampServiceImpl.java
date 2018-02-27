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
package org.ms123.common.wamp;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Reference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.ms123.common.rpc.JsonRpc;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.wamp.WampMessages.*;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;
import rx.subjects.BehaviorSubject;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import org.ms123.common.system.thread.*;
import org.ms123.common.wamp.camel.WampClientEndpoint;
import org.ms123.common.permission.api.PermissionService;

/** WampService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=wamp" })
public class WampServiceImpl extends BaseWampServiceImpl implements WampService {

	private static final Logger m_logger = LoggerFactory.getLogger(WampServiceImpl.class);

	private static Map<String, Realm> m_realms;
	public static String DEFAULT_REALM = "default";

	private List<String> m_registeredMethodList = new ArrayList();
	private Map<Long, Procedure> m_registeredMethodMap = new HashMap();

	private WampRouterSession m_localWampRouterSession;
	private ObjectMapper m_objectMapper = new ObjectMapper();
	private JsonRpc m_jsonRpc;
	private static PermissionService permissionService;

	public WampServiceImpl() {
	}


	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		m_jsonRpc = new JsonRpc(bundleContext);
		registerMethods();
	}

	protected void deactivate() throws Exception {
		for (Realm realm : getRealms().values()) {
			for (WampRouterSession.SessionContext context : realm.m_contextList) {
				realm.removeSession(context, false);
				String goodbye = WampCodec.encode(new GoodbyeMessage(null, ApplicationError.SYSTEM_SHUTDOWN));
				if( context.webSocket != null){
					context.webSocket.sendStringByFuture(goodbye);
				}
			}
			realm.m_contextList.clear();
		}
	}

	//@RequiresRoles("admin")
	private void registerMethods() {
/* $if version >= 1.8 $ */
		List<String> methodList = new ArrayList();
		methodList.add("enumeration.get");
		methodList.add("data.query");
		if (m_localWampRouterSession == null) {
			BaseWebSocket dummyWebSocket = new BaseWebSocket() {

				public void sendStringByFuture(String message) {
					ExecutorService executor = Executors.newSingleThreadExecutor();
					executor.submit(() -> {
						WampMessage msg = WampCodec.decode(message.getBytes());
						info("Local.sendStringByFuture:" + msg);
						if (msg instanceof RegisteredMessage) {
							RegisteredMessage regMsg = (RegisteredMessage) msg;
							Procedure proc = new Procedure(m_registeredMethodList.get((int) regMsg.requestId), null, regMsg.registrationId);
							m_registeredMethodMap.put(regMsg.registrationId, proc);
						} else if (msg instanceof WelcomeMessage) {
							doRegisterMethods(methodList);
						} else if (msg instanceof InvocationMessage) {
							InvocationMessage invMsg = (InvocationMessage) msg;
							Procedure proc = m_registeredMethodMap.get(invMsg.registrationId);
							info("Invocation:" + proc.procName + "/" + invMsg.arguments + "/" + invMsg.argumentsKw + "/ThreadId" + Thread.currentThread().getId());

							String paramString = invMsg.argumentsKw != null ? invMsg.argumentsKw.toString() : "";
							Map<String, Object> result = m_jsonRpc.handleRPC(proc.procName, paramString);
							Object error = result.get("error");
							if (error != null) {
								String errMsg = WampCodec.encode(new ErrorMessage(InvocationMessage.ID, invMsg.requestId, null, result.toString(), null, null));
								m_localWampRouterSession.onWebSocketText(errMsg);
							} else {
								ArrayNode resultNode = m_objectMapper.createArrayNode();
								resultNode.add((JsonNode) m_objectMapper.valueToTree(result));
								String yield = WampCodec.encode(new YieldMessage(invMsg.requestId, null, resultNode, null));
								m_localWampRouterSession.onWebSocketText(yield);
							}
						}
					});
				}
			};
			m_localWampRouterSession = new WampRouterSession(dummyWebSocket, getRealms());
			m_localWampRouterSession.onWebSocketConnect(null);
			m_localWampRouterSession.onWebSocketText(WampCodec.encode(new HelloMessage(DEFAULT_REALM, null)));
		} else {
			doRegisterMethods(methodList);
		}
/* $endif$ */
	}

	private void doRegisterMethods(List<String> methodList) {
		long i = m_registeredMethodList.size();
		for (String meth : methodList) {
			if (m_registeredMethodList.contains(meth)) {
				continue;
			}
			m_registeredMethodList.add(meth);
			String register = WampCodec.encode(new RegisterMessage(i++, null, meth));
			m_localWampRouterSession.onWebSocketText(register);
		}
	}

	@RequiresRoles("admin")
	public void start() throws RpcException {
/* $if version >= 1.8 $ */
		final WampClientSession client1 = createWampClientSession(DEFAULT_REALM);
		client1.statusChanged().subscribe((t1) -> {
			System.out.println("Session1 status changed to " + t1);
			if (t1 == WampClientSession.Status.Connected) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}

				Subscription addProcSubscription= client1.registerProcedure("com.myapp.add2").subscribe((request) -> {
					if (request.arguments() == null || request.arguments().size() != 2 || !request.arguments().get(0).canConvertToLong() || !request.arguments().get(1).canConvertToLong()) {
						try {
							request.replyError(new ApplicationError(ApplicationError.INVALID_PARAMETER));
						} catch (ApplicationError e) {
							e.printStackTrace();
						}
					} else {
						long a = request.arguments().get(0).asLong();
						long b = request.arguments().get(1).asLong();
						request.reply(a + b);
					}
				});

				//							 client1.registerProcedure("enumeration.get").subscribe();

				Observable<Long> result1 = client1.call("com.myapp.add2", Long.class, 33, 66);
				result1.subscribe((t2) -> {
					System.out.println("Completed add with result " + t2);
				}, (t3) -> {
					System.out.println("Completed add with error " + t3);
				});

			}
		}, (t) ->  {
				System.out.println("Session1 ended with error " + t);
		}, ()-> {
				System.out.println("Session1 ended normally");
		});

		//client1.open();
/* $endif$ */
	}
	private static Map<String, Realm> getRealms (){
		if( m_realms == null){
			m_realms = new HashMap();
			Set<WampRoles> roles = new HashSet();
			roles.add(WampRoles.Broker);
			RealmConfig realmConfig = new RealmConfig(roles, false);
			m_realms.put(DEFAULT_REALM, new Realm(realmConfig));
		}
		return m_realms;
	}

	public static WampClientSession createWampClientSession(String realm) {
		return createWampClientSession(realm,null);
	}
	public static WampClientSession createWampClientSession(String realm,WampClientEndpoint endpoint) {
		WampClientWebSocket ws = new WampClientWebSocket();
		WampClientSession wcs = new WampClientSession(ws, endpoint, realm, getRealms());
		ws.setWampClientSession(wcs);
		return wcs;
	}

	public static class WampClientWebSocket extends BaseWebSocket {
		private WampClientSession m_wampClientSession;
		private WampRouterSession m_wampRouterSession;

		public WampClientWebSocket() {
		}

		public void setWampClientSession(WampClientSession wcs) {
			m_wampClientSession = wcs;
		}

		public void setWampRouterSession(WampRouterSession wrs) {
			m_wampRouterSession = wrs;
			m_wampRouterSession.setPermissionService(permissionService);
		}

		public void sendStringByFuture(String message) {
/* $if version >= 1.8 $ */
			info("WampClientWebSocket.sendStringByFuture:" + message);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(() -> {
				m_wampClientSession.onWebSocketText(message);
			});
/* $endif$ */
		}
		@Override
		public void onWebSocketConnect(Session sess) {
			info("WampClientWebSocket.onWebSocketConnect");
			m_wampRouterSession.onWebSocketConnect(sess);
		}

		@Override
		public void onWebSocketText(String message) {
			m_wampRouterSession.onWebSocketText(message);
		}
		@Override
		public void onWebSocketClose(int status, String reason) {
/* $if version >= 1.8 $ */
			info("WampClientWebSocket.onWebSocketClose");
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(() -> {
				m_wampRouterSession.onWebSocketClose(status,reason);
			});
/* $endif$ */
		}
	}

	public WebSocketListener createWebSocket(Map<String, Object> config, Map<String, String> parameterMap) {
		return new WampRouterWebSocket(config, parameterMap);
	}

	public class WampRouterWebSocket extends BaseWebSocket {
		private Map<String, Object> m_config = null;
		private Map<String, String> m_params;
		private WampRouterSession m_wampRouterSession;
		private ThreadContext threadContext;
		private  Map<Object, Object>  shiroResources;

		public WampRouterWebSocket(Map<String, Object> config, Map<String, String> parameterMap) {
			m_config = config;
			m_params = parameterMap;
			String namespace = m_params.get("namespace");
			String routesName = m_params.get("routes");
			m_wampRouterSession = new WampRouterSession(this, getRealms());
			debug("xxxx.WampRouterWebSocket.WampRouterSession:" + m_wampRouterSession);
			this.threadContext = ThreadContext.getThreadContext();
			shiroResources = org.apache.shiro.util.ThreadContext.getResources();
		}

		@Override
		public void onWebSocketConnect(Session sess) {
			super.onWebSocketConnect(sess);
			m_wampRouterSession.onWebSocketConnect(sess);
		}

		@Override
		public void onWebSocketText(String message) {
			ThreadContext.loadThreadContext(this.threadContext);
			org.apache.shiro.util.ThreadContext.setResources(shiroResources);
			m_wampRouterSession.onWebSocketText(message);
		}

		@Override
		public void onWebSocketBinary(byte[] payload, int offset, int len) {
			m_wampRouterSession.onWebSocketBinary(payload, offset, len);
		}

		@Override
		public void onWebSocketClose(int statusCode, String reason) {
			super.onWebSocketClose(statusCode, reason);
			m_wampRouterSession.onWebSocketClose(statusCode, reason);
		}

		@Override
		public void onWebSocketError(Throwable cause) {
			m_wampRouterSession.onWebSocketError(cause);
		}
	}
	@Reference(dynamic = true, optional = true)
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
}

