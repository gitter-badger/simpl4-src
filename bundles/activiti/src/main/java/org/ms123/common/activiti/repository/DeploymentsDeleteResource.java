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
package org.ms123.common.activiti.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.activiti.BaseResource;
import org.ms123.common.activiti.Util;

/**
 */
@SuppressWarnings("unchecked")
public class DeploymentsDeleteResource extends BaseResource {
	List<String> m_deploymentIds;
	boolean m_cascade;

	public DeploymentsDeleteResource(ActivitiService as, List<String> deploymentIds, boolean cascade) {
		super(as, null);
		m_deploymentIds = deploymentIds;
		m_cascade = cascade;
	}

	public Map execute() {
    try {
      Boolean cascade = m_cascade;
      for (String deploymentId : m_deploymentIds) {
        if (cascade) {
          getPE().getRepositoryService().deleteDeployment(deploymentId, true);
        }
        else {
          getPE().getRepositoryService().deleteDeployment(deploymentId);
        }
      }
			Map successNode = new HashMap();
			successNode.put("success", true);
			return successNode;
    } catch(Exception e) {
      throw new RuntimeException("Failed to delete deployments", e);
    }
	}
}
