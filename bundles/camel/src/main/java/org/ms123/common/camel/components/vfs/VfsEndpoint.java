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

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import org.apache.camel.Consumer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriParam;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 * Represents a Vfs endpoint.
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class VfsEndpoint extends DefaultEndpoint {

	private Set<String> archiveSet = new HashSet<String>(Arrays.asList("zip", "jar", "tar"));

	private VfsComponent component = null;
	private VfsConsumer vfsConsumer;

	@UriParam
	private VfsConfiguration configuration;

	public VfsEndpoint() {
	}

	public VfsEndpoint(String uri, VfsComponent component, VfsConfiguration configuration) {
		super(uri, component);
		this.configuration = configuration;
		this.component = component;
		uri = uri.replaceFirst("^(vfs://|vfs:)", "");
		int qm = uri.indexOf("?");
		if (qm > -1) {
			uri = uri.substring(0, qm);
		}
		configuration.setOperation(uri);
	}

	public VfsEndpoint(String endpointUri) {
		super(endpointUri);
	}

	public Producer createProducer() throws Exception {
		String fileSystem = this.configuration.getFileSystem();
		info(this, "VfsEndpoint create Producer:" + fileSystem);
		if ("sftp".equals(fileSystem)) {
			return new VfsSftpProducer(this, this.configuration);
		} else if ("smb".equals(fileSystem)) {
			return new VfsSmbProducer(this, this.configuration);
		} else if (archiveSet.contains(fileSystem)) {
			return new VfsArchiveProducer(this, this.configuration);
		} else {
			return new VfsBaseProducer(this, this.configuration);
		}
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		info(this, "VfsEndpoint create Cosumer");
		return new VfsConsumer(this, processor, configuration);
	}

	public boolean isLenientProperties() {
		return true;
	}

	public boolean isSingleton() {
		return false;
	}

	public String getScheme() {
		return "vfs";
	}
}
