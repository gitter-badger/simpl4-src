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
package org.ms123.common.data.query;

import java.util.*;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.data.api.SessionContext;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;

@SuppressWarnings("unchecked")
public class OrientDBQueryBuilder extends QueryBuilder {

	public OrientDBQueryBuilder(StoreDesc sdesc, String entityName, String configName, SessionContext sessionContext, Map filters, Map params, Map fieldSets) {
		super("orientdb", sdesc, entityName, false, configName, sessionContext, new ArrayList<String>(), filters, params, fieldSets);
	}

	public String getEntityForPath(String _path) {
		info(this, "OrientDBQueryBuilder.getEntityForPath:" + _path);
		String clazz = null;
		try {
			int dollar = _path.lastIndexOf("$");
			if (dollar != -1) {
				String[] path = _path.split("\\$");
				clazz = path[0];
				for (int i = 1; i < path.length; i++) {
					clazz = getLinkedClass(clazz, path[i]);
					info(this, "getLinkedClass(" + clazz + "," + path[i] + "):" + clazz);
				}
				int dot = clazz.lastIndexOf(".");
				String entityname = m_inflector.getEntityName(clazz.substring(dot + 1));
				info(this, "entityname:" + entityname);
				return entityname;
			} else {
				return _path;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("getEntityForPath(" + _path + "):" + e);
		}
	}

	private String getLinkedClass(String entityname, String fieldname) {
		Map<String, Map> fields = m_sessionContext.getPermittedFields(entityname);
		Map<String, String> field = fields.get(fieldname);
		if (field != null) {
			return field.get("linkedclass");
		}
		throw new RuntimeException("OrientDBQueryBuilder.getLinkedClass:field(" + fieldname + "):not found");
	}

	public Class getClass(String className) {
		throw new UnsupportedOperationException("Not implemented:OrientDBQueryBuilder.getClass");
	}

}

