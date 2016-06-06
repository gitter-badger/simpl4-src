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
import org.ms123.common.system.compile.java.JavaCompiler;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import org.osgi.framework.ServiceReference;
import org.ms123.common.camel.api.CamelService;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;

/**
 ScriptEngineServiceImpl implementation
 */
public class JavaHandler implements ScriptHandler {
	BundleContext m_bundleContext;

	public JavaHandler(BundleContext bc) {
		m_bundleContext = bc;
	}

	public void compileScript(String namespace, String path, String code) throws Exception{
		String destDir = System.getProperty("workspace")+"/"+ "java"+"/"+namespace;
		String srcDir = System.getProperty("git.repos")+"/"+namespace;
		File f = new File(destDir);
		if(!f.exists()){
			f.mkdirs();
		}
		try{
			JavaCompiler.compile(namespace, m_bundleContext.getBundle(), FilenameUtils.getBaseName(path), code,new File(destDir));
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		CamelService camelService=null;
		ServiceReference sr = m_bundleContext.getServiceReference("org.ms123.common.camel.api.CamelService");
		if (sr != null) {
			camelService = (CamelService)m_bundleContext.getService(sr);
		}
		if (camelService == null) {
			throw new RuntimeException("JavaHandler.Cannot resolve camelService");
		}
		camelService.newGroovyClassLoader();
	}

}
