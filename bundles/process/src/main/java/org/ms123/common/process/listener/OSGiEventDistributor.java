package org.ms123.common.process.listener;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.info;

/**
 * @author Ronny Bräunlich
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class OSGiEventDistributor extends BaseListener implements TaskListener, ExecutionListener, Serializable {

	private static final long serialVersionUID = -3778622638807349820L;

	private EventAdmin eventAdmin;
	private String tenant;

	public OSGiEventDistributor(EventAdmin eventAdmin, String tenant) {
		this.eventAdmin = eventAdmin;
		this.tenant = tenant;
	}

	@Override
	public void notify(DelegateExecution execution) {
		Event event = createEvent(execution);
		eventAdmin.postEvent(event);
	}

	@Override
	public void notify(DelegateTask delegateTask) {
		Event event = createEvent(delegateTask);
		eventAdmin.postEvent(event);
	}

	private Event createEvent(DelegateTask delegateTask) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		fillDictionary( delegateTask, properties, true);
		info(this,"OSGiEventDistributor.createTaskEvent:"+properties);
		return new Event(Topics.TASK_EVENT_TOPIC+"/"+this.tenant, properties);
	}

	private Event createEvent(DelegateExecution execution) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		fillDictionary( execution, properties, false);
		info(this,"OSGiEventDistributor.createExecutionEvent:"+properties);
		return new Event(Topics.EXECUTION_EVENT_TOPIC+"/"+this.tenant, properties);
	}

}

