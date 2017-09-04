/*
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
/*
*/

/**
	* @ignore(Hash)
*/
qx.Class.define("ms123.datamapper.Config", {
	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {

		EVENT_INPUTTREE_CREATED: "inputTreeCreated",
		EVENT_OUTPUTTREE_CREATED: "outputTreeCreated",
		EVENT_MAPPING_CHANGED: "mappingChanged",
		EVENT_TREE_CHANGED: "treeChanged",
		NODETYPE_ATTRIBUTE : "ntAttribute",
		NODETYPE_ELEMENT : "ntElement",
		NODETYPE_COLLECTION : "ntCollection",
		NODENAME : "name",
		NODELABEL : "label",
		NODETYPE : "label",
		FIELDTYPE : "fieldType",
		INPUT : "input",
		OUTPUT : "output",
		USE_IMPORT:"import",
		USE_CAMEL:"camel",
		PATH_DELIM:'/',
		KIND_LIKE_INPUT: "like_input",
		KIND_USER_DEFINED: "user_defined",
		KIND_FROM_SAMPLE: "from_example",
		BG_COLOR_STRUCTURE_CONNECTED: "#E0E0E0",
		BG_COLOR_STRUCTURE_SELECTED: "#E0E0E0",
		BG_COLOR_ATTRIBUTE_CONNECTED: "#F0F0F0",
		BG_COLOR_READY: "#aff97a",
		BG_COLOR_NOTREADY: "#edeaea",
		STRUCTURE_MAPPING:"structureMapping",
		ATTRIBUTE_MAPPING:"attributeMapping",
		STRUCTURE_SCOPE:"collectionScope",
		ATTRIBUTE_SCOPE:"attributeScope",
		STRUCTURE_LINECOLOR:"red",
		ATTRIBUTE_LINECOLOR:"blue",
		MAPPING_PARAM:"mapping",
		TREE_LABEL_COLOR:"black",
		TREE_LABEL_SELECTED_COLOR:"blue",
		IDPREFIX : "dm_",
		ID_INPREFIX : "dm_input",
		ID_OUTPREFIX : "dm_output",
		FORMAT_JSON : "json",
		FORMAT_XML : "xml",
		FORMAT_FW : "fw",
		FORMAT_MAP : "map",
		FORMAT_CSV : "csv",
		FORMAT_POJO : "pojo",
		FORMAT_EXCEL : "excel",
		EXAMPLE: "xxx"

	}
});
