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
package org.ms123.common.workflow;

import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.context.Context;
import groovy.lang.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.codehaus.groovy.control.*;
// import org.activiti.engine.delegate.Expression;
import org.ms123.common.libhelper.Utils;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;

import org.activiti.engine.impl.el.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import flexjson.*;

/**
 * 
 * @author Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class GroovyExpression implements Expression {
	private static final Logger m_logger = LoggerFactory.getLogger(GroovyExpression.class);
	protected JSONDeserializer ds = new JSONDeserializer();
	protected String m_expressionText;
	protected GroovyShell m_shell;
	protected ProcessEngine m_processEngine;

	private static ConcurrentMap<String, Script> m_scriptCache = new ConcurrentLinkedHashMap.Builder<String, Script>().maximumWeightedCapacity(100).build();

	public GroovyExpression(GroovyShell shell, ProcessEngine pe, String expressionText) {
		debug("GroovyExpression:" + expressionText);
		m_shell = shell;
		m_processEngine = pe;
		m_expressionText = expressionText;
	}

	public synchronized Object getValue(VariableScope variableScope) {
		long start = new Date().getTime();
		debug("GroovyExpression.getValue-->" + m_expressionText);
		Object o = expandString(m_expressionText, variableScope);
		long end = new Date().getTime();
		debug("GroovyExpression.getValue<---:" + o);
		debug("TIME:" + (end - start));
		return o;
	}

	public void setValue(Object value, VariableScope variableScope) {
		debug("GroovyExpression.setValue:" + value);
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
			debug("GroovyExpression.vars:" + binding.getVariables());
			return script.run();
		} catch (Throwable e) {
			log(">>>>>>>>>>>>" + e);
			e.printStackTrace();
			String msg = Utils.formatGroovyException(e, expr);
			if (scope instanceof ActivityExecution) {
				ActivityExecution de = (ActivityExecution) scope;
				String activityId = de.getCurrentActivityId();
				List<HistoricActivityInstance> activityList = m_processEngine.getHistoryService().createHistoricActivityInstanceQuery().activityId(activityId).list();
				String activityName = "";
				for (HistoricActivityInstance h : activityList) {
					log("h:" + h.getActivityName());
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
			error("GroovyExpression.getJSON", e);
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

	private void debug(String message) {
		m_logger.debug(message);
	}

	private void log(String message) {
		m_logger.info(message);
	}

	private void error(String message, Throwable t) {
		m_logger.error(message, t);
	}
}

