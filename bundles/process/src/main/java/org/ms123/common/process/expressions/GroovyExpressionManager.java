/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ms123.common.process.expressions;

import groovy.lang.*;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.ProcessEngine;
import org.codehaus.groovy.control.*;

/**
 */
public class GroovyExpressionManager extends ExpressionManager {
	private ProcessEngine m_processEngine;

	public GroovyExpressionManager() {
	}

	public void setProcessEngine(ProcessEngine pe) {
		m_processEngine = pe;
	}

	public synchronized Expression createExpression(String expression) {
		long st = new java.util.Date().getTime();
		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(org.ms123.common.workflow.api.GroovyTaskDslBase.class.getName());
		GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), new Binding(), config);
		return new GroovyExpression(shell, m_processEngine, expression);
	}

}

