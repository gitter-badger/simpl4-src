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

public class Link extends Base {
	private String m_rel="stylesheet";
	private String m_href="";
	private String m_type=null;
	public Link() {
		super("link");
	}
	public Link(String rel, String href) {
		super("link");
		if( rel != null){
			m_rel=rel;
		}
		m_href=href;
	}

	public void setRel(String r) {
		m_rel = r;
	}
	public void setHref(String h) {
		m_href = h;
	}
	public void setType(String h) {
		m_type = h;
	}


	public Element toXom() {
		m_attributes.add(new Attribute("rel", m_rel));
		m_attributes.add(new Attribute("href", m_href));
		if( m_type != null){
			m_attributes.add(new Attribute("type", m_type));
		}
		Element e = super.toXom();
		return e;
	}
}
