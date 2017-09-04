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
package org.ms123.common.camel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.File;
import org.ms123.common.libhelper.FileSystemClassLoader;
import org.osgi.framework.BundleContext;
import org.ms123.common.libhelper.Inflector;
import org.apache.camel.NoSuchBeanException;
import org.osgi.framework.ServiceReference;
import org.apache.camel.spi.Registry;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;

/**
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class GroovyRegistry implements Registry {

	protected Inflector m_inflector = Inflector.getInstance();
	private ClassLoader m_fsClassLoader;

	private BundleContext m_bundleContect;
	private File[] m_locations;

	public GroovyRegistry(ClassLoader parent, BundleContext bc, String namespace) {
		String basedir = System.getProperty("workspace") + "/" + "groovy" + "/" + namespace;
		String basedir2 = System.getProperty("workspace") + "/" + "java" + "/" + namespace;
		String basedir3 = System.getProperty("workspace") + "/" + "jooq/build";
		String basedir4 = System.getProperty("git.repos") + "/" + namespace + "/.etc/jooq/build";
		m_locations = new File[4];
		m_locations[0] = new File(basedir);
		m_locations[1] = new File(basedir2);
		m_locations[2] = new File(basedir3);
		m_locations[3] = new File(basedir4);
		m_fsClassLoader = new FileSystemClassLoader(parent, m_locations);
		m_bundleContect = bc;
	}

	private Object _lookupByNameType(String name, Class type) {
		Object obj = getObjectFromFileSystemClassloader(name);
		if ((obj == null || !canCast(obj, type)) && name.startsWith("service:")) {
			obj = getService(name);
		}

		if ((obj == null || !canCast(obj, type)) && (isDataSource(type) || name.startsWith("datasource:"))) {
			obj = getDataSource(name);
		}
		debug(this, "_lookupByNameAndType(" + name + "," + type + "): " + obj);
		return obj;
	}

	private boolean canCast(Object obj, Class type) {
		if (type == null)
			return true;
		try {
			return type.cast(obj) != null;
		} catch (Throwable e) {
			return false;
		}
	}

	private boolean isDataSource(Class type) {
		if (type == null){
			return false;
		}
		return type.getName().equals("javax.sql.DataSource");
	}

	private String getServiceClassName(String serviceName) {
		String serviceClassName = null;
		int dot = serviceName.lastIndexOf(".");
		if (dot != -1) {
			String part1 = serviceName.substring(0, dot);
			String part2 = serviceName.substring(dot + 1);
			serviceClassName = "org.ms123.common." + part1 + "." + m_inflector.upperCamelCase(part2, '-') + "Service";
		} else {
			String s = m_inflector.upperCamelCase(serviceName, '-');
			serviceClassName = "org.ms123.common." + s.toLowerCase() + "." + s + "Service";
		}
		debug(this, "ServiceClassName:" + serviceClassName);
		return serviceClassName;
	}

	private Object getService(String name) {
		try {
			name = name.substring("service:".length());
			ServiceReference sr = m_bundleContect.getServiceReference(name);
			if (sr == null) {
				sr = m_bundleContect.getServiceReference(getServiceClassName(name));
			}
			if (sr != null) {
				return m_bundleContect.getService(sr);
			}
		} catch (Throwable e) {
			error(this, "getService(" + name + "):%[exception]s", e);
		}
		return null;
	}

	private Object getDataSource(String name) {
		try {
			if (name.startsWith("datasource:")) {
				name = name.substring("datasource:".length());
			}
			ServiceReference[] sr = m_bundleContect.getServiceReferences((String) null, "(&(objectClass=javax.sql.DataSource)(dataSourceName=" + name + "))");
			if (sr != null && sr.length > 0) {
				return m_bundleContect.getService(sr[0]);
			}
		} catch (Throwable e) {
			error(this, "getDataSource(" + name + "):%[exception]s", e);
		}
		return null;
	}

	private Object getObjectFromFileSystemClassloader(String name) {
		try {
			Class clazz = m_fsClassLoader.loadClass(name);
			Object obj = clazz.newInstance();
			debug(this, "getObjectFromFileSystemClassloader:" + clazz + "/" + obj);
			return obj;
		} catch (Throwable e) {
			debug(this, "getObjectFromFileSystemClassloader(" + name + "):" + e);
		}
		return null;
	}

	public Object lookupByName(String name) {
		return _lookupByNameType(name, null);
	}

	public <T> T lookupByNameAndType(String name, Class<T> type) {
		debug(this, "lookupByNameAndType:" + name + "/" + type);
		Object answer = _lookupByNameType(name, type);
		if (answer == null) {
			return null;
		}
		try {
			return type.cast(answer);
		} catch (Throwable e) {
			String msg = "Found bean: " + name + " in GroovyRegistry: " + this + " of type: " + answer.getClass().getName() + " expected type was: " + type;
			throw new NoSuchBeanException(name, msg, e);
		}
	}

	public <T> Map<String, T> findByTypeWithName(Class<T> type) {
		debug(this, "findByTypeWithName:" + type);
		Map<String, T> result = new HashMap<String, T>();
		T answer = lookupByNameAndType(type.getName(), type);
		if (answer != null) {
			result.put(type.getName(), lookupByNameAndType(type.getName(), type));
		}
		return result;
	}

	public <T> Set<T> findByType(Class<T> type) {
		debug(this, "findByType:" + type);
		Set<T> result = new HashSet<T>();
		T answer = lookupByNameAndType(type.getName(), type);
		if (answer != null) {
			result.add(answer);
		}
		return result;
	}

	public Object lookup(String name) {
		return lookupByName(name);
	}

	public <T> T lookup(String name, Class<T> type) {
		return lookupByNameAndType(name, type);
	}

	public <T> Map<String, T> lookupByType(Class<T> type) {
		return findByTypeWithName(type);
	}

	public void newClassLoader() {
		m_fsClassLoader = new FileSystemClassLoader(GroovyRegistry.class.getClassLoader(), m_locations);
	}
}

