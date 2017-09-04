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
package org.ms123.common.reporting;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import flexjson.*;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.ParameterParser;
import org.ms123.common.git.GitService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.store.StoreDesc;
import static java.text.MessageFormat.format;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.info;

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

	public List<Map> getReports(String namespace) throws Exception{
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace);
		List<String> types = new ArrayList();
		types.add( REPORT_TYPE );
		String user = getUserName();
		Map map= m_gitService.getWorkingTree(sdesc.getRepository(), format(REPORTS_USER_PATH,user), 100, types, null, null,null);
		List<Map> childList = (List)map.get("children");
		List<Map> retList = new ArrayList();
		for( Map child : childList){
			String name = (String)child.get("name");
			String content= m_gitService.getContent(sdesc.getRepository(), format(REPORT_USER_PATH,user,name));
			Map m = new HashMap();
			if( content != null && !content.trim().equals("")){
				m = (Map)m_ds.deserialize(content);
			}
			m.put("name", name);
			retList.add(m);
		}
		return retList;
	}

	public Map<String,List>  getReport(String namespace, String name) throws Exception{
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace);
		String user = getUserName();
		String ret = m_gitService.getContent(sdesc.getRepository(), format(REPORT_USER_PATH,user,name));
		return (Map)m_ds.deserialize(ret );
	}

	public void saveReport(String namespace, String name, Map<String,List> desc) throws Exception{
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace);
		String user = getUserName();
		m_gitService.putContentInternal(sdesc.getRepository(), format(REPORT_USER_PATH,user,name), m_js.deepSerialize(desc), REPORT_TYPE);
	}

	public void deleteReport(String namespace, String name) throws Exception{
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace);
		String user = getUserName();
		m_gitService.deleteObjectInternal(sdesc.getRepository(), format(REPORT_USER_PATH,user,name));
	}

	public String getUserName() {
		return org.ms123.common.system.thread.ThreadContext.getThreadContext().getUserName();
	}
}
