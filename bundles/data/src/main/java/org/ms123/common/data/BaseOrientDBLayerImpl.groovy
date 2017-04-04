/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
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

import flexjson.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.Collections;
import org.ms123.common.libhelper.Inflector;
import java.lang.reflect.Method;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.Inflector;
import org.ms123.common.data.api.SessionContext;


import groovy.transform.CompileStatic;

import java.io.File;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;

/**
 *
 */
//@groovy.transform.CompileStatic
abstract class BaseOrientDBLayerImpl implements org.ms123.common.data.api.DataLayer{

	private Inflector inflector = Inflector.getInstance();
	public List<Map> executeQuery(Class clazz,String className, String where){
		info(this,"executeQuery.sql:"+"select from "+className + " where "+ where);
		List list = clazz.graphQueryMap("select from "+className + " where "+ where,false);
		info(this,"List:"+list);
		return list;
	}
	public Map executeInsertObject(SessionContext sc, String entityName,Map data){
		def obj = _executeInsertObject(sc, entityName, data);
		return [ id : obj.getIdentity() ];
	}
	public Object _executeInsertObject(SessionContext sc, String entityName,Map data){
		Map fields = sc.getPermittedFields(entityName, "write");
		def cleanData = [:];
		fields.each{ k, v ->
			if( isSimple( v.datatype ) && data[k] != null){
				info(this,"Simple("+entityName+":"+k+"):"+data[k]);
				cleanData[k] = data[k];
			}else if( isLinkObj(v.datatype) && data[k] != null){
				cleanData[k] = _executeInsertObject(sc, v.linkedclass, data[k] );
			}else if( (isLinkSet(v.datatype) || isLinkList( v.datatype )) && data[k] != null){
				info(this,"Multi("+entityName+":"+k+"):"+data[k]);
				def cleanList = [];
				for( Map map : data[k] ){
					info(this,"map:"+map);
					def ret = _executeInsertObject(sc, v.linkedclass, map );
					cleanList.add(ret);
				}
				if( isLinkSet( v.datatype )){
					cleanData[k] = cleanList as Set;
				}else{
					cleanData[k] = cleanList;
				}
			}
		}
		Class clazz = getClass(sc, entityName);
		return clazz.newInstance( cleanData );
	}

	public Map executeUpdateObject(SessionContext sc, String entityName, String id, Map data){
		Class clazz = getClass(sc, entityName);
		def obj = clazz.graphQuery("select from "+id,true);
		if( obj == null){
			throw new RuntimeException("executeUpdateObject("+id+"):not found");
		}
		_executeUpdateObject( sc, obj, data);
		return [ id : id ];
	}

	public Map _executeUpdateObject(SessionContext sc, Object obj, Map data){
		if( obj == null){
			info(this,"_executeUpdateObject is null:"+data);
			return;
		}
		def entityName = obj.getClass().getSimpleName();
		entityName = this.inflector.lowerFirst(entityName);
		info(this,"_executeUpdateObject("+entityName+"):"+data);
		Map fields = sc.getPermittedFields(entityName, "write");
		fields.each{ k, v ->
			if( isSimple( v.datatype) && data[k] != null){
				info(this,"Simple("+entityName+":"+k+"):"+data[k]);
				obj[k] = data[k];
			}else if( isLinkObj(v.datatype) && data[k] != null){
				_executeInsertObject(sc, obj[k], data[k] );
			}else if( (isLinkSet(v.datatype) || isLinkList( v.datatype )) && data[k] != null){
				info(this,"Multi("+entityName+":"+k+"):"+data[k]);
				def objList = [];
				Class clazz = getClass(sc, v.linkedclass);
				for( Map childData : data[k] ){
					def child = null;
					if( childData._id && childData._id.startsWith("#") ){
						child = _getObject(clazz, childData._id);
					}
					if( child == null){
						child = clazz.newInstance(  );
					}
					_executeUpdateObject(sc, child, childData );
					objList.add( child );
				}
				info(this,"objList:"+objList);
				obj[k] = objList;
			}
		}
	}

	public Map executeDeleteObject(Class clazz,String id){
		def obj = clazz.graphQuery("select from "+id,true);
		if( obj == null){
			throw new RuntimeException("executeDeleteObject("+id+"):not found");
		}
		obj.remove();
		return [ id : id ];
	}

	public Map executeGet(Class clazz,String id){
		info(this,"executeGet:"+id);
		Map row = clazz.graphQueryMap("select from "+id,true);
		if( row != null){
			row.id = row._id;
			row.remove("_id");
		}
		return row;
	}
	public Object _getObject(Class clazz,String id){
		def obj = clazz.graphQuery("select from "+id,true);
		info(this,"_getObject("+id+"):"+obj);
		return obj;
	}
	def isLinkObj( def dt ){
		def list = ["9", "13"]
		return list.contains( dt );
	}
	def isLinkList( def dt ){
		def list = ["10", "14"]
		return list.contains( dt );
	}
	def isLinkSet( def dt ){
		def list = ["11", "15"]
		return list.contains( dt );
	}

	def isSimple( def dt ){
		def simpleList = ["1", "2", "3", "4", "5", "6", "7", "17", "19", "21"]
		return simpleList.contains( dt );
	}
}


