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
import java.lang.reflect.Method;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.Inflector;


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

	public List<Map> executeQuery(Class clazz,String className, String where){
		info(this,"Where:"+where);
		List list = clazz.graphQueryMap("select from "+className + " where "+ where,false);
		list.each{ row -> 
			row.id = row._id;
			row.remove("_id");
		}
		return list;
	}
	public Map executeInsertObject(Class clazz,Map data, Map fields){
		def cleanData = [:];
		fields.each{ k, v -> 
			if( isSimple( v.datatype )){
				cleanData[k] = data[k];
			}
		}
		info(this,"cleandata:"+cleanData);
		def obj = clazz.newInstance( cleanData );
		return [ id : obj.getIdentity() ];
	}

	public Map executeUpdateObject(Class clazz,String id, Map data, Map fields){
		def obj = clazz.graphQuery("select from "+id,true);
		if( obj == null){
			throw new RuntimeException("executeUpdateObject("+id+"):not found");
		}
		fields.each{ k, v -> 
			if( isSimple( v.datatype ) && data[k] != null){
				obj[k] = data[k];
			}
		}
		return [ id : id ];
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

	def isSimple( def dt ){
		def simpleList = ["1","2","3","4","5","6","7","17","19","21"]
		return simpleList.contains( dt );
	}
}

