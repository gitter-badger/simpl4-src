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
public class BaseE extends Base {
	private String m_href=null;

	private String m_target=null;
	public BaseE() {
		super("base");
	}
	public BaseE(String href) {
		super("base");
		m_href=href;
	}
	public BaseE(String href,String target) {
		super("base");
		m_href=href;
		m_target=target;
	}

	public void setHref(String h) {
		m_href = h;
	}

	public void setTarget(String l) {
		m_target = l;
	}

	public Element toXom() {
		m_attributes.add(new Attribute("href", m_href));
		if( m_target !=  null){
			m_attributes.add(new Attribute("target", m_target));
		}
		Element e = super.toXom();
		return e;
	}
	public Map<String,Object> toMap() {
		m_attributes.add(new Attribute("href", m_href));
		if( m_target !=  null){
			m_attributes.add(new Attribute("target", m_target));
		}
		Map m = super.toMap();
		return m;
	}
}
