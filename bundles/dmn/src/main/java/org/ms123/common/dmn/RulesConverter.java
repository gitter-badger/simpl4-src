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
package org.ms123.common.dmn;


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
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionLogic;
import flexjson.*;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.type.StringDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.BooleanDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.IntegerDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.LongDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.DateDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.DoubleDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.type.DmnTypeDefinitionImpl;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformer;


/**
 *
 */
@SuppressWarnings({"unchecked","deprecation"})
public class RulesConverter {

	protected JSONSerializer js = new JSONSerializer();
	private Map rules;

	/**
	 */
	public RulesConverter(Map rules) {
		this.rules = rules;
		this.js.prettyPrint(true);
	}

	public DmnDecision convert( String cname) throws Exception {
		Map ret = new HashMap();
		System.out.println("RulesProcessor.execute:"+this.js.deepSerialize(rules));
		Map variables = (Map) this.rules.get("variables");
		List<Map> inputVars = (List) variables.get("input");
		List<Map> outputVars = (List) variables.get("output");

		Map columns = (Map) this.rules.get("columns");
		List<Map> conditionColumns = (List) columns.get("conditions");
		List<Map> actionColumns = (List) columns.get("actions");

		DmnDecisionImpl decision = new DmnDecisionImpl() ;
		DmnDecisionTableImpl decisionTable = new DmnDecisionTableImpl() ;
		decision.setDecisionLogic( decisionTable);

		List<DmnDecisionTableInputImpl> inputList = new ArrayList<DmnDecisionTableInputImpl>();
		List<DmnDecisionTableOutputImpl> outputList = new ArrayList<DmnDecisionTableOutputImpl>();
		List<DmnDecisionTableRuleImpl> ruleList = new ArrayList<DmnDecisionTableRuleImpl>();
		processDecisionTable( conditionColumns, actionColumns, inputList, outputList, ruleList);
		decisionTable.setInputs( inputList);
		decisionTable.setOutputs( outputList);
		decisionTable.setRules( ruleList);
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

	private DmnTypeDefinitionImpl getTypeDefinition( String type){
		String typeName=type;
		DmnDataTypeTransformer transformer=null;
		if( "decimal".equals( type)){
			typeName = "double";
			transformer = new DoubleDataTypeTransformer();
		}else if( "integer".equals( type)){
			transformer = new IntegerDataTypeTransformer();
		}else if( "boolean".equals( type)){
			transformer = new BooleanDataTypeTransformer();
		}else if( "date".equals( type)){
			transformer = new DateDataTypeTransformer();
		}else if( "string".equals( type)){
			transformer = new StringDataTypeTransformer();
		}
		return new DmnTypeDefinitionImpl(typeName, transformer);
	}

	protected void processDecisionTable(List<Map> conditionColumns, List<Map> actionColumns, List<DmnDecisionTableInputImpl> inputList, List<DmnDecisionTableOutputImpl> outputList,List<DmnDecisionTableRuleImpl> ruleList) {

		for (Map<String,Object> conditionColumn : conditionColumns) {

			DmnDecisionTableInputImpl inputClause = new DmnDecisionTableInputImpl();
			String variableName = (String)conditionColumn.get("variableName");
			String variableType = convertType((String)conditionColumn.get("variableType"));

			inputClause.setId("input_" + variableName);
			inputClause.setName(variableName);
			inputClause.setInputVariable(variableName);
			DmnExpressionImpl expr = new DmnExpressionImpl();
			expr.setTypeDefinition( getTypeDefinition( variableName));
			inputClause.setExpression( expr );
			expr.setExpression( variableName );

			conditionColumn.put("inputClause", inputClause);
			inputList.add(inputClause);
		}

		for (Map<String,Object> actionColumn : actionColumns) {

			DmnDecisionTableOutputImpl outputClause = new DmnDecisionTableOutputImpl();
			String variableName = (String)actionColumn.get("variableName");
			String variableType = convertType((String)actionColumn.get("variableType"));

			outputClause.setId("output_" + variableName);
			outputClause.setTypeDefinition(getTypeDefinition(variableType));
			outputClause.setName(variableName);
			outputClause.setOutputName(variableName);

			actionColumn.put("outputClause", outputClause);
			outputList.add(outputClause);
		}
		int ruleCounter = 1;

		int countRules = getCountRules(conditionColumns);
		/*for( int i=0; i< countRules;i++){
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
		}*/
	}
}
