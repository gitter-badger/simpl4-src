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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.OutputStream;
import java.io.InputStream;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.permission.api.PermissionService;
import org.apache.camel.util.IntrospectionSupport;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.xmpp.packet.JID;
import org.jivesoftware.openfire.XMPPServer;
import flexjson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings({"unchecked","deprecation"})
class BaseXmppServiceImpl {

	protected PermissionService m_permissionService;

	protected NucleusService m_nucleusService;

	protected CamelService m_camelService;

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();

	protected void createRoom(Map<String, Object> roomSpec, String serviceName) throws Exception {
		List<String> owners = (List) roomSpec.get("owners");
		JID owner = XMPPServer.getInstance().createJID("admin", null);
		if (owners != null && owners.size() > 0) {
			owner = new JID(owners.get(0));
		} else {
			owners = new ArrayList<String>();
			owners.add(owner.toBareJID());
			roomSpec.put("owners", owners);
		}
		String roomName = (String) roomSpec.get("roomName");
		Boolean isPersistent = (Boolean) roomSpec.get("persistent");
		MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName).getChatRoom(roomName.toLowerCase(), owner);
		IntrospectionSupport.setProperties(room, roomSpec);
		room.setRolesToBroadcastPresence(new ArrayList<String>());
		setRoles(room, roomSpec);
		room.setCreationDate(new Date());
		room.setModificationDate(new Date());
		// Unlock the room, because the default configuration lock the room.  		
		room.unlock(room.getRole());
System.out.println("isPersistent:"+isPersistent);
		if( isPersistent == null){
			room.setPersistent(true);
		}
		if (room.isPersistent()) {
			room.saveToDB();
		}
	}

	private void setRoles(MUCRoom room, Map<String, Object> roomSpec) throws Exception {
		List<JID> roles = new ArrayList<JID>();
		Collection<JID> owners = new ArrayList<JID>();
		Collection<JID> existingOwners = new ArrayList<JID>();
		List<String> specOwners = (List) roomSpec.get("owners");
		List<String> specAdmins = (List) roomSpec.get("admins");
		List<String> specMembers = (List) roomSpec.get("members");
		List<String> specOutcasts = (List) roomSpec.get("outcasts");
		List<JID> mucRoomEntityOwners = convertStringsToJIDs(specOwners);
		owners.addAll(room.getOwners());
		// Find same owners
		for (JID jid : owners) {
			if (mucRoomEntityOwners.contains(jid)) {
				existingOwners.add(jid);
			}
		}
		// Don't delete the same owners
		owners.removeAll(existingOwners);
		room.addOwners(convertStringsToJIDs(specOwners), room.getRole());
		// Collect all roles to reset
		roles.addAll(owners);
		roles.addAll(room.getAdmins());
		roles.addAll(room.getMembers());
		roles.addAll(room.getOutcasts());
		for (JID jid : roles) {
			room.addNone(jid, room.getRole());
		}
		room.addOwners(convertStringsToJIDs(specOwners), room.getRole());
		if (specAdmins != null) {
			room.addAdmins(convertStringsToJIDs(specAdmins), room.getRole());
		}
		if (specMembers != null) {
			for (String memberJid : specMembers) {
				room.addMember(new JID(memberJid), null, room.getRole());
			}
		}
		if (specOutcasts != null) {
			for (String outcastJid : specOutcasts) {
				room.addOutcast(new JID(outcastJid), null, room.getRole());
			}
		}
	}

	private static List<String> convertJIDsToStringList(Collection<JID> jids) {
		List<String> result = new ArrayList<String>();
		for (JID jid : jids) {
			result.add(jid.toBareJID());
		}
		return result;
	}

	private static List<JID> convertStringsToJIDs(List<String> jids) {
		List<JID> result = new ArrayList<JID>();
		for (String jidString : jids) {
			result.add(new JID(jidString));
		}
		return result;
	}

	public Map<String, Object> convertToRoomSpec(MUCRoom room) {
		Map<String, Object> roomSpec = new HashMap();
		roomSpec.put("naturalLanguageName", room.getNaturalLanguageName());
		roomSpec.put("name", room.getName());
		roomSpec.put("description", room.getDescription());
		roomSpec.put("canAnyoneDiscoverJID", room.canAnyoneDiscoverJID());
		roomSpec.put("canChangeNickname", room.canChangeNickname());
		roomSpec.put("canOccupantsChangeSubject", room.canOccupantsChangeSubject());
		roomSpec.put("canOccupantsInvite", room.canOccupantsInvite());
		roomSpec.put("publicRoom", room.isPublicRoom());
		roomSpec.put("password", room.getPassword());
		roomSpec.put("ID", room.getID());
		roomSpec.put("persistent", room.isPersistent());
		roomSpec.put("registrationEnabled", room.isRegistrationEnabled());
		roomSpec.put("logEnabled", room.isLogEnabled());
		roomSpec.put("loginRestrictedToNickname", room.isLoginRestrictedToNickname());
		roomSpec.put("maxUsers", room.getMaxUsers());
		roomSpec.put("membersOnly", room.isMembersOnly());
		roomSpec.put("moderated", room.isModerated());
		roomSpec.put("owners", convertJIDsToStringList(room.getOwners()));
		roomSpec.put("admins", convertJIDsToStringList(room.getAdmins()));
		roomSpec.put("members", convertJIDsToStringList(room.getMembers()));
		roomSpec.put("outcasts", convertJIDsToStringList(room.getOutcasts()));
		roomSpec.put("broadcastPresenceRoles", room.getRolesToBroadcastPresence());
		roomSpec.put("creationDate", room.getCreationDate());
		roomSpec.put("modificationDate", room.getModificationDate());
		return roomSpec;
	}
	protected static void debug(String msg) {
		System.out.println(msg);
		m_logger.debug(msg);
	}
	protected static void info(String msg) {
		System.out.println(msg);
		m_logger.info(msg);
	}
	private static final Logger m_logger = LoggerFactory.getLogger(BaseXmppServiceImpl.class);
}
