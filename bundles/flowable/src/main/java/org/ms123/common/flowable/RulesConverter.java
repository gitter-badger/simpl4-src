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
package org.ms123.common.flowable;


import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.Date;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.flowable.dmn.model.Decision;
import org.flowable.dmn.model.DecisionRule;
import org.flowable.dmn.model.DecisionTable;
import org.flowable.dmn.model.DecisionTableOrientation;
import org.flowable.dmn.model.DmnDefinition;
import org.flowable.dmn.model.HitPolicy;
import org.flowable.dmn.model.InputClause;
import org.flowable.dmn.model.LiteralExpression;
import org.flowable.dmn.model.OutputClause;
import org.flowable.dmn.model.RuleInputClauseContainer;
import org.flowable.dmn.model.RuleOutputClauseContainer;
import org.flowable.dmn.model.UnaryTests;
//import org.mvel2.MVEL;
import flexjson.*;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static org.apache.commons.lang3.StringUtils.isEmpty;


/**
 *
 */
@SuppressWarnings({"unchecked","deprecation"})
public class RulesConverter {

	public static final String MODEL_NAMESPACE = "http://activiti.com/dmn";
	public static final String URI_JSON = "http://www.ecma-international.org/ecma-404/";
	protected JSONSerializer js = new JSONSerializer();
	private Map rules;
	private Map m_values;
	private Map m_evalParams = new HashMap();
	private int m_paramCount = 0;

	/**
	 */
	public RulesConverter(Map rules) {
		this.rules = rules;
		this.js.prettyPrint(true);
	}

	public DmnDefinition convert( String cname) throws Exception {
		Map ret = new HashMap();
		System.out.println("RulesProcessor.execute:"+this.js.deepSerialize(rules));
		Map variables = (Map) this.rules.get("variables");
		List<Map> inputVars = (List) variables.get("input");
		List<Map> outputVars = (List) variables.get("output");

		Map columns = (Map) this.rules.get("columns");
		List<Map> conditionColumns = (List) columns.get("conditions");
		List<Map> actionColumns = (List) columns.get("actions");

		DmnDefinition definition = new DmnDefinition();

		definition.setId("def_"+cname);
		definition.setName("abc");
		definition.setNamespace(MODEL_NAMESPACE);
		definition.setTypeLanguage(URI_JSON);

		// decision
		//
		Decision decision = new Decision();
		decision.setId(cname);
		decision.setName("decision");
		decision.setDescription("");

		definition.addDecision(decision);

		// decision table
		//
		DecisionTable decisionTable = new DecisionTable();
		decisionTable.setId("dt_"+cname );

		decisionTable.setHitPolicy(HitPolicy.FIRST);

		// default orientation
		decisionTable.setPreferredOrientation(DecisionTableOrientation.RULE_AS_ROW);

		decision.setExpression(decisionTable);
		processDecisionTable(conditionColumns, actionColumns, definition, decisionTable);

		//String d = js.deepSerialize(definition);
		//info(this,"definition:"+d);
		return definition ;
	}

	private int getCountRules(List<Map> conditions) {
		int count = 0;
		for (Map cond : conditions) {
			List<Object> data = (List) cond.get("data");
			count = Math.max(data.size(), count);
		}
		return count;
	}

	private String getOp( String op, String data, String varType) {
		if ("ge".equals(op)) {
			return ">= " + data;
		}
		if ("le".equals(op)) {
			return "<= " + data;
		}
		if ("gt".equals(op)) {
			return "> " + data;
		}
		if ("lt".equals(op)) {
			return "< " + data;
		}
		if ("eq".equals(op)) {
			if( "string".equals(varType)){
				return "~= (\"(?i)" + data + "\")";
			}else{
				return "== " + data;
			}
		}
		if ("ceq".equals(op)) {
			return " ~= (\"" + data + "\")";
		}
		if ("ne".equals(op)) {
			if( "string".equals(varType)){
				return "!= \"" + data+"\"";
			}else{
				return "!= " + data;
			}
		}
		if ("bw".equals(op)) {
			return "~= (\"(?i)" + data + ".*\")";
		}
		if ("cn".equals(op)) {
			return "~= \"(?i).*" + data + ".*\"";
		}
		if ("in".equals(op)) {
			return "is null";
		}
		if ("inn".equals(op)) {
			return "is not null";
		}
		return "op not found";
	}

	private String convertType( String in ){
		if( "decimal".equals(in)){
			return "double";
		}
		if( "integer".equals(in)){
			return "number";
		}
		return in;
	}

	protected void processDecisionTable(List<Map> conditionColumns, List<Map> actionColumns, DmnDefinition definition, DecisionTable decisionTable) {

		if (definition == null || decisionTable == null) {
			return;
		}

		for (Map<String,Object> conditionColumn : conditionColumns) {

			InputClause inputClause = new InputClause();
			inputClause.setLabel("");
			String variableName = (String)conditionColumn.get("variableName");
			String variableType = convertType((String)conditionColumn.get("variableType"));

			String inputExpressionId = variableName;

			LiteralExpression inputExpression = new LiteralExpression();
			inputExpression.setId("inputExpression_" + inputExpressionId);
			inputExpression.setTypeRef(variableType);
			inputExpression.setLabel(variableName);
			inputExpression.setText(variableName);

			inputClause.setInputExpression(inputExpression);
			conditionColumn.put("inputClause", inputClause);
			decisionTable.addInput(inputClause);
		}

		for (Map<String,Object> actionColumn : actionColumns) {

			OutputClause outputClause = new OutputClause();
			String variableName = (String)actionColumn.get("variableName");
			String variableType = convertType((String)actionColumn.get("variableType"));
			String outputExpressionId = "";

			outputClause.setId("outputExpression_" + variableName);
			outputClause.setTypeRef(variableType);
			outputClause.setLabel(variableName);
			outputClause.setName(variableName);

			actionColumn.put("outputClause", outputClause);
			decisionTable.addOutput(outputClause);
		}
		int ruleCounter = 1;

		int countRules = getCountRules(conditionColumns);
		for( int i=0; i< countRules;i++){
			DecisionRule rule = new DecisionRule();
			for (Map conditionColumn : conditionColumns) {
				List<Object> dataList = (List)conditionColumn.get("data");
				Object data = dataList.get(i);
				RuleInputClauseContainer ruleInputClauseContainer = new RuleInputClauseContainer();
				ruleInputClauseContainer.setInputClause((InputClause)conditionColumn.get("inputClause"));

				String variableName = (String)conditionColumn.get("variableName");
				String variableType = (String)conditionColumn.get("variableType");
				String operation = (String)conditionColumn.get("operation");
			  String text = data != null ? String.valueOf(data) : "";
				if( !isEmpty(text) ){
					if( "date".equals(variableType)){
						text = "fn_date('"+text+"')";
					}
					text = getOp( operation, text, variableType);
				}
				UnaryTests inputEntry = new UnaryTests();
				inputEntry.setId("inputEntry_" + variableName + "_" + ruleCounter);
				inputEntry.setText(text);

				ruleInputClauseContainer.setInputEntry(inputEntry);

				rule.addInputEntry(ruleInputClauseContainer);
			}

			for (Map actionColumn : actionColumns) {
				List<Object> dataList = (List)actionColumn.get("data");
				Object data = dataList.get(i);
				RuleOutputClauseContainer ruleOutputClauseContainer = new RuleOutputClauseContainer();
				ruleOutputClauseContainer.setOutputClause((OutputClause)actionColumn.get("outputClause"));

				String variableName = (String)actionColumn.get("variableName");
				String variableType = (String)actionColumn.get("variableType");
				LiteralExpression outputEntry = new LiteralExpression();
				outputEntry.setId("outputEntry_" + variableName + "_" + ruleCounter);
			  String text = data != null ? String.valueOf(data) : "";
				text = variableType.equals("string") ? ("\"" + text + "\"") : text;
				outputEntry.setText(text);

				ruleOutputClauseContainer.setOutputEntry(outputEntry);

				rule.addOutputEntry(ruleOutputClauseContainer);
			}
			ruleCounter++;
			decisionTable.addRule(rule);
		}
	}
}
