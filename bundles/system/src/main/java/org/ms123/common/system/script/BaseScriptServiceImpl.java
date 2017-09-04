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
package org.ms123.common.system.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ms123.common.git.GitService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.libhelper.Inflector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;

/**
 *
 */
@SuppressWarnings("unchecked")
abstract class BaseScriptServiceImpl {

	protected Inflector m_inflector = Inflector.getInstance();

	protected BundleContext m_bundleContext;
	protected GitService m_gitService;
	protected CamelService m_camelService;

	protected void compileScript(BundleContext bc, String namespace, String path, String code, String type) throws Exception{
		String prefix = m_inflector.capitalizeFirst(type.substring(3));
		info(this,"prefix:"+prefix);
		Class clazz  = bc.getBundle().loadClass("org.ms123.common.system.script.handler."+prefix+"Handler");


		Class[] cargs = new Class[1];
		cargs[0] = BundleContext.class;
		Constructor cons = clazz.getConstructor(cargs);
		Object[] args = new Object[1];
		args[0] = bc;
		Object obj = cons.newInstance(args);

		Class[] margs = new Class[3];
		margs[0] = String.class;
		margs[1] = String.class;
		margs[2] = String.class;
		Method meth = clazz.getDeclaredMethod("compileScript", margs);
		args = new Object[3];
		args[0] = namespace;
		args[1] = path;
		args[2] = code;
		meth.invoke(obj, args);
	}
	protected String checkNull(String s){
		if( s == null) return "";
		return s;
	}
}

