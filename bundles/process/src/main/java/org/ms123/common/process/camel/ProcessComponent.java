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
package org.ms123.common.process.camel;

import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.ProcessEngine;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.process.ProcessService;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import org.ms123.common.process.camel.base.BaseComponent;

/**
 */
public class ProcessComponent extends BaseComponent {

	private ProcessService processService;

	private PermissionService permissionService;

	public ProcessComponent() {
	}

	@Override
	public void setCamelContext(CamelContext context) {
		super.setCamelContext(context);
		getServices(context);
	}

	private void getServices(CamelContext context) {
		if (this.processService == null) {
			this.permissionService = getByType(context, PermissionService.class);
			this.processService = getByType(context, ProcessService.class);
			info(this,"PermissionService:" + this.permissionService);
			info(this,"processService:" + this.processService);
		}
	}

	private <T> T getByType(CamelContext ctx, Class<T> kls) {
		return kls.cast(ctx.getRegistry().lookupByName(kls.getName()));
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		CamelContext cc = getCamelContext();
		getServices(cc);
		ProcessEndpoint endpoint = new ProcessEndpoint(uri, cc, this.processService, this.permissionService);
		info(this,"createEndpoint("+uri+"):"+parameters);
		setProperties(endpoint, parameters);
		return endpoint;
	}
}

