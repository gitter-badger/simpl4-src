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

import java.util.*;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.ms123.common.data.api.DataLayer;
import javax.transaction.UserTransaction;
import org.ms123.common.data.api.SessionContext;
import org.apache.commons.beanutils.*;
import flexjson.*;
import org.ms123.common.dmn.DmnService;
import org.ms123.common.store.StoreDesc;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.ProcessEngine;
import org.ms123.common.git.GitService;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings("unchecked")
public class TaskRulesExecutor extends TaskBaseExecutor implements JavaDelegate {

	protected JSONDeserializer ds = new JSONDeserializer();

	protected JSONSerializer js = new JSONSerializer();

	private Expression rulesname;
	private Expression ruleskey;

	private Expression variablesmapping;

	@Override
	public void execute(DelegateExecution execution) {
		TaskContext tc = new TaskContext(execution);
		String namespace = tc.getNamespace();

		Object rn =  this.rulesname != null ? this.rulesname.getValue(execution) : null;
		Object rk =  this.ruleskey != null ? this.ruleskey.getValue(execution) : null;

		String rname=null;
		String rkey=null;
		if( !isEmpty( rn )){
			rname = rn.toString();
		}else if( !isEmpty( rk )){
			rkey = rk.toString();
		}
		
		if( isEmpty(rname) && isEmpty( rkey)){
			throw new RuntimeException("TaskRulesExecutor: one of ruleskey, rulesname  must be set");
		}

		String vm = variablesmapping.getValue(execution).toString();
		Map map = (Map) this.ds.deserialize(vm);
		List<Map> varmap = (List<Map>) map.get("items");
		this.js.prettyPrint(true);
		info(this, "varmap:" + this.js.deepSerialize(varmap));
		Map<String, Object> values = new HashMap();
		try {
			for (Map<String, String> m : varmap) {
				String direction = m.get("direction");
				if (direction.equals("incoming")) {
					String processvar = m.get("processvar");
					Object o = getValue(execution, processvar);
					String rulesvar = m.get("rulesvar");
					values.put(rulesvar, o);
				}
			}
			info(this, "Incomingvalues:" + this.js.deepSerialize(values));
			Map ret = null;
			if( !isEmpty(rname)){
				Map rules = getRules(rname, namespace);
				RulesProcessor rp = new RulesProcessor(rules, values);
				ret = rp.execute();
			}else{
				String decisionString = getValueFromRegistry( rkey );
				info(this, "RulesProcessor.decisionString:" + decisionString);
				List<Map> resultList = getDmnService().executeDecision( namespace, decisionString, values);
				if( resultList!=null && resultList.size()>0){
					ret = resultList.get(0);
				}
			}
			info(this, "RulesProcessor.ret:" + ret);
			for (Map<String, String> m : varmap) {
				String direction = m.get("direction");
				if (direction.equals("outgoing")) {
					String rulesvar = m.get("rulesvar");
					Object o = ret.get(rulesvar);
					String processvar = m.get("processvar");
					info(this, "ProcessVarsetting:processvar:" + processvar + "->rulesvar:" + rulesvar + "(" + o + ")");
					setValue(execution, processvar, o);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("TaskRulesExecutor.execute:", e);
		}
	}

	public Map getRules(String name, String namespace) {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		GitService gitService = (GitService) beans.get("gitService");
		String filterJson = gitService.searchContent(namespace, name, "sw.rule");
		Map contentMap = (Map) this.ds.deserialize(filterJson);
		return contentMap;
	}
}

