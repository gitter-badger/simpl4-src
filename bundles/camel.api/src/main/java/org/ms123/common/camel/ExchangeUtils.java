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
package org.ms123.common.camel.api;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import org.apache.camel.Exchange;
import org.apache.camel.CamelContext;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNumericSpace;
import static org.apache.commons.lang3.StringUtils.isAlphanumericSpace;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import org.apache.camel.util.ObjectHelper;
import flexjson.*;
import static com.jcabi.log.Logger.info;

/**
 * 
 */
@SuppressWarnings({"unchecked"})
public class ExchangeUtils {
	public static final String CAMELBODY = "camelBody";
	private static JSONSerializer m_js = new JSONSerializer();
	protected static final String IGNORE_MESSAGE_PROPERTY = "CamelMessageHistory";

	public static final Map<String, Class> assignmentTypes = new HashMap<String, Class>() {
		{
			put("string", java.lang.String.class);
			put("integer", java.lang.Integer.class);
			put("long", java.lang.Long.class);
			put("double", java.lang.Double.class);
			put("date", java.util.Date.class);
			put("boolean", java.lang.Boolean.class);
			put("map", java.util.Map.class);
			put("list", java.util.List.class);
			put("any", java.lang.Object.class);
		}
	};

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
	public static <T> T evaluateExpr(String expr, Map<String,Object> varMap, CamelContext context,Class<T> type) {
		return GroovyExpression.evaluate(expr, varMap, context,type);
	}

	public static <T> T getParameter(String expr, Exchange exchange, Class<T> type) {
		return getParameter(expr, exchange, type, null);
	}

	public static <T> T getParameter(String expr, Exchange exchange, Class<T> type, String name) {
		T value = null;
		if (!isEmpty(expr)) {
			try{
				expr = evaluate(expr,exchange);
				value = type.cast(evaluateExpr(expr, exchange, type));
			}catch(Throwable e){
				if( type.equals( String.class )){
					info( ExchangeUtils.class, "\tExpr1("+expr+") casted to:\""+ type.cast(expr)+"\"");
					return type.cast(expr);
				}
				if( true ){
					info( ExchangeUtils.class, "\tExpr2("+expr+") casted to:\""+ type.cast(expr)+"\"");
					return type.cast(expr);
				}
				info( ExchangeUtils.class, "getParameter("+expr+","+type+"):"+ e.getMessage());
				throw e;
			}
		}
		if (ObjectHelper.isEmpty(value) && !isEmpty(name)) {
			throw new RuntimeException("ExchangeUtils.getParameter(" + name + "):" + expr + " evaluates to empty");
		}
		return value;
	}

	public static <T> T getSource(String expr, Exchange exchange, Class<T> type) {
		T value = null;
		if (isEmpty(expr)) {
			expr = "body";
		}
		try{
			expr = evaluate(expr,exchange);
			value = type.cast(evaluateExpr(expr, exchange, type));
		}catch(Throwable e){
			return type.cast(expr);
		}
		return value;
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

	public static Map<String,Object> getVariablesFromHeaderFields(Exchange exchange, String headerFields){
		List<String> _headerList=null;
		if( !isEmpty(headerFields)){
			_headerList = Arrays.asList(headerFields.split(","));
		}else{
			_headerList = new ArrayList();
		}
		Map<String,String> modMap = new HashMap<String,String>();
		List<String> headerList = new ArrayList<String>();
		for( String h : _headerList){
			String[]  _tmp = h.split(":");
			String key = _tmp[0];
			String mod = _tmp.length>1 ? _tmp[1] : "";
			headerList.add(key);
			modMap.put(key,mod);
		}
		Map<String,Object> variables = new HashMap();
		for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
			if( headerList.size()==0 || headerList.contains( header.getKey())){
				if( header.getValue() instanceof Map  && !"asMap".equals(modMap.get(header.getKey()))){
					variables.putAll((Map)header.getValue());
				}else{
					variables.put(header.getKey(), header.getValue());
				}
			}
		}
		return variables;
	}

	private static String makeVariableName( String expr ){
		if( expr.indexOf( ".") == -1){
			return expr;
		}
		int dot = expr.lastIndexOf(".");
		String n = expr.substring(dot+1);
		return n.replaceAll("[^\\p{IsAlphabetic}^\\p{IsDigit}]", "");
	}
	public static Map<String,Object> getAssignments(Exchange exchange, List<Map<String,String>> assignments){
		return getAssignments( exchange, assignments, false);
	}
	public static Map<String,Object> getAssignments(Exchange exchange, List<Map<String,String>> assignments, boolean local){
		Map<String,Object> lvariables = new HashMap();
		Map<String,Object> variables = new HashMap();
			info(ExchangeUtils.class,"getAssignments("+local+"):"+assignments);
		if( assignments == null){
			return variables;
		}
		for( Map<String,String>  a : assignments){
			boolean loc = toBoolean( a.get("local"));
			String expr = a.get("expr");
			Class type = assignmentTypes.get( a.get("type"));
			String variable = a.get("variable");
			if( isEmpty(variable)){
				variable = makeVariableName( expr);
			}

			Object value = ExchangeUtils.getParameter(expr,exchange, type);
			info(ExchangeUtils.class,"put("+variable+",local:"+loc+"):"+value);
			if( local && loc ){
				lvariables.put( variable, value);
			}else if( !local && !loc ){
				variables.put( variable, value);
			}
		}
		return local ? lvariables : variables;
	}
}

