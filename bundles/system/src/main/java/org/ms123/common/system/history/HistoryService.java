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
package org.ms123.common.system.history;
import java.util.List;
import java.util.Map;
import org.ms123.common.rpc.RpcException;

public interface HistoryService {
	public static String HISTORY_TOPIC  = 	"history";

	public static String HISTORY_TYPE = "type";
	public static String HISTORY_KEY = "key";
	public static String HISTORY_HINT = "hint";
	public static String HISTORY_MSG = "msg";
	public static String HISTORY_TIME = "time";

	public static String HISTORY_ACTIVITI_START_PROCESS_EXCEPTION = "activiti/startprocess/exception";
	public static String HISTORY_ACTIVITI_JOB_EXCEPTION = 	"activiti/job/exception";
	public static String HISTORY_ACTIVITI_STARTPROCESS_EXCEPTION = 	"activiti/startprocess/exception";
	public static String HISTORY_CAMEL_HISTORY = 	"camel/history";
	public static String HISTORY_CAMEL_TRACE = 	"camel/trace";


	public static String ACTIVITI_CAMEL_CORRELATION_TYPE = "activitiCamelCorrelation";
	public static String ACC_ACTIVITI_ID = "activitiId";
	public static String ACC_ROUTE_INSTANCE_ID = "routeInstanceId";

	public static String HISTORY_ACTIVITI_PROCESS_KEY = "HistoryActivitiProcessKey";
	public static String HISTORY_ACTIVITI_ACTIVITY_KEY = "HistoryActivitiActivityKey";
	public static String CAMEL_ROUTE_DEFINITION_KEY = "CamelRouteDefinitionKey";
}
