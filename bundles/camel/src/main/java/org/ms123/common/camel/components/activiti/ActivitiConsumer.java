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
package org.ms123.common.camel.components.activiti;

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
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiErrorEvent;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;
import org.activiti.engine.delegate.event.ActivitiSequenceFlowTakenEvent;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.task.IdentityLink;
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

	private JSONDeserializer ds = new JSONDeserializer();
	private JSONSerializer ser = new JSONSerializer();
	private final ActivitiEndpoint endpoint;
	ActivitiEventType[]  events = null;

	public ActivitiConsumer(ActivitiEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		String _events = endpoint.getEvents();
		if( _events != null && _events.trim().length() > 0){
			info(this,"_Events:"+_events);
			String[] arr  = _events.toUpperCase().split(",");
			this.events = new ActivitiEventType[arr.length];
			int i=0;
			for( String elem : arr ){
				this.events[i++] = ActivitiEventType.valueOf( elem );
			}
		}
			
		info(this,"Events:"+this.events);
	}

	public boolean isFailOnException() {
		return false;
	}

	public void onEvent(ActivitiEvent event){
		info(this, "onEvent:"+event);
		info(this, "onEvent:"+event.getClass());
		final boolean reply = false;
		ActivitiEventType type = event.getType();
		Map result = new HashMap();
		result.put( "executionId", event.getExecutionId());
		result.put( "processInstanceId", event.getProcessInstanceId());
		result.put( "type", event.getType().toString());

		CommandContext commandContext = Context.getCommandContext();
		ExecutionEntity ee=				commandContext.getExecutionEntityManager().findExecutionById(event.getExecutionId());
		info(this, "ActivitiConsumer.onEvent:"+ ee.getProcessDefinitionId()+"/tasks:"+ ee.getTasks()+"/aid:"+ ee.getCurrentActivityName());
		result.put( "businessKey", ee.getProcessBusinessKey());

		if( event instanceof ActivitiEntityEvent ){
			List<TaskEntity> tasks = ee.getTasks();
			if( tasks != null && tasks.size() > 0 ){
				result.put( "formKey", tasks.get(0).getFormKey());
				result.put( "taskId", tasks.get(0).getId());
				result.put( "owner", tasks.get(0).getOwner());
				String assignee = tasks.get(0).getAssignee();
				result.put( "assignee", assignee );
				List<String> candidates = new ArrayList<String>();
				Set<IdentityLink> iLinks = tasks.get(0).getCandidates();
				for( IdentityLink il : iLinks ){
					if( il.getUserId() != null){
						candidates.add( il.getUserId());
					}else if( il.getGroupId() != null){
						String[] g = il.getGroupId().split("\\.");
						if( g.length == 1){
							candidates.add( g[0] );
						}else{
							candidates.add( g[1]);
						}
					}
				}
				if( !isEmpty(assignee)){
					candidates.add( assignee );
				}
				result.put( "candidates", "[" + String.join("|", candidates)+ "]" );
			}
		}
		if( event instanceof ActivitiActivityEvent ){
			ActivitiActivityEvent a = (ActivitiActivityEvent)event;
			result.put( "activityId", a.getActivityId());
			result.put( "activityName", a.getActivityName());
			result.put( "activityType", a.getActivityType());
		}
		if( event instanceof ActivitiCancelledEvent ){
			ActivitiCancelledEvent a = (ActivitiCancelledEvent)event;
			result.put( "cause", String.valueOf(a.getCause()));
		}
		if( event instanceof ActivitiErrorEvent ){
			ActivitiErrorEvent a = (ActivitiErrorEvent)event;
			result.put( "errorCode", a.getErrorCode());
		}
		if( event instanceof ActivitiMessageEvent ){
			ActivitiMessageEvent a = (ActivitiMessageEvent)event;
			result.put( "messageName", a.getMessageName());
			result.put( "messageData", ser.deepSerialize(a.getMessageData()));
		}
		if( event instanceof ActivitiSignalEvent ){
			ActivitiSignalEvent a = (ActivitiSignalEvent)event;
			result.put( "signalName", a.getSignalName());
			result.put( "signalData", ser.deepSerialize(a.getSignalData()));
		}
		if( event instanceof ActivitiVariableEvent ){
			ActivitiVariableEvent a = (ActivitiVariableEvent)event;
			result.put( "variableName", a.getVariableName());
			result.put( "variableType", getVariableType(String.valueOf(a.getVariableType())));
			result.put( "variableValue", ser.deepSerialize(a.getVariableValue()));
		}
		if( event instanceof ActivitiSequenceFlowTakenEvent ){
			ActivitiSequenceFlowTakenEvent a = (ActivitiSequenceFlowTakenEvent)event;
			result.put( "id", a.getId());
			result.put( "sourceActivityId", a.getSourceActivityId());
			result.put( "sourceActivityName", a.getSourceActivityName());
			result.put( "sourceActivityType", a.getSourceActivityType());
			result.put( "targetActivityId", a.getTargetActivityId());
			result.put( "targetActivityName", a.getTargetActivityName());
			result.put( "targetActivityType", a.getTargetActivityType());
		}

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
		final Exchange exchange = endpoint.createExchange(reply ? ExchangePattern.InOut : ExchangePattern.InOnly);
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
		if( this.events != null){
			for( ActivitiEventType a : this.events){
				info(this,"ActivitiEvent.register:"+a);
			}
			rs.addEventListener( this, this.events);
		}
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
	private String getVariableType(String s) {
		int lastIndexDot = s.lastIndexOf(".");
		int lastIndexAt = s.lastIndexOf("@");
		if( lastIndexAt == -1){
			lastIndexDot = s.length();
		}
		return s.substring(lastIndexDot+1,lastIndexAt);
	}
}
