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
package org.ms123.common.namespace;

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
interface MetaData {

	public final String NAMESPACE_PATH = "namespaces/{0}";

	public final String BRANDING_PATH = "etc/branding";
	public final String BRANDING_EXAMPLE_PATH = "etc/branding.example";

	public final String NAMESPACES_PATH = "namespaces";

	public final String NAMESPACE_TYPE = "sw.namespace";

	public final String NAMESPACES_TYPE = "sw.namespaces";

	public List<Map> getNamespaces() throws Exception;

	public Map<String, List> getNamespace(String name) throws Exception;

	public void saveNamespace(String name, Map<String, List> desc) throws Exception;

	public void deleteNamespace(String name) throws Exception;
	public Map<String, String> getBranding() throws Exception;

	public void saveBranding(Map<String, String> desc) throws Exception;
}
