/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import static com.jcabi.log.Logger.info;

/**
 * Secure FTP configuration
 */
@UriParams
public class VfsConfiguration {

	public static final int DEFAULT_SFTP_PORT = 22;

	@UriParam(defaultValue = "no")
	private String strictHostKeyChecking = "no";

	@UriParam
	private int serverAliveInterval;

	@UriParam(defaultValue = "1")
	private int serverAliveCountMax = 1;

	@UriParam
	private String fileSystem;

	@UriParam
	private String operation;

	@UriParam
	private String host;

	@UriParam
	private String port;

	@UriParam
	private String share;

	@UriParam
	private String domain;

	@UriParam
	private String userName;

	@UriParam
	private String password;

	@UriParam
	private String privateKey;

	@UriParam
	private String remotePath;

	@UriParam
	private String newRemotePath;

	@UriParam
	private String archivePath;

	@UriParam
	private String localPath;

	@UriParam
	private String query;

	@UriParam
	private String append;

	@UriParam
	private String fullPath;

	private Map<String, Object> options;

	public VfsConfiguration() {
	}

	public VfsConfiguration(URI uri) {
	}

	public void setPort(String p) {
		this.port = p;
	}

	public String getPort() {
		return this.port;
	}

	public void setShare(String s) {
		this.share = s;
	}

	public String getShare() {
		return this.share;
	}

	public void setDomain(String d) {
		this.domain = d;
	}

	public String getDomain() {
		return this.domain;
	}

	public void setOperation(String op) {
		this.operation = op;
	}

	public String getOperation() {
		return this.operation;
	}

	public void setFileSystem(String p) {
		this.fileSystem = p;
	}

	public String getFileSystem() {
		return this.fileSystem;
	}

	public void setUserName(String fn) {
		this.userName = fn;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setPassword(String p) {
		this.password = p;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPrivateKey(String p) {
		this.privateKey = p;
	}

	public String getPrivateKey() {
		return this.privateKey;
	}

	public void setHost(String h) {
		this.host = h;
	}

	public String getHost() {
		return this.host;
	}

	public void setAppend(String a) {
		this.append = a;
	}

	public String getAppend() {
		return append;
	}

	public void setFullPath(String a) {
		this.fullPath = a;
	}

	public String getFullPath() {
		return fullPath;
	}

	public String getLocalPath() {
		return this.localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public String getRemotePath() {
		return this.remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getNewRemotePath() {
		return this.newRemotePath;
	}

	public void setNewRemotePath(String newRemotePath) {
		this.newRemotePath = newRemotePath;
	}

	public String getArchivePath() {
		return this.archivePath;
	}

	public void setArchivePath(String archivePath) {
		this.archivePath = archivePath;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}
