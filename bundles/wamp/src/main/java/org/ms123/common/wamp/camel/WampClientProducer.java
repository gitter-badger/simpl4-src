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
package org.ms123.common.wamp.camel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.InvalidPayloadRuntimeException;
import org.apache.camel.Message;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.MessageHelper;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.ms123.common.camel.api.ExchangeUtils;
import org.ms123.common.wamp.camel.WampClientConstants.*;
import org.ms123.common.wamp.WampClientSession;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.ms123.common.wamp.camel.WampClientConstants.*;

@SuppressWarnings("unchecked")
public class WampClientProducer extends DefaultAsyncProducer {

	private WampClientSession clientSession;
	private ObjectMapper objectMapper = new ObjectMapper();

	public WampClientProducer(WampClientEndpoint endpoint) {
		super(endpoint);
	}

	@Override
	public WampClientEndpoint getEndpoint() {
		return (WampClientEndpoint) super.getEndpoint();
	}

	@Override
	public boolean process(Exchange exchange, AsyncCallback callback) {
		String namespace = getEndpoint().getCamelContext().getName().split("/")[0];
		Map<String, Object> vars = getCamelVariablenMap(exchange);
		String topic = trimToEmpty(eval(getEndpoint().getTopic(), vars));
		info(this,"process.topic:" + topic);
		this.clientSession.publish(topic,null,buildResponse(getPublishData(exchange)));
		return true;
	}

	protected void doStart() throws Exception {
		String namespace = getEndpoint().getCamelContext().getName().split("/")[0];
		super.doStart();
		this.clientSession = getEndpoint().createWampClientSession(org.ms123.common.wamp.WampServiceImpl.DEFAULT_REALM);
	}

	protected void doStop() throws Exception {
		String namespace = getEndpoint().getCamelContext().getName().split("/")[0];
		this.clientSession.close();
		super.doStop();
	}
	private ObjectNode buildResponse(final Object methodResult) {
		ObjectNode node = null;
		if( methodResult instanceof Map ){
			node = this.objectMapper.valueToTree(methodResult);
		}else{
			node = this.objectMapper.createObjectNode();
			node.putPOJO("result", methodResult);
		}
		return node;
	}

	private Object getPublishData(Exchange exchange) {
		String publishSpec = getEndpoint().getPublish();
		List<String> publishHeaderList = getEndpoint().getPublishHeaderList();
		Object camelBody = ExchangeHelper.extractResultBody(exchange, null);
		if ("body".equals(publishSpec)) {
			return ExchangeHelper.extractResultBody(exchange, null);
		} else if ("headers".equals(publishSpec)) {
			Map<String, Object> camelVarMap = new HashMap();
			for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
				if (publishHeaderList.size() == 0 || publishHeaderList.contains(header.getKey())) {
					camelVarMap.put(header.getKey(), header.getValue());
				}
			}
			return camelVarMap;
		} else if ("bodyAndHeaders".equals(publishSpec)) {
			Map<String, Object> camelVarMap = new HashMap();
			if (camelBody instanceof Map<?, ?>) {
				Map<?, ?> camelBodyMap = (Map<?, ?>) camelBody;
				for (@SuppressWarnings("rawtypes")
				Map.Entry e : camelBodyMap.entrySet()) {
					if (e.getKey() instanceof String) {
						camelVarMap.put((String) e.getKey(), e.getValue());
					}
				}
			} else {
				camelVarMap.put("body", camelBody);
			}
			for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
				if (publishHeaderList.size() == 0 || publishHeaderList.contains(header.getKey())) {
					camelVarMap.put(header.getKey(), header.getValue());
				}
			}
			return camelVarMap;
		}
		return null;
	}
	private GroovyClassLoader groovyClassLoader = null;
	private Map<String,Script> scriptCache = new HashMap();
	private Script parse(String expr) {
		if( groovyClassLoader == null){
			ClassLoader parentLoader = this.getClass().getClassLoader();
			groovyClassLoader =   new GroovyClassLoader(parentLoader,new CompilerConfiguration());
		}
		try{
			GroovyCodeSource gcs = new GroovyCodeSource( expr, "script", "groovy/shell");
			return InvokerHelper.createScript(groovyClassLoader.parseClass(gcs,false), new Binding());
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("ActivitiProducer.parse:"+e.getMessage()+" -> "+ expr);
		}
	}

	private String eval2(String expr, Map<String,Object> vars) {
		info(this, "--> eval_in:" + expr+",vars:"+vars);
		Object result = expr;
		Script script = scriptCache.get(expr);
		if( script == null){
			script = parse(expr);
			scriptCache.put(expr, script);
		}
		script.setBinding(new Binding(vars));
		try{
			result = script.run();
		}catch(Exception e){
			String error = org.ms123.common.utils.Utils.formatGroovyException(e, expr);
			info(this, "ActivitiProducer.eval:"+error);
		}
		info(this, "<-- eval_out:" + result);
		return String.valueOf(result);
	}

	private String eval(String str, Map<String,Object> scope) {
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
			} else if (str.charAt(i) == '}' && openBrackets > 0) {
				openBrackets -= 1;
				if (openBrackets == 0) {
					countRepl++;
					replacement = eval2(str.substring(first, i), scope);
					newString += replacement;
				}
			} else if (openBrackets == 0) {
				newString += str.charAt(i);
				countPlainStr++;
			}
		}
		if (countRepl == 1 && countPlainStr == 0) {
			return String.valueOf(replacement);
		} else {
			return newString;
		}
	}

	private Map getCamelVariablenMap(Exchange exchange) {
		Map camelMap = new HashMap();
		Map exVars = ExchangeUtils.prepareVariables(exchange, true, true, true);
		camelMap.putAll(exVars);
		return camelMap;
	}
}
