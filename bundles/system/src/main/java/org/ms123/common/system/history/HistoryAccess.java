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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

/**
 *
 */
@SuppressWarnings("unchecked")
public interface HistoryAccess {
	public final String GLOBAL_KEYSPACE = "global";
	public final String HISTORY_DATABASE = "history";
	public final String STARTTIME = "startTime";
	public final String ENDTIME = "endTime";
	public final String STATUS = "status";

	public void close();
	public void upsertHistory(String key, Date time, String type, String hint, String msg);

	public void upsertAcc(String activitiId, String routeInstanceId);

	public List<Map> getHistory(String key, String type, Long startTime, Long endTime) throws Exception;

	public Set<String> getActivitiCamelCorrelation(String activitiId) throws Exception;
}

