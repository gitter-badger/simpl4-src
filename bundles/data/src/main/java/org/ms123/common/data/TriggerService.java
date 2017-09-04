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
package org.ms123.common.data;

import java.util.Map;
import java.util.List;
import javax.jdo.PersistenceManager;
import org.ms123.common.data.api.SessionContext;

public interface TriggerService {

	// public Map updateObject(Map dataMap, Map filterMap, Map hintsMap, String appName, String module, String pathInfo, String user); 
	public int INSERT = 0;

	public int UPDATE = 1;

	public int DELETE = 2;

	public Map applyInsertRules(SessionContext sessionContext, String entityName, Object insert) throws Exception;

	public Map applyUpdateRules(SessionContext sessionContext, String entityName, Object update,Object preUpdate) throws Exception;

	public Map applyDeleteRules(SessionContext sessionContext, String entityName, Object delete) throws Exception;
}
