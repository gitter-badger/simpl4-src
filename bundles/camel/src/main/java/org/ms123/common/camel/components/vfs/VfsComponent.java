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
package org.ms123.common.camel.components.vfs;

import java.net.URI;
import java.util.Map;
import org.apache.camel.Endpoint;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.UriEndpointComponent;

/**
 * Secure FTP Component
 */
public class VfsComponent extends UriEndpointComponent {

	public VfsComponent() {
		super(VfsEndpoint.class);
	}

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		VfsConfiguration configuration = new VfsConfiguration();
		configuration.setAppend((String) parameters.get("append"));
		configuration.setArchivePath((String) parameters.get("archivePath"));
		configuration.setDomain((String) parameters.get("domain"));
		configuration.setShare((String) parameters.get("share"));
		configuration.setFileSystem((String) parameters.get("fileSystem"));
		configuration.setFullPath((String) parameters.get("fullPath"));
		configuration.setHost((String) parameters.get("host"));
		configuration.setLocalPath((String) parameters.get("localPath"));
		configuration.setNewRemotePath((String) parameters.get("newRemotePath"));
		configuration.setPassword((String) parameters.get("password"));
		configuration.setPort((String) parameters.get("port"));
		configuration.setPrivateKey((String) parameters.get("privateKey"));
		configuration.setQuery((String) parameters.get("query"));
		configuration.setRemotePath((String) parameters.get("remotePath"));
		configuration.setUserName((String) parameters.get("userName"));
		setProperties(configuration, parameters);
		Endpoint endpoint = new VfsEndpoint(uri, this, configuration);
		return endpoint;
	}

	@Override
	public void setCamelContext(CamelContext context) {
		super.setCamelContext(context);
	}

	private <T> T getByType(CamelContext ctx, Class<T> kls) {
		return kls.cast(ctx.getRegistry().lookupByName(kls.getName()));
	}
}
