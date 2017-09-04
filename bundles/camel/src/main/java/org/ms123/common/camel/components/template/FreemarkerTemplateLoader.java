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
package org.ms123.common.camel.components.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.FileReader;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.CamelContext;
import org.ms123.common.git.GitService;

import freemarker.cache.TemplateLoader;

/**
 */
public class FreemarkerTemplateLoader implements TemplateLoader {
	CamelContext camelContext;
	GitService m_gitService;

	public FreemarkerTemplateLoader(CamelContext cc) {
		this.camelContext = cc;
		m_gitService = getGitService();
	}

	public GitService getGitService() {
		return getByType(GitService.class);
	}

	private <T> T getByType(Class<T> kls) {
		return kls.cast(this.camelContext.getRegistry().lookupByName(kls.getName()));
	}

	public final Object findTemplateSource(final String name) throws IOException {
		info("FreemarkerTemplateLoader.findTemplateSource:" + name);
		return name;
	}

	public final long getLastModified(final Object templateSource) {
		return -1;
	}

	public final Reader getReader(Object templateSource, final String encoding) throws IOException {
		String name = (String) templateSource;
		if (name == null || name.indexOf(":") == -1) {
			throw new RuntimeException("FreemarkerTemplateLoader.getReader:name not correct:" + name);
		}
		String[] x = name.split(":");
		String namespace = x[0];
		name = x[1];
		info("FreemarkerTemplateLoader.getReader.name:" + name);
		File file = m_gitService.searchFile(namespace, name, "all");
		info("FreemarkerTemplateLoader.getReader.file:" + file);
		return new FileReader(file);
	}

	public final void closeTemplateSource(final Object templateSource) throws IOException {
		// Do nothing
	}

	private void debug(String msg) {
		m_logger.debug(msg);
	}

	private void info(String msg) {
		m_logger.info(msg);
	}

	private static final org.slf4j.Logger m_logger = org.slf4j.LoggerFactory.getLogger(FreemarkerTemplateLoader.class);
}

