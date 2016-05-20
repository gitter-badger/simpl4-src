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

