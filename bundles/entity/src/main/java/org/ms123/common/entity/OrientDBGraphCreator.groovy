/*
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Persistent;
import java.lang.annotation.Annotation;
import org.apache.commons.beanutils.BeanMap;
import org.ms123.common.utils.annotations.RelatedTo;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.auth.api.AuthService;
import org.ms123.common.utils.UtilsService;
import org.ms123.common.git.GitService;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.utils.ParameterParser;
import org.mvel2.MVEL;

/**
 *
 */
@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
class OrientDBGraphCreator implements GraphCreator, org.ms123.common.entity.api.Constants, org.ms123.common.datamapper.Constants {

	protected Inflector m_inflector = Inflector.getInstance();
	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();


	protected List<Map> m_entityList = [];
	protected List<Map> m_relationList = [];
	protected NucleusService m_nucleusService;

	protected UtilsService m_utilsService;
	protected GitService m_gitService;
	protected MetaData m_gitMetaData;
	protected List<Map> m_strategy;

	protected OrientDBGraphCreator(EntityServiceImpl esi){
		m_gitService = esi.m_gitService
		m_gitMetaData = esi.m_gitMetaData
		m_js.prettyPrint(true);
	}
	public Map createEntitytypes(String storeId, String datamapperConfigName, Map datamapperConfig, List<Map> strategy, String side, boolean infoOnly){
		StoreDesc sdesc = StoreDesc.get(storeId);
		m_strategy = strategy;
		if( datamapperConfigName != null){
		String json = m_gitService.searchContent(sdesc.getNamespace(), datamapperConfigName, "sw.datamapper");
			datamapperConfig = (Map)m_ds.deserialize(json);
		}
		Map inputTree =  side == INPUT ? datamapperConfig.input as Map : datamapperConfig.output as Map;
		println("InputTree:"+m_js.deepSerialize(inputTree));
		traverseTree( inputTree);
		if( !infoOnly){
			for(Map et : m_entityList ){
				if( isCreateEnabled( (String)et.get("name"))){
					m_gitMetaData.saveEntitytype(storeId, (String)et.get("name"), et);
				}
			}
		}
		return [ entityList: m_entityList, relationList : m_relationList ];
	}

	private boolean isCreateEnabled(String name){
		if( m_strategy==null) return true;
		for( Map emap : m_strategy){
			if( name.equals(emap.get("entityname")) && (Boolean)emap.get("create")){
				return true;
			}
		}
		return false;
	}

	private void traverseTree(Map tree){
		Map entityMap = [:];
		m_entityList.add(entityMap);
		initEntityMap(entityMap,tree)
		traverseChildren(entityMap, tree.children as List);
	}

	private void traverseChildren(Map entityMap, List<Map> children){
		for(Map child in children){
			if( child.type == NODETYPE_ATTRIBUTE){
				addField( entityMap, child);
			}else if( child.type == NODETYPE_ELEMENT){
				Map newEntity = [:];
				m_entityList.add(newEntity);
				initEntityMap(newEntity, child);
				addFieldObject( entityMap, newEntity);
				traverseChildren(newEntity, child.children as List);
			}else if( child.type == NODETYPE_COLLECTION){
				Map newEntity = [:];
				m_entityList.add(newEntity);
				initEntityMap(newEntity, child);
				addFieldObjectList( entityMap, newEntity);
				traverseChildren(newEntity, child.children as List);
			}
		}	
	}
	private String getEditType(String datatype){
		if("19".equals(datatype))return "date";
		if("6".equals(datatype))return "datetime";
		if("boolean".equals(datatype))return "checkbox";
		return "text";
	}
	private void addField(Map entityMap,Map treeNode){
		Map field = [:];
		field.name = (treeNode.name as String).toLowerCase();
		def type = treeNode.fieldType;
		if( type == "byte") type = "17";
		if( type == "double") type = "5";
		if( type == "integer") type = "1";
		if( type == "long") type = "3";
		if( type == "string") type = "7";
		if( type == "boolean") type = "0";
		if( type == "decimal") type = "21";
		if( type == "date") type = "19";
		if( type == "calendar") type = "6";
		field.datatype = type;
		field.edittype = getEditType(type as String);
		field.enabled = true;
		field.edgeconn = false;
		Map fields = entityMap.fields as Map;
		fields[field.name as String] = field;
	}

	private void addFieldObject(Map leftEntity,Map rightEntity){
		Map field = [:];
    field.name= firstToLower(rightEntity.name as String);
    field.linkedclass= (rightEntity.name as String);
		field.datatype = "13"; //Link(13) or Embed(9)
		field.edittype = "";
		field.enabled = true;
		field.edgeconn = false;
		field.vertexType = "single";
		Map fields = leftEntity.fields as Map;
		fields[field.name as String] = field;
	}

	private void addFieldObjectList(Map leftEntity,Map rightEntity){
		Map field = [:];
    field.name= firstToLower(rightEntity.name as String);
    field.linkedclass= (rightEntity.name as String);
		field.datatype = "14"; //LinkList(14) or EmbedList(10)
		field.edittype = "";
		field.enabled = true;
		field.edgeconn = false;
		field.vertexType = "single";
		Map fields = leftEntity.fields as Map;
		fields[field.name as String] = field;
	}

	private void initEntityMap(Map entityMap,Map treeNode){
		entityMap.name = firstToLower(treeNode.name as String);
		entityMap.description = "";
		entityMap.default_fields = false;
		entityMap.team_security = false;
		entityMap.superclass = "vertex";
		entityMap.enabled = true;
		entityMap.fields = [:];
	}

	private String firstToLower(String s) {
		String fc = s.substring(0, 1);
		return fc.toLowerCase() + s.substring(1);
	}
}
