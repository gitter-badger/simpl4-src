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

