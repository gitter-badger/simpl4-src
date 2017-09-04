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
package org.ms123.common.process.camel;

import flexjson.*;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.Processor;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Dictionary;
import java.util.Hashtable;
import org.ms123.common.system.thread.ThreadContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import static org.ms123.common.process.listener.Topics.*;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.warn;

@SuppressWarnings({ "unchecked", "deprecation" })
public class ProcessConsumer extends DefaultConsumer implements EventHandler {

	private JSONDeserializer ds = new JSONDeserializer();
	private JSONSerializer ser = new JSONSerializer();
	private ProcessEndpoint endpoint;
	private ServiceRegistration serviceRegistration;
	private String tenant;
	private String wantedEvents[];

	public ProcessConsumer(ProcessEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		String _events = this.endpoint.getEvents();
		if (_events != null && _events.trim().length() > 0) {
			info(this, "_Events:" + _events);
			this.wantedEvents = _events.split(",");
		}
	}

	public boolean isFailOnException() {
		return false;
	}

	public void handleEvent(Event event) {
		String eventName = (String) event.getProperty("eventName");
		String topic = event.getTopic();
		info(this, "HandleEvent.onEvent("+topic+","+eventName+")");

		if( !isEventWanted( topic, eventName)){
			info(this,"Event("+topic+","+eventName+") not wanted");
			return;
		}
		info(this,"Event("+topic+","+eventName+") wanted");

		Map<String,Object> properties = getPropertyMap( event );

		info(this, "onEvent.properties:");
		for( String key : properties.keySet()){
			info(this, " - " + key + ": " + properties.get(key));
		}
		final Exchange exchange = endpoint.createExchange(ExchangePattern.InOnly);
		exchange.getIn().setBody(properties);
		try {
			getAsyncProcessor().process(exchange, new AsyncCallback() {
				@Override
				public void done(boolean doneSync) {
				}
			});
		} catch (Exception e) {
			getExceptionHandler().handleException("ProcessConsumer.Error processing HandleEvent("+topic+","+eventName+")", exchange, e);
		}

	}

	@Override
	protected void doStart() throws Exception {
		this.tenant = ThreadContext.getThreadContext().getUserName();
		Set<String> topicList = new HashSet<String>();
		for( String we : this.wantedEvents){
			info(this,"wantedEvent:"+we);
			if (we.startsWith("task_")) {
				topicList.add( TASK_EVENT_TOPIC + "/" +this.tenant);
			} else if (we.startsWith("activity_")) {
				topicList.add( EXECUTION_EVENT_TOPIC + "/" +this.tenant);
			} else if (we.startsWith("process_")) {
				topicList.add( PROCESS_EVENT_TOPIC + "/" +this.tenant);
			} else if (we.startsWith("variable_")) {
				topicList.add( VARIABLE_EVENT_TOPIC + "/" +this.tenant);
			}
		}

		info(this, "Registering EventHandler handler fore tenant("+this.tenant+"):"+topicList);
		String[] topics = topicList.toArray(new String[topicList.size()]);
		Dictionary dict = new Hashtable();
		dict.put(EventConstants.EVENT_TOPIC, topics);
		this.serviceRegistration = this.endpoint.getBundleContext().registerService(EventHandler.class.getName(), this, dict);
		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		info(this, "Unregistering EventHandler handler fore tenant:" + this.tenant);
		this.serviceRegistration.unregister();
		super.doStop();
	}

	private boolean isEventWanted( String topic, String eventName){
		for( String we : this.wantedEvents){
			int cmpLen = 5;
			String to = firstToUpper(we.split( "_")[0]);
			if( to.equals("Activity")){
				to = "Execution";
			}
			String en = we.split( "_")[1];
			if( (to.equals("Process") || to.equals("Execution")) && en.equals("completed")){
				en = "end";
				cmpLen = 3;
			}
			if( topic.indexOf(to)>0 && en.regionMatches(0, eventName, 0, cmpLen) ){
				return true;
			}
		}
		return false;
	}

	private List<String> propertyExcludeList = new ArrayList<>(Arrays.asList("replacedParent", "scopeActivityInstanceId", "eventScope","skipCustomListeners", "processInstanceStartContext", "activityInstanceState", "preserveScope", "completeScope", "skipIoMappings", "scope", "cachedEntityStateRaw", "executingScopeLeafActivity", "listenerIndex","deleteRoot", "sequenceCounter", "replacedByParent", "concurrent", "cachedEntityState", "revision","event.topics","variablesLocal", "hCode", "persistentState", "variableScopeKey", "suspensionState", "variables", "suspended", "deleted", "tenantId", "revisionNext"));
	private Map<String,Object> getPropertyMap( Event event){
		String topic = event.getTopic();
		String[] propertyNames = event.getPropertyNames();
		Map<String,Object> properties = new HashMap<String,Object>();
		for (String name : propertyNames) {
			if( propertyExcludeList.contains(name)){
				continue;
			}
			properties.put( name, event.getProperty(name));
		}
		return properties;
	}

	private boolean isEmpty(String s) {
		return (s == null || "".equals(s.trim()));
	}

	protected String firstToUpper(String s) {
		char c[] = s.toCharArray();
		c[0] = Character.toUpperCase(c[0]);
		return new String(c);
	}
	private String getVariableType(String s) {
		int lastIndexDot = s.lastIndexOf(".");
		int lastIndexAt = s.lastIndexOf("@");
		if (lastIndexAt == -1) {
			lastIndexDot = s.length();
		}
		return s.substring(lastIndexDot + 1, lastIndexAt);
	}
}

