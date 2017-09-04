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
package org.ms123.common.datamapper;
import com.google.gson.*;
import java.util.*;
import java.io.*;
import com.google.gson.stream.MalformedJsonException;
import flexjson.JSONSerializer;

@SuppressWarnings("unchecked")
public final class JsonMetaData implements MetaData,Constants {

	protected static JSONSerializer js = new JSONSerializer();

	public JsonMetaData() {
		js.prettyPrint(true);
	}

	public Map generateMetadata(File stream) throws Exception {
		Reader bufferedReader = new FileReader(stream);
		return generateMetadata(bufferedReader);
	}

	public Map generateMetadata(Reader bufferedReader) throws Exception {
		JsonElement root = null;
		try {
			root = new JsonParser().parse(bufferedReader);
		} catch (JsonSyntaxException e) {
			Throwable t = e;
			if ((e.getCause() instanceof MalformedJsonException)) {
				t = e.getCause();
			}
			throw new RuntimeException("Provided JSON file contains syntax errors:\n" + t.getMessage(), t);
		}
		String name = root.isJsonArray() ? "array" : "object";
		Map rootMap = processElement(root, name, null);
		System.out.println(js.deepSerialize(rootMap));
		return rootMap;
	}

	private Map processElement(JsonElement jsonElement, String name, Map parent) {
		Map element = new HashMap();
		element.put(NODENAME, name);
		if (parent == null) {
			element.put(ROOT, true);
			element.put(FORMAT, FORMAT_JSON);
		}
		if (jsonElement.isJsonArray()) {
			processArray((JsonArray) jsonElement, name, element);
		} else if (jsonElement.isJsonObject()) {
			processObject((JsonObject) jsonElement, name, element);
		} else if (jsonElement.isJsonPrimitive()) {
			processPrimitive((JsonPrimitive) jsonElement, name, element);
		}
		return element;
	}

	private void processArray(JsonArray array, String name, Map parent) {
		JsonElement nested = getFirstChild(array);
		parent.put(NODETYPE, NODETYPE_COLLECTION);
		List children = new ArrayList();
		parent.put(CHILDREN, children);
		if (nested != null) {
			if (nested.isJsonPrimitive()) {
				processPrimitive(((JsonPrimitive) nested), name, parent);
				parent.put(FIELDTYPE, "list_" + getType(((JsonPrimitive) nested)));
			} else if (nested.isJsonObject()) {
				for (Map.Entry entry : ((JsonObject) nested).entrySet()) {
					Map child = processElement((JsonElement) entry.getValue(), (String) entry.getKey(), parent);
					children.add(child);
				}
			} else if (nested.isJsonArray()) {
				Map child = processElement(nested, name, parent);
				children.add(child);
			}
		}
	}

	private void processObject(JsonObject object, String name, Map parent) {
		parent.put(NODETYPE, NODETYPE_ELEMENT);
		List children = new ArrayList();
		parent.put(CHILDREN, children);
		for (Map.Entry entry : object.entrySet()) {
			Map child = processElement((JsonElement) entry.getValue(), (String) entry.getKey(), parent);
			children.add(child);
		}
	}

	private void processPrimitive(JsonPrimitive primitive, String name, Map parent) {
		parent.put(NODETYPE, NODETYPE_ATTRIBUTE);
		parent.put(FIELDTYPE, getType(primitive));
	}

	private String getType(JsonPrimitive primitive) {
		if (primitive.isString())
			return "string";
		if (primitive.isBoolean())
			return "boolean";
		if (primitive.isNumber()) {
			System.out.println("primitive:" + primitive.getAsLong() + "/" + primitive.getAsDouble());
			return "double";
		}
		return "string";
	}

	private JsonElement getFirstChild(JsonArray array) {
		Iterator i = array.iterator();
		if (i.hasNext()) {
			return (JsonElement) i.next();
		}
		return null;
	}
}
