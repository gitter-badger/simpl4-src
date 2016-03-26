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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
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
import org.ms123.common.system.compile.CompileService;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import nu.xom.*;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import schemacrawler.schema.*;
import schemacrawler.schemacrawler.*;
import schemacrawler.utility.*;
/**
 *
 */
@SuppressWarnings("unchecked")
abstract class BaseDbMetaServiceImpl implements DbMetaService {

	protected Inflector m_inflector = Inflector.getInstance();
	protected JSONSerializer m_js = new JSONSerializer();
	private static EntityService entityService;
	private static String namespace;
	protected CompileService m_compileService;
	protected PermissionService m_permissionService;
	protected EntityService m_entityService;
	protected static String workspace = System.getProperty("workspace");
	protected static String gitRepos = System.getProperty("git.repos");

	protected void generateRelations(SchemaDefinition schema) {
		System.out.println("generateRelations:" + schema.getName() + "/" + schema.getQualifiedName() + "/" + schema.getDatabase());
	}

	protected void generatePojos(SchemaDefinition schema) {
		Database database = schema.getDatabase();
		List<TableDefinition> tableDefinitions = database.getTables(schema);
		System.out.println("generatePojos:" + schema);
		System.out.println("generatePojos:" + database);
		System.out.println("generatePojos:" + tableDefinitions);
	}

	protected void generatePojo(TableDefinition table, JavaWriter out) {
		String className = null;//getStrategy().getJavaClassName(table, Mode.POJO);

		System.out.println("Table:" + table);
		List<String> pkList = new ArrayList<String>();
		if( table.getPrimaryKey() != null ){
			System.out.println("Table.primary:" + table.getPrimaryKey().getKeyColumns());
			System.out.println("Table.foreign:" + table.getPrimaryKey().getForeignKeys());
			for (ColumnDefinition cd : table.getPrimaryKey().getKeyColumns()) {
				pkList.add(getJavaName(cd));
			}
		}
		className = getClassName(className);
		String entityName = m_inflector.getEntityName(className);
		Map<String, Object> entityMap = new HashMap<String, Object>();
		entityMap.put("pack", "data");
		entityMap.put("enabled", true);
		entityMap.put("primaryKeys", pkList);
		entityMap.put("name", entityName);
		entityMap.put("tableName", table.getName());
		entityMap.put("schemaName", table.getSchema().getName());
		Map<String, Object> fieldsMap = new HashMap<String, Object>();
		entityMap.put("fields", fieldsMap);
		for (TypedElementDefinition<?> column : table.getColumns()) {
			if (column instanceof ColumnDefinition) {
				String columnType = null;//getJavaType(column.getType(), Mode.POJO);
				String name = getJavaName((ColumnDefinition)column);
				Map<String, Object> fieldMap = new HashMap<String, Object>();
				fieldMap.put("name", name);
				fieldMap.put("columnName", column.getName());
				fieldMap.put("enabled", true);
				fieldMap.put("index", false);
				fieldMap.put("sqltype", column.getType().getType());
				String datatype = getType(columnType);
				fieldMap.put("datatype", datatype);
				fieldMap.put("edittype", getEditType(datatype));
				if (pkList.contains(name)) {
					fieldMap.put("primary_key", true);
				}
				fieldsMap.put(name, fieldMap);
			}
		}
		m_js.prettyPrint(true);
		System.out.println("Entity:" + m_js.deepSerialize(entityMap));
		if (entityName.toLowerCase().indexOf("team") < 0) {
			entityService.saveEntitytype(namespace+"_data", entityName, entityMap);
		}
	}

	private String getJavaName(ColumnDefinition column){
		String columnMember = null;//getStrategy().getJavaMemberName(column, Mode.POJO);
		return m_inflector.lowerCamelCase(columnMember, ' ', '_', '-').replaceAll("_", "");
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

	private String getClassName(String in) {
		int dollar = in.indexOf("$");
		String out = in;
		if (dollar >= 0) {
			out = in.substring(dollar + 1);
		}
		return firstToLower(out);
	}

	private String firstToLower(String s) {
		String fc = s.substring(0, 1);
		return fc.toLowerCase() + s.substring(1);
	}
	protected void removeDatasource( String namespace, Map<String,String> config) throws Exception{
		String dataSourceName = (String) config.get("dataSourceName");
		deleteQuietly( new File( gitRepos, ".bundles/org.ops4j.datasource-"+dataSourceName+"-ds.cfg"));
		deleteQuietly( new File( gitRepos, ".bundles/org.ops4j.datasource-"+dataSourceName+"-xa.cfg"));
		deleteQuietly( new File( gitRepos, ".bundles/org.ops4j.datasource-"+dataSourceName+"-cp.cfg"));
	}

	protected void createDatasource( String namespace, Map<String,String> config) throws Exception{
		JDBCUrl jdbcUrl = JDBCUrl.parse( config.get("url") );
		System.out.println("createDatasource.Host:"+jdbcUrl.getHostname()+"/database:"+jdbcUrl.getDatabase());

		String dataSourceName = (String) config.get("dataSourceName");
		String dsBaseText =  "osgi.jdbc.driver.name=" + config.get("osgi.jdbc.driver.name") + "\n";
		dsBaseText +=  "osgi.jdbc.driver.class=" + config.get("osgi.jdbc.driver.class") + "\n";
		dsBaseText +=  "url=" + config.get("url") + "\n";
		dsBaseText +=  "user=" + config.get("user") + "\n";
		dsBaseText +=  "password=" + config.get("password") + "\n";
		dsBaseText +=  "dataSourceName=" + config.get("dataSourceName") + "\n";
		dsBaseText +=  "databaseName=" + config.get("databaseName") + "\n";
		if( jdbcUrl != null && jdbcUrl.getHostname() != null){
			dsBaseText +=  "serverName=" + jdbcUrl.getHostname() + "\n";
		}
	
		String dataSourceText = dsBaseText + "dataSourceType=DataSource" + "\n";
		writeStringToFile( new File( gitRepos, ".bundles/org.ops4j.datasource-"+dataSourceName+"-ds.cfg"),dataSourceText, "UTF-8");

		String xaDataSourceText = dsBaseText + "dataSourceType=XADataSource" + "\n";
		writeStringToFile( new File( gitRepos, ".bundles/org.ops4j.datasource-"+dataSourceName+"-xa.cfg"),xaDataSourceText, "UTF-8");

		String cpDataSourceText = dsBaseText + "dataSourceType=ConnectionPoolDataSource" + "\n";
		writeStringToFile( new File( gitRepos, ".bundles/org.ops4j.datasource-"+dataSourceName+"-cp.cfg"),cpDataSourceText, "UTF-8");
	}

	protected File createJooqConfig( String namespace, String dataSourceName,  Map<String,Object> config) throws Exception{
		String ns = "http://www.jooq.org/xsd/jooq-codegen-3.6.0.xsd";
		Element root = new Element("configuration", ns);
		Element generator = new Element("generator",ns);
		root.appendChild( generator);

		Element database = new Element("database",ns);
		generator.appendChild( database );

		Element databaseName = new Element("name",ns);
		database.appendChild(databaseName);
		databaseName.appendChild("org.jooq.util.jdbc.JDBCDatabase");

		Element includes = new Element("includes",ns);
		database.appendChild(includes);
		includes.appendChild((String)config.get("jooq_includes"));

		Element excludes = new Element("excludes",ns);
		database.appendChild(excludes);
		excludes.appendChild((String)config.get("jooq_excludes"));


		Element inputSchema = new Element("inputSchema",ns);
		database.appendChild(inputSchema);
		inputSchema.appendChild( (String)config.get("jooq_inputschema"));

		Element generate = new Element("generate",ns);
		generator.appendChild(generate);

		Element pojos = new Element("pojos",ns);
		generate.appendChild( pojos );
		pojos.appendChild( "true");

		Element relations = new Element("relations",ns);
		generate.appendChild( relations );
		relations.appendChild( "true");

		Element target = new Element("target",ns);
		generator.appendChild(target);
		Element packageName = new Element("packageName",ns);
		target.appendChild(packageName);
		String _packageName = (String)config.get("jooq_packagename");
		if( _packageName == null || _packageName.length() == 0){
			_packageName="jooq."+namespace;
		}
		packageName.appendChild( _packageName);

		Element directory = new Element("directory",ns);
		target.appendChild(directory);
		directory.appendChild( "/tmp/jooq" );


		Document doc = new Document(root);
		File out = new File(new File(gitRepos, namespace), ".etc/jooqConfig-" + dataSourceName+ ".xml" );
		System.out.println("createJooqConfig:"+out);
		Serializer serializer = new Serializer(new FileOutputStream(out), "UTF-8");
		serializer.setIndent(2);
		serializer.setMaxLength(4096);
		serializer.write(doc);  
		return out;
	}




	private static  class JDBCUrl {
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
				}else{
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

}

