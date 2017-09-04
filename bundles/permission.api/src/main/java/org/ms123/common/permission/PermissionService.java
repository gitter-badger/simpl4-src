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
package org.ms123.common.permission.api;

import java.util.Map;
import java.util.List;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.rpc.RpcException;
import org.apache.shiro.authz.Permission;

public interface PermissionService {
	public final String PERMISSION_SERVICE = "permissionService";
	public String PERMITTED_USERS = "permittedUsers";
	public String PERMITTED_ROLES = "permittedRoles";

	public boolean login(String appName, String username, String password);

	public boolean loginInternal(String appName);
	public boolean loginInternal(String appName, String username,String password);

	public boolean isPermitted(String permission);

	public boolean isPermitted(Permission wp);

	public boolean hasRole(String role);

	public boolean hasAdminRole();
	public boolean isUserThis(String name);
	public boolean hasUserRole(String name);

	public Map getRole( String namespace, String name) throws RpcException;
	public List getAllRolesInternal() throws Exception;

	public List<String> getUserRoles( String name) throws Exception;

	public boolean hasEntityPermissions(StoreDesc sdesc, String entity, String actions);

	public Map<String, Object> permissionFieldMapFilter(StoreDesc sdesc, String entity, Map<String, Object> fieldMap, String actions);

	public List<Map> permissionFieldListFilter(StoreDesc sdesc, String entity, List<Map> fieldList, String fieldKey, String actions);
	public List<String> permissionFieldListFilter(StoreDesc sdesc, String entity, List<String> fieldList, String actions);
	public List<Map> getAccessPermissionsForFileList(String namespace,  List<String> filenames) throws RpcException;
	public boolean isFileAccesPermitted(String userName, List<String> permittedUserList, List<String> permittedRoleList);
}
