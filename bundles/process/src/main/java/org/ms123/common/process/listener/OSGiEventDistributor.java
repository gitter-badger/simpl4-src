package org.ms123.common.process.listener;

import java.io.Serializable;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

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
@SuppressWarnings({ "unchecked", "deprecation" })
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
		Map<String, Object> properties = new HashMap<String, Object>();
		fillDictionary(delegateTask, properties, true);
		List<String> c = getCandidates(delegateTask);
		properties.put( "candidates", "[" + String.join("|", c)+ "]" );
		properties.put("candidateList",getCandidates(delegateTask));
		properties.put("assignee",delegateTask.getAssignee());
		info(this, "OSGiEventDistributor.createTaskEvent("+this.tenant+"):" + properties);
		return new Event(Topics.TASK_EVENT_TOPIC + "/" + this.tenant, properties);
	}

	private Event createEvent(DelegateExecution execution) {
		Map<String, Object> properties = new HashMap<String, Object>();
		fillDictionary(execution, properties, false);
		info(this, "OSGiEventDistributor.createExecutionEvent("+this.tenant+"):" + properties);
		return new Event(Topics.EXECUTION_EVENT_TOPIC + "/" + this.tenant, properties);
	}

}

