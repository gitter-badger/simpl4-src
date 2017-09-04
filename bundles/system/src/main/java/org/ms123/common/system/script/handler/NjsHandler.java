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
package org.ms123.common.system.script.handler;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ms123.common.system.script.ScriptEngineService;
import javax.script.ScriptEngine;
import javax.script.Compilable;

/**
 ScriptEngineServiceImpl implementation
 */
public class NjsHandler implements ScriptHandler {
	BundleContext m_bundleContext;

	public NjsHandler(BundleContext bc) {
		m_bundleContext = bc;
	}

	public void compileScript(String namespace, String path, String code) throws Exception{
		ScriptEngine engine = getScriptEngine();
		((Compilable) engine).compile(code);
	}

	private ScriptEngine getScriptEngine(){
		ScriptEngineService scriptEngineService=null;
		ServiceReference sr = m_bundleContext.getServiceReference("org.ms123.common.system.script.ScriptEngineService");
		if (sr != null) {
			scriptEngineService = (ScriptEngineService)m_bundleContext.getService(sr);
		}
		if (scriptEngineService == null) {
			throw new RuntimeException("NjsHandler.Cannot resolve scriptEngineService");
		}
		return scriptEngineService.getEngineByName("nashorn");
	}
}
