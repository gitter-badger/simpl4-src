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
package org.ms123.common.camel.components.activiti;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.Processor;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.warn;

@SuppressWarnings({"unchecked","deprecation"})
public class ActivitiConsumer extends DefaultConsumer implements ActivitiEventListener {

	private final ActivitiEndpoint endpoint;

	public ActivitiConsumer(ActivitiEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	public boolean isFailOnException() {
		return false;
	}

	public void onEvent(ActivitiEvent event){
		final boolean reply = false;
		final Exchange exchange = endpoint.createExchange(reply ? ExchangePattern.InOut : ExchangePattern.InOnly);
		Map result = new HashMap();
		result.put( "executionId", event.getExecutionId());
		result.put( "processInstanceId", event.getProcessInstanceId());
		result.put( "type", event.getType().toString());

		CommandContext commandContext = Context.getCommandContext();
		ExecutionEntity ee=				commandContext.getExecutionEntityManager().findExecutionById(event.getExecutionId());
		info(this, "ActivitiConsumer.onEvent:"+ ee.getProcessDefinitionId()+"/tasks:"+ ee.getTasks()+"/aid:"+ ee.getCurrentActivityName());
		result.put( "businessKey", ee.getProcessBusinessKey());
		List<TaskEntity> tasks = ee.getTasks();
		result.put( "formKey", tasks.get(0).getFormKey());
		result.put( "taskId", tasks.get(0).getId());
		result.put( "owner", tasks.get(0).getOwner());
		result.put( "assignee", tasks.get(0).getAssignee());

		final ProcessInstanceQuery processInstanceQuery = event.getEngineServices().getRuntimeService().createProcessInstanceQuery().processInstanceId(event.getProcessInstanceId()).includeProcessVariables();
		final ProcessInstance processInstance = processInstanceQuery.singleResult();

		if (processInstance == null) {
			warn(this, "Unable to retrieve the process instance for which a user task starts.");
		} else {
			String variableNames = this.endpoint.getVariableNames();
			if( isEmpty(variableNames)){
				result.put("processVariables", processInstance.getProcessVariables());
			}else{
				List<String> nameList = Arrays.asList(variableNames.split(","));
				Map<String,Object> vars = processInstance.getProcessVariables();
				Map<String,Object> varMap = new HashMap();
				for (Map.Entry<String, Object> entry : vars.entrySet()) {
					if( nameList.indexOf( entry.getKey()) > -1){
						varMap.put( entry.getKey(), entry.getValue());
					}
				}
				result.put("processVariables", varMap );
			}
		}

		info(this, "ActivitiConsumer.onEvent.result:"+ result);
		exchange.getIn().setBody(result);
		try {
			getAsyncProcessor().process(exchange, new AsyncCallback() {
				@Override
				public void done(boolean doneSync) {
				}
			});
		} catch (Exception e) {
			getExceptionHandler().handleException("Error processing ActivitiEvent: " + event, exchange, e);
		}
	}
  @Override
	protected void doStart() throws Exception {
		info(this, "Add EventListener");
		RuntimeService rs = this.endpoint.getRuntimeService();
		rs.addEventListener( this, ActivitiEventType.TASK_CREATED);
		super.doStart();
	}

  @Override
	protected void doStop() throws Exception {
		RuntimeService rs = this.endpoint.getRuntimeService();
		info(this, "Remove EventListener");
		rs.removeEventListener( this);
		super.doStop();
	}
	private boolean isEmpty(String s) {
		return (s == null || "".equals(s.trim()));
	}
}
