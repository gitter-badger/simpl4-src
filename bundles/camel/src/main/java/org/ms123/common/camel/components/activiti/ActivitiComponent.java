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
package org.ms123.common.camel.components.activiti;

import java.util.Map;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.ProcessEngine;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.workflow.api.WorkflowService;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
public class ActivitiComponent extends org.activiti.camel.ActivitiComponent {

	private WorkflowService workflowService;

	private PermissionService permissionService;

	public ActivitiComponent() {
	}

	@Override
	public void setCamelContext(CamelContext context) {
		super.setCamelContext(context);
		getServices(context);
	}

	private void getServices(CamelContext context) {
		if (this.workflowService == null) {
			this.permissionService = getByType(context, PermissionService.class);
			this.workflowService = getByType(context, WorkflowService.class);
			info(this,"PermissionService:" + this.permissionService);
			info(this,"WorkflowService:" + this.workflowService);
		}
	}

	private <T> T getByType(CamelContext ctx, Class<T> kls) {
		return kls.cast(ctx.getRegistry().lookupByName(kls.getName()));
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		CamelContext cc = getCamelContext();
		getServices(cc);
		ActivitiEndpoint endpoint = new ActivitiEndpoint(uri, cc, this.workflowService, this.permissionService);
		info(this,"createEndpoint("+uri+"):"+parameters);
		setProperties(endpoint, parameters);
		return endpoint;
	}
}

