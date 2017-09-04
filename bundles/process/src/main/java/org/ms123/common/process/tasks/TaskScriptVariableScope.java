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
package org.ms123.common.process.tasks;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class TaskScriptVariableScope implements VariableScope {
	private RuntimeService m_runtimeService;
	private String m_executionId;

	public TaskScriptVariableScope(RuntimeService rs, String executionId) {
		m_runtimeService = rs;
		m_executionId = executionId;
	}


	@Override
	public Map<String, Object> getVariables() {
		return m_runtimeService.getVariables(m_executionId);
	}

	@Override
	public Map<String, Object> getVariablesLocal() {
		return m_runtimeService.getVariablesLocal(m_executionId);
	}


	@Override
	public Object getVariable(String variableName) {
		return m_runtimeService.getVariable(m_executionId, variableName);
	}


	@Override
	public Object getVariableLocal(String variableName) {
		return m_runtimeService.getVariableLocal(m_executionId, (String) variableName);
	}


	@Override
	public Set<String> getVariableNames() {
		return getVariables().keySet();
	}

	@Override
	public Set<String> getVariableNamesLocal() {
		return getVariablesLocal().keySet();
	}

	@Override
	public void setVariable(String variableName, Object value) {
		m_runtimeService.setVariable(m_executionId, variableName, value);
	}


	@Override
	public void setVariableLocal(String variableName, Object value) {
		m_runtimeService.setVariableLocal(m_executionId, variableName, value);
	}

	@Override
	public void setVariables(Map<String, ? extends Object> variables) {
		m_runtimeService.setVariables(m_executionId, variables);
	}

	@Override
	public void setVariablesLocal(Map<String, ? extends Object> variables) {
		m_runtimeService.setVariablesLocal(m_executionId, variables);
	}

	@Override
	public String getVariableScopeKey() {
		throw new RuntimeException("TaskScriptVariableScope.getVariableScopeKey_not_implemented");
	}

	@Override
	public boolean hasVariables() {
		throw new RuntimeException("TaskScriptVariableScope.hasVariables_not_implemented");
	}

	@Override
	public boolean hasVariablesLocal() {
		throw new RuntimeException("TaskScriptVariableScope.hasVariablesLocal_not_implemented");
	}

	@Override
	public boolean hasVariable(String variableName) {
		//return m_runtimeService.hasVariable(m_executionId,variableName);
		throw new RuntimeException("TaskScriptVariableScope.hasVariable_not_implemented");
	}

	@Override
	public boolean hasVariableLocal(String variableName) {
		//return m_runtimeService.hasVariableLocal(m_executionId,variableName);
		throw new RuntimeException("TaskScriptVariableScope.hasVariablesLocal_not_implemented");
	}

	@Override
	public void removeVariable(String variableName) {
		//return m_runtimeService.removeVariable(m_executionId,variableName);
		throw new RuntimeException("TaskScriptVariableScope.removeVariable_not_implemented");
	}

	@Override
	public void removeVariableLocal(String variableName) {
		//return m_runtimeService.removeVariableLocal(m_executionId,variableName);
		throw new RuntimeException("TaskScriptVariableScope.removeVariableLocal_not_implemented");
	}

	@Override
	public void removeVariables(Collection<String> variableNames) {
		//return m_runtimeService.removeVariables(m_executionId,variableNames);
		throw new RuntimeException("TaskScriptVariableScope.removeVariables_not_implemented");
	}

	@Override
	public void removeVariablesLocal(Collection<String> variableNames) {
		//return m_runtimeService.removeVariablesLocal(m_executionId,variableNames);
		throw new RuntimeException("TaskScriptVariableScope.removeVariablesLocal_not_implemented");
	}

	@Override
	public void removeVariables() {
		throw new RuntimeException("TaskScriptVariableScope.removeVariables_not_implemented");
	}

	@Override
	public void removeVariablesLocal() {
		throw new RuntimeException("TaskScriptVariableScope.removeVariablesLocal_not_implemented");
	}

	@Override
  public VariableMap getVariablesTyped(){
		throw new RuntimeException("TaskScriptVariableScope.getVariablesTyped_not_implemented");
	}

	@Override
  public VariableMap getVariablesTyped(boolean deserializeValues){
		throw new RuntimeException("TaskScriptVariableScope.getVariablesTyped_not_implemented");
	}

	@Override
  public VariableMap getVariablesLocalTyped(){
		throw new RuntimeException("TaskScriptVariableScope.getVariablesLocalTyped_not_implemented");
	}

	@Override
  public VariableMap getVariablesLocalTyped(boolean deserializeValues){
		throw new RuntimeException("TaskScriptVariableScope.getVariablesLocalTyped_not_implemented");
	}

	@Override
  public <T extends TypedValue> T getVariableTyped(String variableName){
		throw new RuntimeException("TaskScriptVariableScope.getVariablesTyped_not_implemented");
	}

	@Override
  public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeValue){
		throw new RuntimeException("TaskScriptVariableScope.getVariablesTyped_not_implemented");
	}

	@Override
  public <T extends TypedValue> T getVariableLocalTyped(String variableName){
		throw new RuntimeException("TaskScriptVariableScope.getVariableLocalTyped_not_implemented");
	}

	@Override
  public <T extends TypedValue> T getVariableLocalTyped(String variableName, boolean deserializeValue){
		throw new RuntimeException("TaskScriptVariableScope.getVariableLocalTyped_not_implemented");
	}
}

