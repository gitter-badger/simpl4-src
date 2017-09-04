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
package org.ms123.common.workflow;


import java.io.*;
import groovy.lang.*;
import org.codehaus.groovy.control.*;
import java.util.*;
import org.ms123.common.libhelper.Utils;

import org.ms123.common.data.api.SessionContext;
import org.ms123.common.workflow.api.WorkflowService;
import org.ms123.common.camel.api.CamelService;
import org.osgi.service.event.EventAdmin;
import org.osgi.framework.BundleContext;

import org.codehaus.groovy.control.customizers.*;

@SuppressWarnings("unchecked")
public class GroovyTaskDsl {
	private SessionContext m_sessionContext;
	private Binding m_binding;
	private GroovyShell m_shell;
	private String m_hint;
	public GroovyTaskDsl(SessionContext sc, EventAdmin ea, WorkflowService ws, String namespace, String processDefinitionKey, String pid, String hint, Map<String, Object> vars) {
		m_sessionContext = sc;
		m_hint = hint;
		CompilerConfiguration config = new CompilerConfiguration();
		config.setDebug(true);
		config.setScriptBaseClass(org.ms123.common.workflow.api.GroovyTaskDslBase.class.getName());
		vars.put("__sessionContext", sc);
		vars.put("__eventAdmin", ea);
		vars.put("__workflowService", ws);
		vars.put("__camelService", ws.lookupServiceByName(CamelService.class.getName()));
		vars.put("__pid", pid);
		vars.put("__namespace", namespace);
		vars.put("__processDefinitionKey", processDefinitionKey);
		vars.put("__queriedObjects", new ArrayList());
		vars.put("__createdObjects", new ArrayList());
		m_binding = new Binding(vars);

		ImportCustomizer importCustomizer = new ImportCustomizer();
		importCustomizer.addStaticStars( "java.util.concurrent.TimeUnit");
		//importCustomizer.addStaticImport( "java.util.concurrent.TimeUnit.MILLISECONDS", "toDays");
		//importCustomizer.addImports( "java.util.concurrent.TimeUnit");
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
			String msg = Utils.formatGroovyException(e,scriptStr);
			String hint = "";
			if( m_hint != null){
				hint = "\n-----------------------------\n"+m_hint+"\n";
				hint += "------------------------------\n";
			}
			throw new RuntimeException(hint +msg);
		}
	}

	public List<Object> getCreatedObjects() {
		return (List) m_binding.getVariable("__createdObjects");
	}

	public List<Object> getQueriedObjects() {
		return (List) m_binding.getVariable("__queriedObjects");
	}

	public  Map getVariables() {
		return  m_binding.getVariables();
	}
	public  Object getVariable(String name) {
		return  m_binding.getVariable(name);
	}
	public  boolean hasVariable(String name) {
		return  m_binding.hasVariable(name);
	}
}
