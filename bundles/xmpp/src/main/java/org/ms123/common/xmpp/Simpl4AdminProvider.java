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
package org.ms123.common.xmpp;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.admin.AdminProvider;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.jivesoftware.openfire.group.GroupProvider;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

/**
 * Admin provider which will map a crowd group with openfire authorized admin users
 */
public class Simpl4AdminProvider implements AdminProvider {

	private static final Logger LOG = LoggerFactory.getLogger(Simpl4AdminProvider.class);

	private static final String JIVE_AUTHORIZED_GROUPS = "admin.authorizedGroups";

	public List<JID> getAdmins() {
		List<JID> results = new ArrayList<JID>();
		results.add(new JID("admin", XMPPServer.getInstance().getServerInfo().getXMPPDomain(), null, true));
System.out.println("getAdmins:"+results);
		return results;
	}

	public void setAdmins(List<JID> admins) {
		return;
	}

	public boolean isReadOnly() {
		return true;
	}
}
