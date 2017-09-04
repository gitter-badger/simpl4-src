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
import org.ms123.common.system.compile.java.JavaCompiler;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import org.osgi.framework.ServiceReference;
import org.ms123.common.camel.api.CamelService;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

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
		System.out.flush();
		camelService.newGroovyClassLoader(namespace);
	}

}
