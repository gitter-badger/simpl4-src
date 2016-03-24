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
package org.ms123.common.system.jooq;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import org.jooq.util.SchemaDefinition;
import org.jooq.util.TableDefinition;
import org.jooq.util.JavaGenerator;
import org.jooq.util.Database;
import java.util.List;

/**
 */
public class Simpl4Generator extends JavaGenerator {
    protected void generateRelations(SchemaDefinition schema) {
			super.generateRelations(schema);
			System.out.println("generateRelations:"+schema.getName()+"/"+schema.getQualifiedName()+"/"+schema.getDatabase());
		}

    protected void generatePojos(SchemaDefinition schema) {
			super.generatePojos(schema);
			Database database = schema.getDatabase();
			List<TableDefinition> tableDefinitions = database.getTables(schema);
			System.out.println("generatePojos:"+schema);
			System.out.println("generatePojos:"+database);
			System.out.println("generatePojos:"+tableDefinitions);
		}
}
