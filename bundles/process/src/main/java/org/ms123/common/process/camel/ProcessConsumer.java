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
	private String events[];

	public ProcessConsumer(ProcessEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		String _events = this.endpoint.getEvents();
		if (_events != null && _events.trim().length() > 0) {
			info(this, "_Events:" + _events);
			this.events = _events.toUpperCase().split(",");
		}
	}

	public boolean isFailOnException() {
		return false;
	}

	public void handleEvent(Event event) {
		info(this, "HandleEvent.onEvent " + event);
		String[] propertyNames = event.getPropertyNames();
		for (String name : propertyNames) {
			info(this, " - " + name + ": " + event.getProperty(name));
		}
		String type = (String) event.getProperty("type");
		if (event.getTopic().startsWith(TASK_EVENT_TOPIC)) {
		} else if (event.getTopic().startsWith(EXECUTION_EVENT_TOPIC)) {
		} else if (event.getTopic().startsWith(VARIABLE_EVENT_TOPIC)) {
		} else if (event.getTopic().startsWith(PROCESS_EVENT_TOPIC)) {
		}

	}

	@Override
	protected void doStart() throws Exception {
		this.tenant = ThreadContext.getThreadContext().getUserName();
		info(this, "Registering EventHandler handler fore tenant:"+this.tenant);
		String[] topics = new String[] { TASK_EVENT_TOPIC + "/" +this.tenant, 
																			EXECUTION_EVENT_TOPIC + "/"+ this.tenant,
																			VARIABLE_EVENT_TOPIC + "/"+ this.tenant,
																			PROCESS_EVENT_TOPIC + "/"+ this.tenant 
															};
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

	private boolean isEmpty(String s) {
		return (s == null || "".equals(s.trim()));
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

