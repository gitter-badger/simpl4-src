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
package org.ms123.common.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.apache.commons.lang.StringUtils;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.auth.api.AuthService;
import flexjson.*;

@SuppressWarnings("unchecked")
public class Simpl4GroupEntityManager extends GroupEntityManager {

	protected JSONSerializer m_js = new JSONSerializer();

	protected PermissionService m_permissionService;

	protected AuthService m_authService;

	public Simpl4GroupEntityManager(AuthService as, PermissionService ps) {
		m_js.prettyPrint(true);
		m_authService = as;
		m_permissionService = ps;
	}

	@Override
	public Group createNewGroup(String groupId) {
		throw new ActivitiException("My group manager doesn't support creating a new group");
	}

	@Override
	public void insertGroup(Group group) {
		throw new ActivitiException("My group manager doesn't support inserting a new group");
	}

	@Override
	public void deleteGroup(String groupId) {
		throw new ActivitiException("My group manager doesn't support deleting a new group");
	}

	@Override
	public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
		return findGroupByQueryCriteria(query, null).size();
	}

	@Override
	public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
		List<Group> groupList = new ArrayList<Group>();
		GroupQueryImpl groupQuery = (GroupQueryImpl) query;
		if (StringUtils.isNotEmpty(groupQuery.getId())) {
			GroupEntity singleGroup = findGroupById(groupQuery.getId());
			groupList.add(singleGroup);
			return groupList;
		} else if (StringUtils.isNotEmpty(groupQuery.getName())) {
			GroupEntity singleGroup = findGroupById(groupQuery.getId());
			groupList.add(singleGroup);
			return groupList;
		} else if (StringUtils.isNotEmpty(groupQuery.getUserId())) {
			return findGroupsByUser(groupQuery.getUserId());
		} else {
			//TODO: get all groups from your identity domain and convert them to List<Group>
			return null;
		}
	}

	//@Override
	public GroupEntity findGroupById(String activitiGroupID) {
		//System.out.println("Simpl4GroupEntityManager:findGroupById:" + activitiGroupID);
		if (m_permissionService.hasRole(activitiGroupID)) {
			GroupEntity g = convertToGroup(activitiGroupID);
			//System.out.println("Simpl4GroupEntityManager:findGroupById:" + m_js.deepSerialize(g));
			return g;
		}
		return null;
	}

	@Override
	public List<Group> findGroupsByUser(String userLogin) {
		List<Group> roleListRet = new ArrayList();
		//System.out.println("Simpl4GroupEntityManager:findGroupsByUser:" + userLogin);
		try{
			List<String> roleList = m_permissionService.getUserRoles(userLogin);
			for (String roleid : roleList) {
				roleListRet.add(convertToGroup(roleid));
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Simpl4GroupEntityManager.findGroupsByUser:",e);
		}
		//System.out.println("Simpl4GroupEntityManager:findGroupsByUser:" + m_js.deepSerialize(roleListRet));
		return roleListRet;
	}

	private GroupEntity convertToGroup(String id) {
		GroupEntity g = new GroupEntity();
		g.setId(id);
		return g;
	}
}
