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
package org.ms123.common.process.listener;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

import org.camunda.bpm.engine.impl.cfg.orientdb.VariableListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.info;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class OSGiVariableEventDistributor extends BaseListener implements VariableListener, Serializable {

	private static final long serialVersionUID = -3778622638807349820L;

	private EventAdmin eventAdmin;
	private String tenant;

	public OSGiVariableEventDistributor(EventAdmin eventAdmin, String tenant) {
		this.eventAdmin = eventAdmin;
		this.tenant = tenant;
	}

	@Override
	public void notify(Map<String,Object> properties) {
		properties.put("eventName", properties.remove("eventType"));
		info(this,"OSGiVariableEventDistributor.notify:"+properties);
		Event event = new Event(Topics.VARIABLE_EVENT_TOPIC + "/" + this.tenant, properties);
		eventAdmin.postEvent(event);
	}
}

