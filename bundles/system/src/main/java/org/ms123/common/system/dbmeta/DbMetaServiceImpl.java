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
package org.ms123.common.system.dbmeta;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jooq.util.GenerationTool;
import org.jooq.util.jaxb.Configuration;
import org.jooq.util.jaxb.Generator;
import org.jooq.util.jaxb.Target;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.domainobjects.api.DomainObjectsService;
import org.ms123.common.namespace.NamespaceService;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.compile.CompileService;
import org.ms123.common.system.orientdb.OrientDBService;
import org.ms123.common.git.GitService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.moveDirectoryToDirectory;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

/** DbMetaServiceImpl implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=dbmeta" })
public class DbMetaServiceImpl extends BaseDbMetaServiceImpl implements DbMetaService {

	public DbMetaServiceImpl() {
		System.out.println("DbMetaServiceImpl construct");
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
		this.bc = bundleContext;
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(Map<String, Object> props) {
	}

	protected void deactivate() throws Exception {
		System.out.println("DbMetaServiceImpl.deactivate");
	}


	public synchronized void deployAll(){
		List<Map> repos = gitService.getRepositories(new ArrayList(),false);
		for(Map<String,String> repo : repos){
			String namespace = repo.get("name");
			deployNamespace(namespace);
		}
	}
	private final String PATH = "path";
	public synchronized void deployNamespace(String namespace){
		List<String> types = new ArrayList();
		types.add(DATASOURCE_TYPE);
		types.add(DIRECTORY_TYPE);
		List<String> typesDatasource = new ArrayList();
		typesDatasource.add(DATASOURCE_TYPE);

		Map map= gitService.getWorkingTree(namespace, null, 100, types, null, null,null);
		List<Map> pathList = new ArrayList();
		toFlatList(map,typesDatasource,pathList);

		List<Map> resultList = new ArrayList();
		for (Map pathMap : pathList) {
			String path = (String) pathMap.get(PATH);
			String s = gitService.getContent(namespace, path);
			Map<String,String> config = (Map<String,String>)jds.deserialize( s );
			try {
				Map<String,String>dsConfig = dsNameMapping(config);
				createDatasource(namespace,dsConfig);
			} catch (Throwable e) {
				throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "DbMetaServiceImpl.createDatasource:", e);
			}
		}
	}

	/*BEGIN JSON-RPC-API*/
	@RequiresRoles("admin")
	public void createMetadata(@PName(StoreDesc.NAMESPACE) String namespace, 
														 @PName("config") @POptional Map config) throws RpcException {
		try {
			info(this,"createMetadata:" + config);

			Map<String, Object> jooqConfig = (Map) config.get("jooq");
			Map<String, String> datanucleusConfig = (Map) config.get("datanucleus");
			Map<String, String> dsConfig = (Map) config.get("datasource");
			Object  _orientDB = dsConfig.get("is_orientdb");
			Boolean isOrientDB = _orientDB != null ? ((Boolean)_orientDB) : false;
			String pack = dsConfig.get("packageName");

			if( isOrientDB ){
				if( "data".equals(pack)){
					throw new RuntimeException("OrientDB: packname cannot be \"data\"");
				}
				String name = dsConfig.get("databaseName");
				orientDBService.getFactory(pack + "." + name );
				gitService.setStoreProperty( namespace, "store", pack, "pack", pack);
				gitService.setStoreProperty( namespace, "store", pack, "namespace", namespace);
			  gitService.setStoreProperty( namespace, "store", pack, "database", "graph:orientdb");
				StoreDesc sdesc = StoreDesc.getNamespaceData(namespace, pack);
				createOrientMetadata(sdesc );
				return;
			}

			createDatasource(namespace, dsConfig);

			gitService.setStoreProperty( namespace, "store", pack, "database", "jdbc:"+(String)dsConfig.get("dataSourceName"));
			gitService.setStoreProperty( namespace, "store", pack, "pack", pack);
			gitService.setStoreProperty( namespace, "store", pack, "namespace", namespace);
			gitService.setStoreProperty( namespace, "store", pack, "repository", namespace+"_data");

			Boolean isSchemaReadonly = (Boolean) ((Map)dsConfig).get("is_schema_readonly");
			gitService.setStoreProperty( namespace, "store", pack, "schemareadonly", isSchemaReadonly == null ? "false" : String.valueOf(isSchemaReadonly) );

			Boolean isSchemaValidate = (Boolean) ((Map)dsConfig).get("is_schema_validate");
			gitService.setStoreProperty( namespace, "store", pack, "schemavalidate", isSchemaValidate == null ? "true" : String.valueOf(isSchemaValidate) );

			StoreDesc sdesc = StoreDesc.getNamespaceData(namespace, pack);
			nucleusService.close(sdesc);

			Boolean generate = (Boolean) jooqConfig.get("create_jooq_metadata");
			if (generate == true) {
				File jooqConfigFile = createJooqConfig(namespace, pack,(String) dsConfig.get("dataSourceName"), jooqConfig);
				buildJooqMetadata(namespace, pack, (String) dsConfig.get("dataSourceName"), jooqConfigFile.toString(), false);
			}

			generate = (Boolean) ((Map)datanucleusConfig).get("create_datanucleus_metadata");
			if (generate == true) {
				StoreDesc.init();
				sdesc = StoreDesc.getNamespaceData(namespace, pack);
				info(this,"createMetadata.sdesc:" + sdesc);
				buildDatanucleusMetadata(sdesc, (String) dsConfig.get("dataSourceName"), datanucleusConfig);
				domainobjectsService.createClasses(sdesc);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "DbMetaServiceImpl.createMetadata:", e);
		} finally {
		}
	}

	@RequiresRoles("admin")
	public void buildJooqMetadata(@PName(StoreDesc.NAMESPACE) String namespace, 
																@PName("pack") String pack, 
																@PName("dataSourceName") String dataSourceName, 
																@PName("configFile") String configFile, 
																@PName("toWorkspace") @POptional @PDefaultBool(false) Boolean toWorkspace) throws RpcException {
		Connection conn = null;
		try {
			File f = new File(configFile);
			if (!f.exists()) {
				if (configFile != null) {
					if (configFile.indexOf("/") > 0) {
						f = new File(new File(gitRepos, namespace), configFile);
					} else {
						f = new File(new File(gitRepos, namespace), ".etc/" + configFile);
					}
				} else {
					f = new File(configFile);
					//f = new File(gitRepos, sdesc.getNamespace() + "/.etc/jooqConfig.xml");
				}
			}
			if (!f.exists()) {
				throw new RuntimeException("DbMetaServiceImpl.readMetadata:(" + f + ") not exists.");
			}

			Configuration config = GenerationTool.load(new FileInputStream(f));
			String packageDir = config.getGenerator().getTarget().getPackageName().replace(".", "/");
			File basedir = null;
			if (toWorkspace) {
				basedir = new File(workspace, "jooq");
			} else {
				basedir = new File(gitRepos, namespace + "/.etc/jooq");
			}
			File pdir = new File(basedir, "gen/" + packageDir);
			deleteDirectory(pdir);

			File bdir = new File(basedir, "build/" + packageDir);
			if( bdir.exists()){
				deleteDirectory(bdir);
			}
			bdir.mkdirs();

			File tdir = new File("/tmp/jooq", packageDir);
			deleteDirectory(tdir);

			DataSource ds = getDataSource(dataSourceName);
			GenerationTool gt = new GenerationTool();
			synchronized (gt) {
				gt.setConnection(conn = ds.getConnection());
				gt.run(config);
				File parent = new File(basedir, "gen/" + packageDir).getParentFile();
				if( tdir.exists()){
					moveDirectoryToDirectory(tdir, parent, true);
					compileMetadata(toWorkspace, namespace);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "DbMetaServiceImpl.buildJooqMetadata:", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}

	/*END JSON-RPC-API*/
	@Reference(dynamic = true)
	public void setPermissionService(PermissionService shiroService) {
		System.out.println("DbMetaServiceImpl:" + shiroService);
		this.permissionService = shiroService;
	}

	@Reference(dynamic = true)
	public void setEntityService(EntityService paramEntityService) {
		System.out.println("DbMetaServiceImpl.setEntityService:" + paramEntityService);
		this.entityService = paramEntityService;
	}

	@Reference(dynamic = true)
	public void setGitService(GitService paramService) {
		System.out.println("DbMetaServiceImpl.setGitService:" + paramService);
		this.gitService = paramService;
	}
	@Reference(dynamic = true)
	public void setOrientDBService(OrientDBService paramService) {
		System.out.println("DbMetaServiceImpl.setOrientDBService:" + paramService);
		this.orientDBService = paramService;
	}
	@Reference(dynamic = true)
	public void setNucleusService(NucleusService paramService) {
		System.out.println("DbMetaServiceImpl.setNucleusService:" + paramService);
		this.nucleusService = paramService;
	}

	@Reference(dynamic = true)
	public void setDomainObjectsService(DomainObjectsService paramService) {
		System.out.println("DbMetaServiceImpl.setDomainObjectsService:" + paramService);
		this.domainobjectsService = paramService;
	}

	@Reference(dynamic = true, optional = true)
	public void setCompileService(CompileService paramService) {
		this.compileService = paramService;
		System.out.println("DbMetaServiceImpl.setCompileService:" + paramService);
	}

	@Reference(dynamic = true, optional = true)
	public void setNamespaceService(NamespaceService paramService) {
		this.namespaceService = paramService;
		System.out.println("DbMetaServiceImpl.setNamespaceService:" + paramService);
	}
}

