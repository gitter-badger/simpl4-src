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
package org.ms123.common.libhelper;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import org.osgi.framework.Bundle;

/**
 * A ClassLoader delegating to a given OSGi bundle.
 *
 */
@SuppressWarnings("unchecked")
public class BundleDelegatingClassLoader extends ClassLoader {

	private final Bundle bundle;

	private final ClassLoader classLoader;

	public BundleDelegatingClassLoader(Bundle bundle) {
		this(bundle, null);
	}

	public BundleDelegatingClassLoader(Bundle bundle, ClassLoader classLoader) {
		this.bundle = bundle;
		this.classLoader = classLoader;
	}

	protected Class findClass(final String name) throws ClassNotFoundException {
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {

				public Class<?> run() throws ClassNotFoundException {
					Class clazz = null;
					try {
						clazz = bundle.loadClass(name);
					} catch (Exception e) {
					}
					if (clazz != null) {
						return clazz;
					}
					throw new ClassNotFoundException(name);
				}
			});
		} catch (PrivilegedActionException e) {
			Exception cause = e.getException();
			if (cause instanceof ClassNotFoundException) {
				throw (ClassNotFoundException) cause;
			} else {
				throw (RuntimeException) cause;
			}
		}
	}

	protected URL findResource(final String name) {
		URL resource = AccessController.doPrivileged(new PrivilegedAction<URL>() {

			public URL run() {
				URL url = bundle.getEntry(name);
				if (url == null) {
					url = bundle.getEntry("/" + name);
				}
				return url;
			}
		});
		if (classLoader != null && resource == null) {
			resource = classLoader.getResource(name);
		}
		return resource;
	}

	protected Enumeration findResources(final String name) throws IOException {
		Enumeration<URL> urls;
		try {
			urls = AccessController.doPrivileged(new PrivilegedExceptionAction<Enumeration<URL>>() {

				@SuppressWarnings("unchecked")
				public Enumeration<URL> run() throws IOException {
					return (Enumeration<URL>) bundle.getResources(name);
				}
			});
		} catch (PrivilegedActionException e) {
			Exception cause = e.getException();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			} else {
				throw (RuntimeException) cause;
			}
		}
		if (urls == null) {
			urls = Collections.enumeration(new ArrayList<URL>());
		}
		return urls;
	}

	protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class clazz;
		try {
			clazz = findClass(name);
		} catch (ClassNotFoundException cnfe) {
			if (classLoader != null) {
				try {
					clazz = classLoader.loadClass(name);
				} catch (ClassNotFoundException e) {
					throw new ClassNotFoundException(name + " from bundle " + bundle.getBundleId() + " (" + bundle.getSymbolicName() + ")", cnfe);
				}
			} else {
				throw new ClassNotFoundException(name + " from bundle " + bundle.getBundleId() + " (" + bundle.getSymbolicName() + ")", cnfe);
			}
		}
		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}
	public String toString() {
		return "BundleDelegatingClassLoader("+bundle+","+classLoader+")";
	}

	public Bundle getBundle() {
		return bundle;
	}
}
