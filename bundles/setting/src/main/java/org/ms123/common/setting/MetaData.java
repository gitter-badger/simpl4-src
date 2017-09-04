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
package org.ms123.common.setting;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.ms123.common.store.StoreDesc;

/**
 *
 */
interface  MetaData {

	public final String SETTING_PATH = "settings/{0}";
	public final String SETTINGS_PATH = "settings";
	public final String SETTING_TYPE = "sw.setting";
	public final String SETTINGS_TYPE = "sw.settings";

	public static String SETTINGS = "settings";

	public void setResourceSetting(String namespace, String settingsid, String resourceid, Map settings,boolean overwrite) throws Exception;
	public Map getResourceSetting(String namespace, String settingsid, String resourceid) throws Exception;
	public List<Map> getResourceSettings(String settingsid, String resourceid) throws Exception;
	public List<String> getResourceSettingNames(String namespace, String settingsid, String resourcePrefix) throws Exception;
	public void deleteResourceSetting(String namespace, String settingsid, String resourceid) throws Exception;

	public Map getFieldSets(String settingsid, String namespace, String entityName) throws Exception;

	public List<Map> getFieldsetsForEntity(String namespace, String settingsid, String entity) throws Exception;
	public List<Map> getFieldsForEntityView(String namespace, String settingsid, String entity, String view) throws Exception;
	public List<Map> getFieldsForEntityView(String namespace, String settingsid, String entity, String view, Map mapping, String filter, String sortField) throws Exception;

	public Map getPropertiesForEntity(String namespace, String settingsid, String entity) throws Exception;
	public Map getPropertiesForEntityView(String namespace, String settingsid, String entity, String view) throws Exception;

}
