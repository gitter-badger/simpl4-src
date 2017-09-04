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

import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.cmd.CheckPassword;
import org.activiti.engine.impl.cmd.CreateGroupCmd;
import org.activiti.engine.impl.cmd.CreateGroupQueryCmd;
import org.activiti.engine.impl.cmd.CreateMembershipCmd;
import org.activiti.engine.impl.cmd.CreateUserCmd;
import org.activiti.engine.impl.cmd.CreateUserQueryCmd;
import org.activiti.engine.impl.cmd.DeleteGroupCmd;
import org.activiti.engine.impl.cmd.DeleteMembershipCmd;
import org.activiti.engine.impl.cmd.DeleteUserCmd;
import org.activiti.engine.impl.cmd.DeleteUserInfoCmd;
//import org.activiti.engine.impl.cmd.GetUserAccountCmd;
import org.activiti.engine.impl.cmd.GetUserInfoCmd;
import org.activiti.engine.impl.cmd.GetUserInfoKeysCmd;
import org.activiti.engine.impl.cmd.GetUserPictureCmd;
import org.activiti.engine.impl.cmd.SaveGroupCmd;
import org.activiti.engine.impl.cmd.SaveUserCmd;
import org.activiti.engine.impl.cmd.SetUserInfoCmd;
import org.activiti.engine.impl.cmd.SetUserPictureCmd;
//import org.activiti.engine.impl.identity.Account;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.identity.NativeUserQuery;
import org.activiti.engine.identity.NativeGroupQuery;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.*;
import static com.jcabi.log.Logger.info;


/**
 * @author Tom Baeyens
 */
public class IdentityServiceImpl extends ServiceImpl implements IdentityService {
  
	public IdentityServiceImpl() {
		super();
		info(this,"IdentityServiceImpl.IdentityServiceImpl");
	}

  public NativeUserQuery createNativeUserQuery(){
		throw new RuntimeException("createNativeUserQuery bnot implemented");
	}
  public NativeGroupQuery createNativeGroupQuery(){
		throw new RuntimeException("createNativeGroupQuery bnot implemented");
	}

	public Group newGroup(String groupId) {
		info(this,"IdentityServiceImpl.newGroup");
		return commandExecutor.execute(new CreateGroupCmd(groupId));
	}

	public User newUser(String userId) {
		info(this,"IdentityServiceImpl.newUser");
		return commandExecutor.execute(new CreateUserCmd(userId));
	}

	public void saveGroup(Group group) {
		info(this,"IdentityServiceImpl.saveGroup");
		commandExecutor.execute(new SaveGroupCmd((GroupEntity) group));
	}

	public void saveUser(User user) {
		info(this,"IdentityServiceImpl.saveUser");
		commandExecutor.execute(new SaveUserCmd(user));
	}
  
	public UserQuery createUserQuery() {
		info(this,"IdentityServiceImpl.createUserQuery");
		return commandExecutor.execute(new CreateUserQueryCmd());
	}
  
	public GroupQuery createGroupQuery() {
		info(this,"IdentityServiceImpl.createGroupQuery");
		return commandExecutor.execute(new CreateGroupQueryCmd());
	}

	public void createMembership(String userId, String groupId) {
		info(this,"IdentityServiceImpl.createMembership");
		commandExecutor.execute(new CreateMembershipCmd(userId, groupId));
	}

	public void deleteGroup(String groupId) {
		commandExecutor.execute(new DeleteGroupCmd(groupId));
	}

	public void deleteMembership(String userId, String groupId) {
		commandExecutor.execute(new DeleteMembershipCmd(userId, groupId));
	}

	public boolean checkPassword(String userId, String password) {
		info(this,"IdentityServiceImpl.checkPassword");
		return commandExecutor.execute(new CheckPassword(userId, password));
	}

	public void deleteUser(String userId) {
		commandExecutor.execute(new DeleteUserCmd(userId));
	}

	public void setUserPicture(String userId, Picture picture) {
		commandExecutor.execute(new SetUserPictureCmd(userId, picture));
	}

	public Picture getUserPicture(String userId) {
		return commandExecutor.execute(new GetUserPictureCmd(userId));
	}

	public void setAuthenticatedUserId(String authenticatedUserId) {
		info(this,"IdentityServiceImpl.setAuthenticatedUserId:"+authenticatedUserId);
		Authentication.setAuthenticatedUserId(authenticatedUserId);
	}

	public String getUserInfo(String userId, String key) {
		info(this,"IdentityServiceImpl.getUserInfo:" + userId);
		return commandExecutor.execute(new GetUserInfoCmd(userId, key));
	}

	public List<String> getUserInfoKeys(String userId) {
		info(this,"IdentityServiceImpl.getUserInfoKeys:" + userId);
		return commandExecutor.execute(new GetUserInfoKeysCmd(userId, IdentityInfoEntity.TYPE_USERINFO));
	}

	/*public List<String> getUserAccountNames(String userId) {
		info(this,"getUserAccountNames:" + userId);
		return commandExecutor.execute(new GetUserInfoKeysCmd(userId, IdentityInfoEntity.TYPE_USERACCOUNT));
	}*/

	public void setUserInfo(String userId, String key, String value) {
		info(this,"IdentityServiceImpl.setUserInfo:" + userId);
		commandExecutor.execute(new SetUserInfoCmd(userId, key, value));
	}

	public void deleteUserInfo(String userId, String key) {
		commandExecutor.execute(new DeleteUserInfoCmd(userId, key));
	}

	public void deleteUserAccount(String userId, String accountName) {
		commandExecutor.execute(new DeleteUserInfoCmd(userId, accountName));
	}

/*	public Account getUserAccount(String userId, String userPassword, String accountName) {
		info(this,"getUserAccount:" + userId);
		return commandExecutor.execute(new GetUserAccountCmd(userId, userPassword, accountName));
	}

	public void setUserAccount(String userId, String userPassword, String accountName, String accountUsername, String accountPassword, Map<String, String> accountDetails) {
		info(this,"setUserAccount:" + userId);
		commandExecutor.execute(new SetUserInfoCmd(userId, userPassword, accountName, accountUsername, accountPassword, accountDetails));
	}*/
}
