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
package org.ms123.common.process.camel;

import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.process.api.ProcessService;
import static com.jcabi.log.Logger.info;

/**
 */
public class ProcessComponent extends DefaultComponent {

	private ProcessService processService;

	private PermissionService permissionService;

	public ProcessComponent() {
		info(this,"new ProcessComponent");
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
		info(this,"createEndpoint("+uri+"):"+parameters);
		CamelContext cc = getCamelContext();
		getServices(cc);
		ProcessEndpoint endpoint = new ProcessEndpoint(uri, cc, this.processService, this.permissionService);
		setProperties(endpoint, parameters);
		return endpoint;
	}
}

