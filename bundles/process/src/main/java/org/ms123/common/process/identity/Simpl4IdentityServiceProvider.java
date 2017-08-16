/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014-2017] [Manfred Sattler] <manfred@ms123.org>
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
package org.ms123.common.process.indentity;

import java.util.HashMap;
import java.util.Map;

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
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.permission.api.PermissionService;

/**
 *
 * @author Manfred Sattler
 *
 */
public class Simpl4IdentityServiceProvider extends AbstractManager implements ReadOnlyIdentityProvider {

	protected PermissionService permissionService;

	protected AuthService authService;

	public Simpl4IdentityServiceProvider(AuthService as, PermissionService ps) {
		this.authService = as;
		this.permissionService = ps;
	}
	public boolean	checkPassword(String userId, String password) {
		return false;
	}
	public GroupQuery	createGroupQuery() {
		return null;
	}
	public GroupQuery	createGroupQuery(CommandContext commandContext) {
		return null;
	}
  @Override public NativeUserQuery createNativeUserQuery() {
    return new NativeUserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }
	public UserQuery	createUserQuery() {
		return null;
	}
	public UserQuery	createUserQuery(CommandContext commandContext) {
		return null;
	}
	public Group	findGroupById(String groupId) {
		return null;
	}
	public User	findUserById(String userId) {
		return null;
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


}
