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
public class Tgroup extends Base {

	private Thead m_thead;

	private Tbody m_tbody;

	private List<Colspec> m_colspecs = new ArrayList();

	public Tgroup() {
		super("tgroup");
	}

	public void setCols(String cols) {
		m_attributes.add(new Attribute("cols", cols));
	}

	public void setColspecs(List<Colspec> csList) {
		m_colspecs = csList;
	}

	public List<Colspec> getColspecs() {
		return m_colspecs;
	}

	public void setThead(Thead thead) {
		m_thead = thead;
	}

	public Thead getThead() {
		return m_thead;
	}

	public void setTbody(Tbody tbody) {
		m_tbody = tbody;
	}

	public Tbody getTbody() {
		return m_tbody;
	}

	public Element toXom() {
		Element e = super.toXom();
		for (Colspec cs : m_colspecs) {
			e.appendChild(cs.toXom());
		}
		if( m_thead != null){
			e.appendChild(m_thead.toXom());
		}
		if( m_tbody != null){
			e.appendChild(m_tbody.toXom());
		}
		return e;
	}
}
