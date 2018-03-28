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
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Collections;
import org.ms123.common.libhelper.Inflector;
import java.lang.reflect.Method;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.Inflector;
import org.ms123.common.data.api.SessionContext;

import com.orientechnologies.orient.core.metadata.schema.OType;

import groovy.transform.CompileStatic;
import groovy.transform.TypeCheckingMode

import java.io.File;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;

/**
 *
 */
//@groovy.transform.CompileStatic
abstract class BaseOrientDBLayerImpl implements org.ms123.common.data.api.DataLayer{

	protected Inflector inflector = Inflector.getInstance();
	public List<Map> executeQuery(SessionContext sc, Class clazz,String className, String where){
		debug(this,"executeQuery.sql:"+"select from "+className + " where ["+ where+"]");
		List list = null;
		if( isEmpty(where) || "null".equals(where)){
			list = clazz.graphQuery("select from "+className,false);
		}else{
			list = clazz.graphQuery("select from "+className + " where "+ where,false);
		}
		List result = [];
		list.each{ obj ->
			result.add( _objToMap( sc, obj,0 ));
		}
		debug(this,"List:"+result);
		return result;
	}
	protected Object _objToMap(SessionContext sc, Object obj, dt ){
		if( isPrimitiveOrWrapper( obj.getClass()) || obj instanceof String ){
			return obj;
		}
		def entityName = this.inflector.lowerFirst(obj.getClass().getSimpleName());
		Map fields = sc.getPermittedFields(entityName, "read");
		def cleanData = [:];
		fields.each{ k, v ->
			if( isSimple( v.datatype ) && obj[k] != null){
				cleanData[k] = obj[k];
			}else if( isObj(v.datatype) && obj[k] != null){
				debug(this,"Obj("+entityName+":"+k+"):"+obj[k]);
				cleanData[k] = _objToMap(sc, obj[k], v.datatype);
			}else if( isList( v.datatype ) && obj[k] != null){
				debug(this,"List("+entityName+":"+k+"):"+obj[k]);
				def mapList = [];
				int i = 0;
				for( Object child : obj[k] ){
					def m = _objToMap(sc,child, v.datatype);
					if( !isPrimitiveOrWrapper( m.getClass()) && !(m instanceof String) ){
						m["_id"] = "id"+(i++);
					}
					mapList.add( m );
				}
				cleanData[k] = mapList;
			}
		}
		if( !isEmbedded(dt)){
			cleanData["_id"] = obj.getId().toString();
		}
		cleanData["@class"] = obj.vertex.getRecord().getClassName();
		return cleanData;
	}

	public Map executeInsertObject(SessionContext sc, String entityName,Map data){
		def obj = _executeInsertObject(sc, entityName, data, false);
		sc.setProperty("inserted", obj );
		return [ id : obj.getIdentity() ];
	}
	protected Object _executeInsertObject(SessionContext sc, String entityName,Map data, detach){
		Map fields = sc.getPermittedFields(entityName, "write");
		Class _clazz = getClass(sc, entityName);
		def obj = _clazz.newInstance( );
		if( detach ){
			obj.detach();
		}
		fields.each{ k, v ->
			debug(this,"K:"+k+"/"+v.datatype);
			if( isSimple( v.datatype ) && data[k] != null){
				debug(this,"Simple("+entityName+":"+k+"):"+data[k]);
				if( isDate( v.datatype) && data[k] instanceof String){
					obj[k] = parseFromString(data[k]);
				}else if( isDate( v.datatype) && data[k] instanceof Long){
					obj[k] = new Date(data[k]);
				}else{
					if( isDecimal(v.datatype) && data[k] instanceof String){
						data[k] = replaceComma( data[k] );
						debug(this,"commaReplaced:"+data[k]);
					}
					def type = OType.getById(Byte.parseByte(v.datatype));
					Class javaClass = type.getDefaultJavaType();
					debug(this,"OType("+type+"):"+javaClass);
					def val = OType.convert( data[k], javaClass);
					debug(this,"NewValue:"+val);
					obj[k] = val;
				}
			}else if( isLinkedObj(v.datatype) && data[k] != null){
				debug(this,"LinkedObj("+entityName+":"+k+"):"+data[k]);
				Class clazz = getClass(sc, v.linkedclass);
				def id = data[k]._id;
				if( id && id.startsWith("#") ){
					obj[k] = sc.getProperty(id);
					if( obj[k] == null){
						obj[k] = _getObject(clazz,id);
					}
				}
			}else if( isEmbeddedObj(v.datatype) && data[k] != null){
				debug(this,"EmbeddedObj("+entityName+":"+k+"):"+data[k]);
				obj[k] = _executeInsertObject(sc, v.linkedclass, data[k], true );
			}else if( (isLinkedSet(v.datatype) || isLinkedList( v.datatype )) && data[k] != null){
				debug(this,"LinkedMulti("+entityName+":"+k+"):"+data[k]);
				def objList = [];
				Class clazz = getClass(sc, v.linkedclass);
				for( def cdata : data[k] ){
					if( cdata instanceof String){
						def child  = sc.getProperty(cdata as String);
						debug(this,"cdata("+cdata+"):"+child);
						if( child == null){
							child = _getObject(clazz, cdata as String);
						}
						objList.add( child );
					}else{	
						Map childData = cdata as Map;
						if( childData._id && childData._id.startsWith("#") ){
							def child = _getObject(clazz, childData._id);
							objList.add( child );
						}
					}
				}
				obj[k] = objList;
			}else if( (isEmbeddedSet(v.datatype) || isEmbeddedList( v.datatype )) && data[k] != null){
				debug(this,"EmbeddedMulti("+entityName+":"+k+"):"+data[k]);
				def cleanList = [];
				if( !isEmpty( v.linkedclass)){
					for( Map map : data[k] ){
						debug(this,"map:"+map);
						def ret = _executeInsertObject(sc, v.linkedclass, map, true );
						cleanList.add(ret);
					}
				}else{
					for( def item : data[k] ){
						debug(this,"item:"+item);
						cleanList.add(item);
					}
				}
				obj[k] = cleanList;
			}
		}
		return obj;
	}

	public Map executeUpdateObject(SessionContext sc, String entityName, String id, Map data){
		Class clazz = getClass(sc, entityName);
		def obj = null;
		if( id != null && id.startsWith("#") && id.indexOf(":") > 0){
			obj = clazz.graphQuery("select from "+id,true);
		}else{
			def className = this.inflector.getClassNameCamelCase(StoreDesc.getSimpleEntityName(entityName));
			def idField = entityName+"Id";
			def idMethod = "get"+this.inflector.capitalizeFirst(entityName)+"Id";
			if( !methodExists(clazz,idMethod)){
				idMethod = "getId";
				idField = "id";
				if( !methodExists(clazz,idMethod)){
					return executeInsertObject(sc, entityName, data);
				}
			}
			def sql = "select from "+className + " where "+idField+"='"+id+"'";
			debug(this,"executeUpdateObject.sql:"+sql);
 		 	obj = clazz.graphQuery(sql,true);
			debug(this,"executeUpdateObject.obj:"+obj);
		}
		if( obj == null){
			return executeInsertObject(sc, entityName, data);
			//throw new RuntimeException("executeUpdateObject("+id+"):not found");
		}
		_executeUpdateObject( sc, obj, data);
		return [ id : id ];
	}

	protected Map _executeUpdateObject(SessionContext sc, Object obj, Map data){
		if( obj == null){
			debug(this,"_executeUpdateObject is null:"+data);
			return;
		}
		def entityName = obj.getClass().getSimpleName();
		entityName = this.inflector.lowerFirst(entityName);
		debug(this,"_executeUpdateObject("+entityName+"):"+data);
		Map fields = sc.getPermittedFields(entityName, "write");
		fields.each{ k, v ->
			debug(this,"K:"+k+"/"+v.datatype);
			if( isSimple( v.datatype) && data[k] != null){
				debug(this,"Simple("+entityName+":"+k+"):"+data[k]);
				if( isDate( v.datatype) && data[k] instanceof String){
					obj[k] = parseFromString(data[k]);
				}else if( isDate( v.datatype) && data[k] instanceof Long){
					obj[k] = new Date(data[k]);
				}else{
					if( isDecimal(v.datatype) && data[k] instanceof String){
						data[k] = replaceComma( data[k] );
						debug(this,"commaReplaced:"+data[k]);
					}
					def type = OType.getById(Byte.parseByte(v.datatype));
					Class javaClass = type.getDefaultJavaType();
					debug(this,"OType("+type+"):"+javaClass);
					def val = OType.convert( data[k], javaClass);
					debug(this,"NewValue:"+val);
					obj[k] = val;
				}
			}else if( isLinkedObj(v.datatype) && data[k] != null){
				debug(this,"LinkedObj("+entityName+":"+k+"):"+data[k]);
				Class clazz = getClass(sc, v.linkedclass);
				def id = data[k]._id;
				if( id && id.startsWith("#") ){
					obj[k] = _getObject(clazz, id);
				}
			}else if( isEmbeddedObj(v.datatype) && data[k] != null){
				debug(this,"EmbeddedObj("+entityName+":"+k+"):"+data[k]);
				Class clazz = getClass(sc, v.linkedclass);
				def child = clazz.newInstance(  );
				child.detach();
				_executeUpdateObject(sc, child, data[k] );
				obj[k] = child;
			}else if( (isLinkedSet(v.datatype) || isLinkedList( v.datatype )) && data[k] != null){
				debug(this,"LinkedMulti("+entityName+":"+k+"):"+data[k]);
				def objList = [];
				Class clazz = getClass(sc, v.linkedclass);
				for( Map childData : data[k] ){
					if( childData._id && childData._id.startsWith("#") ){
						def child = _getObject(clazz, childData._id);
						objList.add( child );
					}
				}
				debug(this,"objList:"+objList);
				obj[k] = objList;
			}else if( (isEmbeddedSet(v.datatype) || isEmbeddedList( v.datatype )) && data[k] != null){
				debug(this,"EmbeddedMulti("+entityName+":"+k+"):"+data[k]);
				def objList = [];
				if( !isEmpty( v.linkedclass)){
					Class clazz = getClass(sc, v.linkedclass);
					for( Map childData : data[k] ){
						def child = clazz.newInstance(  );
						child.detach();
						_executeUpdateObject(sc, child, childData );
						objList.add( child );
					}
				}else{
					for( def item : data[k] ){
						objList.add( item );
					}
				}
				debug(this,"objList:"+objList);
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
		debug(this,"executeGet:"+id);
		Map row = clazz.graphQueryMap("select from "+id,true);
		return row;
	}
	protected Object _getObject(Class clazz,String id){
		def obj = clazz.graphQuery("select from "+id,true);
		debug(this,"_getObject("+id+"):"+obj);
		return obj;
	}
  def methodExists(clazz, methodName) {
    try{
      debug(this,"methodExists("+clazz+","+methodName+")");
			def method = clazz.metaClass.getMetaMethod(methodName)
      debug(this,"methodExists("+method+"):"+(method!=null));
      return method!=null;
    }catch(Exception e){
      debug(this,"methodExists("+methodName+"):false");
      return false;
    }
  }
	def isLinkedObj( def dt ){
		def list = ["13"]
		return list.contains( dt );
	}
	def isLinkedList( def dt ){
		def list = ["14"]
		return list.contains( dt );
	}
	def isLinkedSet( def dt ){
		def list = ["15"]
		return list.contains( dt );
	}
	def isLinkedMap( def dt ){
		def list = ["16"]
		return list.contains( dt );
	}
	def isList ( dt ){
		def list = ["10", "11", "14", "15"]
		return list.contains( dt );
	}

	def isEmbedded( def dt ){
		def list = ["9","10","11", "12"]
		return list.contains( dt );
	}

	def isEmbeddedObj( def dt ){
		def list = ["9"]
		return list.contains( dt );
	}
	def isEmbeddedList( def dt ){
		def list = ["10"]
		return list.contains( dt );
	}
	def isEmbeddedSet( def dt ){
		def list = ["11"]
		return list.contains( dt );
	}
	def isEmbeddedMap( def dt ){
		def list = ["12"]
		return list.contains( dt );
	}

	def isObj ( dt ){
		def list = ["9", "13"]
		return list.contains( dt );
	}

	def isDate ( dt ){
		def list = ["19", "6"]
		return list.contains( dt );
	}
	def isDecimal ( dt ){
		def list = ["21"]
		return list.contains( dt );
	}

	def isSimple( def dt ){
		def simpleList = ["0", "1", "2", "3", "4", "5", "6", "7", "17", "19", "21"]
		return simpleList.contains( dt );
	}
	def parseFromString( String str ){
		return com.mdimension.jchronic.Chronic.parse(str).beginCalendar.time
	}
	def replaceComma(text){
		if( text == null){
			return text;
		}
		def dot = text.lastIndexOf(".");
		def comma = text.lastIndexOf(",");
		double val;
		if( dot > comma ){
			text = text.replace( ",", "");
			def nfIn = NumberFormat.getNumberInstance(Locale.UK);
			val = nfIn.parse(text).doubleValue();
		}else{
			text = text.replace( ".", "");
			def nfIn = NumberFormat.getNumberInstance(Locale.GERMANY);
			val = nfIn.parse(text).doubleValue();
		}
		def nfOut = NumberFormat.getNumberInstance(Locale.UK);
		nfOut.setMaximumFractionDigits(3);
		return nfOut.format(val).replace(",","");
	}
}




