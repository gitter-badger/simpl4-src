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
package org.ms123.common.permission;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import java.util.Comparator;
import java.util.Collections;
import flexjson.*;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.git.GitService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.store.StoreDesc;
import static java.text.MessageFormat.format;

/**
 *
 */
@SuppressWarnings("unchecked")
class GitMetaDataImpl implements MetaData {

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();

	private GitService m_gitService;
	private PermissionServiceImpl m_ps;

	/**
	 */
	public GitMetaDataImpl(PermissionServiceImpl ps, GitService gs) {
		m_ps = ps;
		m_gitService = gs;
		m_js.prettyPrint(true);
	}

	public List<Map> getRoles(String namespace) throws Exception {
		boolean isRuntime = m_ps.m_isRuntimeSystem;		
		List<String> types = new ArrayList();
		types.add(ROLE_TYPE);
		List<Map> childListData=null;
		if( isRuntime ){
			Map map = m_gitService.getWorkingTree(getRepo(namespace,true), ROLES_PATH, 100, types, null, null, null);
			childListData = (List) map.get("children");
		}else{
			childListData = new ArrayList();
		}
		Map map = m_gitService.getWorkingTree(getRepo(namespace,false), ROLES_PATH, 100, types, null, null, null);
		List<Map> childListMeta = (List) map.get("children");
		List<Map> childList = mergeLists( childListData, childListMeta);
		sortListByName(childList);
		List<Map> retList = new ArrayList();
		for (Map child : childList) {
			String name = (String) child.get("name");
			Map m = new HashMap();
			m.put("name", name);
			retList.add(m);
		}
		return retList;
	}


	public Map<String, Object> getRole(String namespace, String name) throws Exception {
		String path =  format(ROLE_PATH, name);
		boolean isRuntime = m_ps.m_isRuntimeSystem;		
		String ret = null;
		if( isRuntime){
			if( m_gitService.exists( getRepo(namespace,true), path)){
				ret = m_gitService.getContent(getRepo(namespace,true), path);
			}else{
				ret = m_gitService.getContent(getRepo(namespace,false), path);
			}
		}else{
			ret = m_gitService.getContent(getRepo(namespace), path);
		}
		return (Map) m_ds.deserialize(ret);
	}

	public void saveRole(String namespace, String name, Map<String, Object> roleData) throws Exception {
		if (roleData == null) {
			roleData = new HashMap();
			roleData.put(PERMISSIONS, new ArrayList());
		} else {
			if (roleData.get(PERMISSIONS) == null) {
				roleData.put(PERMISSIONS, new ArrayList());
			}
		}
		if (roleData.get("name") == null) {
			roleData.put("name", name);
		}
		m_gitService.putContent(getRepo(namespace), format(ROLE_PATH, name), ROLE_TYPE, m_js.deepSerialize(roleData));
	}

	public void deleteRole(String namespace, String name) throws Exception {
		m_gitService.deleteObject(getRepo(namespace), format(ROLE_PATH, name));
	}

	protected void sortListByName(List<Map> list) {
		Collections.sort(list, new ListSortByName());
	}

	protected class ListSortByName implements Comparator<Map> {
		public int compare(Map m1, Map m2) {
			String name1 = (String)m1.get("name");
			String name2 = (String)m2.get("name");
			return name1.compareTo(name2);
		}
	}
	private List mergeLists(List<Map> dataList, List<Map> metaList){
		for( Map<String,String> metaRole : metaList){
			if(!isInList(dataList, metaRole)){
					dataList.add(metaRole);
			}
		}	
		return dataList;
	}

	private boolean isInList( List<Map> dataList, Map<String,String> metaRole){
		for( Map<String,String> dataRole : dataList){
			if(dataRole.get("name").equals(metaRole.get("name"))){
				return true;
			}	
		}
		return false;
	}
	private String getRepo(String namespace, boolean dataRepo){
		StoreDesc sdesc = dataRepo ? StoreDesc.getNamespaceData(namespace) : StoreDesc.getNamespaceMeta(namespace);
		String repo = sdesc.getRepository();
		if( repo == null){
			repo = sdesc.getNamespace();
		}
		return repo;
	}
	private String getRepo(String namespace){
		boolean isRuntime = m_ps.m_isRuntimeSystem;		
		StoreDesc sdesc = isRuntime ? StoreDesc.getNamespaceData(namespace) : StoreDesc.getNamespaceMeta(namespace);
		String repo = sdesc.getRepository();
		if( repo == null){
			repo = sdesc.getNamespace();
		}
		return repo;
	}
}
