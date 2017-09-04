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

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.auth.api.AuthService;

public class Simpl4UserManagerFactory implements SessionFactory {

	protected PermissionService m_permissionService;

	protected AuthService m_authService;

	public Simpl4UserManagerFactory(AuthService as, PermissionService ps) {
		m_authService = as;
		m_permissionService = ps;
	}

	@Override
	public Class<?> getSessionType() {
    return UserIdentityManager.class;
	}

	@Override
	public Session openSession() {
		return new Simpl4UserEntityManager(m_authService, m_permissionService);
	}
}
