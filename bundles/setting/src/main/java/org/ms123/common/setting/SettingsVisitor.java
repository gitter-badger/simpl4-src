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
package org.ms123.common.setting;

import metamodel.*;
import metamodel.visitor.ObjectGraphIterator;
import metamodel.visitor.ObjectGraphVisitor;
import metamodel.coreservices.*;
import metamodel.parser.*;
import java.util.*;
import org.ms123.common.utils.BaseObjectGraphVisitor;
import org.ms123.common.data.api.SessionContext;
import antlr.TokenStreamException;
import antlr.RecognitionException;

/**
 */
@SuppressWarnings("unchecked")
public class SettingsVisitor extends BaseObjectGraphVisitor {

	private static String SETTINGSID = "settingsid";

	private static ClassNode createClassNode(SessionContext sc) throws RecognitionException, TokenStreamException {
		ClassNode classNode = TraversalUtils.parse(
		 "Settingscontainer(" +
		 "               settingsid,description," + 
		 "               children:collection(Settingscontainer(" +
		 "                   settingsid,description,"+
		 "                     children:collection(Settingscontainer("+
		 "                         settingsid,description,"+
		 "                         children:collection(Settingscontainer("+
		 "														settingsid,description)"+
		 "												 )"+
		 "                     )))))");
		classNode.setResolver(new SCClassResolver(sc));
		return classNode;
	}

	public static Map getObjectGraph(Object root, SessionContext sc, Map mapping) {
		try {
			ClassNode classNode = createClassNode(sc);
			SettingsVisitor tf = new SettingsVisitor(classNode, sc, mapping);
			tf.serialize(root);
			return tf.getRoot();
		} catch (Exception e) {
			throw new RuntimeException("SettingsVisitor.getObjectGraph:", e);
		}
	}

	public SettingsVisitor(ClassNode cn, SessionContext sc, Map mapping) {
		m_classNode = cn;
		m_sc = sc;
		m_mapping = mapping;
	}

	public void endCollection(CollectionRef collRef, Collection data, Object parent, boolean hasNext) {
		level = level.substring(0, level.length() - 1);
		Collections.sort(m_currentList, new TComparable());
		m_currentList = m_listStack.pop();
	}

	public class TComparable implements Comparator<Map> {
		@Override
		public int compare(Map m1, Map m2) {
			String s1 = (String) m1.get(SETTINGSID);
			String s2 = (String) m2.get(SETTINGSID);
			if (s1 == null)
				s1 = "X";
			if (s2 == null)
				s2 = "X";
			return s1.compareTo(s2);
		}
	}
}
