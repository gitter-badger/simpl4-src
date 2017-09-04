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
package org.ms123.common.entity;

import flexjson.*;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.ms123.common.utils.TypeUtils;
import org.apache.commons.beanutils.BeanMap;
import org.ms123.common.data.api.SessionContext;
import com.orientechnologies.orient.core.metadata.schema.OType;
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

	private static final String ENTITY = "entity";

	public List getEntities(StoreDesc sdesc, Boolean withChilds, Boolean withTeam, Map mapping, String filter, String sortField) throws Exception {
		List<Map> entTypes = m_gitMetaData.getEntitytypes(sdesc.getStoreId());
		info(this, "entTypes:" + entTypes);
		Map<String, Map> modMaps = toMap(entTypes, "name");
		List<Map> retList = new ArrayList();
		for (Map map : entTypes) {
			Object enabled = map.get("enabled");
			String mn = m_inflector.getEntityNameCamelCase(map.get("name"));
			info(this, "sdesc:" + sdesc);
			info(this, "map:" + map);
			info(this, "Permission:" + m_permissionService.hasEntityPermissions(sdesc, mn, "read"));
			if (enabled != null && ((Boolean) enabled) == true && m_permissionService.hasEntityPermissions(sdesc, mn, "read")) {
				retList.add(map);
			}
		}
		return retList;
	}

	public Map getEntityTree(StoreDesc sdesc, String mainEntity, int maxlevel, Boolean _pathid, String _type, Boolean _listResolved) throws Exception {
		SessionContext sc = m_dataLayer.getSessionContext(sdesc);
		try {
			Map userData = m_authService.getUserProperties(sc.getUserName());
			boolean pathid = _pathid != null && _pathid;
			boolean listResolved = _listResolved != null && _listResolved;
			String type = _type != null ? _type : "all";
			Object[] member = new Object[3];
			member[0] = mainEntity;
			member[1] = m_inflector.getEntityNameCamelCase(mainEntity);
			member[2] = mainEntity;
			Map tree = _getEntitySubTree(sdesc, (String) member[1], member, 0, maxlevel, pathid, listResolved, type, userData);
			return tree;
		} catch (Exception e) {
			sc.handleException(e);
			throw e;
		} finally {
			sc.handleFinally();
		}
	}

	private Map _getEntitySubTree(StoreDesc sdesc, String path, Object[] member, int level, int maxlevel, boolean pathid, boolean listResolved, String type, Map userData) throws Exception {
		String entityName = (String) member[0];
		if (level == maxlevel) {
			return null;
		}
		info(this, "_getEntitySubTree("+entityName+"):" + type);
		if (!m_permissionService.hasEntityPermissions(sdesc, entityName, "read")) {
			info(this, "no read:" + entityName);
			return null;
		}
		Map node = new HashMap();
		node.put("level", level);
		List<Object[]> childs = getSubEntity(sdesc, entityName);
		Object t = member[2];
		boolean collection = ("list".equals(t) || "set".equals(t) || "map".equals(t));
		if (type.equals("one") && collection) {
			return null;
		}
		if (type.equals("collection") && !collection && level > 0) {
			return null;
		}
		if (pathid) {
			node.put("id", path);
		} else {
			node.put("id", member[1]);
		}
		node.put("datatype", (collection || (level == 20 && listResolved)) ? "list" : "object");
		node.put(ENTITY, entityName);
		node.put("name", StoreDesc.getSimpleEntityName((String) member[1]));
		node.put("write", m_permissionService.hasEntityPermissions(sdesc, entityName, "write"));
		node.put("title", (sdesc.getPack() + "." + m_inflector.getEntityNameCamelCase(StoreDesc.getSimpleEntityName((String) member[1]))));
		Map objNode = null;
		if ((collection || level == 20) && listResolved) {
			objNode = getNodeWithSingularNames(node, entityName, path, pathid);
		}
		for (int i = 0; i < childs.size(); i++) {
			Object[] cmem = childs.get(i);
			Map m = _getEntitySubTree(sdesc, path + "/" + cmem[1], cmem, level + 1, maxlevel, pathid, listResolved, type, userData);
			if (m != null) {
				List<Map> childList = (objNode != null) ? (List) objNode.get("children") : (List) node.get("children");
				if (childList == null) {
					childList = new ArrayList<Map>();
					if (objNode != null) {
						objNode.put("children", childList);
					} else {
						node.put("children", childList);
					}
				}
				childList.add(m);
			}
		}
		return node;
	}

	private List<Object[]> getSubEntity(StoreDesc sdesc, String entity) throws Exception {
		List<Map> fields = getFields(sdesc, entity);
		List<Object[]> list = new ArrayList<Object[]>();
		for (Map<String, String> field : fields) {
			String datatype = field.get("datatype");
			if (isSimpleType(datatype)) {
				continue;
			}
			String fieldname = getName(field);
			boolean isEdgeConn = isEdgeConnection(field);
			if (isEdgeConn) {
				continue;
			}
			if ("id".equals(fieldname)) {
				continue;
			}
			OType otype = getType(field);
			boolean isMulti = otype.isMultiValue();
			boolean isLink = otype.isLink();
			boolean isEmbedded = otype.isEmbedded();
			String linkedClass = getLinkedClassName(field);
			String collectionType = null;
			Object[] s = new Object[4];
			s[1] = fieldname;
			if (isMulti) {
				if (isLink) { //Collection
					collectionType = getCollectionType(otype.toString());
				} else { //Embedded Collection
					if (linkedClass != null) { //Object type
						collectionType = getCollectionType(otype.toString());
					} else { //simple collection type(eg.List<String>) not handled
						continue;
					}
				}
			}else{
				collectionType = "object";
			}
			info(this, "field(" + fieldname +","+isEmbedded+","+isLink+ ",lc:"+linkedClass+",ct:"+collectionType+"):otype:"+otype.toString() );

			s[0] = linkedClass;
			s[2] = collectionType;
			s[3] = false;
			list.add(s);
		}
		return list;
	}

	private Map getNodeWithSingularNames(Map node, String entityName, String path, boolean pathid) {
		Map objNode = new HashMap();
		objNode.put("datatype", "object");
		objNode.put(ENTITY, m_inflector.singularize(entityName));
		objNode.put("name", m_inflector.singularize(entityName));
		if (pathid) {
			path = path + "/" + m_inflector.singularize(entityName);
			objNode.put("id", path);
		} else {
			objNode.put("id", m_inflector.singularize(entityName));
		}
		objNode.put("title", ("data." + m_inflector.singularize(entityName)));
		List<Map> childList = new ArrayList<Map>();
		childList.add(objNode);
		node.put("children", childList);
		return objNode;
	}

	private List<Map> getFields(StoreDesc sdesc, String entityName) throws Exception {
		return getFields(sdesc, entityName, false, false);
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

	private String getName(Map<String, String> m) {
		return m.get("name");
	}

	private boolean isEdgeConnection(Map m) {
		Boolean ret = (Boolean) m.get("edgeconn");
		if (ret == null)
			return false;
		return ret;
	}

	private String getLinkedClassName(Map entity) {
		String name = (String) entity.get("linkedclass");
		if (isEmpty(name))
			return null;
		return name;
	}

	private OType getType(Map<String, String> m) {
		return OType.getById(Byte.parseByte(m.get("datatype")));
	}

	private OType getLinkedType(Map<String, String> m) {
		return OType.getById(Byte.parseByte(m.get("linkedtype")));
	}

	private List<String> internalList = Arrays.asList("class", "propertyKeys", "record", "locked", "elementType", "vertexInstance", "metaClass", "baseClassName", "graph");

	boolean isInternal(String name) {
		return internalList.contains(name);
	}

	private List<String> simpleTypeList = Arrays.asList("0","1", "2", "3", "4", "5", "6", "7", "17", "19", "21");

	boolean isSimpleType(String type) {
		return simpleTypeList.contains(type);
	}

	private String getCollectionType(String type) {
		if (type.endsWith("LIST")) {
			return "list";
		} else if (type.endsWith("SET")) {
			return "set";
		} else if (type.endsWith("MAP")) {
			return "map";
		}
		return "Unknown";
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

	private boolean isEmpty(String s) {
		if (s == null || s.trim().length() == 0)
			return true;
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
		info(this, "OrientDBEntityServiceImpl.setDataLayer:" + dataLayer);
		m_dataLayer = dataLayer;
	}

	public void setGitService(GitService gitService) {
		info(this, "OrientDBEntityServiceImpl.setGitService:" + gitService);
		this.m_gitService = gitService;
	}

	public void setPermissionService(PermissionService paramPermissionService) {
		this.m_permissionService = paramPermissionService;
		info(this, "OrientDBEntityServiceImpl.setPermissionService:" + paramPermissionService);
	}

	public void setAuthService(AuthService paramService) {
		this.m_authService = paramService;
		info(this, "OrientDBEntityServiceImpl.setAuthService:" + paramService);
	}

	public void setUtilsService(UtilsService paramUtilsService) {
		this.m_utilsService = paramUtilsService;
		info(this, "OrientDBEntityServiceImpl.setUtilsService:" + paramUtilsService);
	}

	public void setEnumerationService(EnumerationService param) {
		this.m_enumerationService = param;
		info(this, "OrientDBEntityServiceImpl.setEnumerationService:" + param);
	}

	public void setNucleusService(NucleusService paramService) {
		this.m_nucleusService = paramService;
		info(this, "OrientDBEntityServiceImpl.setNucleusService:" + paramService);
	}

	public void setGitMetadata(MetaData md) {
		this.m_gitMetaData = md;
	}

}

