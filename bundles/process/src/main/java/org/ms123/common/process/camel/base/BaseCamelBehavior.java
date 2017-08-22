/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ms123.common.process.camel.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@@@MS import org.activiti.bpmn.model.MapExceptionEntry;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
//@@@MSimport org.camunda.bpm.engine.impl.bpmn.helper.ErrorPropagation;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.lang3.StringUtils;

/**
* This abstract class takes the place of the now-deprecated CamelBehaviour class (which can still be used for legacy compatibility)
* and significantly improves on its flexibility. Additional implementations can be created that change the way in which Activiti
* interacts with Camel per your specific needs.
* 
* Three out-of-the-box implementations of CamelBehavior are provided:
*   (1) CamelBehaviorDefaultImpl: Works just like CamelBehaviour does; copies variables into and out of Camel as or from properties.
*   (2) CamelBehaviorBodyAsMapImpl: Works by copying variables into and out of Camel using a Map<String,Object> object in the body.
*   (3) CamelBehaviorCamelBodyImpl: Works by copying a single variable value into Camel as a String body and copying the Camel
*      body into that same Activiti variable. The variable in Activiti must be named "camelBody".
* 
* The chosen implementation should be set within your ProcessEngineConfiguration. To specify the implementation using Spring, include
* the following line in your configuration file as part of the properties for "org.activiti.spring.SpringProcessEngineConfiguration":
* 
*   <property name="camelBehaviorClass" value="org.activiti.camel.impl.CamelBehaviorCamelBodyImpl"/>
* 
* Note also that the manner in which variables are copied to Activiti from Camel has changed. It will always copy Camel
* properties to the Activiti variable set; they can safely be ignored, of course, if not required. It will conditionally
* copy the Camel body to the "camelBody" variable if it is of type java.lang.String, OR it will copy the Camel body to
* individual variables within Activiti if it is of type Map<String,Object>.
* 
* @author Ryan Johnston (@rjfsu), Tijs Rademakers, Saeid Mirzaei
* @version 5.12
*/
public abstract class BaseCamelBehavior extends AbstractBpmnActivityBehavior implements ActivityBehavior {

  private static final long serialVersionUID = 1L;
  protected Expression camelContext;
  protected CamelContext camelContextObj;
//@@@MS  protected List<MapExceptionEntry> mapExceptions;
  
  protected void setPropertTargetVariable(BaseEndpoint endpoint) {
    toTargetType = TargetType.PROPERTIES;
  }
  
 
  public enum TargetType {
        BODY_AS_MAP, BODY, PROPERTIES
      }  

  protected TargetType toTargetType=null;
  
  protected void updateTargetVariables(BaseEndpoint endpoint) {
    toTargetType = null;
    if (endpoint.isCopyVariablesToBodyAsMap())
      toTargetType = TargetType.BODY_AS_MAP;
    else if (endpoint.isCopyCamelBodyToBody())
      toTargetType = TargetType.BODY;
    else if (endpoint.isCopyVariablesToProperties())
      toTargetType = TargetType.PROPERTIES;

    if (toTargetType == null)
      setPropertTargetVariable(endpoint);
  }
  
  protected void copyVariables(Map<String, Object> variables, Exchange exchange, BaseEndpoint endpoint) {
    switch (toTargetType) {
    case BODY_AS_MAP:
      copyVariablesToBodyAsMap(variables, exchange);
      break;

    case BODY:
      copyVariablesToBody(variables, exchange);
      break;

    case PROPERTIES:
      copyVariablesToProperties(variables, exchange);
    }
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    setAppropriateCamelContext(execution);
    
    final BaseEndpoint endpoint = createEndpoint(execution);
    final Exchange exchange = createExchange(execution, endpoint);
    
    endpoint.process(exchange);
    execution.setVariables(ExchangeUtils.prepareVariables(exchange, endpoint));
    if (!handleCamelException(exchange, execution))
      leave(execution);
  }

  protected BaseEndpoint createEndpoint(ActivityExecution execution) {
    String uri = "process://" + getProcessDefinitionKey(execution) + ":" + execution.getActivity().getId();
    return getEndpoint(uri);
  }

  protected BaseEndpoint getEndpoint(String key) {
    for (Endpoint e : camelContextObj.getEndpoints()) {
      if (e.getEndpointKey().equals(key) && (e instanceof BaseEndpoint)) {
        return (BaseEndpoint) e;
      }
    }
    throw new RuntimeException("Process endpoint not defined for " + key);    
  }

  protected Exchange createExchange(ActivityExecution activityExecution, BaseEndpoint endpoint) {
    Exchange ex = new DefaultExchange(camelContextObj);
    ex.setProperty(BaseProducer.PROCESS_ID_PROPERTY, activityExecution.getProcessInstanceId());
    ex.setProperty(BaseProducer.EXECUTION_ID_PROPERTY, activityExecution.getId());
    Map<String, Object> variables = activityExecution.getVariables();
    updateTargetVariables(endpoint);
    copyVariables(variables, ex, endpoint);
    return ex;
  }
  
  protected boolean handleCamelException(Exchange exchange, ActivityExecution execution) throws Exception {
    Exception camelException = exchange.getException();
    boolean notHandledByCamel = exchange.isFailed() && camelException != null;
    if (notHandledByCamel) {
      if (camelException instanceof BpmnError) {
        //@@@MS ErrorPropagation.propagateError((BpmnError) camelException, execution);
        return true;
      } else {
        //@@@MS if (ErrorPropagation.mapException(camelException, execution, mapExceptions)){
        //@@@MS   return true;
        //@@@MS }else{
          throw new RuntimeException("Unhandled exception on camel route", camelException);
				//@@@MS }
      }
    }
    return false;
  }
  
  protected void copyVariablesToProperties(Map<String, Object> variables, Exchange exchange) {
    for (Map.Entry<String, Object> var : variables.entrySet()) {
      exchange.setProperty(var.getKey(), var.getValue());
    }
  }
  
  protected void copyVariablesToBodyAsMap(Map<String, Object> variables, Exchange exchange) {
    exchange.getIn().setBody(new HashMap<String,Object>(variables));
  }
  
  protected void copyVariablesToBody(Map<String, Object> variables, Exchange exchange) {
    Object camelBody = variables.get(ExchangeUtils.CAMELBODY);
    if(camelBody != null) {
      exchange.getIn().setBody(camelBody);
    }
  }

  protected String getProcessDefinitionKey(ActivityExecution execution) {
    PvmProcessDefinition processDefinition = execution.getActivity().getProcessDefinition();
    return processDefinition.getKey();
  }
  
  protected boolean isASync(ActivityExecution execution) {
     return execution.getActivity().isAsync();
  }
  
  abstract protected void setAppropriateCamelContext(ActivityExecution execution);
  
  protected String getStringFromField(Expression expression, DelegateExecution execution) {
    if (expression != null) {
      Object value = expression.getValue(execution);
      if (value != null) {
        return value.toString();
      }
    }
    return null;
  }

  public void setCamelContext(Expression camelContext) {
    this.camelContext = camelContext;
  }
}
