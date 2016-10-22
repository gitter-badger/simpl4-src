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

