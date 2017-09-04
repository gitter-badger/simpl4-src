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
package org.ms123.common.activiti.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.activiti.engine.history.HistoricProcessInstance;
import org.ms123.common.activiti.AbstractPaginateList;
import org.ms123.common.activiti.BaseResource;
import org.ms123.common.activiti.Util;
import org.activiti.engine.ProcessEngine;

/**
 */
@SuppressWarnings("unchecked")
public class ProcessInstancesPaginateList extends AbstractPaginateList {

	private ProcessEngine m_pe;

	public ProcessInstancesPaginateList(BaseResource br) {
		m_pe = br.getPE();
	}

	protected List processList(List list) {
		List<Map> processResponseList = new ArrayList<Map>();
		for (Object instance : list) {
			Map responseMap = new HashMap();
			HistoricProcessInstance processInstance = (HistoricProcessInstance) instance;
			responseMap.put("id", processInstance.getId());
			responseMap.put("businessKey", processInstance.getBusinessKey());
			responseMap.put("startTime", Util.dateToString(processInstance.getStartTime()));
			responseMap.put("endTime", Util.dateToString(processInstance.getEndTime()));
			responseMap.put("_startTime",processInstance.getStartTime().getTime());
			if( processInstance.getEndTime()!=null){
				responseMap.put("_endTime", processInstance.getEndTime().getTime());
			}
			responseMap.put("duration", processInstance.getDurationInMillis());
			responseMap.put("processDefinitionId", processInstance.getProcessDefinitionId());
			responseMap.put("startUserId", processInstance.getStartUserId());
			processResponseList.add(responseMap);
		}
		return processResponseList;
	}
}
