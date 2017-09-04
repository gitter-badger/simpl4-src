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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.GroupQueryProperty;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.query.QueryProperty;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.permission.api.PermissionService;


/**
 * @author Joram Barrez
 */
public class GroupQueryImpl extends AbstractQuery<GroupQuery, Group> implements GroupQuery {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected String[] ids;
  protected String name;
  protected String nameLike;
  protected String type;
  protected String userId;
  protected String procDefId;
  protected String tenantId;
	protected AuthService authService;
	protected PermissionService permissionService;

  public GroupQueryImpl(AuthService auth, PermissionService ps) {
		this.authService = auth;
		this.permissionService = ps;
  }

  public GroupQuery groupId(String id) {
    ensureNotNull("Provided id", id);
    this.id = id;
    return this;
  }

  public GroupQuery groupIdIn(String... ids) {
    ensureNotNull("Provided ids", (Object[]) ids);
    this.ids = ids;
    return this;
  }

  public GroupQuery groupName(String name) {
    ensureNotNull("Provided name", name);
    this.name = name;
    return this;
  }

  public GroupQuery groupNameLike(String nameLike) {
    ensureNotNull("Provided nameLike", nameLike);
    this.nameLike = nameLike;
    return this;
  }

  public GroupQuery groupType(String type) {
    ensureNotNull("Provided type", type);
    this.type = type;
    return this;
  }

  public GroupQuery groupMember(String userId) {
    ensureNotNull("Provided userId", userId);
    this.userId = userId;
    return this;
  }

  public GroupQuery potentialStarter(String procDefId) {
    ensureNotNull("Provided processDefinitionId", procDefId);
    this.procDefId = procDefId;
    return this;
  }

  public GroupQuery memberOfTenant(String tenantId) {
    ensureNotNull("Provided tenantId", tenantId);
    this.tenantId = tenantId;
    return this;
  }

  //sorting ////////////////////////////////////////////////////////

  public GroupQuery orderByGroupId() {
    return orderBy(GroupQueryProperty.GROUP_ID);
  }

  public GroupQuery orderByGroupName() {
    return orderBy(GroupQueryProperty.NAME);
  }

  public GroupQuery orderByGroupType() {
    return orderBy(GroupQueryProperty.TYPE);
  }
	public GroupQuery  orderBy(QueryProperty s){
		return this;
	}

  //getters ////////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public String getNameLike() {
    return nameLike;
  }
  public String getType() {
    return type;
  }
  public String getUserId() {
    return userId;
  }
  public String getTenantId() {
    return tenantId;
  }

	public List<Group> executeList(CommandContext c,Page p){
		return null;
	}
  public  long executeCount(CommandContext commandContext){
		return -1;
	}
}
