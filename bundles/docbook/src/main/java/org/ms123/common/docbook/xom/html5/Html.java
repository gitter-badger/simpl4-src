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

public class Html extends Base {
	private Head m_head;
	private Body m_body;
	
	public Html() {
		super("html");
	}
	public void setHead(Head head) {
		m_head = head;
	}

	public Head getHead() {
		return m_head;
	}

	public void setBody(Body body) {
		m_body = body;
	}

	public Body getBody() {
		return m_body;
	}

	public Element toXom() {
		Element e = super.toXom();
//		e.addAttribute(new Attribute("xml:lang", "http://www.w3.org/XML/1998/namespace","de"));
		e.addAttribute(new Attribute("lang", "de"));
		if( m_head != null){
			e.appendChild(m_head.toXom());
		}
		if( m_body != null){
			e.appendChild(m_body.toXom());
		}
		return e;
	}
}
