/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014,2017] [Manfred Sattler] <manfred@ms123.org>
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
package org.ms123.common.process.expressions;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import flexjson.*;
import groovy.lang.*;
import java.util.concurrent.ConcurrentMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.codehaus.groovy.control.*;
import org.ms123.common.libhelper.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 * 
 * @author Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class GroovyExpression implements Expression {
	protected JSONDeserializer ds = new JSONDeserializer();
	protected String m_expressionText;
	protected GroovyShell m_shell;
	protected ProcessEngine m_processEngine;

	private static ConcurrentMap<String, Script> m_scriptCache = new ConcurrentLinkedHashMap.Builder<String, Script>().maximumWeightedCapacity(100).build();

	public GroovyExpression(GroovyShell shell, ProcessEngine pe, String expressionText) {
		debug(this, "GroovyExpression:" + expressionText);
		m_shell = shell;
		m_processEngine = pe;
		m_expressionText = expressionText;
	}

	public synchronized Object getValue(VariableScope variableScope) {
		long start = new Date().getTime();
		debug(this, "GroovyExpression.getValue-->" + m_expressionText);
		Object o = expandString(m_expressionText, variableScope);
		debug(this, "GroovyExpression.getValue<---:" + o);
		return o;
	}

	public void setValue(Object value, VariableScope variableScope) {
		info(this, "GroovyExpression.setValue:" + value);
	}

	public void setValue(Object value, VariableScope variableScope, BaseDelegateExecution e) {
		info(this, "GroovyExpression.setValue.BaseDelegateExecution(" + e + "):" + value);
	}

	public Object getValue(VariableScope variableScope, BaseDelegateExecution e) {
		info(this, "GroovyExpression.getValue(" + e + "):" + variableScope);
		return getValue(variableScope);
	}

	public boolean isLiteralText() {
		throw new RuntimeException("GroovyExpression.isLiteralText() not implemented");
	}

	@Override
	public String toString() {
		return m_expressionText;
	}

	public String getExpressionText() {
		return m_expressionText;
	}

	private synchronized Object eval(String expr, VariableScope scope) {
		try {
			Script script = null;//m_scriptCache.get(expr);
			script = m_shell.parse(expr);
			Binding binding = new Binding(scope.getVariables());
			if (scope instanceof DelegateExecution) {
				DelegateExecution e = (DelegateExecution) scope;
				binding.setVariable("__processBusinessKey", e.getProcessBusinessKey());
				binding.setVariable("__processInstanceId", e.getProcessInstanceId());
				binding.setVariable("__processDefinitionId", e.getProcessDefinitionId());
			}
			script.setBinding(binding);
			debug(this, "GroovyExpression.vars:" + binding.getVariables());
			return script.run();
		} catch (Throwable e) {
			e.printStackTrace();
			String msg = Utils.formatGroovyException(e, expr);
			if (scope instanceof ActivityExecution) {
				ActivityExecution de = (ActivityExecution) scope;
				String activityId = de.getCurrentActivityId();
				List<HistoricActivityInstance> activityList = m_processEngine.getHistoryService().createHistoricActivityInstanceQuery().activityId(activityId).list();
				String activityName = "";
				for (HistoricActivityInstance h : activityList) {
					if ("".equals(activityName)) {
						activityName = h.getActivityName();
					}
				}
				msg = activityName + "(" + activityId + ")|" + msg;
			}
			throw new RuntimeException(msg);
		}
	}

	private boolean maybeJSON(String string) {
		return string.startsWith("{") && string.endsWith("}");
	}

	private Object getJSON(String str) {
		try {
			return ds.deserialize(str);
		} catch (Exception e) {
			error(this, "GroovyExpression.getJSON", e);
			return str;
		}
	}

	private Object getService(String clazzName) {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		BundleContext bc = (BundleContext) beans.get("bundleContext");
		ServiceReference sr = bc.getServiceReference(clazzName);
		Object o = bc.getService(sr);
		return o;
	}

	private synchronized Object expandString(String str, VariableScope scope) {
		if (str.startsWith("@")) {
			return getService(str.substring(1));
		}
		if (str.startsWith("~")) {
			return str.substring(1);
		}
		str = str.trim();
		if (str.startsWith("#")) {
			return eval(str.substring(1), scope);
		}
		if (str.startsWith("pc:{")) {
			Object obj = getJSON(str.substring(3));
			if (obj instanceof Map) {
				return ExpressionCamelExecutor.execute((Map) obj, scope);
			}
		}
		int countRepl = 0;
		int countPlainStr = 0;
		Object replacement = null;
		String newString = "";
		int openBrackets = 0;
		int first = 0;
		for (int i = 0; i < str.length(); i++) {
			if (i < str.length() - 2 && str.substring(i, i + 2).compareTo("${") == 0) {
				if (openBrackets == 0) {
					first = i + 2;
				}
				openBrackets++;
			} else if (str.charAt(i) == '}' && openBrackets > 0 && !hasMoreRightBrackets(str, i)) {
				openBrackets -= 1;
				if (openBrackets == 0) {
					countRepl++;
					replacement = eval(str.substring(first, i), scope);
					newString += replacement;
				}
			} else if (openBrackets == 0) {
				newString += str.charAt(i);
				countPlainStr++;
			}
		}
		if (countRepl == 1 && countPlainStr == 0) {
			return replacement;
		} else {
			return newString;
		}
	}

	private boolean hasMoreRightBrackets(String str, int pos) {
		int len = str.length();
		boolean hasMore = false;
		for (int i = pos + 1; i < len; i++) {
			if (str.charAt(i) == '}') {
				return true;
			}
			if (str.charAt(i) == '$' && i + 1 < len && str.charAt(i + 1) == '{') {
				return false;
			}
		}
		return false;
	}

}

