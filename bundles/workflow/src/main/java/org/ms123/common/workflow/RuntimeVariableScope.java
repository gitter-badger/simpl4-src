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
package org.ms123.common.workflow;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.RuntimeService;

public class RuntimeVariableScope implements VariableScope {
	private RuntimeService m_runtimeService;
	private String m_executionId;

	public RuntimeVariableScope(RuntimeService rs, String executionId) {
		m_runtimeService = rs;
		m_executionId = executionId;
	}

	@Override
	public Map<String, Object> getVariables(Collection<String> variableNames) {
		throw new RuntimeException("RuntimeVariableScope.getVariables(Collection) not implemented");
	}

	@Override
	public Map<String, Object> getVariables(Collection<String> variableNames, boolean fetchAllVariables) {
		throw new RuntimeException("RuntimeVariableScope.getVariables(Collection) not implemented");
	}

	@Override
	public Map<String, Object> getVariablesLocal(Collection<String> variableNames) {
		throw new RuntimeException("RuntimeVariableScope.getVariablesLocal(Collection) not implemented");
	}

	@Override
	public Map<String, Object> getVariablesLocal(Collection<String> variableNames, boolean fetchAllVariables) {
		throw new RuntimeException("RuntimeVariableScope.getVariablesLocal(Collection) not implemented");
	}

	public Map<String, Object> getVariables() {
		return m_runtimeService.getVariables(m_executionId);
	}

	public Map<String, Object> getVariablesLocal() {
		return m_runtimeService.getVariablesLocal(m_executionId);
	}

	@Override
	public Object getVariableLocal(String variableName, boolean fetchAllVariables) {
		return m_runtimeService.getVariableLocal(m_executionId, variableName); // No support for fetchAllVariables
	}

	public Object getVariable(String variableName) {
		return m_runtimeService.getVariable(m_executionId, variableName);
	}

	public Object getVariable(String variableName, boolean b) {
		return m_runtimeService.getVariable(m_executionId, variableName);
	}

	@Override
	public <T> T getVariable(String variableName, Class<T> variableClass) {
		return variableClass.cast(m_runtimeService.getVariable(m_executionId, (String) variableName));
	}

	public Object getVariableLocal(String variableName) {
		return m_runtimeService.getVariableLocal(m_executionId, (String) variableName);
	}

	@Override
	public <T> T getVariableLocal(String variableName, Class<T> variableClass) {
		return variableClass.cast(m_runtimeService.getVariableLocal(m_executionId, (String) variableName));
	}

	public Set<String> getVariableNames() {
		return getVariables().keySet();
	}

	public Set<String> getVariableNamesLocal() {
		return getVariablesLocal().keySet();
	}

	public void setVariable(String variableName, Object value) {
		m_runtimeService.setVariable(m_executionId, variableName, value);
	}

	public void setVariable(String variableName, Object value, boolean b) {
		m_runtimeService.setVariable(m_executionId, variableName, value);
	}

	public Object setVariableLocal(String variableName, Object value) {
		m_runtimeService.setVariableLocal(m_executionId, variableName, value);
		return null;
	}

	public Object setVariableLocal(String variableName, Object value, boolean b) {
		m_runtimeService.setVariableLocal(m_executionId, variableName, value);
		return null;
	}

	public void setVariables(Map<String, ? extends Object> variables) {
		m_runtimeService.setVariables(m_executionId, variables);
	}

	public void setVariablesLocal(Map<String, ? extends Object> variables) {
		m_runtimeService.setVariablesLocal(m_executionId, variables);
	}

	public boolean hasVariables() {
		throw new RuntimeException("RuntimeVariableScope.hasVariables_not_implemented");
	}

	public boolean hasVariablesLocal() {
		throw new RuntimeException("RuntimeVariableScope.hasVariablesLocal_not_implemented");
	}

	public boolean hasVariable(String variableName) {
		//return m_runtimeService.hasVariable(m_executionId,variableName);
		throw new RuntimeException("RuntimeVariableScope.hasVariable_not_implemented");
	}

	public boolean hasVariableLocal(String variableName) {
		//return m_runtimeService.hasVariableLocal(m_executionId,variableName);
		throw new RuntimeException("RuntimeVariableScope.hasVariablesLocal_not_implemented");
	}

	public void createVariableLocal(String variableName, Object value) {
		throw new RuntimeException("RuntimeVariableScope.createVariablesLocal_not_implemented");
	}

	public void removeVariable(String variableName) {
		//return m_runtimeService.removeVariable(m_executionId,variableName);
		throw new RuntimeException("RuntimeVariableScope.removeVariable_not_implemented");
	}

	public void removeVariableLocal(String variableName) {
		//return m_runtimeService.removeVariableLocal(m_executionId,variableName);
		throw new RuntimeException("RuntimeVariableScope.removeVariableLocal_not_implemented");
	}

	public void removeVariables(Collection<String> variableNames) {
		//return m_runtimeService.removeVariables(m_executionId,variableNames);
		throw new RuntimeException("RuntimeVariableScope.removeVariables_not_implemented");
	}

	public void removeVariablesLocal(Collection<String> variableNames) {
		//return m_runtimeService.removeVariablesLocal(m_executionId,variableNames);
		throw new RuntimeException("RuntimeVariableScope.removeVariablesLocal_not_implemented");
	}

	public void removeVariables() {
		throw new RuntimeException("RuntimeVariableScope.removeVariables_not_implemented");
	}

	public void removeVariablesLocal() {
		throw new RuntimeException("RuntimeVariableScope.removeVariablesLocal_not_implemented");
	}

}

