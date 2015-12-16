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

import java.io.IOException;
import org.apache.camel.Exchange;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 * The Vfs archive(zip,tar,jar) producer.
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class VfsArchiveProducer extends VfsBaseProducer {

	public VfsArchiveProducer(VfsEndpoint endpoint, VfsConfiguration conf) {
		super(endpoint, conf);
		info(this, "VfsArchiveProducer create");
	}

	protected boolean isAppendSupported() {
		return false;
	}

	protected boolean isPutSupported() {
		return false;
	}

	protected void connect(Exchange exchange) throws IOException {
		initFileSystemManager();
		String url = buildConnectionUrl(exchange);
		info(this, "connect:" + url);
		this.remoteRootDirectory = this.fileSystemManager.resolveFile(url);
		info(this, "remoteRootDirectory:" + this.remoteRootDirectory);
		FileSystemOptions opts = getFileSystemOptions(exchange);
	}

	protected FileSystemOptions getFileSystemOptions(Exchange exchange) throws FileSystemException {
		FileSystemConfigBuilder configBuilder = this.fileSystemManager.getFileSystemConfigBuilder(this.configuration.getFileSystem());
		info(this, "FileSystemConfigBuilder:" + configBuilder);
		FileSystemOptions opts = new FileSystemOptions();
		return opts;
	}

	protected String buildConnectionUrl(Exchange exchange) {
		String fileSystem = getStringCheck(exchange, "fileSystem", this.configuration.getFileSystem());
		String archivePath = getStringCheck(exchange, "archivePath", this.configuration.getArchivePath());
		return String.format("%s://%s!", fileSystem, archivePath);
	}
}
