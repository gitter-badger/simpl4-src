/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ms123.common.workflow;

import java.util.List;
import java.util.Map;

import org.flowable.engine.IdentityService;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.GroupQuery;
import org.flowable.idm.api.Picture;
import org.flowable.idm.api.User;
import org.flowable.idm.api.UserQuery;
import org.flowable.idm.engine.impl.cmd.CheckPassword;
import org.flowable.idm.engine.impl.cmd.CreateGroupCmd;
import org.flowable.idm.engine.impl.cmd.CreateGroupQueryCmd;
import org.flowable.idm.engine.impl.cmd.CreateMembershipCmd;
import org.flowable.idm.engine.impl.cmd.CreateUserCmd;
import org.flowable.idm.engine.impl.cmd.CreateUserQueryCmd;
import org.flowable.idm.engine.impl.cmd.DeleteGroupCmd;
import org.flowable.idm.engine.impl.cmd.DeleteMembershipCmd;
import org.flowable.idm.engine.impl.cmd.DeleteUserCmd;
import org.flowable.idm.engine.impl.cmd.DeleteUserInfoCmd;
//import org.flowable.engine.impl.cmd.GetUserAccountCmd;
import org.flowable.idm.engine.impl.cmd.GetUserInfoCmd;
import org.flowable.idm.engine.impl.cmd.GetUserInfoKeysCmd;
import org.flowable.idm.engine.impl.cmd.GetUserPictureCmd;
import org.flowable.idm.engine.impl.cmd.SaveGroupCmd;
import org.flowable.idm.engine.impl.cmd.SaveUserCmd;
import org.flowable.idm.engine.impl.cmd.SetUserInfoCmd;
import org.flowable.idm.engine.impl.cmd.SetUserPictureCmd;
//import org.flowable.engine.impl.identity.Account;
import org.flowable.engine.impl.identity.Authentication;
import org.flowable.idm.engine.impl.persistence.entity.GroupEntity;
import org.flowable.idm.api.NativeUserQuery;
import org.flowable.idm.api.NativeGroupQuery;
import org.flowable.idm.engine.impl.persistence.entity.IdentityInfoEntity;
import org.flowable.engine.impl.*;
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
	public  List<Group> getPotentialStarterGroups(String processDefinitionId){
		throw new RuntimeException("getPotentialStarterGroups bnot implemented");
	}
	public List<User> getPotentialStarterUsers(String processDefinitionId){
		throw new RuntimeException("getPotentialStarterUsers bnot implemented");
	}

	public void updateUserPassword(User user){
		throw new RuntimeException("updateUserPassword bnot implemented");
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
