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
package org.ms123.common.workflow.api;
import org.activiti.engine.ProcessEngine;
import javax.servlet.http.*;
import org.apache.camel.CamelContext;
import java.util.Map;
import org.activiti.engine.delegate.VariableScope;

public interface WorkflowService {
	public static final String WORKFLOW_SERVICE = "workflowService";
	public static final String PROCESS_ENGINE = "processEngine";
	public static final String PROCESS_TYPE = "sw.process";
	public static final String DIRECTORY_TYPE = "sw.directory";

	public static final String WORKFLOW_ACTIVITY_ID = "WorkflowActivityId";
	public static final String WORKFLOW_ACTIVITY_NAME = "WorkflowActivityName";
	public static final String WORKFLOW_EXECUTION_ID = "WorkflowExecutionId";
	public static final String WORKFLOW_PROCESS_BUSINESS_KEY = "WorkflowProcessBusinessKey";
	public static final String WORKFLOW_PROCESS_DEFINITION_ID = "WorkflowProcessDefinitionId";
	public static final String WORKFLOW_PROCESS_DEFINITION_NAME = "WorkflowProcessDefinitionName";
	public static final String WORKFLOW_PROCESS_INSTANCE_ID = "WorkflowProcessInstanceId";

	public ProcessEngine getProcessEngine();
	public CamelContext getCamelContextForProcess(String namespace, String name);
	public void executeScriptTask( String executionId, String category, String processDefinitionKey, String pid, String script, Map newVariables, String taskName );
	public Object  lookupServiceByName( String name);
	public void deployAll();
	public void deployNamespace(String namespace);
}
