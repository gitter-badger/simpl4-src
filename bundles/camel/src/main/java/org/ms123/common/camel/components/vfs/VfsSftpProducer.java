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
