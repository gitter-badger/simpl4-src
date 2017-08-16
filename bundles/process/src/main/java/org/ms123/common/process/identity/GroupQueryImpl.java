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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.GroupQueryProperty;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.query.QueryProperty;


/**
 * @author Joram Barrez
 */
public abstract class GroupQueryImpl extends AbstractQuery<GroupQuery, Group> implements GroupQuery {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected String[] ids;
  protected String name;
  protected String nameLike;
  protected String type;
  protected String userId;
  protected String procDefId;
  protected String tenantId;

  public GroupQueryImpl() {
  }

  public GroupQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
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

}
