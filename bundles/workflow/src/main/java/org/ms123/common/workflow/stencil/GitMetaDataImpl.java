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
package org.ms123.common.workflow.stencil;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import java.io.*;
import flexjson.*;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.ParameterParser;
import org.ms123.common.git.GitService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.libhelper.Base64;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.IOUtils;
import static java.text.MessageFormat.format;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
class GitMetaDataImpl implements MetaData {

	protected Inflector m_inflector = Inflector.getInstance();

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();

	private GitService m_gitService;

	/**
	 */
	public GitMetaDataImpl(GitService gs) {
		m_gitService = gs;
		m_js.prettyPrint(true);
	}

	public List<Map> getAddonStencils(String namespace) throws Exception {
		List<String> types = new ArrayList();
		types.add(STENCIL_TYPE);
		Map map = m_gitService.getWorkingTree(namespace, STENCILS_PATH, 100, types, null, null, null);
		List<Map> childList = (List) map.get("children");
		for (Map child : childList) {
			String name = (String) child.get("name");
			String content = m_gitService.getContent(namespace, format(STENCIL_PATH, name));
			Map m = (Map) m_ds.deserialize(content);
			m.put("name", name);
			//m.put("view", getStencilView(PROCESS_SS, "activity/servicetask.svg"));
			//m.put("icon", getStencilIcon(PROCESS_SS, "activity/list/type.service.png"));
			m.put("view", getStencilView(PROCESS_SS, (String) m.get("view")));
			m.put("icon", getStencilIcon(PROCESS_SS, (String) m.get("icon")));
			child.putAll(m);
		}
		return childList;
	}

	public Map<String, Object> getAddonStencil(String namespace, String name) throws Exception {
		String ret = m_gitService.getContent(namespace, format(STENCIL_PATH, baseName(name)));
		return (Map) m_ds.deserialize(ret);
	}

	public void saveAddonStencil(String namespace, String name, Map<String, Object> desc) throws Exception {
		m_gitService.putContent(namespace, format(STENCIL_PATH, baseName(name)), STENCIL_TYPE, m_js.deepSerialize(desc));
	}
	
	private String baseName(String name){
		int index = name.lastIndexOf("/");
		if( index != -1){
			name = name.substring(index+1);
		}
		return name;
	}

	public void deleteAddonStencil(String namespace, String name) throws Exception {
		m_gitService.deleteObject(namespace, format(STENCIL_PATH, baseName(name)));
	}

	public String getStencilView(String ssname, String viewpath) throws Exception {
		String gitSpace = System.getProperty("git.repos");
		File file = new File(gitSpace + "/global/stencilsets/", ssname + "/view/" + viewpath);
		InputStream is = new FileInputStream(file);
		Reader in = new InputStreamReader(is, "UTF-8");
		StringWriter sw = new StringWriter();
		IOUtils.copy(in, sw);
		return sw.toString();
	}

	public String getStencilIcon(String ssname, String iconpath) throws Exception {
		String gitSpace = System.getProperty("git.repos");
		File file = new File(gitSpace + "/global/stencilsets/", ssname + "/icons/" + iconpath);
		InputStream is = new FileInputStream(file);
		return "data:image/png;base64," + Base64.encode(is);
	}

}
