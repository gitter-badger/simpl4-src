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
package org.ms123.common.datamapper;

import java.util.Map;
import java.util.TreeMap;
import java.util.*;
import java.io.*;
import net.sf.sojo.core.Constants;
import net.sf.sojo.common.WalkerInterceptor;
import org.xml.sax.ContentHandler;
import javax.xml.XMLConstants;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 */
@SuppressWarnings("unchecked")
public class BeanWalker implements WalkerInterceptor {

	private Object rootObject;

	private String rootTag;

	private ContentHandler m_contentHandler;

	private String m_currentClassName = "ROOT";

	private Stack<String> m_classNameStack = new Stack();

	public BeanWalker() {
	}

	public void setContentHandler(ContentHandler out) {
		m_contentHandler = out;
	}

	public void setRootTag(String tag) {
		rootTag = tag;
		m_currentClassName = rootTag;
	}

	public void endWalk() {
	}

	public void startWalk(Object pvStartObject) {
		rootObject = pvStartObject;
	}

	public boolean visitElement(Object pvKey, int pvIndex, Object pvValue, int pvType, String pvPath, int pvNumberOfRecursion) {
		if (pvType == Constants.TYPE_SIMPLE) {
			if (pvKey != null && !pvKey.equals("~unique-id~") && !pvKey.equals("class") && pvKey.getClass().equals(String.class)) {
				String tag = (String) pvKey;
				startElement(tag);
				text(pvValue + "");
				endElement(tag);
			}
		} else if (pvType == Constants.TYPE_NULL) {
			String tag = (String) pvKey;
			startElement(tag);
			endElement(tag);
		} else if (pvType == Constants.TYPE_ITERATEABLE) {
		} else if (pvType == Constants.TYPE_MAP) {
			if( "PUUID".equals(pvKey) || "PTIME".equals(pvKey)) return true;
		} else if (pvKey != null && pvValue != null) {
			if (pvKey != null && pvKey.getClass().equals(String.class)) {
			}
		}
		return false;
	}

	public void visitIterateableElement(Object pvValue, int pvType, String pvPath, int pvBeginEnd) {
		if (pvBeginEnd == Constants.ITERATOR_BEGIN) {
			if (pvValue.equals(rootObject)) {
				if( rootTag != null && !"".equals(rootTag)){
					startElement(rootTag);
				}
			} else if (pvType == Constants.TYPE_ITERATEABLE) {
				startElement(getLastSegment(pvPath).toLowerCase() + "List");
			} else if (pvType == Constants.TYPE_MAP) {
				m_classNameStack.push(m_currentClassName);
				m_currentClassName = getClassName(((Map) pvValue).get("class"));
				startElement(m_currentClassName);
			}
		}
		if (pvBeginEnd == Constants.ITERATOR_END) {
			if (pvValue.equals(rootObject)) {
				if( rootTag != null && !"".equals(rootTag)){
					endElement(rootTag);
				}
			} else if (pvType == Constants.TYPE_ITERATEABLE) {
				endElement(getLastSegment(pvPath).toLowerCase());
			} else if (pvType == Constants.TYPE_MAP) {
				endElement(m_currentClassName);
				m_currentClassName = m_classNameStack.pop();
			}
		}
	}

	private void startElement(String name) {
		try {
			m_contentHandler.startElement(XMLConstants.NULL_NS_URI, name, "", new AttributesImpl());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void endElement(String name) {
		try {
			m_contentHandler.endElement(XMLConstants.NULL_NS_URI, name, "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void text(String text) {
		try {
			m_contentHandler.characters(text.toCharArray(), 0, text.length());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getLastSegment(String path) {
		return getLastSegment(path, ".");
	}

	private String getLastSegment(String path, String sep) {
		int lastDot = path.lastIndexOf(sep);
		return path.substring(lastDot + 1);
	}

	private String getClassName(Object clazz) {
		if( clazz instanceof String) return (String)clazz;
		return String.valueOf(clazz);
	}

	private String getEntityName(Object clazz) {
		return (String) clazz;
	}

	private String printBeginEnd(int num) {
		switch(num) {
			case Constants.ITERATOR_BEGIN:
				return "BEGIN";
			case Constants.ITERATOR_END:
				return "END";
			default:
				return "";
		}
	}

	private Class _getClass(Object type) {
		if (type == null)
			return null;
		return type.getClass();
	}

	private String printType(int num) {
		switch(num) {
			case Constants.TYPE_NULL:
				return "NULL";
			case Constants.TYPE_SIMPLE:
				return "SIMPLE";
			case Constants.TYPE_ITERATEABLE:
				return "ITERATEABLE";
			case Constants.TYPE_MAP:
				return "MAP";
			default:
				return "";
		}
	}
}
