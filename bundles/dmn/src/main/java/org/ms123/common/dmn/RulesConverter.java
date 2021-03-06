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
package org.ms123.common.dmn;

import flexjson.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionLogic;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.FirstHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.BooleanDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.DateDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.DmnTypeDefinitionImpl;
import org.camunda.bpm.dmn.engine.impl.type.DoubleDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.IntegerDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.LongDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.StringDataTypeTransformer;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class RulesConverter {

	protected JSONSerializer js = new JSONSerializer();
	private Map rules;

	/**
	 */
	public RulesConverter(Map rules) {
		this.rules = rules;
		this.js.prettyPrint(true);
	}

	public DmnDecision convert(String cname) throws Exception {
		Map ret = new HashMap();
		Map variables = (Map) this.rules.get("variables");

		Map config = (Map) this.rules.get("config");
		Map columns = (Map) this.rules.get("columns");
		List<Map> conditionColumns = (List) columns.get("conditions");
		List<Map> actionColumns = (List) columns.get("actions");

		DmnDecisionImpl decision = new DmnDecisionImpl();
		decision.setKey(cname);
		DmnDecisionTableImpl decisionTable = new DmnDecisionTableImpl();
		decision.setDecisionLogic(decisionTable);

		List<DmnDecisionTableInputImpl> inputList = new ArrayList<DmnDecisionTableInputImpl>();
		List<DmnDecisionTableOutputImpl> outputList = new ArrayList<DmnDecisionTableOutputImpl>();
		List<DmnDecisionTableRuleImpl> ruleList = new ArrayList<DmnDecisionTableRuleImpl>();
		processDecisionTable(config,conditionColumns, actionColumns, inputList, outputList, ruleList);
		decisionTable.setInputs(inputList);
		decisionTable.setOutputs(outputList);
		decisionTable.setRules(ruleList);
		decisionTable.setHitPolicyHandler(new FirstHitPolicyHandler());
		return decision;
	}

	private int getCountRules(List<Map> conditions) {
		int count = 0;
		for (Map cond : conditions) {
			List<Object> data = (List) cond.get("data");
			count = Math.max(data.size(), count);
		}
		return count;
	}

	private String getOp(String f, String op, String data, String varType) {
		if ("ge".equals(op)) {
			return f + " >= " + data;
		}
		if ("le".equals(op)) {
			return f + " <= " + data;
		}
		if ("gt".equals(op)) {
			return f + " > " + data;
		}
		if ("lt".equals(op)) {
			return f + " < " + data;
		}
		if ("eq".equals(op)) {
			if ("string".equals(varType)) {
				return f + ".equalsIgnoreCase(\"" + data + "\")";
			} else {
				return f + " == " + data;
			}
		}
		if ("ceq".equals(op)) {
			return f + "== \"" + data + "\"";
		}
		if ("ne".equals(op)) {
			if ("string".equals(varType)) {
				return f + " != \"" + data + "\"";
			} else {
				return f + " != " + data;
			}
		}
		if ("bw".equals(op)) {
			return f + ".startsWith(\"" + data + "\")";
		}
		if ("cn".equals(op)) {
			return f + ".contains(\"" + data + "\")";
		}
		if ("in".equals(op)) {
			return "is null";
		}
		if ("inn".equals(op)) {
			return "is not null";
		}
		return "op not found";
	}

	private DmnTypeDefinitionImpl getTypeDefinition(String type) {
		String typeName = type;
		DmnDataTypeTransformer transformer = null;
		if ("decimal".equals(type) || "double".equals(type)) {
			typeName = "double";
			transformer = new DoubleDataTypeTransformer();
		} else if ("integer".equals(type)) {
			transformer = new IntegerDataTypeTransformer();
		} else if ("long".equals(type)) {
			transformer = new LongDataTypeTransformer();
		} else if ("boolean".equals(type)) {
			transformer = new BooleanDataTypeTransformer();
		} else if ("date".equals(type)) {
			transformer = new DateDataTypeTransformer();
		} else if ("string".equals(type)) {
			transformer = new StringDataTypeTransformer();
		} else if ("list".equals(type)) {
			transformer = new StringDataTypeTransformer();
		} else {
			transformer = new StringDataTypeTransformer();
		}
		return new DmnTypeDefinitionImpl(typeName, transformer);
	}

	protected void processDecisionTable(Map config, List<Map> conditionColumns, List<Map> actionColumns, List<DmnDecisionTableInputImpl> inputList, List<DmnDecisionTableOutputImpl> outputList, List<DmnDecisionTableRuleImpl> ruleList) {

		for (Map<String, Object> conditionColumn : conditionColumns) {

			DmnDecisionTableInputImpl inputClause = new DmnDecisionTableInputImpl();
			String variableName = (String) conditionColumn.get("variableName");
			String variableType = (String) conditionColumn.get("variableType");

			inputClause.setId("input_" + variableName);
			inputClause.setName(variableName);
			inputClause.setInputVariable(variableName);
			DmnExpressionImpl expr = new DmnExpressionImpl();
			expr.setTypeDefinition(getTypeDefinition(variableType));
			inputClause.setExpression(expr);
			expr.setExpression(variableName);
			info(this,"Input("+variableName+","+variableType+"):"+inputClause);
			inputList.add(inputClause);
		}

		for (Map<String, Object> actionColumn : actionColumns) {

			DmnDecisionTableOutputImpl outputClause = new DmnDecisionTableOutputImpl();
			String variableName = (String) actionColumn.get("variableName");
			String variableType = (String) actionColumn.get("variableType");

			outputClause.setId("output_" + variableName);
			outputClause.setTypeDefinition(getTypeDefinition(variableType));
			outputClause.setName(variableName);
			outputClause.setOutputName(variableName);

			info(this,"Output("+variableName+","+variableType+"):"+outputClause);
			outputList.add(outputClause);
		}
		int ruleCounter = 1;

		int countRules = getCountRules(conditionColumns);
		for (int i = 0; i < countRules; i++) {
			DmnDecisionTableRuleImpl rule = new DmnDecisionTableRuleImpl();
			List<DmnExpressionImpl> listCond = new ArrayList<DmnExpressionImpl>();
			for (Map conditionColumn : conditionColumns) {
				List<Object> dataList = (List) conditionColumn.get("data");
				Object data = dataList.get(i);

				String variableName = (String) conditionColumn.get("variableName");
				String variableType = (String) conditionColumn.get("variableType");
				String operation = (String) conditionColumn.get("operation");
				String expr=null;
				if( "expr".equals(operation) ){
					expr = (String)data;
				}else{
					expr = data != null ? String.valueOf(data) : "";
					if (!isEmpty(expr)) {
						expr = getOp(variableName, operation, expr, variableType);
					}
				}
				DmnExpressionImpl cond = new DmnExpressionImpl();
				cond.setId("input_" + variableName + "_" + ruleCounter);
				cond.setExpression(expr);
				cond.setExpressionLanguage("groovy");
				listCond.add(cond);
			}
			rule.setConditions(listCond);

			List<DmnExpressionImpl> listConc = new ArrayList<DmnExpressionImpl>();
			for (Map actionColumn : actionColumns) {
				List<Object> dataList = (List) actionColumn.get("data");
				Object data = dataList.get(i);

				String variableName = (String) actionColumn.get("variableName");
				String variableType = (String) actionColumn.get("variableType");
				String text = data != null ? String.valueOf(data) : "";
				text = variableType.equals("string") ? ("\"" + text + "\"") : text;

				DmnExpressionImpl conc = new DmnExpressionImpl();
				conc.setId("output_" + variableName + "_" + ruleCounter);
				conc.setExpression(text);
				conc.setExpressionLanguage("groovy");
				listConc.add(conc);
			}
			rule.setConclusions(listConc);
			ruleCounter++;
			info(this,"Rule:"+rule);
			ruleList.add(rule);
		}
	}
}

