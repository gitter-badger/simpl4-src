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
package org.ms123.common.domainobjects;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jdo.JDOEnhancer;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.libhelper.FileSystemClassLoader;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.domainobjects.api.ClassGenService;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.setting.api.SettingService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.compile.CompileService;
import org.ms123.common.system.thread.ThreadContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.commons.beanutils.PropertyUtils.getProperty;
import static org.apache.commons.beanutils.PropertyUtils.setProperty;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.moveDirectoryToDirectory;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/** DomainObjectsServiceImpl implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=domainobjects" })
public class DomainObjectsServiceImpl implements org.ms123.common.domainobjects.api.DomainObjectsService, EventHandler {

	private ServiceRegistration m_serviceRegistration;

	protected BundleContext m_bc;

	private EntityService m_entityService;

	protected CompileService m_compileService;

	private ClassGenService m_nucleusClassGenService;

	private ClassGenService m_orientdbClassGenService;

	private PermissionService m_permissionService;

	private NucleusService m_nucleusService;

	private static String SOURCES = "sources";

	private static String ENTITIES = "entities";

	private static String FIELD = "field";

	private static String RELATION = "relation";

	private static String ENTITY = "entity";

	private static String NAMESPACE = "namespace";

	private int INSERT = 0;

	private int UPDATE = 1;

	private int DELETE = 2;

	protected Inflector m_inflector = Inflector.getInstance();

	static final String[] topics = new String[] { "namespace/installed" };
	private static String workspace = System.getProperty("workspace");
	private static String gitRepos = System.getProperty("git.repos");

	public DomainObjectsServiceImpl() {
		info(this,"DomainObjectsServiceImpl construct");
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		m_bc = bundleContext;
		info(this,"DomainObjectsServiceImpl.activate.props:" + props);
		try {
			info(this, "DomainObjectsServiceImpl.activate -->");
			Bundle b = bundleContext.getBundle();
			Dictionary d = new Hashtable();
			d.put(EventConstants.EVENT_TOPIC, topics);
			m_serviceRegistration = b.getBundleContext().registerService(EventHandler.class.getName(), this, d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handleEvent(Event event) {
		info(this, "DomainObjectsServiceImpl.Event: " + event);
		try {
			String ns = (String) event.getProperty("namespace");
			StoreDesc sdesc = StoreDesc.getNamespaceData(ns);
			m_permissionService.loginInternal(ns);
			ThreadContext.loadThreadContext(ns, "admin");
			createClasses(sdesc);
			info(this,">>>> End handleEvent:" + ThreadContext.getThreadContext().get(ThreadContext.SESSION_MANAGER));
			ThreadContext.getThreadContext().finalize(null);
			info(this,">>>> End handleEvent");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	public void update(Map<String, Object> props) {
	}

	protected void deactivate() throws Exception {
		info(this,"DomainObjectsServiceImpl.deactivate");
		m_serviceRegistration.unregister();
	}

	public void createClasses(StoreDesc sdesc) throws Exception {
		String namespace = sdesc.getNamespace();
		String pack = sdesc.getPack();
		List<Map> entities = getEntities(sdesc);
		File outDir = new File(sdesc.getBaseDir(), "classes");
		ClassLoader clParent = Thread.currentThread().getContextClassLoader();
		List<String> classFiles = null;
		try {
			//BundleDelegatingClassLoader bdc = new BundleDelegatingClassLoader(m_bc.getBundle(), clParent);
			//Thread.currentThread().setContextClassLoader(bdc);
			if( isOrientDB(sdesc)){
				outDir = new File(sdesc.getBaseDir(), sdesc.getNamespace());
				classFiles = m_orientdbClassGenService.generate(sdesc, entities, outDir.toString());
				return;
			}else{
				classFiles = m_nucleusClassGenService.generate(sdesc, entities, outDir.toString());
			}
		} finally {
			//Thread.currentThread().setContextClassLoader(clParent);
		}
		enhance(sdesc, entities, classFiles);
	}

	private Map enhance(StoreDesc sdesc, List<Map> entities, List<String> classNames) throws Exception {
		File outDir1 = new File(sdesc.getBaseDir(), "classes");
		if (!outDir1.exists()) {
			outDir1.mkdirs();
		}
		File[] locations = new File[1];
		locations[0] = outDir1;
		info(this,"Enhancer.enhance:" + sdesc);
		FileSystemClassLoader fscl = new FileSystemClassLoader(this.getClass().getClassLoader(), locations);
		JDOEnhancer enhancer = m_nucleusService.getEnhancer(sdesc);
		enhancer.setClassLoader(fscl);
		if (enhancer != null) {
			for (String className : classNames) {
				File file = new File(outDir1, className.replace(".", "/"));
				if (jdoStore(entities, className)) {
					info(this,"DomainObjectsServiceImpl.enhancer.add:" + file);
					enhancer.addClasses(file.toString() + ".class");
				}
			}
			enhancer.enhance();
		}
		try {
			info(this,"Enhancer.close:" + sdesc);
			m_nucleusService.close(sdesc);
		} catch (Exception e) {
			error(this,"Nucleus.close:%[exception]s" , e);
		}
		return new HashMap();
	}

	public ClassLoader getClassLoader( StoreDesc sdesc){
		if( isOrientDB(sdesc)){
			return m_orientdbClassGenService.getClassLoader(sdesc);
		}
		return null;
	}

	private List<Map> getEntities(StoreDesc sdesc) throws Exception {
		String pack = sdesc.getPack();
		boolean withInternal = false;
		if (!StoreDesc.isAidPack(pack)) {
			withInternal = true;
		}
		List entities = m_entityService.getEntities(sdesc, false, null);
		//List entTypes = m_entityService.getEntitytypes(sdesc.getStoreId());
		//info(this,"entities:"+entities);
		//info(this,"entTypes:"+entTypes);
		return entities;
	}

	private List<Map> prepareEntities(List<Map> entities) {
		List<Map> ents = new ArrayList();
		for (Map ent : entities) {
			String name = (String) ent.get("name");
			String clazz = m_inflector.getClassName(name);
			Map<String, Object> m = new HashMap();
			m.put("name", name);
			m.put("classname", clazz);
			m.put("genDefFields", ent.get("default_fields"));
			m.put("fields", ent.get("fields"));
			m.put(StoreDesc.PACK, ent.get(StoreDesc.PACK));
			ents.add(m);
		}
		return ents;
	}

	private boolean jdoStore(List<Map> entities, String className) {
		int dot = className.lastIndexOf(".");
		String entName = m_inflector.getEntityName(className.substring(dot + 1));
		for (Map entMap : entities) {
			String mname = (String) entMap.get("name");
			if (entName.equals(mname)) {
				String store = (String) entMap.get(StoreDesc.STORE);
				info(this,"\tJdoStore:" + entName + "/" + store);
				if (!"jcr".equals(store)) {
					return true;
				}
			}
		}
		return false;
	}

	private String checkNull(Map m, String key, String msg) {
		if (m.get(key) != null) {
			return (String) m.get(key);
		}
		throw new RuntimeException(msg);
	}

	/*BEGIN JSON-RPC-API*/
	@RequiresRoles("admin")
	public void createClasses(@PName(StoreDesc.STORE_ID) String storeId) throws RpcException {
		try {
			StoreDesc sdesc = StoreDesc.get(storeId);
			createClasses(sdesc);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "DomainObjectsServiceImpl.createClasses:", e);
		}
	}

	private boolean isOrientDB(StoreDesc sdesc){
		return StoreDesc.STORE_GRAPH.equals( sdesc.getStore()) && StoreDesc.VENDOR_ORIENTDB.equals( sdesc.getVendor());
	}

	/*END JSON-RPC-API*/
	@Reference(dynamic = true)
	public void setEntityService(EntityService paramEntityService) {
		m_entityService = paramEntityService;
		info(this,"DomainObjectsServiceImpl.setEntityService:" + paramEntityService);
	}

	@Reference(target = "(kind=nucleus)", dynamic = true)
	public void setClassGenNucleusService(ClassGenService sgs) {
		m_nucleusClassGenService = sgs;
		info(this,"DomainObjectsServiceImpl.setClassGenService:" + sgs);
	}
	@Reference(target = "(kind=orient)", dynamic = true)
	public void setClassGenOrientService(ClassGenService sgs) {
		m_orientdbClassGenService = sgs;
		info(this,"DomainObjectsServiceImpl.setClassGenOrientService:" + sgs);
	}

	@Reference(dynamic = true)
	public void setNucleusService(NucleusService paramNucleusService) {
		this.m_nucleusService = paramNucleusService;
		info(this,"DomainObjectsServiceImpl.setNucleusService:" + paramNucleusService);
	}

	@Reference(dynamic = true)
	public void setPermissionService(PermissionService shiroService) {
		info(this,"DomainObjectsServiceImpl:" + shiroService);
		this.m_permissionService = shiroService;
	}
}

