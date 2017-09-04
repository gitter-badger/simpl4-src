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
package org.ms123.common.auth.user;

import org.activiti.engine.*;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import org.activiti.engine.identity.*;

public class ActivitiUserManager implements UserManager {

	private IdentityService m_is;

	public ActivitiUserManager(ProcessEngine pe) throws Exception {
		m_is = pe.getIdentityService();
	}

	public void createUser(String userId, String pw, List<String> groups) throws Exception {
		org.activiti.engine.identity.User u = m_is.createUserQuery().userId(userId).singleResult();
		System.out.println("ActivitiUserManager.createUser:" + userId + " -> " + u);
		if (u == null) {
			u = m_is.newUser(userId);
			u.setPassword(pw);
		} else {
			u.setPassword(pw);
		}
		m_is.saveUser(u);
		updateGroupMemberships(u, groups);
	}

	public void updateUser(String userId, String pw, List<String> groups) throws Exception {
		System.out.println("ActivitiUserManager.updateUser:" + userId);
		createUser(userId, pw, groups);
	}

	public void deleteUser(String userId) throws Exception {
		m_is.deleteUser(userId);
		System.out.println("ActivitiUserManager.deleteUser:" + userId);
	}

	public void createGroup(String groupId) throws Exception {
		org.activiti.engine.identity.Group g = m_is.createGroupQuery().groupId(groupId).singleResult();
		System.out.println("ActivitiUserManager.createGroup:" + groupId + " -> " + g);
		if (g == null) {
			g = m_is.newGroup(groupId);
			m_is.saveGroup(g);
		}
	}

	public void deleteGroup(String groupId) throws Exception {
		m_is.deleteGroup(groupId);
		System.out.println("ActivitiUserManager.deleteGroup:" + groupId);
	}

	private void updateGroupMemberships(org.activiti.engine.identity.User user, List<String> groups) throws Exception {
		if( groups == null)  return;
		System.out.println("updateGroupMemberships.groups:" + groups + "," + groups.size());
		Iterator<Group> git = m_is.createGroupQuery().groupMember(user.getId()).list().iterator();
		while (git.hasNext()) {
			Group g = git.next();
			System.out.println("\tdeleteMembership:" + user.getId() + " -> " + g.getId());
			m_is.deleteMembership(user.getId(), g.getId());
		}
		Iterator<String> it = groups.iterator();
		while (it.hasNext()) {
			String g = it.next();
			if (g != null && g.trim().length() > 0) {
				try {
					System.out.println("\tcreateMembership:" + user.getId() + " -> " + g);
					m_is.createMembership(user.getId(), g);
				} catch (Exception e) {
					boolean ok = true;
					try {
						createGroup(g);
						m_is.createMembership(user.getId(), g);
					} catch (Exception e2) {
						ok = false;
					}
					if (!ok)
						throw new RuntimeException("ActivitiUserManager.createMembership_failed:" + e.getMessage() + "(" + g + ")");
				}
			}
		}
	}
}
