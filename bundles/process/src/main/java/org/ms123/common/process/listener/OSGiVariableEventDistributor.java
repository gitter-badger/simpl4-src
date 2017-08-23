package org.ms123.common.process.listener;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

import org.camunda.bpm.engine.delegate.DelegateVariableInstance;
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
		Event event = new Event(Topics.VARIABLE_EVENT_TOPIC + "/" + this.tenant, properties);
		eventAdmin.postEvent(event);
	}
}

