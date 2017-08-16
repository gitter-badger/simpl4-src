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

import java.util.List;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.UserQueryProperty;
import org.camunda.bpm.engine.query.QueryProperty;


/**
 * @author Joram Barrez
 */
public class UserQueryImpl extends AbstractQuery<UserQuery, User> implements UserQuery {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected String[] ids;
  protected String firstName;
  protected String firstNameLike;
  protected String lastName;
  protected String lastNameLike;
  protected String email;
  protected String emailLike;
  protected String groupId;
  protected String procDefId;
  protected String tenantId;

  public UserQueryImpl() {
  }

  public UserQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public UserQuery userId(String id) {
    ensureNotNull("Provided id", id);
    this.id = id;
    return this;
  }

  public UserQuery userIdIn(String... ids) {
    ensureNotNull("Provided ids", (Object[])ids);
    this.ids = ids;
    return this;
  }

  public UserQuery userFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public UserQuery userFirstNameLike(String firstNameLike) {
    ensureNotNull("Provided firstNameLike", firstNameLike);
    this.firstNameLike = firstNameLike;
    return this;
  }

  public UserQuery userLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public UserQuery userLastNameLike(String lastNameLike) {
    ensureNotNull("Provided lastNameLike", lastNameLike);
    this.lastNameLike = lastNameLike;
    return this;
  }

  public UserQuery userEmail(String email) {
    this.email = email;
    return this;
  }

  public UserQuery userEmailLike(String emailLike) {
    ensureNotNull("Provided emailLike", emailLike);
    this.emailLike = emailLike;
    return this;
  }

  public UserQuery memberOfGroup(String groupId) {
    ensureNotNull("Provided groupId", groupId);
    this.groupId = groupId;
    return this;
  }

  public UserQuery potentialStarter(String procDefId) {
    ensureNotNull("Provided processDefinitionId", procDefId);
    this.procDefId = procDefId;
    return this;

  }

  public UserQuery memberOfTenant(String tenantId) {
    ensureNotNull("Provided tenantId", tenantId);
    this.tenantId = tenantId;
    return this;
  }

  //sorting //////////////////////////////////////////////////////////

  public UserQuery orderByUserId() {
    return orderBy(UserQueryProperty.USER_ID);
  }

  public UserQuery orderByUserEmail() {
    return orderBy(UserQueryProperty.EMAIL);
  }

  public UserQuery orderByUserFirstName() {
    return orderBy(UserQueryProperty.FIRST_NAME);
  }

  public UserQuery orderByUserLastName() {
    return orderBy(UserQueryProperty.LAST_NAME);
  }

	public UserQuery  orderBy(QueryProperty s){
		return this;
	}

  //getters //////////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  public String[] getIds() {
    return ids;
  }
  public String getFirstName() {
    return firstName;
  }
  public String getFirstNameLike() {
    return firstNameLike;
  }
  public String getLastName() {
    return lastName;
  }
  public String getLastNameLike() {
    return lastNameLike;
  }
  public String getEmail() {
    return email;
  }
  public String getEmailLike() {
    return emailLike;
  }
  public String getGroupId() {
    return groupId;
  }
  public String getTenantId() {
    return tenantId;
  }

	public List<User> executeList(CommandContext c,Page p){
		return null;
	}
  public  long executeCount(CommandContext commandContext){
		return -1;
	}
}
