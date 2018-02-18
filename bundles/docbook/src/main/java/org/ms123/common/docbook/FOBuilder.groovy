/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014,2017] [Manfred Sattler] <manfred@ms123.org>
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

import java.io.StringWriter;
import java.io.InputStream;
import java.io.File;
import java.util.Map;
import java.net.URL;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.ms123.common.docbook.rendering.TransformToFo;
import groovy.json.JsonSlurper;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;
import groovy.text.markup.TemplateResolver;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.message.MessageService;
import org.osgi.framework.BundleContext;
import org.commonmark.node.*;
import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.apache.commons.io.IOUtils.toString;
import static org.apache.commons.io.HexDump.dump;




@SuppressWarnings("unchecked")
@groovy.transform.CompileStatic
public class FOBuilder extends TemplateEvaluator{

	private Map<String, Template> templateCache = new LinkedHashMap();

	private MarkupTemplateEngine engine = null;
	private MyTemplateResolver resolver = null;
	private BundleContext bc;
	private String namespace;
	private	TransformToFo transformer = null;
	private MessageService messageService;

	public FOBuilder(String namespace, BundleContext bc) {
		this.bc = bc;
		this.namespace = namespace;
		TemplateConfiguration templateConfiguration = new TemplateConfiguration();
		templateConfiguration.setUseDoubleQuotes(true);
		templateConfiguration.setAutoIndent(true);
		templateConfiguration.setAutoNewLine(true);
		templateConfiguration.setAutoEscape(true);
		templateConfiguration.setCacheTemplates(false);

		this.resolver = new MyTemplateResolver( namespace);
		this.engine = new MarkupTemplateEngine(this.class.getClassLoader(),templateConfiguration, resolver);
		this.transformer = TransformToFo.create(this.bc);
		this.messageService = getMessageService();
	}

	public InputStream toFO( String jsonText, Map<String,Object> variableMap){
		def jsonSlurper = new JsonSlurper();
		def json = (Map)jsonSlurper.parseText(jsonText);
		if( json.templateName == null){
			json = [ state:json, templateName:"master.tpl"];
		}
		def templateName = json.templateName as String;
		def lang = json.lang as String;
		if( lang == null){
			lang = "de";
		}

		def isMod = this.resolver.testModified( templateName );
		def template = this.templateCache.get(templateName);
		if (template == null || isMod) {
			template = this.engine.createTemplateByPath( templateName);
			this.templateCache.put(templateName, template);
		}
		Map binding = [:].withDefault { x -> new DefaultBinding(x) }
		if( variableMap.bindings != null){
			binding.putAll( variableMap.bindings as Map);
			htmlToFoList( json.state as Map, variableMap.bindings as Map, lang );
		}else{
			htmlToFoList( json.state as Map, variableMap, lang );
			binding.putAll( variableMap);
		}

		binding.putAll( json.state as Map);
		info(this,"FOBuilder.binding:" + binding);
		String answer = template.make(binding).toString();
		//info(this,"FOBuilder.answer:"+answer);

		return toInputStream( answer );
	}


	private static class MyTemplateResolver implements TemplateResolver {
		private TemplateConfiguration templateConfiguration;
		private ClassLoader templateClassLoader;
		private String namespace;
		private Map<File,Long> modifiedCache = [:];

		public MyTemplateResolver(String namespace) {
			this.namespace = namespace;
		}

		public void configure(final ClassLoader templateClassLoader, final TemplateConfiguration configuration) {
			this.templateClassLoader = templateClassLoader;
			this.templateConfiguration = configuration;
		}

		public URL resolveTemplate(final String templatePath) throws IOException {
			def templ = resolveTemplateFile( templatePath);
			def url = templ.toURI().toURL();
			info(this,"url:" + url);
			return url;
		}
		public File resolveTemplateFile(final String templatePath) throws IOException {
			def templ = new File(new File(System.getProperty("git.repos"), namespace),"/templates/"+templatePath); 
			return templ;
		}
		public boolean  testModified(final String templatePath){
			info(this,"modifiedCache:"+modifiedCache);
			File file = resolveTemplateFile( templatePath);
			def lastMod = modifiedCache[file];
			info(this,"lastMod:"+lastMod);
			if( lastMod == null){
				lastMod = 0;
			}
			def curMod = file.lastModified();
			info(this,"curMod:"+curMod);
			if( curMod > lastMod){
				modifiedCache[file] = curMod;
				return true;
			}
			return false;
		}
	}


	private void htmlToFo(InputStream is, OutputStream os) throws Exception {
		this.transformer.transform(is, os);
	}
	private MessageService  getMessageService(){
		def service=null;
		def sr = bc.getServiceReference("org.ms123.common.message.MessageService");
		if (sr != null) {
			service = bc.getService(sr);
		}
		if (service == null) {
			throw new RuntimeException("JsonConverter.Cannot resolve :org.ms123.common.message.MessageService");
		}
		return (MessageService)service;
	}
	private boolean hasMoreRightBrackets(String str, int pos) {
		int len = str.length();
		boolean hasMore = false;
		for (int i = pos + 1; i < len; i++) {
			if (str.charAt(i) == '}') {
				return true;
			}
			if (str.charAt(i) == '<') {
				return false;
			}
			if (str.charAt(i) == '@' && i + 1 < len && str.charAt(i + 1) == '{') {
				return false;
			}
			if (str.charAt(i) == '$' && i + 1 < len && str.charAt(i + 1) == '{') {
				return false;
			}
		}
		return false;
	}
	private String evalMessages( String str, String lang){
		int countRepl = 0;
		int countPlainStr = 0;
		Object replacement = null;
		String newString = "";
		int openBrackets = 0;
		int first = 0;
		for (int i = 0; i < str.length(); i++) {
			//info(this,"char("+i+","+openBrackets+","+hasMoreRightBrackets(str, i)+"):"+str.charAt(i));
			if (i < str.length() - 2 && str.substring(i, i + 2).compareTo("@{") == 0) {
				if (openBrackets == 0) {
					first = i + 2;
				}
				openBrackets++;

			} else if (str.charAt(i) == '}' && openBrackets > 0 && !hasMoreRightBrackets(str, i)) {
				openBrackets -= 1;
				if (openBrackets == 0) {
					countRepl++;
					info(this,"msgid:"+str.substring(first, i));
					def msgid = str.substring(first, i);
					Map<String,String> msg = this.messageService.getMessage(namespace,lang, msgid);
					info(this,"msgstr:"+msg);
					if( msg != null){
						replacement = msg.msgstr;
					}else{
						replacement = msgid;
					}
					newString += replacement;
				}
			} else if (openBrackets == 0) {
				newString += str.charAt(i);
				countPlainStr++;
			}
		}
		if (countRepl == 1 && countPlainStr == 0) {
			return replacement;
		} else {
			return newString;
		}
	}

	private List<Map> getCollection( Map bindings, String name){
		String[] arr = name.split("\\.");
		Object obj = bindings;
		for( String s : arr){
			obj = obj[s];
		} 
		return obj as List;
	} 

	private void htmlToFoList(Map state, Map bindings, String lang) throws Exception {
		Map areas = state.areas as Map;
		areas.each{ entry -> 
			String key = entry.key as String;
			if( !key.startsWith("macro")){
				List<Map> flowBlocks = entry.value['flow'] as List;
				List<Map> absoluteBlocks = entry.value['absolute'] as List;
				List<Map> blocks = flowBlocks + absoluteBlocks;
				blocks.each{ Map block ->
					if( block.blocktype == "macro_block"){
						def area = areas["macro"+block.macroNum] as Map;
						def collectionName = block.collection;
						List<Map> collection = getCollection(bindings,collectionName as String);
						List foLists = [];
						block.foLists = foLists;
						collection.each{ Map colEntry ->
							List foList = [];
							area.flow.each{ Map innerblock ->
								evalBlock( innerblock, colEntry, lang);
								foList.add( innerblock.fo);
							}
							foLists.add(foList);
						}
					}else{
						evalBlock( block, bindings, lang);
					}
				}
			}
		}
	}
	private void evalBlock( Map block, Map bindings, String lang){
		String markdown = null;
		String html = null;
		try{
			if( block.markdown ){
				info(this,"xmarkdown:"+block.markdown);
				String md = evalMessages(block.markdown as String, lang);
				info(this,"xmd:"+md);
				info(this,"bindings:"+bindings);
				markdown = render(md, bindings);
			}else{
				markdown = "";
			}
			info(this,"FOBuilder.groovyRendered:"+markdown);
			html= markdownToHtml( markdown );
			info(this,"FOBuilder.markdownToHtml1:"+html);
			info(this,"FOBuilder.markdownToHtml2:"+block.html);

		}catch(Exception e){
			error(this, "FOBuilder.render:%[exception]s",e);
			if( markdown ){
				html = "" + (markdown as String) +":"+e.getMessage()+"";
			}else{
				html = "" + (block.markdown as String) +":"+e.getMessage()+"";
			}
			html = html.replace( "<", "&lt;");
			html = html.replace( ">", "&gt;");
		}
		try{
			info(this,"HTML:"+markdown);
			block.fo = htmlToFo( "<div>" + HtmlEscape.escape(html) +"</div>"  as String );
			info(this,"FO:"+block.fo);
		}catch(Exception e){
			block.fo = "<fo:block>"+block.markdown+":"+e.getMessage()+"</fo:block>";
			info(this,"Text:"+markdown);
			info(this,"Html:"+html);
			error(this, "FOBuilder.htmlToFoList:%[exception]s",e);
		}
	}

	private String htmlToFo(String html) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		this.transformer.transform(toInputStream(html), bos);
		return bos.toString("UTF-8");
	}
	private String markdownToHtml(String md){
		List<Extension> extensions = Arrays.asList(TablesExtension.create());

		Parser parser = Parser.builder().
			extensions(extensions).
			build();
		Node document = parser.parse(md);
		HtmlRenderer renderer = HtmlRenderer.builder().
			extensions(extensions).
			build();
		return renderer.render(document);
	}

}

