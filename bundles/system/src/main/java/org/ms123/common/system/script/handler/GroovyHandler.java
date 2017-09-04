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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import org.codehaus.groovy.tools.FileSystemCompiler;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import static org.ms123.common.libhelper.Utils.formatGroovyException;
import org.ms123.common.camel.api.CamelService;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;


/**
 GroovyHandler implementation
 */
public class GroovyHandler implements ScriptHandler {

	BundleContext m_bundleContext;
	public GroovyHandler(BundleContext bc) {
		m_bundleContext = bc;
	}

	public void compileScript(String namespace, String path, String code) throws Exception{
		List<String> classpath = new ArrayList<String>();
		classpath.add(System.getProperty("workspace") + "/" + "jooq/build");
		classpath.add(System.getProperty("git.repos") + "/" + namespace + "/.etc/jooq/build");
		classpath.add(System.getProperty("workspace") + "/groovy/" + namespace);
		classpath.add(System.getProperty("workspace") + "/java/" + namespace);
		String destDir = System.getProperty("workspace")+"/"+ "groovy"+"/"+namespace;
		String srcDir = System.getProperty("git.repos")+"/"+namespace;
		CompilerConfiguration.DEFAULT.getOptimizationOptions().put("indy", false);
		CompilerConfiguration config = new CompilerConfiguration();
		config.getOptimizationOptions().put("indy", false);
		config.setClasspathList( classpath );
		config.setTargetDirectory( destDir);
		FileSystemCompiler fsc = new FileSystemCompiler(config);

		File[] files = new File[1];
		files[0] = new File(srcDir, path);
		try {
			fsc.compile(files);
		} catch (Throwable e) {
			String msg = formatGroovyException(e,code);
			throw new RuntimeException(msg);
		}
		
		CamelService camelService=null;
		ServiceReference sr = m_bundleContext.getServiceReference("org.ms123.common.camel.api.CamelService");
		if (sr != null) {
			camelService = (CamelService)m_bundleContext.getService(sr);
		}
		if (camelService == null) {
			throw new RuntimeException("GroovyHandler.Cannot resolve camelService");
		}
		camelService.newGroovyClassLoader(namespace);
	}

}
