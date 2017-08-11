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
import org.flowable.engine.common.api.delegate.event.FlowableEvent;
import org.flowable.engine.common.api.delegate.event.FlowableEventType;
import org.flowable.engine.common.api.delegate.event.FlowableEventListener;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.FlowableActivityEvent;
import org.flowable.engine.delegate.event.FlowableCancelledEvent;
import org.flowable.engine.common.api.delegate.event.FlowableEntityEvent;
import org.flowable.engine.delegate.event.FlowableErrorEvent;
import org.flowable.engine.delegate.event.FlowableMessageEvent;
import org.flowable.engine.delegate.event.FlowableSignalEvent;
import org.flowable.engine.delegate.event.FlowableVariableEvent;
import org.flowable.engine.delegate.event.FlowableSequenceFlowTakenEvent;
import org.flowable.engine.delegate.event.impl.FlowableEventImpl;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.task.IdentityLink;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.common.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.TaskEntity;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.warn;

@SuppressWarnings({"unchecked","deprecation"})
public class ActivitiConsumer extends DefaultConsumer implements FlowableEventListener {

	private JSONDeserializer ds = new JSONDeserializer();
	private JSONSerializer ser = new JSONSerializer();
	private final ActivitiEndpoint endpoint;
	FlowableEngineEventType[]  events = null;

	public ActivitiConsumer(ActivitiEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		String _events = endpoint.getEvents();
		if( _events != null && _events.trim().length() > 0){
			info(this,"_Events:"+_events);
			String[] arr  = _events.toUpperCase().split(",");
			this.events = new FlowableEngineEventType[arr.length];
			int i=0;
			for( String elem : arr ){
				this.events[i++] = FlowableEngineEventType.valueOf( elem );
			}
		}
			
		info(this,"Events:"+this.events);
	}

	public boolean isFailOnException() {
		return false;
	}

	public void onEvent(FlowableEvent event){
		info(this, "onEvent:"+event);
		info(this, "onEvent:"+event.getClass());
		final boolean reply = false;
		FlowableEventType type = event.getType();
		FlowableEventImpl eventImpl = (FlowableEventImpl)event;
		Map result = new HashMap();
		result.put( "executionId", eventImpl.getExecutionId());
		result.put( "processInstanceId", eventImpl.getProcessInstanceId());
		result.put( "type", event.getType().name());

		CommandContext commandContext = Context.getCommandContext();
		ExecutionEntity ee=				CommandContextUtil.getExecutionEntityManager().findExecutionById(eventImpl.getExecutionId());
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
