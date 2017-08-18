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
package org.ms123.common.process;

import org.camunda.bpm.engine.ProcessEngine;
import org.ms123.common.form.FormService;
import org.ms123.common.git.GitService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.data.api.DataLayer;
import org.osgi.service.event.EventAdmin;

public interface ProcessService {
	public ProcessEngine getRootProcessEngine();
	public ProcessEngine getProcessEngine();
	public ProcessEngine getPE();
	public FormService getFormService();
	public GitService getGitService();
	public PermissionService getPermissionService();
	public DataLayer getDataLayer();
	public EventAdmin getEventAdmin();
}
