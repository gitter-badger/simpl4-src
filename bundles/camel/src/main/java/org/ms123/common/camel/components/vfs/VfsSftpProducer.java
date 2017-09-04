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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.camel.Exchange;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 * The Vfs base producer.
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class VfsSftpProducer extends VfsBaseProducer {

	private Path knownHosts;

	public VfsSftpProducer(VfsEndpoint endpoint, VfsConfiguration conf) {
		super(endpoint, conf);
		info(this, "VfsSftpProducer create");
	}

	public void setKnownHosts(Path knownHosts) {
		this.knownHosts = knownHosts;
	}

	protected boolean isAppendSupported() {
		return false;
	}

	protected FileSystemOptions getFileSystemOptions(Exchange exchange) throws FileSystemException {
		SftpFileSystemConfigBuilder sftpConfigBuilder = SftpFileSystemConfigBuilder.getInstance();
		FileSystemOptions opts = new FileSystemOptions();
		sftpConfigBuilder.setUserDirIsRoot(opts, false);
		sftpConfigBuilder.setStrictHostKeyChecking(opts, "no");
		String privateKey = getString(exchange, "privateKey", this.configuration.getPrivateKey());
		if (knownHosts != null) {
			sftpConfigBuilder.setKnownHosts(opts, knownHosts.toFile());
		} else {
			sftpConfigBuilder.setKnownHosts(opts, new File("~/.ssh/known_hosts"));
		}
		if (privateKey != null) {
			Path path = Paths.get(privateKey);
			sftpConfigBuilder.setIdentities(opts, new File[] { path.toFile() });
		}
		return opts;
	}

	protected String buildConnectionUrl(Exchange exchange) {
		String host = getStringCheck(exchange, "host", this.configuration.getHost());
		String port = getString(exchange, "port", this.configuration.getPort());
		if (port == null) {
			return String.format("sftp://%s", host);
		} else {
			return String.format("sftp://%s:%s", host, port);
		}
	}
}
