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
