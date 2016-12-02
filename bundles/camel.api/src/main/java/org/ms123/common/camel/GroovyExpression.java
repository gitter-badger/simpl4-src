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
package org.ms123.common.camel.api;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.nio.file.Paths;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.language.groovy.GroovyShellFactory;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.support.ExpressionSupport;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.LRUSoftCache;
import static com.jcabi.log.Logger.info;

/**
 * @version 
 */
public class GroovyExpression {
	private static LRUSoftCache<String, Class<Script>> scriptCache = new LRUSoftCache<String, Class<Script>>(1000);

	private GroovyExpression() {
	}

	private static Class<Script> getScriptFromCache(String script) {
		return scriptCache.get(script);
	}

	private static void addScriptToCache(String script, Class<Script> scriptClass) {
		scriptCache.put(script, scriptClass);
	}

	public static String evaluate(String expr, Exchange exchange) {
		int countRepl = 0;
		int countPlainStr = 0;
		String replacement = null;
		String newString = "";
		int openBrackets = 0;
		int first = 0;
		for (int i = 0; i < expr.length(); i++) {
			if (i < expr.length() - 2 && expr.substring(i, i + 2).compareTo("${") == 0) {
				if (openBrackets == 0) {
					first = i + 2;
				}
				openBrackets++;
			} else if (expr.charAt(i) == '}' && openBrackets > 0) {
				openBrackets -= 1;
				if (openBrackets == 0) {
					countRepl++;
					replacement = evaluate(expr.substring(first, i), exchange, String.class);
					newString += replacement;
				}
			} else if (openBrackets == 0) {
				newString += expr.charAt(i);
				countPlainStr++;
			}
		}
		if (countRepl == 1 && countPlainStr == 0) {
			return replacement;
		} else {
			return newString;
		}
	}

	public static <T> T evaluate(String expr, Exchange exchange, Class<T> type) {
		Script script = instantiateScript(exchange, expr);
		script.setBinding(createBinding(exchange));
		Object value = script.run();

		return exchange.getContext().getTypeConverter().convertTo(type, value);
	}

	@SuppressWarnings("unchecked")
	private static Script instantiateScript(Exchange exchange, String expr) {
		// Get the script from the cache, or create a new instance
		Class<Script> scriptClass = getScriptFromCache(expr);
		if (scriptClass == null) {
			GroovyShell shell;
			Set<GroovyShellFactory> shellFactories = exchange.getContext().getRegistry().findByType(GroovyShellFactory.class);
			if (shellFactories.size() > 1) {
				throw new IllegalStateException("Too many GroovyShellFactory instances found: " + shellFactories.size());
			} else if (shellFactories.size() == 1) {
				shell = shellFactories.iterator().next().createGroovyShell(exchange);
			} else {
				ClassLoader cl = exchange.getContext().getApplicationContextClassLoader();
				shell = cl != null ? new GroovyShell(cl) : new GroovyShell();
			}
			scriptClass = shell.getClassLoader().parseClass(expr);
			addScriptToCache(expr, scriptClass);
		}

		// New instance of the script
		try {
			return scriptClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeCamelException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeCamelException(e);
		}
	}

	private static Binding createBinding(Exchange exchange) {
		Map<String, Object> variables = new HashMap<String, Object>();
		populateVariableMap(exchange, variables);
		return new Binding(variables);
	}

	/**
	 * Creates a Map of the variables which are made available to a script or template
	 *
	 * @param exchange the exchange to make available
	 * @return a Map populated with the require variables
	 */
	private static Map<String, Object> createVariableMap(Exchange exchange) {
		Map<String, Object> answer = new HashMap<String, Object>();
		populateVariableMap(exchange, answer);
		return answer;
	}

	/**
	 * Populates the Map with the variables which are made available to a script or template
	 *
	 * @param exchange the exchange to make available
	 * @param map      the map to populate
	 */
	private static void populateVariableMap(Exchange exchange, Map<String, Object> map) {
		map.put("exchange", exchange);
		Message in = exchange.getIn();
		map.put("in", in);
		map.put("request", in);
		map.put("properties", exchange.getProperties());
		map.put("p", exchange.getProperties());
		map.put("headers", in.getHeaders());
		map.put("header", in.getHeaders());
		map.put("h", in.getHeaders());
		map.put("body", in.getBody());
		map.put("b", in.getBody());
		if (isOutCapable(exchange)) {
			// if we are out capable then set out and response as well
			// however only grab OUT if it exists, otherwise reuse IN
			// this prevents side effects to alter the Exchange if we force creating an OUT message
			Message msg = exchange.hasOut() ? exchange.getOut() : exchange.getIn();
			map.put("out", msg);
			map.put("response", msg);
		}
		String namespace = exchange.getContext().getRegistry().lookupByNameAndType("namespace", String.class);
		String gitRepos = System.getProperty("git.repos");
		map.put("namespace", namespace);
		map.put("gitRepos", gitRepos);
		map.put("home", Paths.get(gitRepos,namespace).toString());
		map.put("homeData", Paths.get(gitRepos,namespace+"_data").toString());
		map.put("camelContext", exchange.getContext());
	}

	private static boolean isOutCapable(Exchange exchange) {
		ExchangePattern pattern = exchange.getPattern();
		return pattern != null && pattern.isOutCapable();
	}
}

