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
package org.ms123.common.docbook.xom.html5;

import nu.xom.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class Head extends Base {
	private Title m_title;
	protected List<Meta> m_metas = new ArrayList();
	protected List<Link> m_links = new ArrayList();
	protected List<Script> m_scripts = new ArrayList();
	
	public Head() {
		super("head");
	}
	public void setTitle(Title title) {
		m_title = title;
	}

	public Title getTitle() {
		return m_title;
	}

	public List<Meta> getMetas() {
		return m_metas;
	}

	public List<Link> getLinks() {
		return m_links;
	}
	public List<Script> getScripts() {
		return m_scripts;
	}

	public Element toXom() {
		Element e = super.toXom();
		for (Meta m : m_metas) {
			e.appendChild(m.toXom());
		}
		if( m_title != null){
			e.appendChild(m_title.toXom());
		}
		for (Link l : m_links) {
			e.appendChild(l.toXom());
		}
		for (Script s : m_scripts) {
			e.appendChild(s.toXom());
		}
		return e;
	}
}
