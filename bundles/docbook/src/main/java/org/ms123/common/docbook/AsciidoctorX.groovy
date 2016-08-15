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
import org.ms123.common.docbook.DocbookService;

//@groovy.transform.CompileStatic
public class AsciidoctorX{
	private Asciidoctor asciidoctor;
	private DocbookService docbookService;
	public AsciidoctorX(Asciidoctor ad, DocbookService ds){
		asciidoctor = ad;
		docbookService = ds;
	}

	def bigBlock = {
		block(name: 'BIG', contexts: [':paragraph', ':open']) { parent, reader, attributes ->
			def upperLines = reader.readLines()
				.collect {it.toUpperCase()}
				.inject('') {a, b -> a + '\n' + b}

			System.out.println("upperLines:"+upperLines);
			createBlock(parent, 'paragraph', [upperLines], attributes, [:])
		}
	}

	def imageZoomBlockMacro = {
		block_macro (name: 'imagezoom') { parent, target, attributes ->
			def classes= attributes.get(1);
			if( classes == null ){
				classes="";
			}
			classes= classes.replace('.', ' ');
			String content = "<simpl-zoom class=\""+classes+"\" image=\"${target}\"></simpl-zoom>"
			createBlock(parent, "pass", [content], attributes, config);
		}
	}
	def collapseItemBlock = {
		block(name: 'CI', contexts: [':paragraph', ':open']) { parent, reader, attributes ->
			try{
				def lines = reader.readLines();
				def text = '';
				if( lines != null ){
					text = lines.join('\n');
				}
				if( text == "-"){
					text = '';
				}else if(text != null && !text.empty){
					text = ':linkattrs:\n\n' + text;
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

	def rowBlock = {
		block(name: 'ROW', contexts: [':paragraph', ':open']) { parent, reader, attributes ->
			try{
				def classes="col-xs-12 col-md-6";
				def delim = "###";
				def swap = false;
				def orders = [:]
				def cells = [:].withDefault { "" }
				def cols = [:].withDefault { "" }
				attributes.each{ k, v ->
					if( k == "classes"){
						classes=complete(v,'col-');
					}
					if( k == "delim"){
						delim = v;
					}
					if( k == "swap"){
						swap = true;
					}
					if( k instanceof String && k.startsWith( "order")){
						orders[getCol(k,5)]=complete(v, 'order-');
					}
					if( k instanceof String && k.startsWith( "cell")){
						cells[getCol(k,4)]=v;
					}
					if( k instanceof String && k.startsWith( "col")){
						cols[getCol(k,3)]=v;
					}
				}
				def lines = reader.readLines();
				def adocBlock = "";
				def htmlBlock = '<div class="grid row">';
				def index = 0;
				lines.each{ l ->
					if( l == delim ){
						htmlBlock += '<div class="'+classes+' ' +getOrder(orders,index,swap)+' ' + cols[index]+'"><div class="cell '+cells[index]+'">' + adocToHtml(adocBlock) + '</div></div>';
						adocBlock = "";
						index++;
					}else{
						adocBlock += l + "\n";
					}
				}
				htmlBlock += '<div class="'+classes+' '+getOrder(orders,index,swap)+' ' + cols[index]+'"><div class="cell '+cells[index]+'">' + adocToHtml(adocBlock) + '</div></div></div>';
				
				createBlock(parent, 'pass', [htmlBlock], attributes, [:])
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	def includeProcessor = {
		include_processor (filter: {true}) { document, reader, target, attributes -> 
			def namespace = attributes.get("namespace");
			System.err.println("include_processor:"+target+"/"+attributes+"/"+namespace);
			if( namespace == null){
				reader.push_include("IncludeProcessor: namespace is null:target:"+target, target, target, 1, attributes);
			}else{
				def content = docbookService.loadContent( namespace, target);
				if( content != null && content.length() > 0){
					reader.push_include(content, target, target, 1, attributes);
				}else{
					reader.push_include("IncludeProcessor: file("+namespace+","+target+") not found or the file has no content", target, target, 1, attributes);
				}
			}
		}
	}

	public void register(){
		AsciidoctorExtensions.extensions(bigBlock);
		AsciidoctorExtensions.extensions(imageZoomBlockMacro);
		AsciidoctorExtensions.extensions(collapseItemBlock);
		AsciidoctorExtensions.extensions(rowBlock);
		AsciidoctorExtensions.extensions(includeProcessor);
	}

	private String getOrder( orders, index, swap){
		if( orders[index] ) return orders[index];
		if( !swap) return "";
		if( index == 0) return "order-xs-1 order-sm-1";
		if( index == 1) return "order-xs-0 order-sm-0";
		return "";
	}

	private String complete(s, prefix){
		def l = s.split(' ');
		def nl = [];
		l.each{ e -> e.startsWith(prefix) ? nl.add(e) : nl.add(prefix+e) }
		return nl.join(' ');
	}

	private int getCol(s,len){
		if( s.length() == len) return -1;
		try{
			return Integer.parseInt( s.substring(len));
		}catch(def e){
			return -1;
		}
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




