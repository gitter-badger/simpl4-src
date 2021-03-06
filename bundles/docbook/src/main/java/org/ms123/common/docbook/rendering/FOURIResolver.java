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
package org.ms123.common.docbook.rendering;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.ms123.common.git.GitService;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;

public class FOURIResolver implements ResourceResolver {
	protected GitService m_gitService;
	protected String m_namespace;
	protected StandardFileSystemManager fileSystemManager;

	public FOURIResolver(GitService gs, String namespace) {
		m_gitService = gs;
		m_namespace = namespace;
		initFileSystemManager();
	}

	public Resource getResource(URI href) throws IOException {
		info(this,"[FOURIResolver.resolve: href=" + href + "]");
		String path = href.toString();
		String type = null;
		if (path.endsWith(".svg")) {
			type = "image/svg+xml";
		}
		if (path.endsWith(".png")) {
			type = "image/png";
		}
		if (path.endsWith(".jpg")) {
			type = "image/jpg";
		}
		if (path.endsWith(".jepg")) {
			type = "image/jpg";
		}
		if (path.endsWith(".swf")) {
			type = "image/swf";
		}
		if (path.endsWith(".pdf")) {
			type = "image/pdf";
		}
		try {
			if (path.startsWith("repo:")) {
				path = path.substring(5);
				String namespace = m_namespace;
				int delim = path.indexOf("!");
				if( delim == -1){
					delim = path.indexOf(":");
				}
				if (delim > 0) {
					namespace = path.substring(0, delim);
					path = path.substring(delim + 1);
				}
				info(this,"FOURIResolver.searchFile:" + namespace + "|" + path);
				File _file = m_gitService.searchFile(namespace, path, type);
				return new Resource(type, new FileInputStream(_file));
			} else {
				FileObject fo = this.fileSystemManager.resolveFile(path, getFileSystemOptions(href));
				info(this,"FileObject("+href+").exists:" + fo.exists());
				return new Resource(type, fo.getContent().getInputStream());
			}
		} catch (Exception e) {
			if (path.indexOf("en.hyp") < 0) {
				error(this,"FOURIResolver.getResource:(" + href + "):%[exception]s" , e);
			}
		}
		return null;
	}

	public OutputStream getOutputStream(URI uri) throws IOException {
		throw new RuntimeException("ResourceResolver.getOutputStream:not implemented");
	}

	protected FileSystemOptions getFileSystemOptions(URI href) throws Exception {
		FileSystemOptions opts = new FileSystemOptions();
		String au = href.getAuthority();
		if (au == null) {
			return opts;
		}
		String us = StringUtils.substringBefore(au, ":");
		String pw = StringUtils.substringBetween(au, ":", "@");
		StaticUserAuthenticator auth = new StaticUserAuthenticator(null, us, pw);
		DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
		return opts;
	}

	protected void initFileSystemManager() {
		if (this.fileSystemManager == null) {
			try {
				this.fileSystemManager = new StandardFileSystemManager();
				this.fileSystemManager.init();
			} catch (FileSystemException e) {
				e.printStackTrace();
			}
		}
	}
}
