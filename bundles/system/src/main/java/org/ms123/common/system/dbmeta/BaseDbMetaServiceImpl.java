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

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
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
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.system.compile.CompileService;
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
	protected EntityService entityService;
	protected PermissionService permissionService;
	protected Inflector m_inflector = Inflector.getInstance();
	protected JSONSerializer js = new JSONSerializer();
	protected static String gitRepos = System.getProperty("git.repos");
	protected static String workspace = System.getProperty("workspace");

	protected void buildDatanucleusMetadata(StoreDesc sdesc, String dataSourceName, Map<String, String> datanucleusConfig) throws Exception {
		String namespace = sdesc.getNamespace();
		final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
		this.js.prettyPrint(true);
		options.setRoutineTypes(null);

		System.out.println("InputSchema:" + datanucleusConfig.get("datanucleus_inputschema"));
		System.out.println("Include:" + datanucleusConfig.get("datanucleus_includes"));
		System.out.println("Exclude:" + datanucleusConfig.get("datanucleus_excludes"));
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
				System.out.println("++++++++++++++++++++++:" + schema);
				for (final Table table : catalog.getTables(schema)) {
					Map entityMap = buildEntity(namespace, table);
					if (entityMap != null) {
						entityList.add(entityMap);
					}
					List<Map<String, Object>> rels = getRelations(table, table.getForeignKeys());
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
				this.entityService.saveEntitytype(namespace + "_data", (String) entityMap.get("name"), entityMap);
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

	private Map buildEntity(String namespace, Table table) {
		String entityName = entityName(table.getName());
		System.out.println("Table:" + table.getName() + "/entityName:" + entityName);

		List<String> pkList = new ArrayList<String>();
		if (table.getPrimaryKey() != null) {
			System.out.println("Table.primary:" + table.getPrimaryKey().getColumns());
			System.out.println("Table.foreign:" + table.getForeignKeys());
			for (Column col : table.getPrimaryKey().getColumns()) {
				pkList.add(fieldName(col.getName()));
			}
		}
		System.out.println("pkList:" + pkList);
		Map<String, Object> entityMap = new HashMap<String, Object>();
		entityMap.put("pack", "data");
		entityMap.put("enabled", true);
		entityMap.put("primaryKeys", pkList);
		entityMap.put("name", entityName);
		entityMap.put("tableName", tableName(table.getName()));
		entityMap.put("schemaName", schemaName(table.getSchema().getName()));
		Map<String, Object> fieldsMap = new HashMap<String, Object>();
		entityMap.put("fields", fieldsMap);
		for (Column column : table.getColumns()) {
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
			if (pkList.contains(name)) {
				fieldMap.put("primary_key", true);
			}
			fieldsMap.put(name, fieldMap);
		}
		System.out.println("Entity:" + this.js.deepSerialize(entityMap));
		if (entityName.toLowerCase().indexOf("team") < 0) {
			return entityMap;
		}
		return null;
	}

	private List<Map<String, Object>> getRelations(final Table table, final Collection<? extends BaseForeignKey<?>> foreignKeys) {
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
					relations.add(getRelation(foreignKey.getName(), columnRef, fkCardinality));
				}
			}
		}
		return relations;
	}

	private Map<String, Object> getRelation(final String fkName, final ColumnReference columnRef, final ForeignKeyCardinality fkCardinality) {
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

		System.out.println("--->>> fkName:" + fkName + "\t/primaryKeyColumn(" + leftEntity + "):" + _primaryKeyColumn + "\t/foreignKeyColumn(" + rightEntity + "):" + _foreignKeyColumn + "\t/fkCardinality:" + fkCardinality);
		String rel = null;
		if (fkCardinality == ForeignKeyCardinality.zero_one) {
			rel = "one-to-one";
		} else if (fkCardinality == ForeignKeyCardinality.zero_many) {
			rel = "one-to-many";
		} else if (fkCardinality == ForeignKeyCardinality.one_one) {
			rel = "one-to-one";
		}

		rm.put("rightmodule", "data." + rightEntity);
		rm.put("righttable", rightTable);

		rm.put("leftmodule", "data." + leftEntity);
		rm.put("lefttable", leftTable);

		rm.put("primaryKeyField", primaryKeyField);
		rm.put("primaryKeyColumn", _primaryKeyColumn);

		rm.put("foreignKeyField", foreignKeyField);
		rm.put("foreignKeyColumn", _foreignKeyColumn);

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
		if ("date".equals(in)) {
			out = "date";
		}
		return out;
	}

	private String cleanName(String in) {
		in = strip(in, "\"");
		int dollar = in.indexOf("$");
		String out = in;
		if (dollar >= 0) {
			out = in.substring(dollar + 1);
		}
		return getJavaName(out);
	}
	private String fieldName(String in) {
		return getJavaName(in);
	}
	private String columnName(String in) {
		return strip(in, "\"");
	}
	private String tableName(String in) {
		return strip(in, "\"");
	}
	private String schemaName(String in) {
		return strip(in, "\"");
	}
	private String entityName(String in) {
		in = cleanName(in);
		return m_inflector.getEntityName(in);
	}

	private String getJavaName(String name) {
		String columnMember = strip(name, "\"");
		return m_inflector.lowerCamelCase(columnMember, ' ', '_', '-').replaceAll("_", "");
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
			String rightmodule = r.get("rightmodule");
			String leftmodule = r.get("leftmodule");
System.out.println("relationContainsEntity:"+name+"/"+rightmodule+"/"+leftmodule);
			if (("data." + name).equals(rightmodule)) {
				rightFound = true;
			}
			if (("data." + name).equals(leftmodule)) {
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

	protected void createDatasource(String namespace, Map<String, String> config) throws Exception {
		JDBCUrl jdbcUrl = JDBCUrl.parse(config.get("url"));
		System.out.println("createDatasource.Host:" + jdbcUrl.getHostname() + "/database:" + jdbcUrl.getDatabase());

		String dataSourceName = (String) config.get("dataSourceName");
		String dsBaseText = "osgi.jdbc.driver.name=" + config.get("osgi.jdbc.driver.name") + "\n";
		dsBaseText += "osgi.jdbc.driver.class=" + config.get("osgi.jdbc.driver.class") + "\n";
		dsBaseText += "url=" + config.get("url") + "\n";
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
	protected File createJooqConfig(String namespace, String dataSourceName, Map<String, Object> config) throws Exception {
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
		String _packageName = (String) config.get("jooq_packagename");
		if (_packageName == null || _packageName.length() == 0) {
			_packageName = "jooq." + namespace;
		}
		packageName.appendChild(_packageName);

		Element directory = new Element("directory", ns);
		target.appendChild(directory);
		directory.appendChild("/tmp/jooq");

		Document doc = new Document(root);
		File out = new File(new File(gitRepos, namespace), ".etc/jooqConfig-" + dataSourceName + ".xml");
		System.out.println("createJooqConfig:" + out);
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
			System.out.println("ds:" + ds);
			count--;
		}
		return ds;
	}
}

