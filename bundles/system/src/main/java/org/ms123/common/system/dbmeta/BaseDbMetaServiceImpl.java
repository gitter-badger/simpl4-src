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

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import nu.xom.*;
import org.jooq.util.ColumnDefinition;
import org.jooq.util.Database;
import org.jooq.util.GeneratorStrategy.Mode;
import org.jooq.util.JavaGenerator;
import org.jooq.util.JavaWriter;
import org.jooq.util.SchemaDefinition;
import org.jooq.util.TableDefinition;
import org.jooq.util.TypedElementDefinition;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.namespace.NamespaceService;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.domainobjects.api.DomainObjectsService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.git.GitService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.compile.CompileService;
import org.ms123.common.system.orientdb.OrientDBService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.*;
import schemacrawler.utility.*;
import schemacrawler.utility.MetaDataUtility.ForeignKeyCardinality;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.strip;
import static schemacrawler.utility.MetaDataUtility.findForeignKeyCardinality;

/**
 *
 */
@SuppressWarnings("unchecked")
abstract class BaseDbMetaServiceImpl implements DbMetaService {

	protected BundleContext bc;
	protected CompileService compileService;
	protected OrientDBService orientDBService;
	protected NamespaceService namespaceService;
	protected EntityService entityService;
	protected GitService gitService;
	protected DomainObjectsService domainobjectsService;
	protected NucleusService nucleusService;
	protected PermissionService permissionService;
	protected Inflector m_inflector = Inflector.getInstance();
	protected JSONSerializer js = new JSONSerializer();
	protected JSONDeserializer jds = new JSONDeserializer();
	protected static String gitRepos = System.getProperty("git.repos");
	protected static String workspace = System.getProperty("workspace");

	public final List<String> simpleDataTypeList = new ArrayList<String>() {
		{
			add("decimal");
			add("number");
			add("string");
		}
	};
	protected void createOrientMetadata(StoreDesc sdesc) throws Exception {
		File packDir = new File( gitRepos, sdesc.getNamespace() + "/data_description/"+sdesc.getPack());
		info(this, "createOrientMetadata.packDir:" + packDir);
		if( packDir.exists() ){
			return;
		}
		File entDir = new File(packDir, "entitytypes" );
		entDir.mkdirs();
		File relFile = new File( packDir, "relations");
		writeStringToFile( relFile, "[sw]\ntype = sw.relations\n--\n[]\n", "UTF-8");

	}

	protected void buildDatanucleusMetadata(StoreDesc sdesc , String dataSourceName, Map<String, String> datanucleusConfig) throws Exception {
		String namespace = sdesc.getNamespace();
		final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
		this.js.prettyPrint(true);
		options.setRoutineTypes(null);

		info(this, "InputSchema:" + datanucleusConfig.get("datanucleus_inputschema"));
		info(this, "Include:" + datanucleusConfig.get("datanucleus_includes"));
		info(this, "Exclude:" + datanucleusConfig.get("datanucleus_excludes"));
		if (!isEmpty(datanucleusConfig.get("datanucleus_inputschema"))) {
			options.setSchemaInclusionRule(new RegularExpressionInclusionRule(datanucleusConfig.get("datanucleus_inputschema")));
		}
		if (!isEmpty(datanucleusConfig.get("datanucleus_includes"))) {
			options.setTableInclusionRule(new RegularExpressionInclusionRule(datanucleusConfig.get("datanucleus_includes")));
		}
		if (!isEmpty(datanucleusConfig.get("datanucleus_excludes"))) {
			options.setTableInclusionRule(new RegularExpressionExclusionRule(datanucleusConfig.get("datanucleus_excludes")));
		}
		List<String> tableTypes = new ArrayList<String>();
		tableTypes.add("TABLE");
		options.setTableTypes(tableTypes);

		DataSource ds = getDataSource(dataSourceName);
		Connection conn = ds.getConnection();
		try {
			final Catalog catalog = SchemaCrawlerUtility.getCatalog(conn, options);
			List<Map> newRelationList = new ArrayList<Map>();
			List<Map> entityList = new ArrayList<Map>();
			for (final Schema schema : catalog.getSchemas()) {
				info(this, "++++++++++++++++++++++:" + schema);
				for (final Table table : catalog.getTables(schema)) {
					Map entityMap = buildEntity(sdesc, table);
					if (entityMap != null) {
						entityList.add(entityMap);
					}
					List<Map<String, Object>> rels = getRelations(table, table.getForeignKeys(), sdesc.getPack());
					if (rels != null && rels.size() > 0) {
						newRelationList.addAll(rels);
					}
				}
			}
			List<Map> relations = this.entityService.getRelations(sdesc);
			relations = removeExistingRelations(relations, entityList);
			relations.addAll(newRelationList);
			this.entityService.saveRelations(sdesc, relations);
			for (Map<String, Object> entityMap : entityList) {
				this.entityService.saveEntitytype(sdesc.getStoreId(), (String) entityMap.get("name"), entityMap);
			}
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

	private Map buildEntity(StoreDesc sdesc, Table table) {
		String namespace = sdesc.getNamespace();
		String entityName = entityName(table.getName());
		info(this, "Table:" + table.getName() + "/entityName:" + entityName);

		List<String> pkList = new ArrayList<String>();
		List<String> fkList = new ArrayList<String>();
		List<String> pkListAll = new ArrayList<String>();
		if (table.getPrimaryKey() != null) {
			info(this, "Table.primaryKeys:" + table.getPrimaryKey().getColumns());
			for (Column col : table.getPrimaryKey().getColumns()) {
				pkList.add(fieldName(col.getName()));
			}
		}
/*		if (table.getForeignKeys() != null) {
			info(this, "Table.foreignKeys:" + table.getForeignKeys());
			for (ForeignKey fk : table.getForeignKeys()) {
				fkList.add(fieldName(col.getName()));
			}
		}*/
		if( pkList.size() == 0){
			for (final Index index : table.getIndexes()) {
        info(this,"\tIndex:" + index.getName()+"\t"+index.getColumns()+"\tunique:"+index.isUnique());
				if (index.isUnique()){
					for (Column col : index.getColumns()) {
						pkList.add(fieldName(col.getName()));
					}
					break;
				}
      }
		}
		info(this, "pkList:" + pkList);
		Map<String, Object> entityMap = new HashMap<String, Object>();
		entityMap.put("pack", sdesc.getPack());
		entityMap.put("enabled", true);
		entityMap.put("state_fields", false);
		entityMap.put("default_fields", false);
		entityMap.put("team_security", false);
		entityMap.put("maker", "dbmetaService");
		entityMap.put("primaryKeys", pkList);
		entityMap.put("name", entityName);
		entityMap.put("tableName", tableName(table.getName()));
		entityMap.put("schemaName", schemaName(table.getSchema().getName()));
		Map<String, Object> fieldsMap = new HashMap<String, Object>();
		entityMap.put("fields", fieldsMap);
		info(this,"Columns:"+table.getColumns());
		for (Column column : table.getColumns()) {
			info(this,"\tColumn:"+column);
			String columnType = column.getColumnDataType().getTypeMappedClass().toString();
			String name = fieldName(column.getName());
			Map<String, Object> fieldMap = new HashMap<String, Object>();
			fieldMap.put("name", name);
			fieldMap.put("columnName", columnName(column.getName()));
			fieldMap.put("enabled", true);
			fieldMap.put("index", false);
			fieldMap.put("sqltype", column.getColumnDataType().getDatabaseSpecificTypeName());
			String datatype = getType(columnType);
			fieldMap.put("datatype", datatype);
			fieldMap.put("edittype", getEditType(datatype));
			if (false/*pkList.size() == 0*/) {
				if (isSimplDatatype(datatype) && !column.isNullable()) {
					fieldMap.put("primary_key", true);
					fieldMap.put("fakePrimaryKey", true);
					pkListAll.add(name);
				}
			} else {
				if (pkList.contains(name)) {
					fieldMap.put("primary_key", true);
				}
			}
			fieldsMap.put(name, fieldMap);
		}
		if( pkList.size() == 0 && pkListAll.size()> 0){
			entityMap.put("primaryKeys", pkListAll);
		}
		info(this, "Entity:" + this.js.deepSerialize(entityMap));
		if (entityName.toLowerCase().indexOf("team") < 0) {
			return entityMap;
		}
		return null;
	}

	private boolean isSimplDatatype(String dt) {
		return simpleDataTypeList.contains(dt);
	}

	private List<Map<String, Object>> getRelations(final Table table, final Collection<? extends BaseForeignKey<?>> foreignKeys, String pack) {
		List<Map<String, Object>> relations = new ArrayList<Map<String, Object>>();
		for (final BaseForeignKey<? extends ColumnReference> foreignKey : foreignKeys) {
			final ForeignKeyCardinality fkCardinality = findForeignKeyCardinality(foreignKey);
			if (fkCardinality == ForeignKeyCardinality.unknown) {
				continue;
			}
			for (final ColumnReference columnRef : foreignKey) {
				final Table referencedTable = columnRef.getForeignKeyColumn().getParent();
				final boolean isForeignKeyFiltered = referencedTable.getAttribute("schemacrawler.table.no_grep_match", false);
				if (isForeignKeyFiltered) {
					continue;
				}
				if (table.equals(columnRef.getPrimaryKeyColumn().getParent())) {
					relations.add(getRelation(foreignKey.getName(), columnRef, fkCardinality, pack));
				}
			}
		}
		return relations;
	}

	private Map<String, Object> getRelation(final String fkName, final ColumnReference columnRef, final ForeignKeyCardinality fkCardinality, String pack) {
		final boolean isForeignKey = columnRef instanceof ForeignKeyColumnReference;
		Map<String, Object> rm = new HashMap<String, Object>();

		final Column primaryKeyColumn = columnRef.getPrimaryKeyColumn();
		final Column foreignKeyColumn = columnRef.getForeignKeyColumn();

		String rightEntity = entityName(foreignKeyColumn.getParent().getName());
		String rightTable = tableName(foreignKeyColumn.getParent().getName());

		String foreignKeyField = fieldName(foreignKeyColumn.getName());
		String _foreignKeyColumn = columnName(foreignKeyColumn.getName());

		String leftEntity = entityName(primaryKeyColumn.getParent().getName());
		String leftTable = tableName(primaryKeyColumn.getParent().getName());

		String primaryKeyField = fieldName(primaryKeyColumn.getName());
		String _primaryKeyColumn = columnName(primaryKeyColumn.getName());

		info(this, "--->>> fkName:" + fkName + "\t/primaryKeyColumn(" + leftEntity + "):" + _primaryKeyColumn + "\t/foreignKeyColumn(" + rightEntity + "):" + _foreignKeyColumn + "\t/fkCardinality:" + fkCardinality);
		String rel = null;
		if (fkCardinality == ForeignKeyCardinality.zero_one) {
			rel = "one-to-one";
		} else if (fkCardinality == ForeignKeyCardinality.zero_many) {
			rel = "one-to-many";
		} else if (fkCardinality == ForeignKeyCardinality.one_one) {
			rel = "one-to-one";
		}

		rm.put("rightmodule", pack+"." + rightEntity);
		rm.put("righttable", rightTable);

		rm.put("leftmodule", pack+"." + leftEntity);
		rm.put("lefttable", leftTable);

		rm.put("primaryKeyField", primaryKeyField);
		rm.put("primaryKeyColumn", _primaryKeyColumn);

		rm.put("foreignKeyField", foreignKeyField);
		rm.put("foreignKeyColumn", _foreignKeyColumn);
		rm.put("foreignKeyName", fkName);

		rm.put("dependent", false);
		rm.put("relation", rel);

		return rm;
	}

	private String getType(String in) {
		int dot = in.lastIndexOf(".");
		String out = in;
		if (dot >= 0) {
			out = in.substring(dot + 1);
		}
		out = out.toLowerCase();
		if ("bigdecimal".equals(out)) {
			out = "decimal";
		}
		if ("double".equals(out)) {
			out = "decimal";
		}
		if ("float".equals(out)) {
			out = "decimal";
		}
		if ("short".equals(out)) {
			out = "number";
		}
		if ("long".equals(out)) {
			out = "number";
		}
		if ("biginteger".equals(out)) {
			out = "number";
		}
		if ("integer".equals(out)) {
			out = "number";
		}
		if ("clob".equals(out)) {
			out = "string";
		}
		if ("timestamp".equals(out)) {
			out = "date";
		}
		return out;
	}

	private String getEditType(String in) {
		String out = "text";
		if ("decimal".equals(in)) {
			out = "text";
		}
		if ("string".equals(in)) {
			out = "text";
		}
		if ("number".equals(in)) {
			out = "text";
		}
		if ("clob".equals(in)) {
			out = "textarea";
		}
		if ("date".equals(in)) {
			out = "date";
		}
		if ("boolean".equals(in)) {
			out = "checkbox";
		}
		return out;
	}


	private String fieldName(String in) {
		in = strip(in, "\"");
		return getJavaName(in);
	}

	private String columnName(String in) {
		if( in.indexOf(" ") >=0){
			return strip(in, "\"");
		}else{
			if( in.indexOf("-") >=0){
				return in;
			}
			return strip(in, "\"");
		}
	}

	private String tableName(String in) {
		return strip(in, "\"");
	}

	private String schemaName(String in) {
		return strip(in, "\"");
	}

	private String entityName(String in) {
		in = strip(in, "\"");
		in = cleanName(in);
		return m_inflector.getEntityName(in).replaceAll(" ","_");
		//return getJavaName(in);
	}

	private String cleanName(String in) {
		int dollar = in.indexOf("$");
		String out = in;
		if (dollar >= 0) {
			out = in.substring(dollar + 1);
		}
		return out;
	}

	private String getJavaName(String name) {
		info(this,"getJavaName:"+name + " -> " );
		StringBuilder sb = new StringBuilder();
		if(!Character.isJavaIdentifierStart(name.charAt(0))) {
			sb.append("_");
		}
		for (char c : name.toCharArray()) {
			if(!Character.isJavaIdentifierPart(c)) {
				sb.append("_");
			} else {
				sb.append(c);
			}
		}
		name = sb.toString();
		info(this,  "Name1:"+name );
		if( checkAllUpper(name)){
			name = name.toLowerCase();
		}
		name = m_inflector.lowerCamelCase(name, ' ', '_', '-').replaceAll("_", "");
		info(this,  "Name2:"+name );
		return name;
	}

	private boolean checkAllUpper(String value){
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (Character.isLetter(c) && !Character.isUpperCase(c)) {
				return false;
			}
		}
		return true;
	}

	private List<Map> removeExistingRelations(List<Map> relations, List<Map> entityList) {
		List<Map> newRelations = new ArrayList();
		for (Map relation : relations) {
			if (!relationContainsEntity(relation, entityList)) {
				newRelations.add(relation);
			}
		}
		return newRelations;
	}

	private boolean relationContainsEntity(Map<String, String> r, List<Map> etList) {
		boolean leftFound = false;
		boolean rightFound = false;
		for (Map<String, String> et : etList) {
			String name = et.get("name");
			String pack = et.get("pack");
			String rightmodule = r.get("rightmodule");
			String leftmodule = r.get("leftmodule");
			if ((pack+"." + name).equals(rightmodule)) {
				rightFound = true;
			}
			if ((pack+"." + name).equals(leftmodule)) {
				leftFound = true;
			}
		}
		return leftFound && rightFound;
	}

	/*--End build datanucleus meta ---------------------------------------------------------------------------------------------*/

	/*-- create/remove datasource ----------------------------------------------------------------------------------------------*/
	protected void removeDatasource(String namespace, Map<String, String> config) throws Exception {
		String dataSourceName = (String) config.get("dataSourceName");
		deleteQuietly(new File(gitRepos, ".bundles/org.ops4j.datasource-" + dataSourceName + "-ds.cfg"));
		deleteQuietly(new File(gitRepos, ".bundles/org.ops4j.datasource-" + dataSourceName + "-xa.cfg"));
		deleteQuietly(new File(gitRepos, ".bundles/org.ops4j.datasource-" + dataSourceName + "-cp.cfg"));
	}

	public void createDatasource(String namespace, Map<String, String> config) throws Exception {
		if( isEmpty( config.get("url") ) ){
			return;
		}
		String url = config.get("url").replace("%ns", namespace);
		JDBCUrl jdbcUrl = JDBCUrl.parse(url);
		info(this, "createDatasource.Host:" + jdbcUrl.getHostname() + "/database:" + jdbcUrl.getDatabase());

		String dataSourceName = (String) config.get("dataSourceName");
		String dsBaseText = "osgi.jdbc.driver.name=" + config.get("osgi.jdbc.driver.name") + "\n";
		dsBaseText += "osgi.jdbc.driver.class=" + config.get("osgi.jdbc.driver.class") + "\n";
		dsBaseText += "url=" + url + "\n";
		dsBaseText += "user=" + config.get("user") + "\n";
		dsBaseText += "password=" + config.get("password") + "\n";
		dsBaseText += "dataSourceName=" + config.get("dataSourceName") + "\n";
		dsBaseText += "databaseName=" + config.get("databaseName") + "\n";
		if (jdbcUrl != null && jdbcUrl.getHostname() != null) {
			dsBaseText += "serverName=" + jdbcUrl.getHostname() + "\n";
		}

		String dataSourceText = dsBaseText + "dataSourceType=DataSource" + "\n";
		writeStringToFile(new File(gitRepos, ".bundles/org.ops4j.datasource-" + dataSourceName + "-ds.cfg"), dataSourceText, "UTF-8");

		String xaDataSourceText = dsBaseText + "dataSourceType=XADataSource" + "\n";
		writeStringToFile(new File(gitRepos, ".bundles/org.ops4j.datasource-" + dataSourceName + "-xa.cfg"), xaDataSourceText, "UTF-8");

		String cpDataSourceText = dsBaseText + "dataSourceType=ConnectionPoolDataSource" + "\n";
		writeStringToFile(new File(gitRepos, ".bundles/org.ops4j.datasource-" + dataSourceName + "-cp.cfg"), cpDataSourceText, "UTF-8");
	}

	/*-- jooq metadata ------ ----------------------------------------------------------------------------------------------*/
	protected File createJooqConfig(String namespace, String pack, String dataSourceName, Map<String, Object> config) throws Exception {
		String ns = "http://www.jooq.org/xsd/jooq-codegen-3.6.0.xsd";
		Element root = new Element("configuration", ns);
		Element generator = new Element("generator", ns);
		root.appendChild(generator);

		Element database = new Element("database", ns);
		generator.appendChild(database);

		Element databaseName = new Element("name", ns);
		database.appendChild(databaseName);
		databaseName.appendChild("org.jooq.util.jdbc.JDBCDatabase");

		Element includes = new Element("includes", ns);
		database.appendChild(includes);
		includes.appendChild((String) config.get("jooq_includes"));

		Element excludes = new Element("excludes", ns);
		database.appendChild(excludes);
		excludes.appendChild((String) config.get("jooq_excludes"));

		Element inputSchema = new Element("inputSchema", ns);
		database.appendChild(inputSchema);
		inputSchema.appendChild((String) config.get("jooq_inputschema"));

		Element generate = new Element("generate", ns);
		generator.appendChild(generate);

		Element pojos = new Element("pojos", ns);
		generate.appendChild(pojos);
		pojos.appendChild("true");

		Element relations = new Element("relations", ns);
		generate.appendChild(relations);
		relations.appendChild("true");

		Element target = new Element("target", ns);
		generator.appendChild(target);
		Element packageName = new Element("packageName", ns);
		target.appendChild(packageName);
		packageName.appendChild(pack);

		Element directory = new Element("directory", ns);
		target.appendChild(directory);
		directory.appendChild("/tmp/jooq");

		Document doc = new Document(root);
		File out = new File(new File(gitRepos, namespace), ".etc/jooqConfig-" + dataSourceName + ".xml");
		if (!out.getParentFile().exists()) {
			out.getParentFile().mkdirs();
		}
		info(this, "createJooqConfig:" + out);
		Serializer serializer = new Serializer(new FileOutputStream(out), "UTF-8");
		serializer.setIndent(2);
		serializer.setMaxLength(4096);
		serializer.write(doc);
		return out;
	}

	private static class JDBCUrl {
		private final String username;
		private final String password;
		private final String hostname;
		private final String database;
		private final int port;

		private JDBCUrl(String username, String password, String hostname, int port, String database) {
			this.username = username;
			this.password = password;
			this.hostname = hostname;
			this.database = database;
			this.port = port;
		}

		public static JDBCUrl parse(final String url) {
			final String username;
			final String password;
			final String hostname;
			final String database;
			final int port;

			final Pattern p = Pattern.compile("^jdbc:(h2:tcp|postgresql|jtds:sqlserver|as400|mariadb|mysql:thin)://((\\w+)(:(\\w*))?@)?([^/:]+)(:(\\d+))?(/(\\w+))?");
			final Matcher m = p.matcher(url);
			if (m.find()) {
				username = (m.group(3) == null ? "" : m.group(3));
				password = (m.group(5) == null ? "" : m.group(5));
				hostname = (m.group(6) == null ? "" : m.group(6));
				if (m.group(8) != null) {
					port = Integer.parseInt(m.group(8));
				} else {
					port = 0;
				}
				database = m.group(10);
				return new JDBCUrl(username, password, hostname, port, database);
			} else {
				return null;
			}
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		public String getHostname() {
			return hostname;
		}

		public int getPort() {
			return port;
		}

		public String getDatabase() {
			return database;
		}
	}

	private Object getService(Class clazz, String vendor) throws Exception {
		Collection<ServiceReference> sr = this.bc.getServiceReferences(clazz, "(dataSourceName=" + vendor + ")");
		if (sr.size() > 0) {
			Object o = this.bc.getService((ServiceReference) sr.toArray()[0]);
			return o;
		}
		return null;
	}

	protected DataSource getDataSource(String dataSourceName) throws Exception {
		DataSource ds = null;
		int count = 10;
		while (ds == null && count > 0) {
			Thread.sleep(1000);
			ds = (DataSource) getService(javax.sql.DataSource.class, dataSourceName);
			info(this, "ds:" + ds);
			count--;
		}
		return ds;
	}
	protected final Map<String, String> dsDriverClass = Collections.unmodifiableMap(new HashMap<String, String>() {{
		put("oracle", "oracle.jdbc.OracleDriver");
		put("as400", "com.ibm.as400.access.AS400JDBCDriver");
		put("jtds", "net.sourceforge.jtds.jdbc.Driver");
		put("mariadb", "org.mariadb.jdbc.Driver");
		put("mysql", "com.mysql.jdbc.Driver");
		put("postgresql", "org.postgresql.Driver");
		put("h2", "org.h2.Driver");
	}});

	protected final Map<String, String> dsMapping = Collections.unmodifiableMap(new HashMap<String, String>() {{
		put("osgi.jdbc.driver.name", "name");
		put("url", "url");
		put("user", "username");
		put("password", "password");
		put("dataSourceName", "datasourcename");
		put("databaseName", "databasename");
		put("packageName", "packagename");
	}});
	protected Map<String,String> dsNameMapping(Map<String,String> in){
		Iterator entries = dsMapping.entrySet().iterator();
		Map<String,String> out = new HashMap<String,String>();
		while (entries.hasNext()) {
			Map.Entry<String,String> entry = (Map.Entry) entries.next();
			String key = entry.getKey();
			String value = entry.getValue();
			out.put(key, in.get(value));
		}
		out.put("osgi.jdbc.driver.class", dsDriverClass.get(in.get("name")));
		return out;
	}

	protected void compileMetadata(Boolean toWorkspace, String namespace) throws Exception {
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
	protected void toFlatList(Map<String,Object> fileMap,List<String> types,List<Map> result){
		String type = (String)fileMap.get("type");
		if( types.indexOf(type) != -1){
			result.add(fileMap);
		}
		List<Map> childList = (List)fileMap.get("children");
		for( Map child : childList){
			toFlatList(child,types,result);
		}
	}
}

