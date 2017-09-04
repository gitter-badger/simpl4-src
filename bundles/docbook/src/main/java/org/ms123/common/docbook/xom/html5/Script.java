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

public class Script extends Base {
	private String m_script=null;
	private String m_src="";
	private String m_type="text/javascript";
	public Script() {
		super("script");
	}
	public Script(String src) {
		super("script");
		m_src=src;
	}
	public Script(String type, String src) {
		super("script");
		m_type=type;
		m_src=src;
	}

	public void setScr(String s) {
		m_src = s;
	}
	public void setScript(String s) {
		m_script = s;
	}
	public void setType(String t) {
		m_type = t;
	}

	public Element toXom() {
		Element e = null;
		m_attributes.add(new Attribute("type", m_type));
		if( m_script == null){
			m_attributes.add(new Attribute("src", m_src));
			e = super.toXom();
			e.appendChild(new Text(" "));
		}else{
			e = super.toXom();
			e.appendChild(new Text(m_script));
		}
		return e;
	}
}
