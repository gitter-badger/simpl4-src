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
