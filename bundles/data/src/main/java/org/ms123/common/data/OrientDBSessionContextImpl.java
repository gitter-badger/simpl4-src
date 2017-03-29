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
package org.ms123.common.data;

import flexjson.JSONSerializer;
import flexjson.JSONDeserializer;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Date;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.setting.api.SettingService;
import org.ms123.common.data.query.QueryBuilder;
import org.ms123.common.data.query.SelectBuilder;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.data.api.DataLayer;
import org.apache.commons.lang.StringUtils;
import org.ms123.common.data.api.LuceneSession;
import org.ms123.common.git.GitService;
import org.ms123.common.system.orientdb.OrientDBService;
import org.ms123.common.team.api.TeamService;
import javax.jdo.PersistenceManager;
import javax.transaction.UserTransaction;
import org.ms123.common.nucleus.api.NucleusService;
import java.lang.UnsupportedOperationException;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings("unchecked")
public class OrientDBSessionContextImpl extends BaseOrientDBSessionContextImpl implements org.ms123.common.data.api.SessionContext{

	protected Inflector inflector = Inflector.getInstance();

	private JSONSerializer js = new JSONSerializer();
	private JSONDeserializer ds = new JSONDeserializer();
	private StoreDesc sdesc;
	private DataLayer dataLayer;
	private GitService gitService;
	private EntityService entityService;
	private OrientDBService orientdbService;
	private SettingService settingService;
	private PermissionService permissionService;
	public OrientDBSessionContextImpl( DataLayer data, StoreDesc sd, OrientDBService os){
		this.sdesc = sd;
		this.dataLayer = data;
		this.orientdbService = os;
	}

	public void setUserProperties(Map data){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.setUserProperties");
	}

	public Map getUserProperties(){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getUserProperties");
	}

	public void setDataLayer(DataLayer data){
		this.dataLayer = data;
	}

	public DataLayer getDataLayer(){
		return this.dataLayer;
	}

	public StoreDesc getStoreDesc(){
		return this.sdesc;
	}

	public void setStoreDesc(StoreDesc data){
		this.sdesc = data;
	}

	public Map executeNamedFilter(String name){
		return executeNamedFilter(name,new HashMap(),null);
	}

	public Map executeNamedFilter(String name, Map<String, Object> fparams){
		return executeNamedFilter(name,new HashMap(),null);
	}

	public Map executeNamedFilter(String name, Map<String, Object> fparams,Map<String, Object> options){
		name = getName(name);
		String filterJson = this.gitService.searchContent( this.sdesc.getNamespace(), name, "sw.filter" );
		Map contentMap = (Map) this.ds.deserialize(filterJson);
		return executeFilter(contentMap,fparams,options);
	}

	public Map executeFilter(Map filterDesc, Map<String, Object> fparams){
		return executeFilter(filterDesc,fparams, new HashMap());
	}

	public Map executeFilter(Map filterDesc, Map<String, Object> fparams,Map<String, Object> options){
		List<String> missingParamList = new ArrayList();
		if( getBoolean(options, CHECK_PARAMS, false )){
			getMissingFilterParameter((Map)filterDesc.get("filter"), missingParamList,fparams);
			if( missingParamList.size() > 0){
				Map ret = new HashMap();
				ret.put("missingParamList", missingParamList);
				return ret;
			}
		}
		String entityName = (String)filterDesc.get("modulename");
		this.js.prettyPrint(true);
		debug(this,"executeFilter:"+this.js.deepSerialize(filterDesc));

		List<String> aliasList = new ArrayList();
		List<String> fieldList = new ArrayList();
		List<Map> fieldsArray = (List)filterDesc.get("fields");
		if( fieldsArray == null){
			List<Map> rfields = getReportFields(entityName);
			for (Map f : rfields) {
				fieldList.add((String) entityName+"."+f.get("name"));
				aliasList.add((String) f.get("name"));
			}
		}else{
			for (Map f : fieldsArray) {
				if ((Boolean) f.get("display")) {
					fieldList.add((String) f.get("path") + "." + (String) f.get("id"));
					aliasList.add((String) f.get("mapping"));
				}
			}
		}
		String orderby=null; //Needs a better solution
		for( String field : fieldList){
			if( StringUtils.countMatches(field,"$") ==0){
				orderby=field;
				break;
			}
		}
		debug(this,"orderby:"+orderby);
		debug(this,"fieldList:"+fieldList);
		List moduleList = new ArrayList();
		String clazzName = this.inflector.getClassName(entityName);
		moduleList.add(clazzName);
		Map<String, Object> params = options != null ? new HashMap(options) : new HashMap();
		params.put("fields", this.js.serialize(moduleList));
		Map filter = (Map)filterDesc.get("filter");
		filter = addExclusionFilter(filter,(List)filterDesc.get("exclusion"));
		debug(this,"FilterWith:"+this.js.deepSerialize(filter));
		debug(this,"filterParams:"+fparams);
		params.put("filter", filter);
		params.put("orderby", orderby);
		params.put("filterParams", fparams);
		if( params.get("pageSize") == null){
			params.put("pageSize", "0");
		}
		Map<String, Object> ret = this.dataLayer.query(this, params, this.sdesc, entityName);
		List<Map> rows = (List) ret.get("rows");
		List<Object> retList = new ArrayList();
		if( getBoolean(options, GET_OBJECT, false) == false && getBoolean(options, GET_ID, false) == false ){
			for (Map row : rows) {
				Object obj = row.get(StoreDesc.getSimpleEntityName(clazzName));
				//retList.add(SojoFilterInterceptor.filterFields(obj, this, fieldList, aliasList, StoreDesc.getPackName(entityName,m_sdesc.getPack())));
			}
		}else if( getBoolean(options, GET_ID, false) ){
			PersistenceManager pm = getPM();
			for (Map row : rows) {
				Object obj = row.get(StoreDesc.getSimpleEntityName(clazzName));
				//Object id = pm.getObjectId( obj);
				//retList.add(id);
			}
		}else if( getBoolean(options, GET_OBJECT, false) ){
			for (Map row : rows) {
				Object obj = row.get(StoreDesc.getSimpleEntityName(clazzName));
				retList.add(obj);
			}
		}
		boolean withMeta = getBoolean(options, "withMeta", false );
		if( withMeta){
			Map meta = new HashMap();
			meta.put("params", getFilterParameter(filterDesc));
			meta.put("fields", Utils.prepareFields(fieldList,aliasList));
			meta.put("aliases", aliasList);
			ret.put("meta",meta);
		}
		ret.put("rows", retList);
		return ret;
	}

	public Map getNamedFilter(String name){
		name = getName(name);
		String filterJson = this.gitService.searchContent( this.sdesc.getNamespace(), name, "sw.filter" );
		Map contentMap = (Map) this.ds.deserialize(filterJson);
		return contentMap;
	}

	private String getName(String s){
		int i = s.lastIndexOf("/");
		if( i == -1){
			return s;
		}
		return s.substring(i+1);
	}

	private boolean getBoolean(Map m, String key, boolean _def) {
		try {
			return (Boolean) m.get(key);
		} catch (Exception e) {
		}
		return _def;
	}

	private List<String> getFilterParameter(Map filter) {
		String label = (String) filter.get("label");
		List<String> params = new ArrayList();
		if (filter.get("connector") == null && label != null) {
			if (label.matches("^[a-zA-Z].*")) {
				params.add(label);
			}
		}
		List<Map> children = (List) filter.get("children");
		if( children != null){
			for (Map<String,Object> c : children) {
				params.addAll(getFilterParameter(c));
			}
		}
		return params;
	}

	private void getMissingFilterParameter(Map<String, Object> filter, List<String> missingParamList,Map params) {
		String label = (String) filter.get("label");
		if (filter.get("connector") == null && label != null) {
			if (label.matches("^[a-zA-Z].*")) {
				if(params.get(label) == null){
					missingParamList.add(label);
				}
			}
		}
		List<Map> children = (List) filter.get("children");
		if( children != null){
			for (Map<String,Object> c : children) {
				getMissingFilterParameter(c, missingParamList,params);
			}
		}
	}

	private Map addExclusionFilter(Map filter, List<Map> exclusion){
		if (exclusion != null && exclusion.size() > 0) {
			Map idFilter = createIdFilter(exclusion);
			Map newFilter = new HashMap();
			newFilter.put("connector", "except");
			List<Map> children = new ArrayList();
			newFilter.put("children", children);

			children.add(filter);
			children.add(idFilter);
			return newFilter;
		}else{
			return filter;
		}
	}
	private Map createIdFilter(List<Map> exclusion) {
		Map ret = new HashMap();
		ret.put("connector", "or");
		List<Map> children = new ArrayList();
		ret.put("children", children);
		if (exclusion.size() == 0) {
			return null;
		}
		for (Map ex : exclusion) {
			String id = (String)ex.get("id");
			Map cmap = new HashMap();
			cmap.put("field", "id");
			cmap.put("op", "eq");
			cmap.put("data", id);
			cmap.put("connector", null);
			children.add(cmap);
		}
		return ret;
	}

	private List<Map> getReportFields(String entityName) {
		try {
			return getSettingService().getFieldsForEntityView(getStoreDesc().getNamespace(), entityName, "report");
		} catch (Exception e) {
			return new ArrayList();
		}
	}

	public Object getObjectIdByNamedFilter(String name, Map<String, Object> fparams){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getObjectsByNamedFilter");
	}

	public List<Object> getObjectIdsByNamedFilter(String name, Map<String, Object> fparams){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getObjectIdsByNamedFilter");
	}

	public Object getObjectByNamedFilter(String name, Map<String, Object> fparams){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getObjectByNamedFilter");
	}

	public List<Object> getObjectsByNamedFilter(String name, Map<String, Object> fparams){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getObjectsByNamedFilter");
	}



	public List query(String entityName, Map filtersMap){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.query");
	}

	public List<Object> query(String entityName, String filter){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.query");
	}

	public void setEntityService(EntityService data){
		this.entityService=data;
	}

	public EntityService getEntityService(){
		return this.entityService;
	}

	public void setSettingService(SettingService data){
		this.settingService = data;
	}

	public SettingService getSettingService(){
		return this.settingService;
	}


	public void setGitService(GitService data){
		this.gitService = data;
	}

	public GitService getGitService(){
		return this.gitService;
	}

	public void setPermissionService(PermissionService data){
		this.permissionService = data;
	}

	public PermissionService getPermissionService(){
		return this.permissionService;
	}

	public void setTeamService(TeamService data){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.setTeamService");
	}

	public TeamService getTeamService(){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getTeamService");
	}



	public void setNucleusService(NucleusService data){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.setNucleusService");
	}

	public NucleusService getNucleusService(){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getNucleusService");
	}

	public UserTransaction getUserTransaction(){
		return null;
	}

	public ClassLoader getClassLoader(){
		return this.dataLayer.getClassLoader(this.sdesc);
	}

	public ClassLoader getClassLoader( StoreDesc sdesc){
		return this.dataLayer.getClassLoader(sdesc);
	}

	public Class getClass(StoreDesc sdesc, String className){
		return this.dataLayer.getClass(sdesc,className);
	}

	public Class getClass(String className){
		return this.dataLayer.getClass(this,className);
	}

	public Object createObject(String entityName){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.createObject");
	}

	public List validateObject(Object objectInsert){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.validateObject");
	}

	public List validateObject(Object objectInsert, String entityName){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.validateObject");
	}

	public void insertIntoMaster(Object objectInsert, String entityName, Class masterClazz, String fieldName, Object masterId) throws Exception{
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.insertIntoMaster");
	}

	public void makePersistent(Object objectInsert){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.makePersistent");
	}

	public void populate(Map from, Object to){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.makePersistent");
	}

	public void populate(Map from, Object to, Map hintsMap){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.populate");
	}

	public void evaluteFormulas(String entityName, Map<String, Object> mapInsert){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.populate");
	}

	public void deleteObject(String entityName, Object id) throws Exception{
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.deleteObject");
	}

	public Object getObjectById(Class clazz, Object id){
		return getObjectById(this.sdesc, clazz, id);
	}

	public Object getObjectByAttr(Class clazz, String attr, Object value){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getObjectByAttr");
	}

	public Object getObjectByFilter(Class clazz, String filter){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getObjectByFilter");
	}

	public List getObjectsByFilter(Class clazz, String filter){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getObjectsByFilter");
	}

	public Map deleteObjectById(String entityName, String id) throws Exception{
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.deleteObjectById");
	}

	public Map getObjectMapById(String entityName, String id){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getObjectMapById");
	}

	public Map insertObjectMap(Map data, String entityName) throws Exception{
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.insertObjectMap");
	}

	public Map updateObjectMap(Map data, String entityName, String id) throws Exception{
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.updateObjectMap");
	}

	public void retrieve(Object o){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.retrieve");
	}

	public void handleFinally(){
	}

	public void handleFinally(UserTransaction ut){
	}

	public void handleException(Throwable e){
		throw new RuntimeException("OrientDBSessionContext:",e);
	}

	public void handleException(UserTransaction ut, Throwable e){
		throw new RuntimeException("OrientDBSessionContext:",e);
	}



	public Map getEntitytype(String name){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getEntitytype");
	}

	public boolean hasTeamPermission(Object o){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.hasTeamPermission");
	}

	public Map getPermittedFields(String entityName){
		return getPermittedFields(entityName, "read");
	}

	private Map<String,Map> permittedFieldsMap = new HashMap<String,Map>();
	public Map getPermittedFields(String entityName,String actions){
		Map permittedFields = permittedFieldsMap.get(entityName.toLowerCase()+"/"+actions);
		if (permittedFields == null) {
			permittedFields = this.entityService.getPermittedFields(getStoreDesc(), entityName.toLowerCase(),actions);
			permittedFieldsMap.put(entityName.toLowerCase()+"/"+actions, permittedFields);
		}
		return permittedFields;
	}

	public boolean isFieldPermitted(String fieldName, String entityName){
		return isFieldPermitted(fieldName, entityName, "read");
	}

	public boolean isFieldPermitted(String fieldName, String entityName,String actions){
		Map permittedFields = permittedFieldsMap.get(entityName.toLowerCase()+"/"+actions);
		if (permittedFields == null) {
			permittedFields = this.entityService.getPermittedFields(getStoreDesc(), entityName.toLowerCase(), actions);
			permittedFieldsMap.put(entityName.toLowerCase()+"/"+actions, permittedFields);
		}
		return permittedFields.get(fieldName) == null ? false : true;
	}

	public PersistenceManager getPM(){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getPM");
	}

	public PersistenceManager getPM(StoreDesc sdesc){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getPM");
	}

	public LuceneSession getLuceneSession(){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getLuceneSession");
	}

	public void setLuceneSession(LuceneSession data){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.setLuceneSession");
	}

	public String getUserName(){
		return getThreadContext().getUserName();
	}

	private org.ms123.common.system.thread.ThreadContext getThreadContext() {
		return org.ms123.common.system.thread.ThreadContext.getThreadContext();
	}

	public boolean hasAdminRole(){
		return this.permissionService.hasRole("admin");
	}

	public String getConfigName(){
		return "global";
	}

	public void setConfigName(String data){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.setConfigName");
	}

	public List<Map> getPrimaryKeyFields(String entity){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getPrimaryKeyFields");
	}

	public List<Object> persistObjects(Object objs, Map<String,Object> persistenceSpecification){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.persistObjects");
	}

	public void setProperty(String key, Object value){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.setProperty");
	}
	public Object getProperty(String key){
		throw new UnsupportedOperationException("Not implemented:OrientDBSessionContext.getProperty");
	}
}
