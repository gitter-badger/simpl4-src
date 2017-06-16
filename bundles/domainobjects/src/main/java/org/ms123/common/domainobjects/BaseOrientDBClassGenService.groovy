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
package org.ms123.common.domainobjects;

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

import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.system.orientdb.OrientDBService;
import org.ms123.common.libhelper.FileSystemClassLoader;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import com.orientechnologies.orient.core.metadata.schema.OType;
import groovy.transform.CompileStatic;
import org.ms123.groovy.orient.graph.Vertex;
import org.ms123.groovy.orient.graph.Edge;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.File;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;

/**
 *
 */
@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
abstract class BaseOrientDBClassGenService implements org.ms123.common.domainobjects.api.ClassGenService{

	protected Inflector m_inflector = Inflector.getInstance();
	protected EntityService m_entityService;
	protected OrientDBService m_orientdbService;
	private Map<StoreDesc,ClassLoader> classLoaderMap = new HashMap<StoreDesc,ClassLoader>();

	public ClassLoader getClassLoader( StoreDesc sdesc){
		ClassLoader cl = this.classLoaderMap.get( sdesc );
		if( cl == null){
			File[] locations = new File[1];
			locations[0] = new File(sdesc.getBaseDir(), sdesc.getNamespace());
			cl = new FileSystemClassLoader(this.getClass().getClassLoader(), locations);
			info(this,"BaseOrientDBClassGenService.getClassLoader("+sdesc+"):"+locations[0]);
			classLoaderMap.put( sdesc, cl);
		}else{
			info(this,"BaseOrientDBClassGenService.getClassLoader("+sdesc+"):"+cl);
		}
		return cl;
	}

	public List<String> generate(StoreDesc sdesc, List<Map> entities, String outDir) throws Exception {
		info(this,"--->generate:" + sdesc.getString());
		List<String> classnameList = new ArrayList();
		String javaPackage=null;
		for (int i = 0; i < entities.size(); i++) { //Make empty classes to resolve relations
			Map m = entities.get(i);
			if( i==0){
				javaPackage = getJavaPackage(sdesc, m);
			}
		}
		info(this,"entities:"+entities);
		File[] locations = new File[1];
		locations[0] = new File( outDir);
		FileSystemClassLoader fscl = new FileSystemClassLoader(this.getClass().getClassLoader(), locations);
		classLoaderMap.put( sdesc, fscl);
		ClassBuilder builder = new ClassBuilder(this.getClass().getClassLoader());
		builder.setPack( javaPackage );
		builder.addImport( OType);
		builder.addImport( CompileStatic);
		builder.addImport( Vertex);
		builder.addImport( Edge);
		for (int i = 0; i < entities.size(); i++) {
			Map entMap = entities.get(i);
			String name = (String) entMap.get("name");
			String classname = getClassName(entMap);
			boolean isVertex = isVertex(entMap);
			String restricted = isRestricted(entMap) ? " restricted = true, " : "restricted = false, ";
			String  superclass = getSuperclass(entMap);

			builder.newClazz(classname);
			if( isVertex ){
				builder.setAnnotation("@"+superclass+"(initSchema = true, " + restricted + "value = '"+classname+"')");
			}else{
				String _from = getFrom(entMap);
				String _to = getTo(entMap);
				builder.setAnnotation("@"+superclass+"(initSchema = true, " + restricted + "from="+_from+", to="+_to+", value = '"+classname+"')");
			}
			List<Map> fields = getEntityMetaData(sdesc, name);
			for( int j=0; j< fields.size(); j++){
				Map field = fields.get(j);
				String fieldname = getName(field);
				if( "id".equals(fieldname)){
					continue;
				}
				boolean isEdgeConn = isEdgeConnection(field);
				OType otype = null;
				boolean isMulti=false;
				boolean isLink=false;
				boolean isEmbedded=false;
				if( !isEdgeConn){
					otype = getType(field);
					isMulti = otype.isMultiValue();
					isLink = otype.isLink();
					isEmbedded = otype.isEmbedded();
				}
				String linkedClass = getLinkedClassName(field);
				info(this,"field:"+field);
				info(this,"isEdgeConn:"+isEdgeConn);
				info(this,"isLink:"+isLink);
				info(this,"isMulti:"+isMulti);
				info(this,"OType:"+otype);
				info(this,"linkedClass:"+linkedClass);
				if( isEdgeConn ){
					String vertex = getVertexClassName(field);
					String vtype = getVertexType(field);
					String edge = getEdgeClassName(field);
					info(this,"vertex:"+vertex);
					info(this,"vtype:"+vtype);
					info(this,"edge:"+edge);
					info(this,"VertexDecl:"+getVertexDeclaration(vertex,vtype));
					if( isEmpty( vertex) || isEmpty(edge)){
						throw new RuntimeException("Vertex or Edge missing:"+fieldname);
					}
					builder.addField(fieldname, getVertexDeclaration(vertex,vtype));
					builder.addMapping(fieldname, "edge: "+ edge )
				}else if( isMulti ){
					if( isLink ){
						builder.addField(fieldname, getDeclaration(linkedClass,otype.toString()));
						builder.addMapping(fieldname, "type: OType."+ otype.toString() )
					}else{
						if( linkedClass != null ){
							builder.addField(fieldname, getDeclaration(linkedClass,otype.toString()));
							builder.addMapping(fieldname, "type: OType."+ otype.toString() )
						}else{
							builder.addField(fieldname, getDeclaration(getLinkedType(field).getDefaultJavaType().getSimpleName(),otype.toString()));
							builder.addMapping(fieldname, "type: OType."+ otype.toString() )
						}
					}
				}else if( isEmbedded && !isMulti ){
					builder.addField(fieldname, linkedClass);
					builder.addMapping(fieldname, "type: OType."+ otype.toString() )
				}else if( isLink ){
					builder.addField(fieldname, linkedClass);
					builder.addMapping(fieldname, "type: OType."+  otype.toString())
				}else{
					Class javaClazz = otype.getDefaultJavaType()
					builder.addField(fieldname, javaClazz)
				}
			}

			classnameList.add(classname);
			List<String> pkNameList = (List) entMap.get("primaryKeys");
			if (pkNameList != null && pkNameList.size() > 1) {
				String classnamePK = classname + "_PK";
			}
		}

		File _outDir = getOutDir( outDir, javaPackage );
		if (!_outDir.exists()) {
			_outDir.mkdirs();
		}

		builder.createClasses()
		OrientGraphFactory f = m_orientdbService.getFactory(sdesc.getNamespace());
		ODatabaseDocumentTx db = f.getNoTx().getRawGraph();
		for( int i=0; i < classnameList.size();i++){
			String cn = classnameList.get(i);
			builder.writeClassFile( new File(_outDir, cn+ ".class"), cn );
		}
		for( int i=0; i < classnameList.size();i++){
			String cn = classnameList.get(i);
			Class clazz = builder.getClazz( cn );
			callInit( db, clazz, "initSchema");
		}
		for( int i=0; i < classnameList.size();i++){
			String cn = classnameList.get(i);
			Class clazz = builder.getClazz( cn );
			callInit( db, clazz, "initSchemaLinks");
		}
		info(this,"classnameList:"+classnameList);
		db.commit();
		return classnameList;
	}

	protected File getOutDir( String out, String javaPackage ){
		return new File(out,  javaPackage.replace(".","/"));
	}
	protected File getOutDir( File out, String javaPackage ){
		return new File(out,  javaPackage.replace(".","/"));
	}

	protected List getEntityMetaData(StoreDesc sdesc, String entity) throws Exception {
		List list = m_entityService.getFields(sdesc, entity, false);
		info(this,"getEntityMetaData:"+list);
		return list;
	}

	private void callInit( ODatabaseDocumentTx db, Class clazz, String meth ){
		Class[] cargs = new Class[1];
		cargs[0] = ODatabaseDocumentTx.class;
		try {
			Method _meth = clazz.getDeclaredMethod(meth, cargs);
			Object[] args = new Object[1];
			args[0] = db;
			_meth.invoke(null, args);
			info(this, "Init("+clazz+":" + meth);
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw e.getCause();
		}
	}

	private boolean isVertex( Map<String,String> m){
		return "vertex".equals(m.get("superclass"));
	}

	private boolean isEdge( Map<String,String> m){
		return "edge".equals(m.get("superclass"));
	}
	private boolean isEdgeConnection( Map<String,Boolean> m){
		def ret = m.get("edgeconn");
		if( ret == null) return false;
		return ret;
	}
	private boolean isRestricted( Map<String,Boolean> m){
		def ret = m.get("restricted");
		if( ret == null) return false;
		return ret;
	}

	private String getSuperclass( Map<String,String> m){
		return firstToUpper(m.get("superclass"));
	}
	private OType getType( Map<String,String> m){
		return OType.getById(Byte.parseByte(m.get("datatype")));
	}
	private OType getLinkedType( Map<String,String> m){
		return OType.getById(Byte.parseByte(m.get("linkedtype")));
	}

	private String getName( Map<String,String> m){
		return m.get("name");
	}

	private String firstToUpper(String s) {
		String fc = s.substring(0, 1);
		return fc.toUpperCase() + s.substring(1);
	}

	private String firstToLower(String s) {
		String fc = s.substring(0, 1);
		return fc.toLowerCase() + s.substring(1);
	}

	private boolean getBoolean(Object o, boolean _def) {
		if (o == null)
			return _def;
		try {
			boolean b = (Boolean) o;
			return b;
		} catch (Exception e) {
		}
		return _def;
	}
	private String getLinkedClassName(Map entity) {
		String name = (String)entity.get("linkedclass");
		if( isEmpty(name)) return null;
		return firstToUpper((String) name);
	}
	private String getClassName(Map entity) {
		String name = (String)entity.get("name");
		if( isEmpty(name)) return null;
		return firstToUpper((String) name);
	}

	private String getEdgeClassName(Map entity) {
		String name = (String)entity.get("edgeclass");
		if( isEmpty(name)) return null;
		return firstToUpper((String) name);
	}

	private String getVertexClassName(Map entity) {
		String name = (String)entity.get("vertexclass");
		if( isEmpty(name)) return null;
		return firstToUpper((String) name);
	}
	private String getVertexType(Map entity) {
		String name = (String)entity.get("vertextype");
		if( isEmpty(name)) return null;
		return name;
	}
	private String getVertexDeclaration(String name, String type) {
		if( type.equals("single")){
			return name;
		}else if( type.equals("list")){
			return "List<"+name+">";
		}else if( type.equals("set")){
			return "Set<"+name+">";
		}else if( type.equals("map")){
			return "Map<String,"+name+">";
		}
		return null;
	}

	private String getDeclaration(String name, String type) {
		if( type.endsWith("LIST")){
			return "List<"+name+">";
		}else if( type.endsWith("SET")){
			return "Set<"+name+">";
		}else if( type.endsWith("MAP")){
			return "Map<String,"+name+">";
		}
		return name;
	}

	private String getFrom(Map entity) {
		String name = (String)entity.get("from");
		if( isEmpty(name)) return null;
		return firstToUpper((String) name);
	}
	private String getTo(Map entity) {
		String name = (String)entity.get("to");
		if( isEmpty(name)) return null;
		return firstToUpper((String) name);
	}

	private String getJavaPackage(StoreDesc sdesc, Map entity) {
		String name = (String)entity.get("name");
		String pack = StoreDesc.getPackName(name,sdesc.getPack());
		return pack;
	}
}

