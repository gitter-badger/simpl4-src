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
package org.ms123.common.process.tasks;

import groovy.lang.*;
import java.io.*;
import java.util.*;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.control.customizers.*;
import org.ms123.common.libhelper.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

@SuppressWarnings("unchecked")
public class GroovyTaskDsl {
	private Binding m_binding;
	private GroovyShell m_shell;
	private String m_hint;

	public GroovyTaskDsl(EventAdmin ea, String namespace, String processDefinitionKey, String pid, String hint, Map<String, Object> vars) {
		m_hint = hint;
		CompilerConfiguration config = new CompilerConfiguration();
		config.setDebug(true);
		config.setScriptBaseClass(GroovyTaskDslBase.class.getName());
		vars.put("__eventAdmin", ea);
		vars.put("__pid", pid);
		vars.put("__namespace", namespace);
		vars.put("__processDefinitionKey", processDefinitionKey);
		m_binding = new Binding(vars);

		ImportCustomizer importCustomizer = new ImportCustomizer();
		importCustomizer.addStaticStars("java.util.concurrent.TimeUnit");
		importCustomizer.addStarImports("org.apache.camel");
		importCustomizer.addStarImports("org.apache.camel.impl");
		importCustomizer.addStarImports("org.apache.camel.builder");
		config.addCompilationCustomizers(importCustomizer);

		m_shell = new GroovyShell(this.getClass().getClassLoader(), m_binding, config);
	}

	public Object eval(String scriptStr) {
		try {
			return m_shell.evaluate(scriptStr);
		} catch (Throwable e) {
			String msg = Utils.formatGroovyException(e, scriptStr);
			String hint = "";
			if (m_hint != null) {
				hint = "\n-----------------------------\n" + m_hint + "\n";
				hint += "------------------------------\n";
			}
			throw new RuntimeException(hint + msg);
		}
	}

	public Map getVariables() {
		return m_binding.getVariables();
	}

	public Object getVariable(String name) {
		return m_binding.getVariable(name);
	}

	public boolean hasVariable(String name) {
		return m_binding.hasVariable(name);
	}
}

