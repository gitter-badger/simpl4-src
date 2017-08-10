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
package org.ms123.common.workflow;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.flowable.engine.delegate.VariableScope;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.VariableInstance;

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



	public void setTransientVariable(String variableName, Object variableValue){
		throw new RuntimeException("RuntimeVariableScope.setTransientVariable not implemented");
	}

	public void setTransientVariableLocal(String variableName, Object variableValue){
		throw new RuntimeException("RuntimeVariableScope.setTransientVariableLocal not implemented");
	}

	public void setTransientVariables(Map<String, Object> transientVariables){
		throw new RuntimeException("RuntimeVariableScope.setTransientVariables not implemented");
	}

	public Object getTransientVariable(String variableName){
		throw new RuntimeException("RuntimeVariableScope.getTransientVariable not implemented");
	}

	public Map<String, Object> getTransientVariables(){
		throw new RuntimeException("RuntimeVariableScope.getTransientVariables not implemented");
	}

	public void setTransientVariablesLocal(Map<String, Object> transientVariables){
		throw new RuntimeException("RuntimeVariableScope.setTransientVariablesLocal not implemented");
	}

	public Object getTransientVariableLocal(String variableName){
		throw new RuntimeException("RuntimeVariableScope.getTransientVariableLocal not implemented");
	}

	public Map<String, Object> getTransientVariablesLocal(){
		throw new RuntimeException("RuntimeVariableScope.getTransientVariablesLocal not implemented");
	}

	public void removeTransientVariableLocal(String variableName){
		throw new RuntimeException("RuntimeVariableScope.removeTransientVariableLocal not implemented");
	}

	public void removeTransientVariable(String variableName){
		throw new RuntimeException("RuntimeVariableScope.removeTransientVariable not implemented");
	}

	public void removeTransientVariables(){
		throw new RuntimeException("RuntimeVariableScope.removeTransientVariables not implemented");
	}

	public void removeTransientVariablesLocal(){
		throw new RuntimeException("RuntimeVariableScope.removeTransientVariablesLocal not implemented");
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

	public Object setVariableLocal(String variableName, Object value, ExecutionEntity sourceActivityExecution, boolean fetchAllVariables){
		throw new RuntimeException("RuntimeVariableScope.setVariableLocal not implemented");
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

	public VariableInstance getVariableInstanceLocal(String variableName, boolean fetchAllVariables){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstanceLocal not implemented");
	}
	public Map<String, VariableInstance> getVariableInstances(){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstances not implemented");
	}
	public Map<String, VariableInstance> getVariableInstances(Collection<String> variableNames){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstances not implemented");
	}
	public Map<String, VariableInstance> getVariableInstances(Collection<String> variableNames, boolean fetchAllVariables){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstances not implemented");
	}
	public Map<String, VariableInstance> getVariableInstancesLocal(){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstancesLocal not implemented");
	}
	public Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> variableNames){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstancesLocal not implemented");
	}
	public Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> variableNames, boolean fetchAllVariables){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstancesLocal not implemented");
	}
	public VariableInstance getVariableInstance(String variableName){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstance not implemented");
	}
	public VariableInstance getVariableInstance(String variableName, boolean fetchAllVariables){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstance not implemented");
	}
	public VariableInstance getVariableInstanceLocal(String variableName){
		throw new RuntimeException("RuntimeVariableScope.getVariableInstanceLocal not implemented");
	}

}

