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

public class Colspec extends Base {

	private String m_align;

	private String m_colwidth;

	private String m_colsep;

	private String m_rowsep;

	private String m_colnum;

	public Colspec() {
		super("colspec");
	}

	public void setAlign(String a) {
		m_align = a;
		m_attributes.add(new Attribute("align", a));
	}

	public void setColwidth(String w) {
		m_colwidth = w;
		m_attributes.add(new Attribute("colwidth", w));
	}

	public void setColsep(String s) {
		m_colsep = s;
		m_attributes.add(new Attribute("colsep", s));
	}

	public void setRowsep(String r) {
		m_rowsep = r;
		m_attributes.add(new Attribute("rowsep", r));
	}

	public void setColnum(String s) {
		m_colnum = s;
		m_attributes.add(new Attribute("colnum", s));
	}

	public String getColnum() {
		return m_colnum;
	}
}
