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
package org.ms123.common.docbook.xom.db;

import nu.xom.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class Base {

	private String m_name;

	private List<Object> m_content = new ArrayList();

	protected List<Attribute> m_attributes = new ArrayList();

	public Base(String name) {
		m_name = name;
	}

	public List<Object> getContent() {
		return m_content;
	}

	public void add(Base e) {
		m_content.add(e);
	}

	public void add(String e) {
		m_content.add(e);
	}

	public void setRole(String role) {
		this.m_attributes.add(new Attribute("role", role));
	}

	public Element toXom() {
		Element e = new Element(m_name, "http://docbook.org/ns/docbook");
		for (Attribute a : m_attributes) {
			e.addAttribute(a);
		}
		for (Object o : m_content) {
			if (o instanceof String) {
				e.appendChild(new Text((String) o));
			}else if (o instanceof Element) {
				e.appendChild((Element)o);
			}else if (o instanceof ProcessingInstruction) {
				e.appendChild((ProcessingInstruction)o);
			} else {
				Base b = (Base) o;
				e.appendChild(b.toXom());
			}
		}
		return e;
	}
}
