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
}

