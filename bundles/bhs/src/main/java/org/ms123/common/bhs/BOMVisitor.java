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
package org.ms123.common.bhs;

import metamodel.*;
import metamodel.visitor.ObjectGraphIterator;
import metamodel.visitor.ObjectGraphVisitor;
import metamodel.coreservices.*;
import metamodel.parser.*;
import java.util.*;
import org.ms123.common.utils.BaseObjectGraphVisitor;
import org.apache.commons.beanutils.PropertyUtils;
import org.ms123.common.data.api.SessionContext;
import antlr.TokenStreamException;
import antlr.RecognitionException;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
/**
 */
@SuppressWarnings("unchecked")
public class BOMVisitor extends BaseObjectGraphVisitor {

	protected JSONSerializer m_js = new JSONSerializer();

	private static ClassNode createClassNode(SessionContext sc) throws RecognitionException, TokenStreamException {
		ClassNode classNode = TraversalUtils.parse(
		 "Bom as bo(" +
		 "               part,path,masterdata," + 
		 "               children:collection(reference(bo)))");
		classNode.setResolver(new SCClassResolver(sc));
		return classNode;
	}

	public static Map getObjectGraph(Object root, SessionContext sc, Map mapping) {
		try {
			ClassNode classNode = createClassNode(sc);
			BOMVisitor tf = new BOMVisitor(classNode, sc,mapping);
			tf.serialize(root);
			return tf.getRoot();
		} catch (Exception e) {
			throw new RuntimeException("BOMVisitor.getObjectGraph:", e);
		}
	}

	public BOMVisitor(ClassNode cn, SessionContext sc, Map mapping) {
		m_classNode = cn;
		m_sc = sc;
		m_mapping = mapping;
	}

	public void endCollection(CollectionRef collRef, Collection data, Object parent, boolean hasNext) {
		level = level.substring(0, level.length() - 1);
		Collections.sort( m_currentList, new TComparable() );
		m_currentList = m_listStack.pop();
	}

	public void visitFlatProperty(FlatProperty property, Object value, Object parent, boolean hasNext) {
		debug("visitFlatProperty:" + property.getName() + "/" + value + "/" + hasNext+"/"+property.getTypeName());
		if( value instanceof Date ){
			m_currentMap.put(property.getName(), ((Date)value).getTime());
		}else if( property.getName().equals("masterdata") ){
			String name = (String)getProperty(value,"name");
			String name2 = (String)getProperty(value,"name2");
			m_currentMap.put("name", isEmpty(name2) ? name : name2);
		}else{
			m_currentMap.put(property.getName(), value);
		}
	}
	private Object getProperty(Object o, String attr){
		try{
			return PropertyUtils.getProperty(o,attr);
		}catch(Exception e){
			return "NoValue";
		}
	}
	public boolean isOk(ClassNode classNode, DeepProperty dp, Object data, Object parent) {
		try {
			String path = (String)getProperty(data,"path");
			if( path.startsWith("2100097.141028500") || path.startsWith("2100097.141028600")) return false;
			Object qty = getProperty(data,"qty");
			if( qty == null ) return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	private boolean isEmpty(String s){
		if(s==null || s.trim().equals("")) return true;
		return false;
	}

	public class TComparable implements Comparator<Map>{
		@Override
		public int compare(Map m1, Map m2) {
			String s1 = (String)m1.get(NAME);
			String s2 = (String)m2.get(NAME);
			if( s1 == null) s1 = "X";
			if( s2 == null) s2 = "X";
			return s1.compareTo(s2);
		}
	}
}
