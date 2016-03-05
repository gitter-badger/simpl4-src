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
import org.ms123.common.camel.components.ExchangeUtils;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.system.thread.ThreadContext;
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

	private PermissionService permissionService;
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
		if (this.permissionService == null) {
			this.permissionService = getByType(exchange.getContext(), PermissionService.class);
		}
		String namespace = getEndpoint().getCamelContext().getName().split("/")[0];
		Map<String, Object> vars = getCamelVariablenMap(exchange);
		String topic = trimToEmpty(eval(getEndpoint().getTopic(), vars));
		info(this,"process.topic:" + topic);
		List<String> permittedRoleList = getEndpoint().getPermittedRoles();
		List<String> permittedUserList = getEndpoint().getPermittedUsers();
		String userName = getUserName();
		List<String> userRoleList = getUserRoles(userName);
		debug(this,"Producer.prepare.userName:" + userName);
		debug(this,"Producer.prepare.userRoleList:" + userRoleList);
		debug(this,"Producer.prepare.permittedRoleList:" + permittedRoleList);
		debug(this,"Producer.prepare.permittedUserList:" + permittedUserList);
		if (!isPermitted(userName, userRoleList, permittedUserList, permittedRoleList)) {
			throw new RuntimeException(PERMISSION_DENIED + ":User(" + userName + ") has no permission");
		}
		this.clientSession.publish(topic,null,buildResponse(getPublishData(exchange)));
		return true;
	}

	protected void doStart() throws Exception {
		String namespace = getEndpoint().getCamelContext().getName().split("/")[0];
		super.doStart();
		this.clientSession = getEndpoint().createWampClientSession("realm1");
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
	protected String getUserName() {
		return ThreadContext.getThreadContext().getUserName();
	}

	protected boolean isPermitted(String userName, List<String> userRoleList, List<String> permittedUserList, List<String> permittedRoleList) {
		if (permittedUserList.contains(userName)) {
			return true;
		}
		for (String userRole : userRoleList) {
			if (permittedRoleList.contains(userRole)) {
				return true;
			}
		}
		return false;
	}

	protected List<String> getUserRoles(String userName) {
		List<String> userRoleList = null;
		try {
			userRoleList = this.permissionService.getUserRoles(userName);
		} catch (Exception e) {
			userRoleList = new ArrayList<>();
		}
		return userRoleList;
	}
	private <T> T getByType(CamelContext ctx, Class<T> kls) {
		return kls.cast(ctx.getRegistry().lookupByName(kls.getName()));
	}
	private Map getCamelVariablenMap(Exchange exchange) {
		Map camelMap = new HashMap();
		Map exVars = ExchangeUtils.prepareVariables(exchange, true, true, true);
		camelMap.putAll(exVars);
		return camelMap;
	}
}
