/**
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

import java.io.StringWriter;
import java.io.InputStream;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;
import org.ms123.common.data.api.DataLayer;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;


@SuppressWarnings("unchecked")
@groovy.transform.CompileStatic
public class FOBuilder extends TemplateEvaluator{

	private Map<String, Template> m_templateCache = new LinkedHashMap();

	private TemplateEngine m_engine = new MarkupTemplateEngine();

	public FOBuilder(DataLayer dl) {
		TemplateConfiguration templateConfiguration = new TemplateConfiguration();
		templateConfiguration.setUseDoubleQuotes(true);
		templateConfiguration.setAutoIndent(true);
		templateConfiguration.setAutoNewLine(true);
		templateConfiguration.setAutoEscape(true);
		m_engine = new MarkupTemplateEngine(templateConfiguration);
	}

	public InputStream toFO( String namespace, String json, Map<String,String> variableMap){
	}

	public String render( String text, Map<String,Object> variableMap){
		info(this,"GroovyMarkupEngine.convert:"+text);
		String key = getMD5OfUTF8(text);
		Template template = m_templateCache.get(key);
		if (template == null) {
			template = m_engine.createTemplate(text);
			m_templateCache.put(key, template);
		}
		Map binding = [:].withDefault { x -> new DefaultBinding(x) }
		binding.putAll( variableMap);
		info(this,"Template is writing using attributes:" + binding);
		String answer = template.make(binding).toString();
		debug(this,"GroovyMarkupEngine.answer:"+answer);
		return answer;
	}
}

