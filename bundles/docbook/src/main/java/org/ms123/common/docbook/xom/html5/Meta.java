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

public class Meta extends Base {
	private String m_charset="UTF-8";
	public Meta() {
		super("meta");
		addAttribute("charset", m_charset);
	}
	public Meta(String name, String content) {
		super("meta");
		addAttribute("name", name);
		addAttribute("content", content);
	}

	//public void setCharset(String c) {
	//	m_charset = c;
	//}

	public Element toXom() {
		//m_attributes.add(new Attribute("charset", m_charset));
		Element e = super.toXom();
		return e;
	}
}
