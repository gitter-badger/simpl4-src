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
package org.ms123.common.namespace;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import flexjson.*;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.git.GitService;
import org.ms123.common.store.StoreDesc;
import static java.text.MessageFormat.format;
import static org.apache.commons.io.FileUtils.readFileToString;

/**
 *
 */
@SuppressWarnings("unchecked")
class GitMetaDataImpl implements MetaData {

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();

	private GitService m_gitService;

	/**
	 */
	public GitMetaDataImpl(GitService gs) {
		m_gitService = gs;
		m_js.prettyPrint(true);
	}

	public List<Map> getNamespaces() throws Exception {
		StoreDesc sdesc = StoreDesc.getGlobalData();
		List<String> types = new ArrayList();
		types.add(NAMESPACE_TYPE);
		Map map = m_gitService.getWorkingTree(sdesc.getRepository(), NAMESPACES_PATH, 100, types, null, null, null);
		List<Map> childList = (List) map.get("children");
		for (Map child : childList) {
			String name = (String) child.get("name");
			Map m = new HashMap();
			m.put("name", name);
			child.putAll(m);
			child.remove("children");
			child.remove("path");
		}
		return childList;
	}

	public Map<String, String> getBranding() throws Exception {
		String swDir = System.getProperty("simpl4.dir");
		File file = new File(swDir, BRANDING_PATH);
		if( !file.exists()){
			file = new File(swDir, BRANDING_EXAMPLE_PATH);
		}
		String ret = readFileToString(file);
		return (Map) m_ds.deserialize(ret);
	}

	public void saveBranding(Map<String, String> desc) throws Exception {
		StoreDesc sdesc = StoreDesc.getGlobalData();
		m_gitService.putContent(sdesc.getRepository(), BRANDING_PATH, "sw.setting", m_js.deepSerialize(desc));
	}

	public Map<String, List> getNamespace(String name) throws Exception {
		StoreDesc sdesc = StoreDesc.getGlobalData();
		String ret = m_gitService.getContent(sdesc.getRepository(), format(NAMESPACE_PATH, name));
		return (Map) m_ds.deserialize(ret);
	}

	public void saveNamespace(String name, Map<String, List> desc) throws Exception {
		StoreDesc sdesc = StoreDesc.getGlobalData();
		m_gitService.putContent(sdesc.getRepository(), format(NAMESPACE_PATH, name), NAMESPACE_TYPE, m_js.deepSerialize(desc));
	}

	public void deleteNamespace(String name) throws Exception {
		StoreDesc sdesc = StoreDesc.getGlobalData();
		m_gitService.deleteObject(sdesc.getRepository(), format(NAMESPACE_PATH, name));
	}
}
