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
package org.ms123.common.importing;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import com.Ostermiller.util.*;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.jdo.JDOObjectNotFoundException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.datamapper.DatamapperService;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultLong;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.permission.api.PermissionException;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.utils.UtilsService;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.data.dupcheck.DublettenCheckService;
import org.ms123.common.entity.api.EntityService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jdo.PersistenceManager;
import javax.jdo.Extent;
import javax.jdo.Query;
import javax.transaction.UserTransaction;
import org.apache.commons.beanutils.PropertyUtils;
import org.ms123.common.libhelper.Bean2Map;
import org.milyn.SmooksFactory;
import org.ms123.common.libhelper.Base64;
import org.ms123.common.system.orientdb.OrientDBService;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import static org.ms123.common.utils.IOUtils.toByteArray;
import static org.apache.commons.beanutils.PropertyUtils.setProperty;
import static org.apache.commons.beanutils.PropertyUtils.getProperty;
import static java.text.MessageFormat.format;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

/** ImportingService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=importing" })
public class ImportingServiceImpl extends BaseImportingServiceImpl implements ImportingService, Constants {

	private static final Logger m_logger = LoggerFactory.getLogger(ImportingServiceImpl.class);

	public ImportingServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
	}

	protected void deactivate() throws Exception {
		System.out.println("ImportingServiceImpl deactivate");
	}

	private StoreDesc getStoreDesc(String ns){
		StoreDesc sdesc = null;
		try{
			sdesc = StoreDesc.get(ns+"_config");
		}catch(Exception e){
		}
		if( sdesc == null){
			sdesc = StoreDesc.getNamespaceMeta(ns);
		}
		return sdesc;
	}

	/* BEGIN JSON-RPC-API*/
	public List<Map> getImportings(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(PREFIX)          @POptional String prefix,
			@PName(MAPPING)          @POptional Map mapping) throws RpcException {
		StoreDesc sdesc = getStoreDesc(namespace);
		try {
			return _getImportings(sdesc, prefix, mapping);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ImportingService.getImportings:", e);
		}
	}

	public Map createImporting(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(IMPORTING_ID)       String importingid, 
			@PName(DESCRIPTION)        @POptional String description, 
			@PName(SETTINGS)           @POptional Map settings) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc(namespace);
			Map data = new HashMap();
			data.put(IMPORTING_ID, importingid);
			data.put(DESCRIPTION, description);
			data.put(USER, getUserName());
			if (settings != null) {
				String jsonBody = m_js.deepSerialize(settings);
				data.put(JSON_BODY, jsonBody);
			}
			try {
				getDataLayer(sdesc).deleteObject(null, sdesc, IMPORTING_ENTITY, importingid);
			} catch (Exception e) {
			}
			Map ret = getDataLayer(sdesc).insertObject(data, sdesc, IMPORTING_ENTITY, null, null);
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ImportingService.createImporting:", e);
		} finally {
		}
	}

	public Map updateImporting(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(IMPORTING_ID)       String importingid, 
			@PName(DESCRIPTION)        @POptional String description, 
			@PName(SETTINGS)           @POptional Map settings) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc(namespace);
			Map data = new HashMap();
			data.put(IMPORTING_ID, importingid);
			data.put(DESCRIPTION, description);
			if (settings != null) {
				String jsonBody = m_js.deepSerialize(settings);
				data.put("jsonBody", jsonBody);
			}
			Map ret = getDataLayer(sdesc).updateObject(data, null, null, sdesc, IMPORTING_ENTITY, importingid, null, null);
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ImportingServiceImpl.updateImporting:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public Map deleteImporting(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(IMPORTING_ID)       String importingid) throws RpcException {
		try {
			StoreDesc sdesc = getStoreDesc(namespace);
			Map ret = getDataLayer(sdesc).deleteObject(null, sdesc, IMPORTING_ENTITY, importingid);
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ImportingServiceImpl.deleteImporting:", e);
		} finally {
		}
	}

	public Map getSettings(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(IMPORTING_ID)       @POptional String importingid) throws RpcException {
		StoreDesc sdesc = getStoreDesc(namespace);
		SessionContext sessionContext = getDataLayer(sdesc).getSessionContext(sdesc);
		try {
			String className = m_inflector.getClassName(IMPORTING_ENTITY);
			Class clazz = sessionContext.getClass(className);
			Object obj = sessionContext.getObjectById(clazz, importingid);
			if (obj == null) {
				System.out.println("ImportingServiceImpl.getSettings:importingid:\"" + importingid + "\" not found");
				return new HashMap();
			}
			Map settings = (Map) m_ds.deserialize((String) getProperty(obj, JSON_BODY));
			return settings;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ImportingServiceImpl.getSettings:", e);
		} finally {
			sessionContext.handleFinally(null);
		}
	}

	public String getFileContent(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(IMPORTING_ID)       @POptional String importingid) throws RpcException {
		StoreDesc sdesc = getStoreDesc(namespace);
		SessionContext sessionContext = getDataLayer(sdesc).getSessionContext(sdesc);
		try {
			String className = m_inflector.getClassName(IMPORTING_ENTITY);
			Class clazz = sessionContext.getClass(className);
			Object obj = sessionContext.getObjectById(clazz, importingid);
			if (obj == null) {
				System.out.println("ImportingServiceImpl.getFileContent:importingid:\"" + importingid + "\" not found");
				return null;
			}
			byte[] content = (byte[]) getProperty(obj, CONTENT);
			return new String(content);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ImportingServiceImpl.getFileContent:", e);
		} finally {
			sessionContext.handleFinally(null);
		}
	}

	public Map getFileModel(
			@PName(StoreDesc.NAMESPACE) String namespace, 
			@PName(IMPORTING_ID)       @POptional String importingid) throws RpcException {
		StoreDesc sdesc = getStoreDesc(namespace);
		SessionContext sessionContext = getDataLayer(sdesc).getSessionContext(sdesc);
		try {
			String className = m_inflector.getClassName(IMPORTING_ENTITY);
			Class clazz = sessionContext.getClass(className);
			Object obj = sessionContext.getObjectById(clazz, importingid);
			if (obj == null) {
				System.out.println("ImportingServiceImpl.getFileModel:importingid:\"" + importingid + "\" not found");
				return null;
			}
			Map settings = (Map) m_ds.deserialize((String) getProperty(obj, JSON_BODY));
			byte[] content = (byte[]) getProperty(obj, CONTENT);
			return getFileModel(content, (Map) settings.get(SOURCE_SETUP));
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ImportingServiceImpl.getFileModel:", e);
		} finally {
			sessionContext.handleFinally(null);
		}
	}

	public Object doImport(
			@PName(StoreDesc.STORE_ID) String storeId, 
			@PName(IMPORTING_ID)       @POptional String importingid, 
			@PName("withoutSave")      @POptional @PDefaultBool(false) Boolean withoutSave,
			@PName("max")      @POptional @PDefaultInt(-1) Integer max
					) throws RpcException {
		StoreDesc data_sdesc = StoreDesc.get(storeId);
		StoreDesc aid_sdesc = getStoreDesc(data_sdesc.getNamespace());
		SessionContext sessionContext = getDataLayer(aid_sdesc).getSessionContext(aid_sdesc);
		try {
			String className = m_inflector.getClassName(IMPORTING_ENTITY);
			Class clazz = sessionContext.getClass(className);
			Object obj = sessionContext.getObjectById(clazz, importingid);
			if (obj == null) {
				throw new RuntimeException("ImportingServiceImpl.doImport:importingid:\"" + importingid + "\" not found");
			}
			Map settings = (Map) m_ds.deserialize((String) getProperty(obj, JSON_BODY));
			byte[] content = (byte[]) getProperty(obj, CONTENT);
			if( settings.get("input")!= null){
				System.out.println("doImport:"+settings);
				System.out.println("doImport:"+m_datamapper+"/"+data_sdesc+"/"+content);
				sessionContext = getDataLayer(data_sdesc).getSessionContext(data_sdesc);
				BeanFactory bf = new BeanFactory(sessionContext, settings);
				settings.put("database", "default");
				if( isOrientDB(data_sdesc)){
					settings.put("database", "orientdb");
				}
				UserTransaction ut = sessionContext.getUserTransaction();
				OrientGraphFactory factory=null;
				OrientGraph orientGraph=null;
				if( ut == null){
					factory = this.m_orientdbService.getFactory(data_sdesc.getNamespace(),false);
					orientGraph = factory.getTx();
				}
				Object ret = m_datamapper.transform(data_sdesc.getNamespace(), settings, null, new String(content), bf);
				if( withoutSave) return ret;
				try{
					if( ut != null){
						ut.begin();
					}else{
						orientGraph.begin();
					}
					Map outputTree = (Map)settings.get("output");
					Map<String,Object> persistenceSpecification = (Map)outputTree.get("persistenceSpecification");
					Object o = null;
					if( ut != null){
						o = org.ms123.common.data.MultiOperations.persistObjects(sessionContext,ret,persistenceSpecification, -1);
						ut.commit();
					}else{
						o = new HashMap();
						orientGraph.commit();
					}
					return o;
				}catch(Exception e){
					if( ut != null){
						ut.rollback();
					}else{
						orientGraph.rollback();
					}
					throw e;
				} finally{
					if( orientGraph != null){
						orientGraph.shutdown();
					}
				}
			
			}else{
				return doImport(data_sdesc, settings, content, withoutSave, max);
			}
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ImportingServiceImpl.doImport:", e);
		} finally {
			sessionContext.handleFinally(null);
		}
	}

	public Map upload(
			@PName(StoreDesc.STORE_ID) String storeId, 
			@PName(IMPORTING_ID)       @POptional String importingid, 
			@PName(FILE_CONTENT)       @POptional String fileContent, 
			@PName(FILE_MAP)           @POptional Map fileMap, 
			@PName(SETTINGS)           @POptional Map settings, 
			@PName("withoutImport")    @POptional @PDefaultBool(false) Boolean withoutImport) throws RpcException {

		if( fileMap == null && fileContent == null){
				throw new RuntimeException("fileMap or fileContent is needed");
		}
		StoreDesc data_sdesc = StoreDesc.get(storeId);
		StoreDesc aid_sdesc = getStoreDesc(data_sdesc.getNamespace());
		SessionContext sessionContext = getDataLayer(aid_sdesc).getSessionContext(aid_sdesc);
		PersistenceManager pm = sessionContext.getPM();
		UserTransaction ut = sessionContext.getUserTransaction();
		try {
			ut.begin();
			System.out.println("upload:" + data_sdesc + "/" + fileMap);
			String className = m_inflector.getClassName(IMPORTING_ENTITY);
			Class clazz = sessionContext.getClass(className);
			Object obj = sessionContext.getObjectById(clazz, importingid);
			if (obj == null) {
				obj = sessionContext.createObject(IMPORTING_ENTITY);
				setProperty(obj, IMPORTING_ID, importingid);
				sessionContext.makePersistent(obj);
			}
			byte[] bytes = null;
			if( fileMap != null){
				Map importFile = (Map) fileMap.get("importfile");
				String storeLocation = (String) importFile.get("storeLocation");
				InputStream is = new FileInputStream(new File(storeLocation));
				is = checkForUtf8BOMAndDiscardIfAny(is);
				bytes = toByteArray(is);
				is.close();
			}else if (fileContent != null && fileContent.startsWith("data:")){
				int ind = fileContent.indexOf(";base64,");
				bytes = Base64.decode(fileContent.substring(ind+8));
			}
			bytes = convertToUTF8(bytes);
			setProperty(obj, USER, getUserName());
			setProperty(obj, CONTENT, bytes);
			if (settings != null) {
				setProperty(obj, JSON_BODY, m_js.deepSerialize(settings));
			}
			ut.commit();
			Map ret = null;
			if (settings != null && !withoutImport) {
				ret = doImport(data_sdesc, settings, bytes, false, -1);
			}
			return ret;
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "ImportingServiceImpl.upload:", e);
		} finally {
			sessionContext.handleFinally(ut);
		}
	}
	public Object dmUpload(
			@PName(StoreDesc.STORE_ID) String storeId, 
			@PName(IMPORTING_ID)       @POptional String importingid, 
			@PName("dmConfig")         @POptional Map config, 
			@PName(FILE_CONTENT)       @POptional String fileContent, 
			@PName(FILE_MAP)           @POptional Map fileMap ) throws RpcException {
			upload(storeId,importingid, fileContent, fileMap,null,true);
			if( fileContent == null){
				StoreDesc sdesc = StoreDesc.get(storeId);
				fileContent = getFileContent(sdesc.getNamespace(),importingid);
			}
		return m_datamapper.getMetaData2(config,fileContent);
	}


	/* END JSON-RPC-API*/
	@Reference(target = "(kind=jdo)", dynamic = true, optional = true)
	public void setDataLayerNucleus(DataLayer dataLayer) {
		System.out.println("ImportingServiceImpl.setDataLayer:" + dataLayer);
		m_dataLayerJDO = dataLayer;
	}
	@Reference(target = "(kind=orientdb)", dynamic = true)
	public void setDataLayerOrientDB(DataLayer paramDataLayer) {
		this.m_dataLayerOrientDB = paramDataLayer;
		System.out.println("DataServiceImpl.setDataLayerOrientDB:" + paramDataLayer);
	}
	@Reference(dynamic = true, optional = true)
	public void setDatamapper(DatamapperService datamapper) {
		System.out.println("ImportingServiceImpl.setDatamapper:" + datamapper);
		m_datamapper = datamapper;
	}

	@Reference(dynamic = true)
	public void setPermissionService(PermissionService paramPermissionService) {
		this.m_permissionService = paramPermissionService;
		System.out.println("ImportingServiceImpl.setPermissionService:" + paramPermissionService);
	}

	@Reference(dynamic = true)
	public void setUtilsService(UtilsService paramUtilsService) {
		this.m_utilsService = paramUtilsService;
		System.out.println("ImportingServiceImpl.setUtilsService:" + paramUtilsService);
	}

	@Reference(dynamic = true)
	public void setSmooksFactory(SmooksFactory paramSmooksFactory) {
		m_smooksFactory = paramSmooksFactory;
		System.out.println("ImportingServiceImpl.setSmooksFactory:" + paramSmooksFactory);
	}

	@Reference
	public void setOrientDBService(OrientDBService paramEntityService) {
		m_orientdbService = paramEntityService;
		System.out.println("ImportingServiceImpl.setOrientDBService:" + paramEntityService);
	}

	@Reference(dynamic = true)
	public void setEntityService(EntityService paramEntityService) {
		m_entityService = paramEntityService;
		System.out.println("ImportingServiceImpl.setEntityService:" + paramEntityService);
	}
	@Reference(target = "(impl=default)", dynamic = true, optional=true)
	public void setDublettenCheckService(DublettenCheckService paramDublettenCheckService) {
		m_dublettenCheckService = paramDublettenCheckService;
		System.out.println("ImportingServiceImpl.setDublettenCheckService:" + paramDublettenCheckService);
	}
}
