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

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;
import java.io.Reader;
import java.io.File;

/**
 *
 */
public interface  Constants {

	static String NODETYPE_ATTRIBUTE = "ntAttribute";

	static String NODETYPE_ELEMENT = "ntElement";

	static String NODETYPE_COLLECTION = "ntCollection";


	static String FIELDTYPE = "fieldType";
	static String FIELDTYPE_STRING = "string";
	static String FIELDTYPE_DATE = "date";
	static String FIELDTYPE_CALENDAR = "calendar";
	static String FIELDTYPE_INTEGER = "integer";
	static String FIELDTYPE_LONG = "long";
	static String FIELDTYPE_DOUBLE = "double";
	static String FIELDTYPE_DECIMAL = "decimal";
	static String FIELDTYPE_BYTE = "byte";
	static String FIELDTYPE_BOOLEAN = "boolean";


	static String NODETYPE = "type";
	static String NODENAME = "name";
	static String NODELABEL = "label";

	static String CHILDREN = "children";

	static String ID = "id";

	static String ROOT = "root";

	static String FORMAT = "format";

	static String FORMAT_JSON = "json";
	static String FORMAT_XML = "xml";
	static String FORMAT_MAP = "map";
	static String FORMAT_CSV = "csv";
	static String FORMAT_POJO = "pojo";

	static String MAP_ROOT = "map";
	static String JSON_ROOT = "json";
	static String CSV_ROOT = "csv-set";
	static String CSV_RECORD = "record";
	static String CSV_DELIM = "columnDelim";
	static String CSV_QUOTE = "quote";
	static String CSV_HEADER = "header";

	static String TRANSFORMER_CONTEXT = "__transformerContext";

	static String SCRIPT_NAME = "__scriptName";
	static String SCRIPT_SOURCE = "__scriptSource";
	static String DATAMAPPER_CONFIG = "__datamapperConfig";

	static String INPUT = "input";

	//In Smooks set
	static String PROPERTY_NAME = "__propertyName";

	static String DECODER = "__decoder";

	static String DATAOBJECT = "__dataObject";
}
