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
package org.ms123.common.enumeration;

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
import org.ms123.common.utils.ParameterParser;
import org.ms123.common.git.GitService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.store.StoreDesc;
import static java.text.MessageFormat.format;

/**
 *
 */
@SuppressWarnings("unchecked")
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

	public List<Map> getEnumerations(String namespace) throws Exception{
		List<String> types = new ArrayList();
		types.add( ENUMERATION_TYPE );
		Map map= m_gitService.getWorkingTree(namespace, ENUMERATIONS_PATH, 100, types, null, null,null);
		List<Map> childList = (List)map.get("children");
		for( Map child : childList){
			String name = (String)child.get("name");
			//String content= m_gitService.getContent(namespace, format(ENTITYTYPE_PATH,name));
			//Map m = (Map)m_ds.deserialize(content);
			Map m = new HashMap();
			m.put("name", name);
			child.putAll(m);
		}
		return childList;
	}

	public Map<String,List>  getEnumeration(String namespace, String name) throws Exception{
		String ret = m_gitService.getContent(namespace, format(ENUMERATION_PATH,name));
		return (Map)m_ds.deserialize(ret );
	}

	public void saveEnumeration(String namespace, String name, Map<String,List> desc) throws Exception{
		m_gitService.putContent(namespace, format(ENUMERATION_PATH,name), ENUMERATION_TYPE, m_js.deepSerialize(desc));
	}


	public void deleteEnumeration(String namespace, String name) throws Exception{
		m_gitService.deleteObject(namespace, format(ENUMERATION_PATH,name));
	}

}
