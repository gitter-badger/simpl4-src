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
