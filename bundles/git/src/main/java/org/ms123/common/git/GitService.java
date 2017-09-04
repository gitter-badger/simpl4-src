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
package org.ms123.common.git;

import java.util.List;
import java.util.Map;
import java.io.File;
import org.ms123.common.rpc.RpcException;

public interface GitService {

	public List getRepositories(List<String> flags, Boolean all) throws RpcException;

	public void createRepository(String name) throws RpcException;

	public void cloneRepository(String name, String fromUri) throws RpcException;

	public void deleteRepository(String name) throws RpcException;

	public void commitAll(String name, String message) throws RpcException;

	public void push(String name) throws RpcException;

	public void pull(String name) throws RpcException;

	public void add(String name, String pattern) throws RpcException;

	public String getFileContent(String repoName, String path) throws Exception;
	public String getFileContent(File file) throws Exception;

	public boolean exists( String repoName, String path) throws RpcException;
	public String getContent(String repoName, String path) throws RpcException;
	public String getContentRaw(String repoName, String path) throws RpcException;
	public String searchContent(String repoName, String name, String type) throws RpcException;
	public File searchFile(String repoName, String name, String type) throws RpcException;

	public Map getContentCheckRaw(String repoName, String path) throws RpcException;

	public void putContent(String repoName, String path, String type, String content) throws RpcException;

	public Map getWorkingTree(String repoName, String path, Integer depth, List<String> includeTypeList, List<String> includePathList, List<String> excludePathList, Map mapping) throws RpcException;

	public void deleteObject(String repoName, String path) throws RpcException;
	public void deleteObjects(String repoName, String directory,String regex) throws RpcException;
	public List<String> assetList( String repoName,  String name,  String type, Boolean onlyFirst) throws RpcException;
	public FileHolderApi getFileHolder( String repoName, String path);
	public void addRemoteOrigin(String repo, String url);

	public void createObjectInternal( String repoName, String path, Boolean overwrite, String content, String type);
	public void putContentInternal(String repoName, String path, String type, String content);
	public void deleteObjectInternal( String repoName, String path);
	public String getFileType(File file);
	public void setStoreProperty( String repoName, String section, String subsection, String key, String value) throws RpcException;
}
