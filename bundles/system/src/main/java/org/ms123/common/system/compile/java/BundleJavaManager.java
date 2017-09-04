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

package org.ms123.common.system.compile.java;

import java.io.IOException;

import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Date;

import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

public class BundleJavaManager extends ForwardingJavaFileManager<JavaFileManager> implements Constants {

	public BundleJavaManager(Bundle bundle, JavaFileManager javaFileManager) throws IOException {
		super(javaFileManager);

		this.javaFileManager = javaFileManager;
		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		this.classLoader = bundleWiring.getClassLoader();
		if( bundleWiringCache == null){
			bundleWiringCache = new HashMap<String, BundleWiring>();
			for (Bundle b : bundle.getBundleContext().getBundles()) {
				bundleWiring = b.adapt(BundleWiring.class);
				if( bundleWiring == null){
					error(this,"bundleWiring is null:"+b);
					continue;
				}
				addResourcesToCache(bundleWiring);
			}
		}
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		if (location != StandardLocation.CLASS_PATH) {
			return this.javaFileManager.getClassLoader(location);
		}
		return this.classLoader;
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if ((location == StandardLocation.CLASS_PATH) && (file instanceof BundleJavaFileObject)) {
			BundleJavaFileObject bundleJavaFileObject = (BundleJavaFileObject) file;
			debug(this, "\t\tInfer:" + bundleJavaFileObject);
			return bundleJavaFileObject.inferBinaryName();
		}
		return this.javaFileManager.inferBinaryName(location, file);
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		List<JavaFileObject> javaFileObjects = new ArrayList<JavaFileObject>();
		String packagePath = packageName.replace('.', '/');
		if (!packageName.startsWith(JAVA_PACKAGE) && (location == StandardLocation.CLASS_PATH)) {
			BundleWiring bundleWiring = bundleWiringCache.get(packagePath);
			if (bundleWiring != null) {
				debug(this, "-----> List.packageName=" + packageName + "\twiring:" + bundleWiring.getBundle());
				list(packagePath, bundleWiring, javaFileObjects);
			}
		}

		if (packageName.startsWith(JAVA_PACKAGE) || (location != StandardLocation.CLASS_PATH) || (javaFileObjects.isEmpty())) {
			Iterable<JavaFileObject> localJavaFileObjects = this.javaFileManager.list(location, packagePath, kinds, recurse);
			for (JavaFileObject javaFileObject : localJavaFileObjects) {
				javaFileObjects.add(javaFileObject);
			}
		}
		return javaFileObjects;
	}

	private String getClassNameFromPath(URL resource, String packageName) {
		String className = resource.getPath();
		if (resource.getProtocol().equals("jar")) {
			int pos = className.indexOf("!");
			className = className.substring(pos + 1);
		}

		int x = className.indexOf(packageName);
		int y = className.indexOf('.');

		className = className.substring(x, y).replace('/', '.');

		if (className.startsWith(PERIOD)) {
			className = className.substring(1);
		}

		return className;
	}

	private JavaFileObject getJavaFileObject(URL resourceURL, String packagePath) {
		String protocol = resourceURL.getProtocol();
		String className = getClassNameFromPath(resourceURL, packagePath);

		if (protocol.equals("bundle") || protocol.equals("bundleresource")) {
			try {
				return new BundleJavaFileObject(resourceURL.toURI(), className);
			} catch (Exception e) {
				error(this, "getJavaFileObject.error:%[exception]s", e);
			}
		} else if (protocol.equals("jar")) {
			try {
				JarURLConnection jarUrlConnection = (JarURLConnection) resourceURL.openConnection();
				URI uri = jarUrlConnection.getJarFileURL().toURI();
				String entryName = jarUrlConnection.getEntryName();

				return new JarJavaFileObject(uri, className, resourceURL, entryName);
			} catch (Exception e) {
				error(this, "getJavaFileObject2.error:%[exception]s", e);
			}
		}

		return null;
	}

	private void addResourcesToCache(BundleWiring bundleWiring) {
		debug(this, "=====================================:" + bundleWiring + "/" + new Date().getTime());
		ResourceResolver resourceResolver = getResourceResolver();

		Collection<String> resources = resourceResolver.resolveResources(bundleWiring, "/", "*.class", BundleWiring.LISTRESOURCES_RECURSE);
		if ((resources == null) || resources.isEmpty()) {
			return;
		}

		for (String resourceName : resources) {
			URL resourceURL = resourceResolver.getResource(bundleWiring, resourceName);
			int slash = resourceName.lastIndexOf("/");
			if (slash != -1) {
				String packagePath = resourceName.substring(0, slash);
				if (bundleWiringCache.get(packagePath) == null) {
					bundleWiringCache.put(packagePath, bundleWiring);
				}
			}
		}
	}

	private ResourceResolver getResourceResolver() {
		if (this.resourceResolver != null) {
			return this.resourceResolver;
		}
		this.resourceResolver = new BasicResourceResolver();
		return this.resourceResolver;
	}

	private void list(String packagePath,BundleWiring bundleWiring, List<JavaFileObject> javaFileObjects) {
		ResourceResolver resourceResolver = getResourceResolver();
		Collection<String> resources = resourceResolver.resolveResources(bundleWiring, packagePath, "*.class", 0);
		if ((resources == null) || resources.isEmpty()) {
			return;
		}
		for (String resourceName : resources) {
			URL resourceURL = resourceResolver.getResource(bundleWiring, resourceName);
			JavaFileObject javaFileObject = getJavaFileObject(resourceURL, packagePath);
			if (javaFileObject == null) {
				debug(this, "\tCould not create JavaFileObject for {" + resourceURL + "}");
				continue;
			}
			debug(this, "\t" + javaFileObject);
			javaFileObjects.add(javaFileObject);
		}
	}

	private ClassLoader classLoader;
	private JavaFileManager javaFileManager;
	private ResourceResolver resourceResolver;
	private static Map<String, BundleWiring> bundleWiringCache;
}

