/**
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
import org.codehaus.groovy.ast.ClassNode

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.SourceUnit
import java.io.FileOutputStream
import groovy.transform.CompileStatic

@CompileStatic
class ClassBuilder {

	CollectorClassLoader loader
	List<ClazzConfig> clazzConfigList = [];
	ClazzConfig config;
	List imports;
  String pack;
	Class cls

	def ClassBuilder(ClassLoader parent) {
		this.loader = new CollectorClassLoader(parent)
		this.imports = [];
	}

	def newClazz(String name) {
		config = new ClazzConfig()
		clazzConfigList.add( config );
		config.name = name
	}
	def setPack(String p) {
		pack = p
	}
	def setAnnotation(String ann) {
		config.annotation = ann
	}

	def addImport(Class importClass) {
		this.imports << "${importClass.getPackage().getName()}" + ".${importClass.getSimpleName()}"
	}

	def addField(String name, Class type) {
		config.fields[name] = type.simpleName
	}
	def addField(String name, String type) {
		config.fields[name] = type
	}

  def addMapping(String name, String m) {
    config.mapping[name] = m
  }

	def writeClassFile( File file, name ) {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write( this.loader.bytecodes[pack+"."+name] );
		fos.close();
	}
	def getClazz( name ) {
		return this.loader.classes[pack+"."+name ];
	}

	def createClasses() {

		def templateText = '''
package $pack
<%imports.each {%>import $it\n<% } %> 
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import com.orientechnologies.orient.core.command.OCommandRequest
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.orientechnologies.orient.core.sql.OCommandSQL
import org.ms123.groovy.orient.graph.OrientGraphHelper
import com.orientechnologies.orient.core.metadata.schema.OType

<%clazzConfigList.each { clazz -> %>
$clazz.annotation
@CompileStatic
class $clazz.name {
 public final OrientVertex vertex;
<%clazz.fields.each {%>    $it.value $it.key \n<% } %>
    static mapping = {
<%clazz.mapping.each {%>      $it.key($it.value)\n<% } %>
    }

}
<% } %>
'''
		def data = [pack:pack,imports:imports,clazzConfigList:clazzConfigList]

		def engine = new groovy.text.SimpleTemplateEngine()
		def template = engine.createTemplate(templateText)
		def result = template.make(data)
		println result.toString()
		loader.parseClass(result.toString())
	}
	class ClazzConfig{
		String name
		String annotation
		def fields = [:];
		def mapping = [:];
	}
	class CollectorClassLoader extends GroovyClassLoader {
		CollectorClassLoader(ClassLoader parent) {
			super(parent)
		}
		Map<String,byte[]> bytecodes = [:]
		Map<String,Class> classes = [:]

		class ClassCollector extends GroovyClassLoader.ClassCollector {
			protected ClassCollector(GroovyClassLoader.InnerLoader cl, CompilationUnit unit, SourceUnit su) {
				super(cl, unit, su)
			}

			@Override
			protected Class createClass(byte[] code, ClassNode classNode) {
				bytecodes[classNode.name] = code
				Class clazz = super.createClass(code, classNode)
				classes[classNode.name] = clazz
				return clazz;
			}
		}

		@Override
		protected GroovyClassLoader.ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
			new ClassCollector(new GroovyClassLoader.InnerLoader(this), unit, su)
		}
	}
}

/* Some examples
<%clazz.methodAdd.each { it -> %>
  def $it.key ( $it.value.type	val ){
    Collection coll = (Collection)((OrientVertex)this.vertex).getProperty("$it.value.property")
		if( coll == null){
			coll = new ArrayList();
		}
		coll.add( val );
    ((OrientVertex)this.vertex).setProperty("$it.value.property", coll)
  }
<% } %>



  def insertJSON( String json ){
    def sql = "insert into $clazz.name content "+json;
    OCommandRequest update = new OCommandSQL(sql);
    def graph = this.vertex.getGraph();
    graph.command(update).execute(null);
  }
  public <T> T executeQuery2( String query, boolean singleResult=false,Object... args ){
    def graph = this.vertex.getGraph();
    def sqlQuery = new OSQLSynchQuery<com.tinkerpop.blueprints.Vertex>(query)
    def result = graph.command(sqlQuery).execute(args)
    def resultClass = ${clazz.name}.class
    if (singleResult) {
        return (T) OrientGraphHelper.transformVertexToEntity(resultClass, (OrientVertex) ((Iterable) result)[0])
    } else {
        return (T) OrientGraphHelper.transformVertexCollectionToEntity(resultClass, (Iterable) result, OType.LINKLIST)
    }
  }
*/
