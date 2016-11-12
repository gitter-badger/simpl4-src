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
package org.ms123.common.system.groovy;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.List;
import java.util.Map;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner;
import org.codehaus.groovy.runtime.m12n.StandardPropertiesModuleFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.SynchronousBundleListener;
import static org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport.closeQuietly;

import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import groovy.lang.GroovySystem;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;

import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/** GroovyService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=groovy" })
public class GroovyServiceImpl implements GroovyService, SynchronousBundleListener {
	public static final String MODULE_META_INF_FILE = "META-INF/services/org.codehaus.groovy.runtime.ExtensionModule";

	private BundleContext bundleContext;
	private ClassLoader classLoader;

	public GroovyServiceImpl() {
	}
	@Override
	public void bundleChanged(BundleEvent event) {
		info(this,"bundleChanged("+event.getType()+"):"+event.getBundle().getSymbolicName());
		if (event.getType() == BundleEvent.STARTED) {
			addExtensionMethods(event.getBundle());
		}
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bundleContext = bundleContext;
		bundleContext.addBundleListener(this);

		// also check already installed bundles
		Bundle[] bundles = bundleContext.getBundles();
		if (bundles != null) {
			for (Bundle bundle : bundles) {
				try {
					//		if (bundle.getState() == Bundle.ACTIVE) {
					addExtensionMethods(bundle);
					//		}
				} catch (Throwable e) {
					// this can happen at any time if a bundle was uninstalled after gathering the list of previous installed bundles.
				}
			}
		}
	}

	public void update(Map<String, Object> props) {
	}

	protected void deactivate() throws Exception {
		info(this, "GroovyServiceImpl.deactivate");
		this.bundleContext.removeBundleListener(this);
	}

	private void addExtensionMethods(final Bundle bundle) {
		try {
			Enumeration<URL> resources = bundle.getResources(ExtensionModuleScanner.MODULE_META_INF_FILE);
			if( resources == null) return;
			BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
			ClassLoader classLoader = bundleWiring.getClassLoader();
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				scanExtensionModuleFromMetaInf(url, classLoader);
			}
		} catch (IOException e) {
			error(this, "store:%[exception]s",e);
		}
	}

	private void scanExtensionModuleFromMetaInf(final URL metadata,ClassLoader classLoader) {
		Properties properties = new Properties();
		InputStream inStream = null;
		try {
			inStream = metadata.openStream();
			properties.load(inStream);
		} catch (IOException e) {
			throw new GroovyRuntimeException("Unable to load module META-INF descriptor", e);
		} finally {
			closeQuietly(inStream);
		}
		scanExtensionModuleFromProperties(properties, classLoader);
	}

	private void scanExtensionModuleFromProperties(final Properties properties, ClassLoader classLoader) {
		info(this, "GroovyServiceImpl.scanExtensionModuleFromProperties("+classLoader+"):" + properties);
		StandardPropertiesModuleFactory factory = new StandardPropertiesModuleFactory();
		ExtensionModule module = factory.newModule(properties, classLoader);
		addExtensionMethods(module);
	}
	private void addExtensionMethods(ExtensionModule module) {
		MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
		((MetaClassRegistryImpl) metaClassRegistry).getModuleRegistry().addModule(module);
		Map<CachedClass, List<MetaMethod>> classMap = new HashMap<>();
		for (MetaMethod metaMethod : module.getMetaMethods()){
			if (classMap.containsKey(metaMethod.getDeclaringClass())){
				classMap.get(metaMethod.getDeclaringClass()).add(metaMethod);
			} else {
				List<MetaMethod> methodList = new ArrayList<>();
				methodList.add(metaMethod);
				classMap.put(metaMethod.getDeclaringClass(), methodList);
			}
			if (metaMethod.isStatic()){
				((MetaClassRegistryImpl)metaClassRegistry).getStaticMethods().add(metaMethod);
			} else {
				((MetaClassRegistryImpl)metaClassRegistry).getInstanceMethods().add(metaMethod);
			}
			info(this, "registered method: "+ metaMethod);
		}
		for (CachedClass cachedClass : classMap.keySet()) {
			cachedClass.addNewMopMethods(classMap.get(cachedClass));
		}
	}
}


