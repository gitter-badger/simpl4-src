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

public class Style extends Base {
	private String m_style="";	
	private String m_type="text/css";	
	public Style() {
		super("style");
	}

	public void setStyle(String style){
		m_style = style;
	}
	public Element toXom() {
		Element e = super.toXom();
		e.addAttribute(new Attribute("type",m_type));
		//e.addAttribute(new Attribute("xml:space", "http://www.w3.org/XML/1998/namespace","preserve"));
		e.appendChild(new Text(m_style));
		return e;
	}
}
