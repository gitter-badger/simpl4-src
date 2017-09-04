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

