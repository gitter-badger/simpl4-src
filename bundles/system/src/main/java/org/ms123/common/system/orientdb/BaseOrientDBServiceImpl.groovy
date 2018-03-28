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
package org.ms123.common.system.orientdb;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import flexjson.*;
import groovy.json.JsonSlurper;
import groovy.transform.CompileStatic;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.store.StoreDesc;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 */
@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
abstract class BaseOrientDBServiceImpl{
	protected DataLayer dataLayer;
	protected Inflector inflector = Inflector.getInstance();

	public void doImport( OrientGraph graph,String namespace, File imp, boolean drop){
		StoreDesc sdesc = StoreDesc.getNamespaceData(namespace,"odata");
		SessionContext sc = this.dataLayer.getSessionContext(sdesc);
		def inputJSON = new JsonSlurper().parseText(imp.text);
		def records = (inputJSON as Map).records as List;
		records.sort{a,b->
			def aF = [];
			findFrom(a,aF);
			def aT = [];
			findTo(a,aT);

			def bF = [];
			findFrom(b,bF);
			def bT = [];
			findTo(b,bT);

			def ret = 0;
			for( f in aF ){
				if( bT.indexOf( f) >=0){
					ret = 1;
					info(this,"B:"+bT+".indexOf("+f+"):"+ret);
					break;
				}
			}
			for( f in bF ){
				if( aT.indexOf( f) >=0){
					ret = -1;
					info(this,"A:"+aT+".indexOf("+f+"):"+ret);
					break;
				}
			}
			info(this,"ret:"+ret);
			return ret;
		}
		if( drop){
			def firstMap = [:];
			records.each{ record ->
				def clazzName = record["@class"];
				if( firstMap[clazzName] == null){
					firstMap[clazzName] = "";
					info( this, "deleteVertex("+clazzName+")");
					executeUpdate(graph, "delete vertex "+clazzName);
				}
			}
		}
		records.each{ insertRecord(sc, it as Map) }
	}

	def insertRecord( SessionContext sc, Map record ){
		def clazzName = record["@class"];
		def entityName = this.inflector.getEntityNameCamelCase(clazzName);
		info( this, "insertRecord("+entityName+"):"+record);
		def rid = record["@rid"] as String;
		record.remove('@class');
		record.remove('@rid');
		record.remove('@type');
		record.remove('@version');
		this.dataLayer.insertObject(sc, record, inflector.lowerFirst(entityName));
		info(this,"rid("+rid+");"+sc.getProperty("inserted"));
		sc.setProperty(rid, sc.getProperty("inserted"));
	}

	def findFrom( def obj, List result ){
		if( obj instanceof String){
			String s = obj as String;
			if(s.startsWith("#") ){
				result.add(s);
			}
		}else if( obj instanceof List ){
			List list = obj as List;
			for( o in list ){
				findFrom(o,result);
			}
		}else if( obj instanceof Map){
			Map map = obj as Map;
			for( e in map ){
				if( e.key != "@rid"){
					findFrom(e.value,result);
				}
			}
		}
	}
	def findTo( def obj, List result ){
		if( obj instanceof List ){
			List list = obj as List;
			for( o in list ){
				findTo(o, result);
			}
		}else if( obj instanceof Map){
			Map map = obj as Map;
			for( e in map ){
				if( e.key == "@rid"){
					result.add( e.value);
				}else{
					findTo(e.value,result);
				}
			}
		}
	}
	abstract void executeUpdate(OrientGraph graph, String sql, Object... args);
}

