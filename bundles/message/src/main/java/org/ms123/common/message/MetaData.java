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
package org.ms123.common.message;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.Reader;

/**
 *
 */
interface MetaData {

	public final String MESSAGESLANG_PATH = "messages/{0}";

	public final String MESSAGES_PATH = "messages";

	public final String MESSAGESLANG_TYPE = "sw.messageslang";

	public final String MESSAGES_TYPE = "sw.messages";

	public List<Map> getLanguages(String namespace) throws Exception;

	public List<Map> getMessages(String namespace, String lang, Map filter) throws Exception;

	public Map<String, String> getMessage(String namespace, String lang, String id) throws Exception;

	public void saveMessage(String namespace, String lang, Map msg,boolean overwrite) throws Exception;

	public void addMessages(String namespace, String lang, List<Map> msgs,boolean overwrite) throws Exception;

	public void saveMessages(String namespace, String lang, List<Map> msgs) throws Exception;

	public void deleteMessages(String namespace, String lang,String regex, List<String> msgIds) throws Exception;

	public void deleteMessage(String namespace, String lang, String id) throws Exception;

	public List<Map> parseCSV(Reader reader) throws Exception;
}
