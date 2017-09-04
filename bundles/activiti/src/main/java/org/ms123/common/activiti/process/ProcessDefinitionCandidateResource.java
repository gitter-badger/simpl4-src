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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import org.activiti.engine.ActivitiException;
import org.apache.commons.lang.StringUtils;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.activiti.BaseResource;
import org.ms123.common.activiti.Util;
import org.activiti.engine.task.IdentityLink;

/**
 */
public class ProcessDefinitionCandidateResource extends BaseResource {
	private List<String> m_userList;
	private List<String> m_groupList;
	private String m_processDefinitionId;

	public ProcessDefinitionCandidateResource(ActivitiService as,String processDefinitionId,List<String> userList,List<String> groupList) {
		super(as, null);
		m_processDefinitionId = processDefinitionId;
		m_userList = userList;
		m_groupList = groupList;
	}
	public void execute() {
		List<IdentityLink> links = getPE().getRepositoryService().getIdentityLinksForProcessDefinition(m_processDefinitionId);
		for( IdentityLink il : links){
			if( il.getGroupId() != null){
				getPE().getRepositoryService().deleteCandidateStarterGroup(m_processDefinitionId, il.getGroupId());
			}
			if( il.getUserId() != null){
				getPE().getRepositoryService().deleteCandidateStarterUser(m_processDefinitionId, il.getUserId());
			}
		}
		if( m_userList != null && m_userList.size()>0){
			for( String user : m_userList){
				getPE().getRepositoryService().addCandidateStarterUser(m_processDefinitionId, user);
			}
		}
		if( m_groupList != null && m_groupList.size()>0){
			for( String group : m_groupList){
				getPE().getRepositoryService().addCandidateStarterGroup(m_processDefinitionId, group);
			}
		}
		links = getPE().getRepositoryService().getIdentityLinksForProcessDefinition(m_processDefinitionId);
		for( IdentityLink il : links){
			//System.out.println("ProcessDefinitionCandidateResource(group:"+il.getGroupId()+"/user:"+il.getUserId()+")");
		}
	}
}
