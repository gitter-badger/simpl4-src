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
/**
 */
qx.Class.define("ms123.util.Xml2Json", {
	extend: qx.core.Object,

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function () {},

	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {
		NODE_TYPES: {
			Element: 1,
			Attribute: 2,
			Text: 3,
			CDATA: 4,
			Root: 9,
			Fragment: 11
		},
		parseXMLString: function (strXML) {
			var xmlDoc = null,
				out = null,
				isParsed = true;
			try {
				xmlDoc = ("DOMParser" in window) ? new DOMParser() : new ActiveXObject("MSXML2.DOMDocument");
				xmlDoc.async = false;
			} catch (e) {
				throw new Error("XML Parser could not be instantiated");
			}

			if ("parseFromString" in xmlDoc) {
				out = xmlDoc.parseFromString(strXML, "text/xml");
				isParsed = (out.documentElement.tagName !== "parsererror");
			} else { //If old IE
				isParsed = xmlDoc.loadXML(strXML);
				out = (isParsed) ? xmlDoc : false;
			}
			if (!isParsed) {
				throw new Error("Error parsing XML string");
			}
			return out;
		}
	},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		prefixAttr: false,
		toLower: true,
		withText: false,
		withRoot: false,
		isXML: function (o) {
			return (typeof(o) === "object" && o.nodeType !== undefined);
		},
		getRoot: function (doc) {
			return (doc.nodeType === this.constructor.NODE_TYPES.Root) ? doc.documentElement : (doc.nodeType === this.constructor.NODE_TYPES.Fragment) ? doc.firstChild : doc;
		},
		convert: function (xml) {
			var out = {},
				xdoc = typeof(xml) === "string" ? ms123.util.Xml2Json.parseXMLString(xml) : this.isXML(xml) ? xml : undefined,
				root;
			if (!xdoc) {
				throw new Error("Unable to parse XML");
			}
			//If xdoc is just a text or CDATA return value
			if (xdoc.nodeType === this.constructor.NODE_TYPES.Text || xdoc.nodeType === this.constructor.NODE_TYPES.CDATA) {
				return xdoc.nodeValue;
			}
			root = this.getRoot(xdoc);
			var rootName = this.toLower ? root.nodeName.toLowerCase() : root.nodeName;
			out[rootName] = {};
			this.process(root, out[rootName]);
			return this.withRoot ? out : out[rootName];
		},
		process: function (node, buff) {
			var child, attr, name, att_name, value, i, j, tmp, iMax, jMax;
			if (node.hasChildNodes()) {
				iMax = node.childNodes.length;
				for (i = 0; i < iMax; i++) {
					child = node.childNodes[i];
					//Check nodeType of each child node
					switch (child.nodeType) {
					case this.constructor.NODE_TYPES.Text:
						if( this.withText){
							buff.Text = buff.Text ? buff.Text + child.nodeValue.trim() : child.nodeValue.trim();
						}
						break;
					case this.constructor.NODE_TYPES.CDATA:
						if( this.withText){
							value = child[child.text ? "text" : "nodeValue"]; //IE attributes support
							buff.Text = buff.Text ? buff.Text + value : value;
						}
						break;
					case this.constructor.NODE_TYPES.Element:
						name = child.nodeName;
						if (this.toLower) {
							name = name.toLowerCase();
						}
						tmp = {};
						//Node name already exists in the buffer and it's a NodeSet
						if (name in buff) {
							if (buff[name].length) {
								this.process(child, tmp);
								buff[name].push(tmp);
							} else { //If node exists in the parent as a single entity
								this.process(child, tmp);
								buff[name] = [buff[name], tmp];
							}
						} else { //If node does not exist in the parent
							this.process(child, tmp);
							buff[name] = tmp;
						}
						break;
					}
				}
			}
			//Populate attributes
			if (node.attributes.length) {
				for (j = node.attributes.length - 1; j >= 0; j--) {
					attr = node.attributes[j];
					att_name = attr.name.trim();
					if (this.toLower) {
						att_name = att_name.toLowerCase();
					}
					value = attr.value;
					buff[(this.prefixAttr ? "@" : "") + att_name] = value;
				}
			}
		}
	}
});
