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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.eclipse.jetty.websocket.api.Session;
import org.ms123.common.wamp.BaseWebSocket;
import org.ms123.common.wamp.WampMessages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.ms123.common.wamp.WampRouterSession.State.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.ms123.common.system.thread.ThreadContext;
import org.ms123.common.permission.api.PermissionService;


/**
 *
 */
class WampRouterSession {

	enum State {
		DISCONNECTED, CONNECTED, SESSION
	}

	private ObjectMapper m_objectMapper = new ObjectMapper();
	private SessionContext m_context = new SessionContext();
	private Map<String, Realm> m_realms;
	private State state = DISCONNECTED;
	private PermissionService m_permissionService;

	protected static class SessionContext {
		long sessionId;
		long lastUsedId = IdValidator.MIN_VALID_ID;
		String user;
		Realm realm;
		Set<WampRoles> roles;
		Map<Long, Procedure> providedProcedures;
		Map<Long, Invocation> pendingInvocations;
		Map<Long, Subscription> subscriptionsById;
		BaseWebSocket webSocket;
	}

	protected WampRouterSession(BaseWebSocket ws, Map<String, Realm> realms) {
		m_realms = realms;
		m_context.webSocket = ws;
	}
	protected String getUserName() {
		return ThreadContext.getThreadContext().getUserName();
	}

	protected void setPermissionService( PermissionService ps){
		m_permissionService = ps;
	}

	private void handleMessage(SessionContext context, WampMessage msg) {
		if (state == CONNECTED && msg instanceof HelloMessage) {
			HelloMessage hello = ((HelloMessage) msg);
			Realm realm = null;
			String errorMsg = null;
			if (!UriValidator.tryValidate(hello.realm, false)) {
				errorMsg = ApplicationError.INVALID_URI;
			} else {
				realm = m_realms.get(hello.realm);
				if (realm == null) {
					errorMsg = ApplicationError.NO_SUCH_REALM;
				}
			}
			if (errorMsg != null) {
				String abort = WampCodec.encode(new AbortMessage(null, errorMsg));
				debug("--> SendMessage(abort):" + abort);
				context.webSocket.sendStringByFuture(abort);
				return;
			}
			long sessionId = IdGenerator.newRandomId(null);
			Set<WampRoles> roles = new HashSet<WampRoles>();
			realm.includeSession(context, sessionId, roles);
			roles.add(WampRoles.Broker);
			ObjectNode welcomeDetails = m_objectMapper.createObjectNode();
			welcomeDetails.put("agent", "simpl4-1.0");
			ObjectNode routerRoles = welcomeDetails.putObject("roles");
			ObjectNode roleNode = routerRoles.putObject("broker");
			String wm = WampCodec.encode(new WampMessages.WelcomeMessage(sessionId, welcomeDetails));
			debug("--> SendMessage(welcome):" + wm + "/" + context.webSocket);
			context.webSocket.sendStringByFuture(wm);
			state = SESSION;
		}
		if (state != SESSION) {
			debug("Unexpected Message:" + msg);
			return;
		}
		if (msg instanceof SubscribeMessage) {
			SubscribeMessage sub = (WampMessages.SubscribeMessage) msg;
			debug("    SUBSCRIPING");
			String err = null;
			SubscriptionFlags flags = SubscriptionFlags.Wildcard;
			if (sub.options != null) {
				JsonNode match = sub.options.get("match");
				if (match != null) {
					String matchValue = match.asText();
					if ("prefix".equals(matchValue)) {
						flags = SubscriptionFlags.Prefix;
					} else if ("exact".equals(matchValue)) {
						flags = SubscriptionFlags.Exact;
					}
				}
			}
			if (!UriValidator.tryValidate(sub.topic, context.realm.config.useStrictUriValidation, flags == SubscriptionFlags.Wildcard)) {
				err = ApplicationError.INVALID_URI;
			}
			if (err == null && !(IdValidator.isValidId(sub.requestId))) {
				err = ApplicationError.INVALID_ARGUMENT;
			}
			if (err != null) {
				String errMsg = WampCodec.encode(new ErrorMessage(SubscribeMessage.ID, sub.requestId, null, err, null, null));
				info("   ErrorMessage:" + errMsg);
				context.webSocket.sendStringByFuture(errMsg);
				return;
			}
			if (context.subscriptionsById == null) {
				context.subscriptionsById = new HashMap<Long, Subscription>();
			}
			Map<String, Subscription> subscriptionMap = context.realm.subscriptionsByFlags.get(flags);
			Subscription subscription = subscriptionMap.get(sub.topic);
			if (subscription == null) {
				long subscriptionId = IdGenerator.newLinearId(context.realm.lastUsedSubscriptionId, context.realm.subscriptionsById);
				context.realm.lastUsedSubscriptionId = subscriptionId;
				subscription = new Subscription(sub.topic, flags, subscriptionId);
				subscriptionMap.put(sub.topic, subscription);
				context.realm.subscriptionsById.put(subscriptionId, subscription);
			}
			if (subscription.subscribers.add(context)) {
				context.subscriptionsById.put(subscription.subscriptionId, subscription);
			}
			String subscribed = WampCodec.encode(new SubscribedMessage(sub.requestId, subscription.subscriptionId));
			debug("--> SendMessage(subscribed):" + subscribed);
			context.webSocket.sendStringByFuture(subscribed);
		}
		if (msg instanceof UnsubscribeMessage) {
			debug("    UNSUBSCRIPING");
			UnsubscribeMessage unsub = (UnsubscribeMessage) msg;
			String err = null;
			if (!(IdValidator.isValidId(unsub.requestId)) || !(IdValidator.isValidId(unsub.subscriptionId))) {
				err = ApplicationError.INVALID_ARGUMENT;
			}
			Subscription s = null;
			if (err == null) {
				if (context.subscriptionsById != null) {
					s = context.subscriptionsById.get(unsub.subscriptionId);
				}
				if (s == null) {
					err = ApplicationError.NO_SUCH_SUBSCRIPTION;
				}
			}
			if (err != null) {
				String errMsg = WampCodec.encode(new ErrorMessage(UnsubscribeMessage.ID, unsub.requestId, null, err, null, null));
				info("   ErrorMessage:" + errMsg);
				context.webSocket.sendStringByFuture(errMsg);
				return;
			}
			s.subscribers.remove(context);
			context.subscriptionsById.remove(s.subscriptionId);
			if (context.subscriptionsById.isEmpty()) {
				context.subscriptionsById = null;
			}
			if (s.subscribers.isEmpty()) {
				context.realm.subscriptionsByFlags.get(s.flags).remove(s.topic);
				context.realm.subscriptionsById.remove(s.subscriptionId);
			}
			String unsubscribed = WampCodec.encode(new UnsubscribedMessage(unsub.requestId));
			context.webSocket.sendStringByFuture(unsubscribed);
		}
		if (msg instanceof PublishMessage) {
			PublishMessage pub = ((WampMessages.PublishMessage) msg);
			debug("    PUBLISHING");
			boolean sendAcknowledge = false;
			JsonNode ackOption = pub.options.get("acknowledge");
			if (ackOption != null && ackOption.asBoolean() == true) {
				sendAcknowledge = true;
			}
			String err = null;
			if (!UriValidator.tryValidate(pub.topic, context.realm.config.useStrictUriValidation)) {
				err = ApplicationError.INVALID_URI;
			}
			if (err == null && !(IdValidator.isValidId(pub.requestId))) {
				err = ApplicationError.INVALID_ARGUMENT;
			}
			if (err != null) {
				if (sendAcknowledge) {
					String errMsg = WampCodec.encode(new ErrorMessage(PublishMessage.ID, pub.requestId, null, err, null, null));
					context.webSocket.sendStringByFuture(errMsg);
				}
				return;
			}
			long publicationId = IdGenerator.newRandomId(null);
			// Store that somewhere?
			Subscription exactSubscription = context.realm.subscriptionsByFlags.get(SubscriptionFlags.Exact).get(pub.topic);
			if (exactSubscription != null) {
				publishEvent(context, pub, publicationId, exactSubscription);
			}
			Map<String, Subscription> prefixSubscriptionMap = context.realm.subscriptionsByFlags.get(SubscriptionFlags.Prefix);
			for (Subscription prefixSubscription : prefixSubscriptionMap.values()) {
				if (pub.topic.startsWith(prefixSubscription.topic)) {
					publishEvent(context, pub, publicationId, prefixSubscription);
				}
			}
			Map<String, Subscription> wildcardSubscriptionMap = context.realm.subscriptionsByFlags.get(SubscriptionFlags.Wildcard);
			String[] components = pub.topic.split("\\.", -1);
			for (Subscription wildcardSubscription : wildcardSubscriptionMap.values()) {
				boolean matched = true;
				if (components.length == wildcardSubscription.components.length) {
					for (int i = 0; i < components.length; i++) {
						if ( isSet(components[i]) && isSet(wildcardSubscription.components[i] )){
							Set<String> s1 = toSet( components[i] );	
							Set<String> s2 = toSet( wildcardSubscription.components[i] );	
							info("wildcardSubscriptionMap.s1/s2:"+s1+"/"+s2);
							s1.retainAll( s2);
							matched = s1.size()>0;
							info("wildcardSubscriptionMap.matched:"+matched+"/"+s1);
							if( !matched ) break;
						}else if (wildcardSubscription.components[i].length() > 0 && !components[i].equals(wildcardSubscription.components[i])) {
							matched = false;
							break;
						}
					}
				} else
					matched = false;
				if (matched) {
					publishEvent(context, pub, publicationId, wildcardSubscription);
				}
			}
			if (sendAcknowledge) {
				String published = WampCodec.encode(new PublishedMessage(pub.requestId, publicationId));
				debug("--> SendMessage(published):" + published);
				context.webSocket.sendStringByFuture(published);
			}
		}
		if (msg instanceof RegisterMessage) {
			RegisterMessage reg = ((WampMessages.RegisterMessage) msg);
			debug("    REGISTERING");
			String err = null;
			if (!UriValidator.tryValidate(reg.procedure, true)) {
				err = ApplicationError.INVALID_URI;
			}
			if (err == null && !(IdValidator.isValidId(reg.requestId))) {
				err = ApplicationError.INVALID_ARGUMENT;
			}
			Procedure proc = null;
			if (err == null) {
				proc = context.realm.procedures.get(reg.procedure);
				if (proc != null)
					err = ApplicationError.PROCEDURE_ALREADY_EXISTS;
			}
			if (err != null) {
				String errMsg = WampCodec.encode(new ErrorMessage(RegisterMessage.ID, reg.requestId, null, err, null, null));
				info("   ErrorMessage:" + errMsg);
				context.webSocket.sendStringByFuture(errMsg);
				return;
			}
			long registrationId = IdGenerator.newLinearId(context.lastUsedId, context.providedProcedures);
			context.lastUsedId = registrationId;
			Procedure procInfo = new Procedure(reg.procedure, context, registrationId);

			info("RegisterMessage.realm:" + context.realm);
			context.realm.procedures.put(reg.procedure, procInfo);
			info("RegisterMessage.realm.providedProcedures:" + context.realm.procedures);
			if (context.providedProcedures == null) {
				context.providedProcedures = new HashMap<Long, Procedure>();
				context.pendingInvocations = new HashMap<Long, Invocation>();
			}
			context.providedProcedures.put(procInfo.registrationId, procInfo);
			String response = WampCodec.encode(new RegisteredMessage(reg.requestId, procInfo.registrationId));
			context.webSocket.sendStringByFuture(response);
		}
		if (msg instanceof UnregisterMessage) {
			UnregisterMessage unreg = ((UnregisterMessage) msg);
			debug("    UNREGISTERING");
			String err = null;
			if (!(IdValidator.isValidId(unreg.requestId)) || !(IdValidator.isValidId(unreg.registrationId))) {
				err = ApplicationError.INVALID_ARGUMENT;
			}
			Procedure proc = null;
			if (err == null) {
				if (context.providedProcedures != null) {
					proc = context.providedProcedures.get(unreg.registrationId);
				}
				if (proc == null) {
					err = ApplicationError.NO_SUCH_REGISTRATION;
				}
			}
			if (err != null) {
				String errMsg = WampCodec.encode(new ErrorMessage(UnregisterMessage.ID, unreg.requestId, null, err, null, null));
				context.webSocket.sendStringByFuture(errMsg);
				return;
			}
			for (Invocation invoc : proc.pendingCalls) {
				context.pendingInvocations.remove(invoc.invocationRequestId);
				if (invoc.caller.isConnected()) {
					String errMsg = WampCodec.encode(new ErrorMessage(CallMessage.ID, invoc.callRequestId, null, ApplicationError.NO_SUCH_PROCEDURE, null, null));
					context.webSocket.sendStringByFuture(errMsg);
				}
			}
			proc.pendingCalls.clear();
			context.realm.procedures.remove(proc.procName);
			context.providedProcedures.remove(proc.registrationId);
			if (context.providedProcedures.size() == 0) {
				context.providedProcedures = null;
				context.pendingInvocations = null;
			}
			String unregister = WampCodec.encode(new UnregisteredMessage(unreg.requestId));
			context.webSocket.sendStringByFuture(unregister);
		}
		if (msg instanceof CallMessage) {
			CallMessage callMsg = (CallMessage) msg;
			String err = null;
			if (!UriValidator.tryValidate(callMsg.procedure, context.realm.config.useStrictUriValidation)) {
				err = ApplicationError.INVALID_URI;
			}
			if (err == null && !(IdValidator.isValidId(callMsg.requestId))) {
				err = ApplicationError.INVALID_ARGUMENT;
			}
			Procedure proc = null;
			if (err == null) {
				info("Procedures:" + context.realm.procedures);
				info("call.realm:" + context.realm);
				proc = context.realm.procedures.get(callMsg.procedure);
				if (proc == null)
					err = ApplicationError.NO_SUCH_PROCEDURE;
			}
			if (err != null) {
				String errMsg = WampCodec.encode(new ErrorMessage(CallMessage.ID, callMsg.requestId, null, err, null, null));
				info("   ErrorMessage.Call:" + errMsg);
				context.webSocket.sendStringByFuture(errMsg);
				return;
			}
			Invocation invoc = new Invocation();
			invoc.callRequestId = callMsg.requestId;
			invoc.caller = context.webSocket;
			invoc.procedure = proc;
			invoc.invocationRequestId = IdGenerator.newLinearId(context.lastUsedId, context.pendingInvocations);
			context.lastUsedId = invoc.invocationRequestId;
			if (proc.context.pendingInvocations == null) {
				proc.context.pendingInvocations = new HashMap<Long, Invocation>();
			}
			proc.context.pendingInvocations.put(invoc.invocationRequestId, invoc);
			proc.pendingCalls.add(invoc);
			String imsg = WampCodec.encode(new InvocationMessage(invoc.invocationRequestId, proc.registrationId, null, callMsg.arguments, callMsg.argumentsKw));
			debug("    InvocationMessage:" + imsg + "/ThreadId:" + Thread.currentThread().getId());
			proc.provider.sendStringByFuture(imsg);
		}
		if (msg instanceof YieldMessage) {
			debug("    RESULT");
			YieldMessage yieldMsg = (YieldMessage) msg;
			if (!(IdValidator.isValidId(yieldMsg.requestId))) {
				return;
			}
			debug("    Result.pendingInvocations:" + context.pendingInvocations);
			if (context.pendingInvocations == null) {
				return;
			}
			Invocation invoc = context.pendingInvocations.get(yieldMsg.requestId);
			if (invoc == null) {
				return;
			}
			context.pendingInvocations.remove(yieldMsg.requestId);
			invoc.procedure.pendingCalls.remove(invoc);
			String result = WampCodec.encode(new ResultMessage(invoc.callRequestId, null, yieldMsg.arguments, yieldMsg.argumentsKw));
			debug("    SendResult:" + result);
			invoc.caller.sendStringByFuture(result);
		}
		if (msg instanceof ErrorMessage) {
			ErrorMessage errorMsg = (ErrorMessage) msg;
			if (errorMsg.requestType == InvocationMessage.ID) {
				Invocation invoc = context.pendingInvocations.get(errorMsg.requestId);
				if (invoc == null) {
					return;
				}
				context.pendingInvocations.remove(errorMsg.requestId);
				invoc.procedure.pendingCalls.remove(invoc);
				errorMsg.requestType = CallMessage.ID;
				errorMsg.requestId = invoc.callRequestId;
				String error = WampCodec.encode(errorMsg);
				debug("    SendError:" + error);
				invoc.caller.sendStringByFuture(error);
			} else {
			}
		}
	}

	public void onWebSocketConnect(Session sess) {
		debug("<-- WampRouterSession.SocketConnect");
		state = CONNECTED;
	}

	public void onWebSocketBinary(byte[] payload, int offset, int len) {
	}

	public void onWebSocketText(String message) {
		try {
			WampMessage msg = WampCodec.decode(message.getBytes());
			if (msg instanceof ErrorMessage) {
				ErrorMessage errMsg = (ErrorMessage) msg;
				debug("<-- ReceiveMessage(error):" + errMsg.error + "/requestId:" + errMsg.requestId + "/requestType:" + errMsg.requestType);
			} else {
				debug("<-- ReceiveMessage(" + getMessageName(msg) + "):" + message);
			}
			m_context.user = getUserName();
			info("onWebSocketText.context("+m_context.user+"):"+msg.getClass().getSimpleName());
			handleMessage(m_context, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onWebSocketClose(int statusCode, String reason) {
		debug("<-- SocketClose:" + statusCode + "/" + reason);
		m_context.realm.removeSession(m_context, true);
		state = DISCONNECTED;
	}

	public void onWebSocketError(Throwable cause) {
		debug("<-- SocketError:" + cause);
	}

	private void publishEvent(SessionContext publisher, PublishMessage pub, long publicationId, Subscription subscription) {
		ObjectNode details = null;
		if (subscription.flags != SubscriptionFlags.Exact) {
			details = m_objectMapper.createObjectNode();
			details.put("topic", pub.topic);
		}
		List<String> permittedUserList=null;
		List<String> permittedRoleList=null;
		try{
			String node = pub.options.get("permittedUserList").toString();
			permittedUserList = m_objectMapper.readValue(node, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
			node = pub.options.get("permittedRoleList").toString();
			permittedRoleList = m_objectMapper.readValue(node, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
			info("permittedUserList:"+permittedUserList);
			info("permittedRoleList:"+permittedRoleList);
		}catch(Exception e){
			e.printStackTrace();
		}

		String ev = WampCodec.encode(new EventMessage(subscription.subscriptionId, publicationId, details, pub.arguments, pub.argumentsKw));
		for (SessionContext receiver : subscription.subscribers) {
			info("receiver.user:"+receiver.user);
			List<String> userRoleList = getUserRoles( receiver.user);
			if (!isPermitted(receiver.user, userRoleList, permittedUserList, permittedRoleList)) {
				info("publish:User(" + receiver.user + ") has no permission");
				continue;
			}
			if (receiver == publisher) {
				boolean skipPublisher = true;
				if (pub.options != null) {
					JsonNode excludeMeNode = pub.options.get("exclude_me");
					if (excludeMeNode != null) {
						skipPublisher = excludeMeNode.asBoolean(true);
					}
				}
				if (skipPublisher)
					continue;
			}
			debug("--> SendMessage(publish):" + ev);
			receiver.webSocket.sendStringByFuture(ev);
		}
	}

	private String getMessageName(Object o) {
		String s = o.toString();
		int nameEndIndex = s.indexOf("Message@");
		int dollarIndex = s.lastIndexOf("$");
		String name = s.substring(dollarIndex + 1, nameEndIndex);
		return name.toLowerCase();
	}

	private boolean isSet( String s){
		if( s.length() < 2 ) return false;
		return s.startsWith("[") && s.endsWith("]");
	}

	private Set<String> toSet( String s){
		s = s.substring(1, s.length()-1);
		String a[] = s.split("\\|");
		
		Set<String> ret = Arrays.stream(a).collect(Collectors.toSet());
		return ret;
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
			userRoleList = this.m_permissionService.getUserRoles(userName);
		} catch (Exception e) {
			userRoleList = new ArrayList<>();
		}
		return userRoleList;
	}

	protected static void debug(String msg) {
		m_logger.debug(msg);
	}

	protected static void info(String msg) {
		m_logger.info(msg);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(WampRouterSession.class);
}

