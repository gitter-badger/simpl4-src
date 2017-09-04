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
package org.ms123.common.process.indentity;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.NativeUserQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.NativeUserQueryImpl;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TenantEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.permission.api.PermissionService;
import org.apache.commons.lang3.StringUtils;
import static com.jcabi.log.Logger.info;

/**
 *
 * @author Manfred Sattler
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class Simpl4IdentityServiceProvider extends AbstractManager implements ReadOnlyIdentityProvider {

	protected PermissionService permissionService;

	protected AuthService authService;

	public Simpl4IdentityServiceProvider(AuthService as, PermissionService ps) {
		this.authService = as;
		this.permissionService = ps;
	}
	public boolean	checkPassword(String userId, String password) {
		info(this,"Simpl4IdentityServiceProvider.checkPassword("+userId+"):"+password);
		return false;
	}
	public GroupQuery	createGroupQuery() {
		return new GroupQueryImpl(this.authService, this.permissionService);
	}
	public GroupQuery	createGroupQuery(CommandContext commandContext) {
		return new GroupQueryImpl(this.authService, this.permissionService);
	}
  @Override public NativeUserQuery createNativeUserQuery() {
    return new NativeUserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }
	public UserQuery	createUserQuery() {
		return new UserQueryImpl(this.authService);
	}
	public UserQuery	createUserQuery(CommandContext commandContext) {
		return new UserQueryImpl(this.authService);
	}
	public Group	findGroupById(String groupId) {
		if (this.permissionService.hasRole(groupId)) {
			GroupEntity group= convertToGroup(groupId);
			info(this,"Simpl4IdentityServiceProvider.findGroupById("+groupId+"):"+group);
			return group;
		}
		return null;
	}
	public User	findUserById(String userId) {
		if (StringUtils.isNotEmpty(userId)) {
			User user = convertToUser(this.authService.getUser(userId));
			info(this,"Simpl4IdentityServiceProvider.findUserById("+userId+"):"+user);
			return user;
		} else {
			return null;
		}
	}

	public TenantQuery	createTenantQuery() {
		throw new RuntimeException("Simpl4IdentityServiceProvider.createTenantQuery:not implemented");
	}
	public TenantQuery	createTenantQuery(CommandContext commandContext) {
		throw new RuntimeException("Simpl4IdentityServiceProvider.createTenantQuery:not implemented");
	}
	public Tenant	findTenantById(String tenantId) {
		throw new RuntimeException("Simpl4IdentityServiceProvider.findTenantById:not implemented");
	}

	private User convertToUser(Map<String,String> userProps){
		User u =new UserEntity();
		u.setId( userProps.get("userid"));
		return u;
	}

	private GroupEntity convertToGroup(String id) {
		GroupEntity g = new GroupEntity();
		g.setId(id);
		return g;
	}
}
