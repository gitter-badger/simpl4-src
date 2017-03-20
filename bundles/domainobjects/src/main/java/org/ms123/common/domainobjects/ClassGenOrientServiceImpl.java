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
package org.ms123.common.domainobjects;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.system.orientdb.OrientDBService;
import static org.ms123.common.entity.api.Constants.LEFT_ENTITY;
import static org.ms123.common.entity.api.Constants.RIGHT_ENTITY;
import static org.ms123.common.entity.api.Constants.LEFT_FIELD;
import static org.ms123.common.entity.api.Constants.RIGHT_FIELD;
import static org.ms123.common.entity.api.Constants.RELATION;
import org.ms123.common.utils.annotations.RelatedTo;
import org.ms123.common.utils.annotations.Functional;
import org.ms123.common.libhelper.Inflector;
import org.osgi.framework.BundleContext;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.ms123.common.store.StoreDesc;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "kind=orient,name=classGen" })
public class ClassGenOrientServiceImpl implements org.ms123.common.domainobjects.api.ClassGenService {

	protected Inflector m_inflector = Inflector.getInstance();
	protected EntityService m_entityService;
	protected OrientDBService m_orientdbService;

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
	}

	public List<String> generate(StoreDesc sdesc, List<Map> entities, String outDir) throws Exception {
		info(this,"--->generate:" + sdesc.getString());
		List<String> classnameList = new ArrayList();
		for (int i = 0; i < entities.size(); i++) { //Make empty classes to resolve relations
			Map m = entities.get(i);
			String classname = getFQN(sdesc, m);
			info(this,"\tmakeClass:" + classname);
		}
		for (int i = 0; i < entities.size(); i++) {
			Map entMap = entities.get(i);
			String name = (String) entMap.get("name");
			String classname = getFQN(sdesc, entMap);
			List fields = getEntityMetaData(sdesc, name);
			//makeClass(sdesc, cp, fields, classname, sdesc.getNamespace(), entMap, true);
			classnameList.add(classname);
			List<String> pkNameList = (List) entMap.get("primaryKeys");
			if (pkNameList != null && pkNameList.size() > 1) {
				String classnamePK = classname + "_PK";
				classnameList.add(classnamePK);
				//new PrimaryKeyClassCreator().makePKClass(sdesc, cp, pkNameList, classnamePK, fields);
			}
		}
		List<Map> rels = getRelations(sdesc);
		for (int i = 0; rels != null && i < rels.size(); i++) {
			//addRelations(cp, rels.get(i), sdesc);
		}
		for (int i = 0; i < entities.size(); i++) {
			Map entMap = entities.get(i);
			String classname = getFQN(sdesc, entMap);
			boolean genDefFields = getBoolean(entMap.get("default_fields"), false);
			boolean genStateFields = getBoolean(entMap.get("state_fields"), false);
			boolean team_security = getBoolean(entMap.get("team_security"), false);
		}
		return classnameList;
	}

	protected List getRelations(StoreDesc sdesc) throws Exception {
		List list = m_entityService.getRelations(sdesc);
		info(this,"getRelations:" + list);
		return list;
	}

	protected List getEntityMetaData(StoreDesc sdesc, String entity) throws Exception {
		List list = m_entityService.getFields(sdesc, entity, false);
		return list;
	}

	private boolean getBoolean(Object o, boolean def) {
		if (o == null)
			return def;
		try {
			boolean b = (Boolean) o;
			return b;
		} catch (Exception e) {
		}
		return def;
	}

	private String getFQN(StoreDesc sdesc, Map entity) {
		String name = (String)entity.get("name");
		String pack = StoreDesc.getPackName(name,sdesc.getPack());
		name = StoreDesc.getSimpleEntityName(name);
		String className = m_inflector.getClassName((String) name);
		return sdesc.getJavaPackage(pack) + "." + className;
	}

	@Reference
	public void setEntityService(EntityService paramEntityService) {
		m_entityService = paramEntityService;
		info(this,"ClassGenOrientServiceImpl.setEntityService:" + paramEntityService);
	}

	@Reference
	public void setOrientDBService(OrientDBService paramEntityService) {
		m_orientdbService = paramEntityService;
		info(this,"ClassGenOrientServiceImpl.setOrientDBService:" + paramEntityService);
	}

}

