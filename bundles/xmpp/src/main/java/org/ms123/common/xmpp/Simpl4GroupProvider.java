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
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.group.AbstractGroupProvider;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

/**
 */
public class Simpl4GroupProvider extends AbstractGroupProvider {

	private static final Logger LOG = LoggerFactory.getLogger(Simpl4GroupProvider.class);

	private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private static final Simpl4Manager manager = Simpl4Manager.getInstance();

	private static List<String> groups = new ArrayList<String>();

	private final XMPPServer server = XMPPServer.getInstance();

	public Simpl4GroupProvider() {
	}

	public Group getGroup(String name) throws GroupNotFoundException {
		throw new GroupNotFoundException();
	}

	private Collection<JID> getGroupMembers(String groupName) {
		return Collections.emptyList();
	}

	public Collection<String> getGroupNames(JID user) {
		return Collections.emptyList();
	}

	public int getGroupCount() {
		lock.readLock().lock();
		try {
			return groups.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	public Collection<String> getGroupNames() {
		lock.readLock().lock();
		try {
			return groups;
		} finally {
			lock.readLock().unlock();
		}
	}

	public Collection<String> getGroupNames(int startIndex, int numResults) {
		lock.readLock().lock();
		try {
			Collection<String> results = new ArrayList<String>(numResults);
			for (int i = 0, j = startIndex; i < numResults && j < groups.size(); ++i, ++j) {
				results.add(groups.get(j));
			}
			return results;
		} finally {
			lock.readLock().unlock();
		}
	}

	public Collection<String> search(String query) {
		lock.readLock().lock();
		try {
			ArrayList<String> results = new ArrayList<String>();
			if (query != null && query.trim().length() > 0) {
				if (query.endsWith("*")) {
					query = query.substring(0, query.length() - 1);
				}
				if (query.startsWith("*")) {
					query = query.substring(1);
				}
				query = query.toLowerCase();
				for (String groupName : groups) {
					if (groupName.toLowerCase().contains(query)) {
						results.add(groupName);
					}
				}
			}
			return results;
		} finally {
			lock.readLock().unlock();
		}
	}

	public Collection<String> search(String query, int startIndex, int numResults) {
		lock.readLock().lock();
		try {
			ArrayList<String> foundGroups = (ArrayList<String>) search(query);
			Collection<String> results = new ArrayList<String>();
			for (int i = 0, j = startIndex; i < numResults && j < foundGroups.size(); ++i, ++j) {
				results.add(foundGroups.get(j));
			}
			return results;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Modifying group not implemented - read-only for now
	 */
	public boolean isReadOnly() {
		return true;
	}

	public boolean isSearchSupported() {
		return true;
	}

	/**
	 * @see org.jivesoftware.openfire.group.AbstractGroupProvider#search(java.lang.String, java.lang.String)
	 */
	// TODO search on group attributes in Crowd
	@Override
	public Collection<String> search(String key, String value) {
		LOG.info("Search groups on attibutes not implemented yet");
		return Collections.emptyList();
	}
}
