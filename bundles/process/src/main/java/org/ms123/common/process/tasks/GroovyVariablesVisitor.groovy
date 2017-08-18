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
package org.ms123.common.process.tasks;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.GroovyClassVisitor
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.*
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import org.ms123.common.process.ProcessServiceImpl;

import java.security.CodeSource

/**
 * Class to Class parsing a script to detect all bound and unbound variables.
 */
@SuppressWarnings("unchecked")
@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
public class GroovyVariablesVisitor {

	static class VariableVisitor extends ClassCodeVisitorSupport implements GroovyClassVisitor {
		Set<String> bound = new HashSet<String>();
		Set<String> unbound = new HashSet<String>();

		@Override
		void visitVariableExpression(VariableExpression expression) {
			if (!(expression.getText() in ['args', 'context', 'this', 'super'])) {
				if (expression.getAccessedVariable() instanceof DynamicVariable) {
					unbound << expression.getText();
				} else {
					bound << expression.getText();
				}
			}
			super.visitVariableExpression(expression);
		}

		@Override
		protected SourceUnit getSourceUnit() {
			return null;
		}
	}
	static class VisitorSourceOperation extends CompilationUnit.PrimaryClassNodeOperation {
		final GroovyClassVisitor visitor;
		VisitorSourceOperation(final GroovyClassVisitor visitor) {
			this.visitor = visitor;
		}

		@Override
		void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
			classNode.visitContents(visitor);
		}
	}

	static class VisitorClassLoader extends GroovyClassLoader {
		final GroovyClassVisitor visitor;

		VisitorClassLoader(final GroovyClassVisitor visitor) {
			this.visitor = visitor;
		}

		@Override
		protected CompilationUnit createCompilationUnit(final CompilerConfiguration config, final CodeSource source) {
			CompilationUnit cu = super.createCompilationUnit(config, source)
			cu.addPhaseOperation(new VisitorSourceOperation(visitor), Phases.CLASS_GENERATION);
			return cu;
		}
	}

	private static ClassLoader _setContextClassLoader() {
		ClassLoader saveCl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ProcessServiceImpl.class.getClassLoader());
		return saveCl;
	}

	public static Map<String,Set<String>> getVariables(final String scriptText) {
		assert scriptText != null;
		GroovyClassVisitor visitor = new VariableVisitor()
		ClassLoader saveCl = _setContextClassLoader();
		try{
			VisitorClassLoader myCL = new VisitorClassLoader(visitor);
			myCL.parseClass(scriptText);
		}finally{
			Thread.currentThread().setContextClassLoader(saveCl);
		}
		def ret = [:];
		ret.bound = visitor.bound;
		ret.unbound = visitor.unbound;
		return ret;
	}
}

