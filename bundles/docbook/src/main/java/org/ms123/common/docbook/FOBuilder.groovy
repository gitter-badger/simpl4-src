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
import org.osgi.framework.BundleContext;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.apache.commons.io.IOUtils.toString;


@SuppressWarnings("unchecked")
@groovy.transform.CompileStatic
public class FOBuilder extends TemplateEvaluator{

	private Map<String, Template> templateCache = new LinkedHashMap();

	private MarkupTemplateEngine engine = null;
	private MyTemplateResolver resolver = null;
	private BundleContext bc;
	private String namespace;
	private	TransformToFo transformer = null;

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
	}

	public InputStream toFO( String jsonText, Map<String,Object> variableMap){
		def jsonSlurper = new JsonSlurper();
		def json = (Map)jsonSlurper.parseText(jsonText);
		if( json.templateName == null){
			json = [ state:json, templateName:"master.tpl"];
		}
		def templateName = json.templateName as String;

		def isMod = this.resolver.testModified( templateName );
		def template = this.templateCache.get(templateName);
		if (template == null || isMod) {
			template = this.engine.createTemplateByPath( templateName);
			this.templateCache.put(templateName, template);
		}
		if( variableMap.bindings != null){
			htmlToFoList( json.state as Map, variableMap.bindings as Map );
		}else{
			htmlToFoList( json.state as Map, variableMap );
		}

		Map binding = [:].withDefault { x -> new DefaultBinding(x) }
		binding.putAll( variableMap);
		binding.putAll( json.state as Map);
		info(this,"FOBuilder.binding:" + binding);
		String answer = template.make(binding).toString();
		info(this,"FOBuilder.answer:"+answer);

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
	private void htmlToFoList(Map state, Map bindings) throws Exception {

		Map areas = state.areas as Map;
		areas.each{ entry -> 
			List<Map> flowBlocks = entry.value['flow'] as List;
			List<Map> staticBlocks = entry.value['static'] as List;
			List<Map> blocks = flowBlocks + staticBlocks;
			blocks.each{ block ->
				def text = null;
				try{
					text = render(block.html as String, bindings);
				}catch(Exception e){
					text = (block.html as String) + "<div>"+e.getMessage()+"</div>";
				}
				info(this,"HTML:"+text);
				block.fo = htmlToFo( "<div>" + text +"</div>"  as String );
				info(this,"FO:"+block.fo);
			}
		}
	}
	private String htmlToFo(String html) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		this.transformer.transform(toInputStream(html), bos);
		return bos.toString("UTF-8");
	}
}

