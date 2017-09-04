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
package org.ms123.common.rpc;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CallService {

	public static String SERVICENAME = "service";
	public static String METHODNAME = "method";
	public static String METHODPARAMS = "params";
	public static String ACTION = "action";
	public static String SYNC = "sync";
	public static String AT = "at";
	public static String AT_BEFORE = "before";
	public static String AT_AFTER = "after";
	public static String METHODRESULT = "result";
	public static String PRECONDITION = "preCondition";
	public static String CAMELSERVICENAME = "camel-routing";
	public static String CAMELSERVICENAME2 = "camelRoute";
	public static String ACTIVITI_CAMEL_PROPERTIES = "__activitiCamelProperties";

	public void callHooks(Map params);
	public Object callCamel(String methodName, Object methodParams);
	public Object callCamel(String methodName, Object methodParams, HttpServletRequest request, HttpServletResponse response);
	
}
