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
package org.ms123.common.entity;

import flexjson.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.git.GitService;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.utils.UtilsService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.enumeration.EnumerationService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.utils.ParameterParser;
import flexjson.JSONDeserializer;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 *
 */
@SuppressWarnings("unchecked")
class OrientDBEntityServiceImpl implements org.ms123.common.entity.api.Constants {

	protected Inflector m_inflector = Inflector.getInstance();

	protected DataLayer m_dataLayer;

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected AuthService m_authService;

	protected PermissionService m_permissionService;

	protected EnumerationService m_enumerationService;

	protected NucleusService m_nucleusService;

	protected GitService m_gitService;

	protected UtilsService m_utilsService;

	protected MetaData m_gitMetaData;

	public List getEntities(StoreDesc sdesc, Boolean withChilds, Boolean withTeam, Map mapping, String filter, String sortField) throws Exception {
		List<Map> entTypes = m_gitMetaData.getEntitytypes(sdesc.getStoreId());
		info(this,"entTypes:"+entTypes);
		Map<String, Map> modMaps = toMap(entTypes, "name");
		List<Map> retList = new ArrayList();
		for (Map map : entTypes) {
			Object enabled = map.get("enabled");
			String mn = m_inflector.getEntityName(map.get("name"));
			info(this,"sdesc:"+sdesc);
			info(this,"map:"+map);
			info(this,"Permission:"+m_permissionService.hasEntityPermissions(sdesc, mn, "read"));
			if (enabled != null && ((Boolean) enabled) == true && m_permissionService.hasEntityPermissions(sdesc, mn, "read")) {
				retList.add(map);
			}
		}
		return retList;
	}

	public Map getEntityTree(StoreDesc sdesc, String mainEntity, int maxlevel, Boolean _pathid, String _type, Boolean _listResolved) throws Exception {
		return new HashMap();
	}

	public List<Map> getFields(StoreDesc sdesc, String entityName, Boolean withAutoGen, Boolean withAllRelations) throws Exception {
		try {
			List<Map> metaFields = m_gitMetaData.getFields(sdesc.getStoreId(), entityName);
			if (metaFields == null) {
				return new ArrayList();
			}
			sortListToName(metaFields);
			return metaFields;
		} catch (Exception e) {
			throw e;
		} finally {
		}
	}

	private void sortListToIndex(List list) {
		Collections.sort(list, new ListSortByIndex());
	}

	private void sortListToName(List list) {
		Collections.sort(list, new ListSortByName());
	}

	private class ListSortByIndex implements Comparator<Map> {

		public int compare(Map o1, Map o2) {
			if (o1.get("index") == null || o2.get("index") == null) {
				return 0;
			}
			int index1 = getIntFromObject(o1.get("index"));
			int index2 = getIntFromObject(o2.get("index"));
			return (int) (index1 - index2);
		}
	}

	private class ListSortByName implements Comparator<Map> {

		public int compare(Map o1, Map o2) {
			if (o1.get("name") == null || o2.get("name") == null) {
				return 0;
			}
			String s1 = (String) o1.get("name");
			String s2 = (String) o2.get("name");
			return s1.compareTo(s2);
		}
	}

	private int getIntFromObject(Object object) {
		if (object instanceof Long) {
			return ((Long) object).intValue();
		}
		if (object instanceof Integer) {
			return ((Integer) object).intValue();
		}
		return -1;
	}

	private <T extends Map> Map<String, T> toMap(List<T> list, String key) {
		Map<String, T> retMap = new HashMap();
		if (list == null) {
			return retMap;
		}
		for (T m : list) {
			retMap.put((String) m.get(key), m);
		}
		return retMap;
	}

	private boolean isEmpty(String s){
		if( s == null|| s.trim().length()==0) return true;
		return false;
	}

	private String getString(Map m, String key, String _def) {
		try {
			if (m.get(key) != null) {
				return (String) m.get(key);
			}
		} catch (Exception e) {
		}
		return _def;
	}

	public void setDataLayer(DataLayer dataLayer) {
		info(this,"OrientDBEntityServiceImpl.setDataLayer:" + dataLayer);
		m_dataLayer = dataLayer;
	}

	public void setGitService(GitService gitService) {
		info(this,"OrientDBEntityServiceImpl.setGitService:" + gitService);
		this.m_gitService = gitService;
	}

	public void setPermissionService(PermissionService paramPermissionService) {
		this.m_permissionService = paramPermissionService;
		info(this,"OrientDBEntityServiceImpl.setPermissionService:" + paramPermissionService);
	}

	public void setAuthService(AuthService paramService) {
		this.m_authService = paramService;
		info(this,"OrientDBEntityServiceImpl.setAuthService:" + paramService);
	}

	public void setUtilsService(UtilsService paramUtilsService) {
		this.m_utilsService = paramUtilsService;
		info(this,"OrientDBEntityServiceImpl.setUtilsService:" + paramUtilsService);
	}

	public void setEnumerationService(EnumerationService param) {
		this.m_enumerationService = param;
		info(this,"OrientDBEntityServiceImpl.setEnumerationService:" + param);
	}

	public void setNucleusService(NucleusService paramService) {
		this.m_nucleusService = paramService;
		info(this,"OrientDBEntityServiceImpl.setNucleusService:" + paramService);
	}

	public void setGitMetadata( MetaData md){
		this.m_gitMetaData = md;
	}

}
