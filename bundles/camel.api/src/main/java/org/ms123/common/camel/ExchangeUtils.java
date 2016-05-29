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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import org.apache.camel.Exchange;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import org.apache.camel.util.ObjectHelper;
import flexjson.*;

/**
 * 
 */
public class ExchangeUtils {
	public static final String CAMELBODY = "camelBody";
	private static JSONSerializer m_js = new JSONSerializer();
	protected static final String IGNORE_MESSAGE_PROPERTY = "CamelMessageHistory";

	/**
	 */
	public static Map<String, Object> prepareVariables(Exchange exchange, boolean shouldCopyVariablesFromHeader, boolean shouldCopyVariablesFromProperties, boolean shouldCopyCamelBodyToBodyAsString) {
		return prepareVariables(exchange, shouldCopyVariablesFromHeader, (List<String>) null, shouldCopyVariablesFromProperties, (List<String>) null, shouldCopyCamelBodyToBodyAsString);
	}

	public static Map<String, Object> prepareVariables(Exchange exchange, boolean shouldCopyVariablesFromHeader, String headerNames, boolean shouldCopyVariablesFromProperties, String propertyNames, boolean shouldCopyCamelBodyToBodyAsString) {
		List<String> headerList = null;
		if (headerNames != null) {
			headerList = Arrays.asList(headerNames.split("\\s*,\\s*"));
		}
		List<String> propertyList = null;
		if (propertyNames != null) {
			propertyList = Arrays.asList(propertyNames.split("\\s*,\\s*"));
		}
		return prepareVariables(exchange, shouldCopyVariablesFromHeader, headerList, shouldCopyVariablesFromProperties, propertyList, shouldCopyCamelBodyToBodyAsString);
	}

	public static Map<String, Object> prepareVariables(Exchange exchange, boolean shouldCopyVariablesFromHeader, List<String> headerList, boolean shouldCopyVariablesFromProperties, List<String> propertyList, boolean shouldCopyCamelBodyToBodyAsString) {
		Map<String, Object> camelVarMap = null;
		if (shouldCopyVariablesFromProperties) {
			camelVarMap = exchange.getProperties();
			Map<String, Object> newCamelVarMap = new HashMap<String, Object>();
			for (String s : camelVarMap.keySet()) {
				if (IGNORE_MESSAGE_PROPERTY.equalsIgnoreCase(s) == false) {
					if (propertyList == null || propertyList.indexOf(s) > -1) {
						newCamelVarMap.put(s, camelVarMap.get(s));
					}
				}
			}
			camelVarMap = newCamelVarMap;
		}
		if (true) {
			if (camelVarMap == null) {
				camelVarMap = new HashMap<String, Object>();
			}
			Object camelBody = null;
			if (exchange.hasOut())
				camelBody = exchange.getOut().getBody();
			else
				camelBody = exchange.getIn().getBody();
			if (camelBody instanceof Map<?, ?>) {
				Map<?, ?> camelBodyMap = (Map<?, ?>) camelBody;
				for (@SuppressWarnings("rawtypes")
				Map.Entry e : camelBodyMap.entrySet()) {
					if (e.getKey() instanceof String) {
						camelVarMap.put((String) e.getKey(), e.getValue());
					}
				}
			} else {
				if (shouldCopyCamelBodyToBodyAsString && !(camelBody instanceof String)) {
					camelBody = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, camelBody);
				}
				camelVarMap.put(CAMELBODY, camelBody);
			}
			if (shouldCopyVariablesFromHeader) {
				for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
					String key = header.getKey();
					if (headerList == null || headerList.indexOf(key) > -1) {
						camelVarMap.put(header.getKey(), header.getValue());
					}
				}
			}
		}
		return camelVarMap;
	}

	public static String prepareBody(Exchange exchange) {
		Map<String, Object> camelBodyMap = new HashMap<String, Object>();
		Object camelBody = null;
		if (exchange.hasOut()) {
			camelBody = exchange.getOut().getBody();
		} else {
			camelBody = exchange.getIn().getBody();
		}
		if (camelBody != null && !(camelBody instanceof String)) {
			camelBody = m_js.deepSerialize(camelBody);
		}
		return (String) camelBody;
	}

	public static String evaluate(String expr, Exchange exchange) {
		return GroovyExpression.evaluate(expr, exchange);
	}

	public static <T> T evaluateExpr(String expr, Exchange exchange, Class<T> type) {
		return GroovyExpression.evaluate(expr, exchange, type);
	}

	public static <T> T getParameter(String expr, Exchange exchange, Class<T> type) {
		return getParameter(expr, exchange, type, null);
	}

	public static <T> T getParameter(String expr, Exchange exchange, Class<T> type, String name) {
		T value = null;
		if (!isEmpty(expr)) {
			value = type.cast(evaluateExpr(expr, exchange, type));
		}
		if (ObjectHelper.isEmpty(value) && !isEmpty(name)) {
			throw new RuntimeException("ExchangeUtils.getParameter(" + name + "):" + expr + " evaluates to empty");
		}
		return value;
	}

	public static <T> T getSource(String expr, Exchange exchange, Class<T> type) {
		if (isEmpty(expr)) {
			expr = "body";
		}
		return type.cast(evaluateExpr(expr, exchange, type));
	}

	public static void setDestination(String expr, Object result, Exchange exchange) {
		if (isEmpty(expr)) {
			expr = "body";
		}
		if (expr.equals("body")) {
			exchange.getIn().setBody(result);
		} else if (expr.equals("b")) {
			exchange.getIn().setBody(result);
		} else if (expr.startsWith("header.")) {
			exchange.getIn().setHeader(expr.substring(7), result);
		} else if (expr.startsWith("headers.")) {
			exchange.getIn().setHeader(expr.substring(8), result);
		} else if (expr.startsWith("h.")) {
			exchange.getIn().setHeader(expr.substring(2), result);
		} else if (expr.startsWith("property.")) {
			exchange.setProperty(expr.substring(9), result);
		} else if (expr.startsWith("properties.")) {
			exchange.setProperty(expr.substring(10), result);
		} else if (expr.startsWith("p.")) {
			exchange.setProperty(expr.substring(2), result);
		} else {
			exchange.getIn().setHeader(expr, result);
		}
	}

}

