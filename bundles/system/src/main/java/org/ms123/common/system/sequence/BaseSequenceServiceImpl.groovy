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
package org.ms123.common.system.sequence;

import flexjson.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.util.Collection;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.store.StoreDesc;
import org.osgi.framework.BundleContext;
import org.ms123.common.system.orientdb.OrientDBService;
import org.ms123.common.libhelper.FileSystemClassLoader;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 *
 */
@SuppressWarnings("unchecked")
abstract class BaseSequenceServiceImpl {
	protected OrientDBService orientdbService;
	protected BundleContext bundleContext;
	protected JSONDeserializer ds = new JSONDeserializer();
	protected JSONSerializer js = new JSONSerializer();
	protected Object sequenceClass;

	protected void  _set(String key, String value,Map attributes){
		def obj = this.sequenceClass.graphQuery("select from Sequence where key=?", true, key)
		if( obj == null){
			this.sequenceClass.newInstance( [key:key,value:value,attributes:attributes] );
		}else{
			obj.value = value;
			obj.attributes = attributes;
		}
	}

	protected String  _get(String key){
		def obj = this.sequenceClass.graphQuery("select from Sequence where key=?", true, key)
		if( obj == null){
			throw new RuntimeException("_get("+key+"):not found");
		}
		return obj.value;
	}
	protected List<Map>  _getAll(Map attributes){
		def sql = "select from Sequence where ";
		def and ="";
		def values = [];
		attributes.each{ key, value ->
			sql += and + 'attributes.'+key+'=?';
			and = " and ";
			values.add( value);
		}
		info(this, "getAll.sql:"+ sql);
		info(this, "getAll.values:"+ values);
		def objs = this.sequenceClass.graphQuery(sql, false, values.toArray())
		def list = [];
		objs.each{ obj ->
			list.add([key:obj.key,value:obj.value]);
		}
		info(this, "getAll.result:"+ list);
		return list;
	}

	protected List<String>  _getKeys(Map attributes){
		def sql = "select from Sequence where ";
		def and ="";
		def values = [];
		attributes.each{ key, value ->
			sql += and + 'attributes.'+key+'=?';
			and = " and ";
			values.add( value);
		}
		info(this, "getKeys.sql:"+ sql);
		info(this, "getKeys.values:"+ values);
		def objs = this.sequenceClass.graphQuery(sql, false, values.toArray())
		def list = [];
		objs.each{ obj ->
			list.add(obj.key);
		}
		info(this, "getKeys.result:"+ list);
		return list;
	}

	protected void  _delete(String key){
		def obj = this.sequenceClass.graphQuery("select from Sequence where key=?", true, key)
		if( obj == null){
			throw new RuntimeException("_delete("+key+"):not found");
		}
		obj.remove();
	}

	protected Class getSequenceClass(){
		try{
			File[] locations = new File[1];
			String ws = System.getProperty("workspace");
			locations[0] = new File(ws,"java/global");
			ClassLoader cl = new FileSystemClassLoader(this.getClass().getClassLoader(), locations);
			return cl.loadClass("odata.Sequence");
		}catch(Exception e){
			throw new RuntimeException("SequenceService:can't load sequenceClass(odata.Sequence)",e);
		}
	}
}

