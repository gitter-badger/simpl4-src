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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import org.apache.commons.io.FileUtils;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.ms123.common.camel.components.vfs.result.VfsGetResult;
import org.ms123.common.camel.components.vfs.result.VfsPutResult;
import org.ms123.common.camel.components.vfs.result.VfsDelResult;
import org.ms123.common.camel.components.vfs.result.VfsMoveResult;
import org.ms123.common.camel.components.vfs.result.VfsCopyResult;
import org.ms123.common.camel.components.vfs.result.VfsSearchResult;
import org.ms123.common.camel.components.vfs.result.VfsResult;
import org.ms123.common.camel.components.vfs.result.VfsResultCode;
import org.ms123.common.utils.IOUtils;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 * The Vfs base producer.
 */
@SuppressWarnings({ "unchecked", "deprecation" })
class VfsBaseProducer extends DefaultProducer {

	private VfsOperation operation;
	protected VfsEndpoint endpoint;
	protected VfsConfiguration configuration;
	protected StandardFileSystemManager fileSystemManager;
	protected FileObject remoteRootDirectory;


	public VfsBaseProducer(VfsEndpoint endpoint, VfsConfiguration conf) {
		super(endpoint);
		this.configuration = conf;
		this.endpoint = endpoint;
		String endpointKey = endpoint.getEndpointKey();
		this.operation = VfsOperation.valueOf(this.configuration.getOperation());
	}

	public void process(Exchange exchange) throws Exception {
		info(this, "process:" + this.operation);
		invokeOperation(this.operation, exchange);
	}

	/**
	 * Entry method that selects the appropriate MongoDB operation and executes it
	 * @param operation
	 * @param exchange
	 * @throws Exception
	 */
	protected void invokeOperation(VfsOperation operation, Exchange exchange) throws Exception {
		connect(exchange);
		switch(operation) {
			case get:
				doGet(exchange);
				break;
			case put:
				doPut(exchange);
				break;
			case search:
				doSearch(exchange);
				break;
			case copy:
				doCopy(exchange);
				break;
			case move:
				doMove(exchange);
				break;
			case delete:
				doDelete(exchange);
				break;
			default:
				throw new RuntimeException("VfsProducer.Operation not supported. Value: " + operation);
		}
	}

	private void doSearch(Exchange exchange) throws Exception {
	}

	private void doCopy(Exchange exchange) throws Exception {
		VfsConfiguration c = this.configuration;
		String remotePath = getStringCheck(exchange, "remotePath", c.getRemotePath());
		String newRemotePath = getStringCheck(exchange, "newRemotePath", c.getNewRemotePath());
		copy(remotePath, newRemotePath);
		VfsCopyResult result = new VfsCopyResult();
		result.setResultEntries(remotePath + "-" + newRemotePath);
	}

	private void doMove(Exchange exchange) throws Exception {
		VfsConfiguration c = this.configuration;
		String remotePath = getStringCheck(exchange, "remotePath", c.getRemotePath());
		String newRemotePath = getStringCheck(exchange, "newRemotePath", c.getNewRemotePath());
		move(remotePath, newRemotePath);
		VfsMoveResult result = new VfsMoveResult();
		result.setResultEntries(remotePath + "-" + newRemotePath);
	}

	private void doDelete(Exchange exchange) throws Exception {
		VfsConfiguration c = this.configuration;
		VfsResult result = null;
		String remotePath = getStringCheck(exchange, "remotePath", c.getRemotePath());
		delete(remotePath);
		result = new VfsDelResult();
		result.setResultEntries(remotePath);
	}

	protected void doGet(Exchange exchange) throws Exception {
		VfsConfiguration c = this.configuration;
		boolean fullPath = getBoolean(exchange, "fullPath", toBoolean(c.getFullPath()));
		VfsGetResult getResult = new VfsGetResult();
		String remotePath = getStringCheck(exchange, "remotePath", c.getRemotePath());
		FileObject fo = null;
		Map<String, ByteArrayOutputStream> entries = new HashMap<String, ByteArrayOutputStream>();
		try {
			fo = remoteRootDirectory.resolveFile(remotePath);
			if (fo.getType().equals(FileType.FILE)) {
				getSingleFile(fo, entries, fullPath);
			} else {
				getInFolder(fo, entries, fullPath);
			}
		} finally {
			fo.close();
		}
		getResult.setResultEntries(entries);
		getResult.populateExchange(exchange);
		info(this, "Exchange.entries:" + entries.keySet());
	}

	private void getInFolder(FileObject folder, Map<String, ByteArrayOutputStream> resultEntries, boolean fullPath) throws Exception {
		FileObject[] children = null;
		try {
			children = folder.getChildren();
		} catch (Exception e) {
			throw new RuntimeException("getInFolder:" + folder + ":can get children", e);
		}
		if (children.length == 0) {
			info(this, "getInFolder:" + folder + " is empty");
			return;
		}
		for (FileObject entry : children) {
			if (entry.isFile()) {
				try {
					getSingleFile(entry, resultEntries, fullPath);
				} catch (Exception e) {
					throw new RuntimeException("getInFolder.can't get from " + entry, e);
				}
			} else {
				getInFolder(entry, resultEntries, fullPath);
			}
		}
	}

	private void getSingleFile(FileObject fo, Map<String, ByteArrayOutputStream> entries, boolean fullPath) throws Exception {
		info(this, "getSingleFile.size:" + fo.getContent().getSize());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		fo.getContent().write(os);
		os.close();
		String key = fullPath ? fo.getName().getPath() : fo.getName().getBaseName();
		entries.put(key, os);
	}

	protected void doPut(Exchange exchange) throws Exception {
		VfsConfiguration c = this.configuration;
		String localPath = getStringCheck(exchange, "localPath", c.getLocalPath());
		String remotePath = getStringCheck(exchange, "remotePath", c.getRemotePath());
		boolean append = getBoolean(exchange, "append", toBoolean(c.getAppend()));
		VfsResult result = new VfsPutResult();
		Map<String, VfsResultCode> resultEntries = null;
		String vfsPath = remotePath == null ? localPath : remotePath;
		FileObject entry = null;
		try {
			entry = remoteRootDirectory.resolveFile(vfsPath);
		} catch (Exception e) {
			throw new RuntimeException(vfsPath + " does not exist or can't obtain metadata");
		}
		boolean isBodySrc = "#body".equals(localPath);
		File fileLocalPath = new File(localPath);
		if (isBodySrc || fileLocalPath.isFile()) {
			if (entry != null && entry.exists() && !entry.isFile()) {
				throw new RuntimeException(vfsPath + " exists on remote and is not a file!");
			}
			if (entry == null) {
				if (vfsPath.endsWith("/")) {
					vfsPath = vfsPath + (isBodySrc ? "body" : fileLocalPath.getName());
				}
			}
			entry.close();
			resultEntries = new HashMap<String, VfsResultCode>(1);
			FileObject puttedFile = null;
			try {
				if (isBodySrc) {
					puttedFile = putSingleFile(exchange, vfsPath, append);
				} else {
					puttedFile = putSingleFile(fileLocalPath, vfsPath, append);
				}
				if (puttedFile == null) {
					resultEntries.put(vfsPath, VfsResultCode.KO);
				} else {
					resultEntries.put(vfsPath, VfsResultCode.OK);
				}
			} catch (Exception ex) {
				resultEntries.put(vfsPath, VfsResultCode.KO);
			} finally {
				result.setResultEntries(resultEntries);
				puttedFile.close();
			}
		} else {
			info(this, "doPut:uploading a dir...");
			if (entry != null && entry.exists() && !entry.isFolder()) {
				throw new RuntimeException(vfsPath + " exists on remote and is not a folder!");
			}
			entry.close();
			if (!vfsPath.endsWith("/")) {
				vfsPath = vfsPath + "/";
			}
			String oldVfsPath = vfsPath;
			Collection<File> listFiles = FileUtils.listFiles(fileLocalPath, null, true);
			if (listFiles == null || listFiles.isEmpty()) {
				throw new RuntimeException(localPath + " doesn't contain any files");
			}
			resultEntries = new HashMap<String, VfsResultCode>(listFiles.size());
			for (File file : listFiles) {
				String absPath = file.getAbsolutePath();
				int indexRemainingPath = localPath.length();
				if (!localPath.endsWith("/")) {
					indexRemainingPath += 1;
				}
				String remainingPath = absPath.substring(indexRemainingPath);
				vfsPath = vfsPath + remainingPath;
				FileObject puttedFile = null;
				try {
					info(this, "doPut:uploading:" + fileLocalPath + "," + vfsPath);
					puttedFile = putSingleFile(file, vfsPath, append);
					if (puttedFile == null) {
						resultEntries.put(vfsPath, VfsResultCode.KO);
					} else {
						resultEntries.put(vfsPath, VfsResultCode.OK);
					}
				} catch (Exception ex) {
					error(this, "doPut.error:%[exception]s", ex);
					resultEntries.put(vfsPath, VfsResultCode.KO);
				} finally {
					puttedFile.close();
				}
				vfsPath = oldVfsPath;
			}
			result.setResultEntries(resultEntries);
		}
		result.populateExchange(exchange);
		info(this, "doPut:exchange.headers:" + exchange.getIn().getHeaders());
	}

	private FileObject putSingleFile(Exchange exchange, String vfsPath, boolean append) throws Exception {
		info(this, "putSingleFile:body -> \tvfsPath:" + vfsPath + "\tappend:" + append);
		InputStream is = exchange.getIn().getBody(InputStream.class);
		return putSingleFile(is, vfsPath, append);
	}

	private FileObject putSingleFile(File inputFile, String vfsPath, boolean append) throws Exception {
		info(this, "putSingleFile:inputFile:" + inputFile + "\tvfsPath:" + vfsPath + "\tappend:" + append);
		FileInputStream is = new FileInputStream(inputFile);
		return putSingleFile(is, vfsPath, append);
	}

	private FileObject putSingleFile(InputStream is, String vfsPath, boolean append) throws Exception {
		FileObject fo = remoteRootDirectory.resolveFile(vfsPath);
		if (!fo.exists()) {
			fo.createFile();
		} else if (fo.isFolder()) {
			throw new RuntimeException("Cannot write to a folder:" + vfsPath);
		}
		if (!isAppendSupported()) {
			append = false;
		}
		OutputStream os = fo.getContent().getOutputStream(append);
		IOUtils.copy(is, os);
		is.close();
		os.close();
		return fo;
	}

	protected boolean isAppendSupported() {
		return true;
	}

	protected boolean isPutSupported() {
		return true;
	}

	protected void connect(Exchange exchange) throws IOException {
		initFileSystemManager();
		VfsConfiguration c = this.configuration;
		String userName = getString(exchange, "userName", c.getUserName());
		String password = getString(exchange, "password", c.getPassword());
		String domain = getString(exchange, "domain", c.getDomain());
		FileSystemOptions opts = getFileSystemOptions(exchange);
		if (userName != null || password != null || domain != null) {
			StaticUserAuthenticator auth = new StaticUserAuthenticator(domain, userName, password);
			DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
		}
		String url = buildConnectionUrl(exchange);
		info(this, "connect:" + url);
		this.remoteRootDirectory = this.fileSystemManager.resolveFile(url, opts);
		info(this, "remoteRootDirectory:" + this.remoteRootDirectory);
	}

	protected void initFileSystemManager() throws FileSystemException {
		if (fileSystemManager == null) {
			fileSystemManager = new StandardFileSystemManager();
			fileSystemManager.init();
		}
	}

	protected FileSystemOptions getFileSystemOptions(Exchange exchange) throws FileSystemException {
		FileSystemOptions opts = new FileSystemOptions();
		return opts;
	}

	protected String buildConnectionUrl(Exchange exchange) {
		String host = getStringCheck(exchange, "host", this.configuration.getHost());
		String port = getString(exchange, "port", this.configuration.getPort());
		String fileSystem = this.configuration.getFileSystem();
		if (port != null) {
			return String.format("%s://%s:%s", fileSystem, host, port);
		} else {
			return String.format("%s://%s", fileSystem, host);
		}
	}

	public void disconnect() {
		if (fileSystemManager != null) {
			fileSystemManager.close();
			fileSystemManager = null;
		}
	}

	public void download(String remotePath, Path local) throws IOException {
		LocalFile localFileObject = (LocalFile) fileSystemManager.resolveFile(local.toUri().toString());
		FileObject remoteFileObject = this.remoteRootDirectory.resolveFile(remotePath);
		try {
			localFileObject.copyFrom(remoteFileObject, new AllFileSelector());
		} finally {
			localFileObject.close();
			remoteFileObject.close();
		}
	}

	public void upload(Path local, String remotePath) throws IOException {
		LocalFile localFileObject = (LocalFile) fileSystemManager.resolveFile(local.toUri().toString());
		FileObject remoteFileObject = this.remoteRootDirectory.resolveFile(remotePath);
		try {
			remoteFileObject.copyFrom(localFileObject, new AllFileSelector());
		} finally {
			localFileObject.close();
			remoteFileObject.close();
		}
	}

	public void move(String oldRemotePath, String newRemotePath) throws IOException {
		FileObject remoteOldFileObject = this.remoteRootDirectory.resolveFile(oldRemotePath);
		FileObject newRemoteFileObject = this.remoteRootDirectory.resolveFile(newRemotePath);
		try {
			remoteOldFileObject.moveTo(newRemoteFileObject);
			remoteOldFileObject.close();
		} finally {
			newRemoteFileObject.close();
		}
	}

	public void copy(String oldRemotePath, String newRemotePath) throws IOException {
		FileObject newRemoteFileObject = this.remoteRootDirectory.resolveFile(newRemotePath);
		FileObject oldRemoteFileObject = this.remoteRootDirectory.resolveFile(oldRemotePath);
		try {
			newRemoteFileObject.copyFrom(oldRemoteFileObject, new AllFileSelector());
		} finally {
			oldRemoteFileObject.close();
			newRemoteFileObject.close();
		}
	}

	public void delete(String remotePath) throws IOException {
		FileObject remoteFileObject = this.remoteRootDirectory.resolveFile(remotePath);
		try {
			remoteFileObject.delete();
		} finally {
			remoteFileObject.close();
		}
	}

	public boolean fileExists(String remotePath) throws IOException {
		FileObject remoteFileObject = this.remoteRootDirectory.resolveFile(remotePath);
		try {
			return remoteFileObject.exists();
		} finally {
			remoteFileObject.close();
		}
	}

	public List<String> listChildrenNames(String remotePath) throws IOException {
		return listChildrenNamesByFileType(remotePath, null);
	}

	public List<String> listChildrenFolderNames(String remotePath) throws IOException {
		return listChildrenNamesByFileType(remotePath, FileType.FOLDER);
	}

	public List<String> listChildrenFileNames(String remotePath) throws IOException {
		return listChildrenNamesByFileType(remotePath, FileType.FILE);
	}

	private List<String> listChildrenNamesByFileType(String remotePath, FileType fileType) throws FileSystemException {
		FileObject remoteFileObject = this.remoteRootDirectory.resolveFile(remotePath);
		try {
			FileObject[] fileObjectChildren = remoteFileObject.getChildren();
			List<String> childrenNames = new ArrayList<String>();
			for (FileObject child : fileObjectChildren) {
				if (fileType == null || child.getType() == fileType) {
					childrenNames.add(child.getName().getBaseName());
				}
			}
			return childrenNames;
		} finally {
			remoteFileObject.close();
		}
	}

	protected String getStringCheck(Exchange e, String key, String def) {
		String value = e.getIn().getHeader(key, String.class);
		info(this, "getStringCheck:" + key + "=" + value + "/def:" + def);
		if (value == null) {
			value = e.getProperty(key, String.class);
		}
		if (value == null && def == null) {
			throw new RuntimeException("VfsProducer." + key + "_is_null");
		}
		return value != null ? value : def;
	}

	protected String getString(Exchange e, String key, String def) {
		String value = e.getIn().getHeader(key, String.class);
		if (value == null) {
			value = e.getProperty(key, String.class);
		}
		info(this, "getString:" + key + "=" + value + "\tdef:" + def);
		return value != null ? value : def;
	}

	protected boolean getBoolean(Exchange e, String key, boolean def) {
		Boolean value = e.getIn().getHeader(key, Boolean.class);
		if (value == null) {
			value = e.getProperty(key, Boolean.class);
		}
		info(this, "getBoolean:" + key + "=" + value + "\tdef:" + def);
		return value != null ? value : def;
	}

	protected boolean toBoolean(String s) {
		if (s == null){
			return false;
		}
		s = s.toLowerCase();
		Set<String> trueSet = new HashSet<String>(Arrays.asList("1", "true", "yes"));
		Set<String> falseSet = new HashSet<String>(Arrays.asList("0", "false", "no"));
		if (trueSet.contains(s)) {
			return true;
		}
		if (falseSet.contains(s)) {
			return false;
		}
		throw new IllegalArgumentException(s + " is not a boolean.");
	}
}
