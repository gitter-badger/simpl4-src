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

import java.util.Map;
import java.util.HashMap;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.ms123.common.process.ProcessServiceImpl;
import org.ms123.common.system.thread.ThreadContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.info;

public class ProcessEndExecutionListener extends BaseListener implements ExecutionListener {

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		String tenant = ThreadContext.getThreadContext().getUserName();
		Map<String, Object> properties = new HashMap<String, Object>();
		fillDictionary( execution, properties, false);
		info(this,"ProcessEndExecutionListener.createExecutionEvent("+tenant+"):"+properties);
		Event event = new Event(Topics.PROCESS_EVENT_TOPIC+"/"+tenant, properties);
		ProcessServiceImpl.getEventAdminStatic().postEvent(event);
	}
}
