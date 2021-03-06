/*
 * Copyright 2014 Matthias Einwag
 *
 * The jawampa authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.ms123.common.wamp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.EnumMap;
import org.ms123.common.wamp.WampMessages.ErrorMessage;
import org.ms123.common.wamp.WampMessages.CallMessage;

/**
 */
public class Realm {
	final RealmConfig config;
	final List<WampRouterSession.SessionContext> m_contextList = new ArrayList<WampRouterSession.SessionContext>();
	final Map<String, Procedure> procedures = new HashMap<String, Procedure>();

	final EnumMap<SubscriptionFlags, Map<String, Subscription>> subscriptionsByFlags = new EnumMap<SubscriptionFlags, Map<String, Subscription>>(SubscriptionFlags.class);
	final Map<Long, Subscription> subscriptionsById = new HashMap<Long, Subscription>();
	long lastUsedSubscriptionId = IdValidator.MIN_VALID_ID;

	public Realm(RealmConfig config) {
		this.config = config;
		subscriptionsByFlags.put(SubscriptionFlags.Exact, new HashMap<String, Subscription>());
		subscriptionsByFlags.put(SubscriptionFlags.Prefix, new HashMap<String, Subscription>());
		subscriptionsByFlags.put(SubscriptionFlags.Wildcard, new HashMap<String, Subscription>());
	}

	void includeSession(WampRouterSession.SessionContext sessionContext, long sessionId, Set<WampRoles> roles) {
		m_contextList.add(sessionContext);
		sessionContext.realm = this;
		sessionContext.sessionId = sessionId;
		sessionContext.roles = roles;
	}

	void removeSession(WampRouterSession.SessionContext sessionContext, boolean removeFromList) {
		if (sessionContext.realm == null)
			return;

		if (sessionContext.subscriptionsById != null) {
			for (Subscription sub : sessionContext.subscriptionsById.values()) {
				sub.subscribers.remove(sessionContext);
				if (sub.subscribers.isEmpty()) {
					subscriptionsByFlags.get(sub.flags).remove(sub.topic);
					subscriptionsById.remove(sub.subscriptionId);
				}
			}
			sessionContext.subscriptionsById.clear();
			sessionContext.subscriptionsById = null;
		}

		if (sessionContext.providedProcedures != null) {
			for (Procedure proc : sessionContext.providedProcedures.values()) {
				// Clear all pending invocations and thereby inform other clients 
				// that the proc has gone away
				for (Invocation invoc : proc.pendingCalls) {
					//if (invoc.caller.state != RouterHandlerState.Open) //@@@MS
					//	continue;
					String errMsg = WampCodec.encode(new ErrorMessage(CallMessage.ID, invoc.callRequestId, null, ApplicationError.NO_SUCH_PROCEDURE, null, null));
					invoc.caller.sendStringByFuture(errMsg);
				}
				proc.pendingCalls.clear();
				procedures.remove(proc.procName);
			}
			sessionContext.providedProcedures = null;
			sessionContext.pendingInvocations = null;
		}

		sessionContext.realm = null;
		sessionContext.roles.clear();
		sessionContext.roles = null;
		sessionContext.sessionId = 0;
		sessionContext.webSocket = null;

		if (removeFromList) {
			m_contextList.remove(sessionContext);
		}
	}
}

