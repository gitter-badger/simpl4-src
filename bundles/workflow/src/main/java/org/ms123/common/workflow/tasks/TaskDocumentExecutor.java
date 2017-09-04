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
package org.ms123.common.workflow.tasks;

import java.util.*;
import java.io.*;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.ms123.common.docbook.DocbookService;
import org.apache.commons.beanutils.*;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import flexjson.*;

@SuppressWarnings("unchecked")
public class TaskDocumentExecutor extends TaskBaseExecutor implements JavaDelegate {

	protected JSONDeserializer m_ds = new JSONDeserializer();

	private Expression documentname;
	private Expression filename;

	private Expression variablesmapping;

	@Override
	public void execute(DelegateExecution execution) {
		TaskContext tc = new TaskContext(execution);
		showVariablenNames(tc);
		DocbookService ds = getDocbookService(execution);
		String namespace = tc.getTenantId();
		String fn = getName(documentname.getValue(execution).toString());
		List ret = null;
		try {
			Map<String, Object> vars = new HashMap(execution.getVariables());
			Map<String, Object> fparams = getParams(execution, variablesmapping, "documentvar");
			vars.putAll(fparams);
			System.out.println("TaskDocumentExecutor,fparams:" + fparams);
			System.out.println("TaskDocumentExecutor,filename:" + filename);
			if (filename == null || filename.getValue(execution) == null) {
				throw new RuntimeException("TaskDocumentExecutor.execute:Filename is null");
			}
			OutputStream os = new FileOutputStream(new File(getProcessDocBasedir(execution), filename.getValue(
					execution).toString()));
			String json = getFileContentFromGit(execution, namespace, fn, "sw.document");
			ds.jsonToPdf(namespace, json, vars, os);
			os.close();
		} catch (Exception e) {
			throw new RuntimeException("TaskDocumentExecutor.execute:", e);
		} finally {
		}
	}

	private String getName(String s) {
		if (s == null) {
			throw new RuntimeException("TaskDocumentExecutor.documentname is null");
		}
		if (s.indexOf(",") == -1)
			return s;
		return s.split(",")[1];
	}
}

