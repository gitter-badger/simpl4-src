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

public class Informaltable extends Base {

	private String m_keepTogether = null;
	private Tgroup m_tgroup;

	private Caption m_caption;

	public Informaltable() {
		super("informaltable");
	}

	public Informaltable(String s) {
		super(s);
	}


	public void setKeepTogether(String x){
		if( "auto".equals(x) || "always".equals(x)){
			m_keepTogether = x;
		}else{
			m_keepTogether = "always";
		}
	}

	public void setFrame(String frame) {
		this.m_attributes.add(new Attribute("frame", frame));
	}

	public void setRowsep(String rowsep) {
		this.m_attributes.add(new Attribute("rowsep", rowsep));
	}

	public void setColsep(String colsep) {
		this.m_attributes.add(new Attribute("colsep", colsep));
	}

	public void setTgroup(Tgroup tgroup) {
		m_tgroup = tgroup;
	}

	public Tgroup getTgroup() {
		return m_tgroup;
	}

	public void setCaption(Caption caption) {
		m_caption = caption;
	}

	public Caption getCaption() {
		return m_caption;
	}

	public Element toXom() {
		Element e = super.toXom();
		if( m_keepTogether != null){
			e.appendChild( new ProcessingInstruction("dbfo", "keep-together=\""+m_keepTogether+"\""));
		}
		if( m_caption != null){
			e.appendChild(m_caption.toXom());
		}
		e.appendChild(m_tgroup.toXom());
		return e;
	}
}
