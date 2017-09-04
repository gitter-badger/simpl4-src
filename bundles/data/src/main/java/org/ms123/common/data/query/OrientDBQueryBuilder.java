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
				String entityname = m_inflector.getEntityNameCamelCase(clazz.substring(dot + 1));
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

