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
package org.ms123.common.reporting;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 */
interface  MetaData {
	public final String REPORT_USER_PATH = "reports/{0}/{1}";
	public final String REPORTS_USER_PATH = "reports/{0}";

	public final String REPORT_PATH = "reports/{0}";
	public final String REPORTS_PATH = "reports";
	public final String REPORT_TYPE = "sw.report";
	public final String REPORTS_TYPE = "sw.reports";


	public List<Map> getReports(String namespace) throws Exception;
	public Map<String,List>  getReport(String namespace, String name) throws Exception;
	public void saveReport(String namespace, String name, Map<String,List> desc) throws Exception;
	public void deleteReport(String namespace, String name) throws Exception;
}
