package org.ms123.common.camel.jsonconverter;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;
import java.io.FileOutputStream;
public class CollectorClassLoader extends GroovyClassLoader {
	CollectorClassLoader(ClassLoader parent, CompilerConfiguration config) {
		super(parent,config);
	}
	Map<String,byte[]> bytecodes = [:]
		Map<String,Class> classes = [:]

		class ClassCollector extends GroovyClassLoader.ClassCollector {
			protected ClassCollector(GroovyClassLoader.InnerLoader cl, CompilationUnit unit, SourceUnit su) {
				super(cl, unit, su)
			}

			@Override
			protected Class createClass(byte[] code, ClassNode classNode) {

				println("createClass");
				FileOutputStream fos = new FileOutputStream("/tmp/script.class");
				fos.write( code );
				fos.close();

				//bytecodes[classNode.name] = code
				Class clazz = super.createClass(code, classNode)
					//classes[classNode.name] = clazz
					return clazz;
			}
		}

	@Override
	protected GroovyClassLoader.ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
		new ClassCollector(new GroovyClassLoader.InnerLoader(this), unit, su)
	}
}
