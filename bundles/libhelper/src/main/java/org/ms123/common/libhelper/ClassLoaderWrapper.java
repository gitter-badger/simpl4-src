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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


/**
 */
@SuppressWarnings("unchecked")
public class ClassLoaderWrapper extends ClassLoader {

	private ClassLoader[] parents;

	public ClassLoaderWrapper(ClassLoader... parents) {
		this.parents = parents;
	}

	protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class cachedClass = findLoadedClass(name);

		if (cachedClass != null) {
			if (resolve) {
				resolveClass(cachedClass);
			}
			return cachedClass;
		}

		for (int i = 0; i < parents.length; i++) {
			ClassLoader parent = parents[i];

			try {
				Class clazz = parent.loadClass(name);
				if (resolve) {
					resolveClass(clazz);
				}
				return clazz;
			} catch (ClassNotFoundException ignored) {
			}
		}
		throw new ClassNotFoundException(name +" in " + Arrays.asList(parents));
	}

	public URL getResource(String name) {
		for (int i = 0; i < parents.length; i++) {
			ClassLoader parent = parents[i];
			URL url = parent.getResource(name);
			if (url != null) {
				return url;
			}
		}
		return null;
	}

	public Enumeration findResources(String name) throws IOException {
		List resources = new ArrayList();

		for (int i = 0; i < parents.length; i++) {
			ClassLoader parent = parents[i];
			List parentResources = Collections.list(parent.getResources(name));
			resources.addAll(parentResources);
		}
		return Collections.enumeration(resources);
	}
	public String toString(){
		String ret = "ClassLoaderWrapper:";
		for( ClassLoader cl : this.parents){
			ret += "\t"+cl;
		}
		return ret;
	}

}
