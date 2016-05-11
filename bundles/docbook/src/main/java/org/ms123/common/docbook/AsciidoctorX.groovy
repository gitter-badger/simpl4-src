/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ms123.common.docbook;

import groovy.text.Template;
import java.util.*;
import groovy.lang.*;
import org.asciidoctor.groovydsl.AsciidoctorExtensions
import org.asciidoctor.Asciidoctor

//@groovy.transform.CompileStatic
public class AsciidoctorX{
	private Asciidoctor asciidoctor;
	public AsciidoctorX(Asciidoctor ad){
		asciidoctor = ad;
	}

	def ext1 = {
		block(name: 'BIG', contexts: [':paragraph', ':open']) { parent, reader, attributes ->
			def upperLines = reader.readLines()
				.collect {it.toUpperCase()}
				.inject('') {a, b -> a + '\n' + b}

			System.out.println("upperLines:"+upperLines);
			createBlock(parent, 'paragraph', [upperLines], attributes, [:])
		}
	}

	def ext2 = {
		blockmacro (name: 'imagezoom') { parent, target, attributes ->
			def classes= attributes.get(1);
			if( classes == null ){
				classes="";
			}
			classes= classes.replace('.', ' ');
			String content = "<simpl-zoom class=\""+classes+"\" image=\"${target}\"></simpl-zoom>"
			createBlock(parent, "pass", [content], attributes, config);
		}
	}
	def extCollapseItem = {
		block(name: 'CI', contexts: [':paragraph', ':open']) { parent, reader, attributes ->
			try{
				def lines = reader.readLines();
				def text = '';
				if( lines != null ){
					text = ':linkattrs:\n\n' + lines.join('\n');
				}
				if( text == "-"){
					text = '';
				}
				def opened = false;
				def icon= "image:lens";
				def eicon = null;
				def header= '';
				attributes.each{ k, v ->
					if( v == "opened" && (k+"").isNumber()){
						opened = true;
					}else if( k == "icon"){
						icon = v;
					}else if( k == "eicon"){
						eicon = v;
					}else if( k == "header"){
						header = v;
					}
				}
				def classes = "asciidoctor";
				if( text == ''){
					classes += " empty";
					if( eicon != null ){
						icon = eicon;
					}
				}
				def content = '<paper-collapse-item class="'+classes+'"  icon="'+icon+'" header="'+header+'" '+(opened ? 'opened' : '')+'>' + adocToHtml(text) + '</paper-collapse-item>';
				createBlock(parent, 'pass', [content], attributes, [:])
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public void register(){
		AsciidoctorExtensions.extensions(ext1);
		AsciidoctorExtensions.extensions(ext2);
		AsciidoctorExtensions.extensions(extCollapseItem);
	}
	private String adocToHtml( String adoc) throws Exception {
		Map<String, Object> options = new HashMap();
		Map<String, Object> attributes = new HashMap();
		attributes.put("icons", org.asciidoctor.Attributes.FONT_ICONS);
		options.put("attributes", attributes);
		options.put("safe", 0);
		return asciidoctor.convert( adoc, options);
	}
}




