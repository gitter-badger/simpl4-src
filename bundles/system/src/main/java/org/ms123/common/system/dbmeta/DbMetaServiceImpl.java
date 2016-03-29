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
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.compile.CompileService;
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

	private void compileMetadata(Boolean toWorkspace, String namespace) throws Exception {
		List<File> classPath = new ArrayList<File>();
		File basedir = null;
		if (toWorkspace) {
			classPath.add(new File(workspace, "jooq/build"));
			basedir = new File(workspace, "jooq");
		} else {
			classPath.add(new File(gitRepos, namespace + "/.etc/jooq/build"));
			basedir = new File(gitRepos, namespace + "/.etc/jooq");
		}

		File destinationDirectory = new File(basedir, "build");
		File sourceDirectory = new File(basedir, "gen");
		this.compileService.compileJava(this.bc.getBundle(), destinationDirectory, sourceDirectory, classPath);
	}

	/*BEGIN JSON-RPC-API*/
	@RequiresRoles("admin")
	public void createMetadata(@PName(StoreDesc.STORE_ID) String storeId, @PName("config") @POptional Map config) throws RpcException {
		try {
			System.out.println("createMetadata:" + config);
			StoreDesc sdesc = StoreDesc.get(storeId);
			String namespace = sdesc.getNamespace();

			Map<String, Object> jooqConfig = (Map) config.get("jooq");
			Map<String, String> datanucleusConfig = (Map) config.get("datanucleus");
			Map<String, String> dsConfig = (Map) config.get("datasource");

			createDatasource(namespace, dsConfig);

			Boolean isMainDb = (Boolean) ((Map)dsConfig).get("is_main_db");
			if (isMainDb == true) {
				gitService.setStoreProperty( namespace, "store", "data", "database", "jdbc:"+(String)dsConfig.get("dataSourceName"));
				nucleusService.close(sdesc);
			}

			Boolean generate = (Boolean) jooqConfig.get("create_jooq_metadata");
			if (generate == true) {
				File jooqConfigFile = createJooqConfig(namespace, (String) dsConfig.get("dataSourceName"), jooqConfig);
				buildJooqMetadata(storeId, (String) dsConfig.get("dataSourceName"), jooqConfigFile.toString(), false);
			}

			generate = (Boolean) ((Map)datanucleusConfig).get("create_datanucleus_metadata");
			if (generate == true) {
				buildDatanucleusMetadata(sdesc, (String) dsConfig.get("dataSourceName"), datanucleusConfig);
				domainobjectsService.createClasses(sdesc);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "DbMetaServiceImpl.createMetadata:", e);
		} finally {
			/*try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ee) {
				ee.printStackTrace();
			}*/
		}
	}

	@RequiresRoles("admin")
	public void buildJooqMetadata(@PName(StoreDesc.STORE_ID) String storeId, @PName("dataSourceName") String dataSourceName, @PName("configFile") String configFile, @PName("toWorkspace") @POptional @PDefaultBool(false) Boolean toWorkspace) throws RpcException {
		Connection conn = null;
		try {
			StoreDesc sdesc = StoreDesc.get(storeId);
			String namespace = sdesc.getNamespace();
			File f = new File(configFile);
			if (!f.exists()) {
				if (configFile != null) {
					if (configFile.indexOf("/") > 0) {
						f = new File(new File(gitRepos, sdesc.getNamespace()), configFile);
					} else {
						f = new File(new File(gitRepos, sdesc.getNamespace()), ".etc/" + configFile);
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
			bdir.mkdirs();

			File tdir = new File("/tmp/jooq", packageDir);
			deleteDirectory(tdir);

			DataSource ds = getDataSource(dataSourceName);
			System.out.println("generate.call:" + ds);
			GenerationTool gt = new GenerationTool();
			synchronized (gt) {
				gt.setConnection(conn = ds.getConnection());
				gt.run(config);
				File parent = new File(basedir, "gen/" + packageDir).getParentFile();
				if( tdir.exists()){
					moveDirectoryToDirectory(tdir, parent, true);
					compileMetadata(toWorkspace, sdesc.getNamespace());
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
}

