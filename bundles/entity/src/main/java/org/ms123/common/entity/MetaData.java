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
package org.ms123.common.entity;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import flexjson.*;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.ParameterParser;
import org.ms123.common.data.api.DataLayer;
import org.apache.commons.beanutils.PropertyUtils;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Extent;
import javax.jdo.Query;
import javax.jdo.JDOHelper;
import javax.jdo.Transaction;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.libhelper.Bean2Map;
import org.ms123.common.store.StoreDesc;

/**
 *
 */
interface  MetaData {

	public final String ENTITY = "entity";
	public final String ENTITYTYPES_PATH = "data_description/{0}/entitytypes";
	public final String ENTITYTYPE_PATH = "data_description/{0}/entitytypes/{1}";
	public final String RELATIONS_PATH = "data_description/{0}/relations";
	public final String ENTITYTYPE_TYPE = "sw.entitytype";
	public final String RELATIONS_TYPE = "sw.relations";

	public final String FIELD = "field";

	public final String RELATION = "relation";

	public List<Map> getEntitytypes(String storeId) throws Exception;
	public List<Map> getEntitytypeInfo(String storeId,List<String> names) throws Exception;
	public List<Map> getFields(String storeId, String entityType) throws Exception;
	public List<Map> getRelations(String storeId) throws Exception;
	public void saveRelations(String storeId, List<Map> relations) throws Exception;
	public void saveEntitytype(String storeId, String name, Map<String,Object> desc) throws Exception;
	public void deleteEntitytype(String storeId, String name) throws Exception;
	public void deleteEntitytypeField(String storeId, String entitytype, String name) throws Exception;
	public void deleteEntitytypes(String storeId) throws Exception;
	public Map<String,Object>  getEntitytype(String storeId, String name) throws Exception;
	public void saveEntitytypeField(String storeId, String entitytype, String name, Map<String,Object> data) throws Exception;
	public Map<String,Object> getEntitytypeField(String storeId, String entitytype, String name) throws Exception;

}
