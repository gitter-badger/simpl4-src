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
