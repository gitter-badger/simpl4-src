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
import java.util.List;
import java.util.Map;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.apache.commons.lang.StringUtils;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.auth.api.AuthService;
import flexjson.*;

@SuppressWarnings("unchecked")
public class Simpl4UserEntityManager extends UserEntityManager {
	protected JSONSerializer m_js = new JSONSerializer();

	protected PermissionService m_permissionService;

	protected AuthService m_authService;

	public Simpl4UserEntityManager(AuthService as, PermissionService ps) {
		m_js.prettyPrint(true);
		m_authService = as;
		m_permissionService = ps;
	}

	@Override
	public User createNewUser(String userId) {
		throw new ActivitiException("My user manager doesn't support creating a new user");
	}

	@Override
	public void insertUser(User user) {
		throw new ActivitiException("My user manager doesn't support inserting a new user");
	}

	@Override
	public void deleteUser(String userId) {
		throw new ActivitiException("My user manager doesn't support deleting a user");
	}

	@Override
	public UserEntity findUserById(String userLogin) {
		//TODO: get my user according to userLogin and convert it to UserEntity
		throw new ActivitiException("My user manager doesn't support finding a user");
	}

	@Override
	public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page) {
		//System.out.println("Simpl4UserEntityManager.findUserByQueryCriteria:"+m_js.deepSerialize(query));
		List<User> userList = new ArrayList<User>();
		UserQueryImpl userQuery = (UserQueryImpl) query;
		if (StringUtils.isNotEmpty(userQuery.getId())) {
			userList.add(convertToUser(m_authService.getUser(userQuery.getId())));
			//System.out.println("Simpl4UserEntityManager.findUserByQueryCriteria:"+m_js.deepSerialize(userList));
			return userList;
		} else {
			//TODO: get all users from your identity domain and convert them to List<User>
			return null;
		}
	}

	@Override
	public long findUserCountByQueryCriteria(UserQueryImpl query) {
		return findUserByQueryCriteria(query, null).size();
	}

	@Override
	public Boolean checkPassword(String userId, String password) {
		//TODO: check the password in your domain and return the appropriate boolean
		return false;
	}
	private User convertToUser(Map<String,String> userProps){
		User u =new UserEntity();
		u.setId( userProps.get("userid"));
		return u;
	}
}
