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
package org.ms123.common.data.dupcheck;

import org.ms123.common.data.api.SessionContext;
import java.util.Map;
import java.util.List;
import org.ms123.common.store.StoreDesc;

public interface DublettenCheckService {

	public DupCheckContext getContext();
	public DupCheckContext getContext(SessionContext sc, String entityName);
	public DupCheckContext getContext(SessionContext sc, String entityName, String idField);
	public Map dublettenCheck(DupCheckContext dcc, Object dataObject);

	public Map dublettenCheck(DupCheckContext dcc, Map dataMap);

	public Map dublettenCheck(DupCheckContext dcc, List<Map> dataList);

	public void dublettenCheckOne(DupCheckContext dcc, List<Map> compareList, Map dataMap);
	public boolean compare(DupCheckContext dcc, String valueInput, String valueCompareTo,String[] algos);
}
