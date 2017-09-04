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
package org.ms123.common.xmpp.camel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.Consumer;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
public class XmppConnectionContext {

	private XMPPTCPConnection m_connection;
	private XmppConsumer m_consumer;
	private String m_participant;
	private String m_username;
	private String m_nickname;
	private String m_resourceId;
	private Map<String, MultiUserChat> m_mucs = new ConcurrentHashMap();

	public XmppConnectionContext() {
	}

	public void setConnection(XMPPTCPConnection conn) {
		m_connection = conn;
	}

	public XMPPTCPConnection getConnection() {
		return m_connection;
	}

	public void setParticipant(String participant) {
		m_participant = participant;
	}

	public String getParticipant() {
		return m_participant;
	}

	public void setUsername(String username) {
		m_username = username;
	}

	public String getUsername() {
		return m_username;
	}

	public String getSessionId() {
		return m_username + "/"+ m_resourceId;
	}

	public void setResourceId(String resourceId) {
		m_resourceId = resourceId;
	}

	public String getResourceId() {
		return m_resourceId;
	}

	public void setNickname(String nickname) {
		m_nickname = nickname;
	}

	public String getNickname() {
		if( m_nickname == null){
			return m_username;
		}
		return m_nickname;
	}

	public void setConsumer(XmppConsumer consumer) {
		m_consumer = consumer;
	}

	public XmppConsumer getConsumer() {
		return m_consumer;
	}

	public void putMUC(String room, MultiUserChat muc) {
		m_mucs.put(room,  muc);
	}

	public MultiUserChat getMUC(String room) {
		return m_mucs.get(room);
	}

	public Map getMUCs(){
		return m_mucs;
	}

	public String getFQRoomname(String roomname) {
		return m_mucs.get(roomname) != null ? m_mucs.get(roomname).getRoom() : null;
	}

	public String getChatId() {
		return "Chat:" + getParticipant() + ":" + getUsername();
	}

	public String toString() {
		return "Context[Username:" + getUsername() + "/Participant:" + getParticipant() + "]";
	}
}
