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

class ClassBuilder {

	GroovyClassLoader loader
	String name
	Class cls
	def imports
	def fields
	def methods

	def ClassBuilder(GroovyClassLoader loader) {
		this.loader = new MyGroovyClassLoader(loader)
		imports = []
		fields = [:]
		methods = [:]
	}

	def setName(String name) {
		this.name = name
	}

	def addImport(Class importClass) {
		imports << "${importClass.getPackage().getName()}" +
			".${importClass.getSimpleName()}"
	}

	def addField(String name, Class type) {
		fields[name] = type.simpleName
	}

	def addMethod(String name, Closure closure) {
		methods[name] = closure
	}

	def writeClassFile( String file ) {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write( this.loader.bytecodes[this.name] );
		fos.close();
	}

	def getCreatedClass() {

		def templateText = '''
<%imports.each {%>import $it\n <% } %> 
class $name
{
<%fields.each {%>    $it.value $it.key \n<% } %>
}
'''
		def data = [name: name, imports: imports, fields: fields]

		def engine = new groovy.text.SimpleTemplateEngine()
		def template = engine.createTemplate(templateText)
		def result = template.make(data)
		cls = loader.parseClass(result.toString())
		methods.each {
			cls.metaClass."$it.key" = it.value
		}
		return cls
	}
	class MyGroovyClassLoader extends GroovyClassLoader {
		MyGroovyClassLoader(ClassLoader parent) {
			super(parent)
		}
		Map bytecodes = [:]


		class MyClassCollector extends GroovyClassLoader.ClassCollector {
			protected MyClassCollector(GroovyClassLoader.InnerLoader cl, CompilationUnit unit, SourceUnit su) {
				super(cl, unit, su)
			}


			@Override
			protected Class createClass(byte[] code, ClassNode classNode) {
				bytecodes[classNode.name] = code
				super.createClass(code, classNode)
			}
		}


		@Override
		protected GroovyClassLoader.ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
			new MyClassCollector(new GroovyClassLoader.InnerLoader(this), unit, su)
		}
	}
}

