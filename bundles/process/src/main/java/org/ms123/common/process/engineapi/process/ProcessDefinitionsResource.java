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
package org.ms123.common.process.engineapi.process;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.process.engineapi.BaseResource;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryProperty;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.query.QueryProperty;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.ms123.common.process.Constants;
import org.apache.commons.lang3.StringUtils;
import static com.jcabi.log.Logger.info;

/**
 */
public class ProcessDefinitionsResource extends BaseResource implements Constants{

	private String m_startableByUser;
	private String m_startableByGroup;
	private String m_name;
	private String m_namespace;
	private String m_key;
	private Integer m_version;

	private Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();

	public ProcessDefinitionsResource(ProcessService as, Map<String, Object> listParams, String namespace, String key, String name, Integer version,String startableByUser,String startableByGroup) {
		super(as, listParams);
		m_startableByUser = startableByUser;
		m_startableByGroup = startableByGroup;
		m_name = name;
		m_namespace = namespace;
		m_key = key;
		m_version = version;
		properties.put("id", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
		properties.put("key", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
		properties.put("version", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
		properties.put("deploymentId", ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
		properties.put("name", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
	}

	public Map getProcessDefinitions() {
		ProcessDefinitionQuery query = getPE().getRepositoryService().createProcessDefinitionQuery();
		if( m_name != null){
			query = query.processDefinitionName(this.m_name);
		}
		if( m_key != null){
			query = query.processDefinitionKey(this.m_key);
		}else{
			query = query.processDefinitionKeyLike(this.m_namespace+NAMESPACE_DELIMITER+"%");
		}
		if( m_version != null){
			if( m_version != -1){
				query = query.processDefinitionVersion(this.m_version);
			}else{
				query = query.latestVersion();
			}
		}
		if (StringUtils.isNotEmpty(m_startableByUser)) {
			query = query.startableByUser(m_startableByUser);
		}
		if( m_key == null){
			query = query.orderByProcessDefinitionKey();
		}
		if( m_version == null){
			query = query.orderByProcessDefinitionVersion();
		}
		Map response = new ProcessDefinitionsPaginateList(this,m_startableByGroup).paginateList(m_listParams, query, "id", properties);
		return response;
	}
}
