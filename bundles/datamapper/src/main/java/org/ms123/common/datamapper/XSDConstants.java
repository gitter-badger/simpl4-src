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
import javax.xml.namespace.QName;

/**
 * Constants
 */
public  interface XSDConstants {

	public static final String URI_2001_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";
	public static final QName XSD_ANY = new QName(URI_2001_SCHEMA_XSD, "any");
	public static final QName XSD_BASE64 = new QName(URI_2001_SCHEMA_XSD, "base64Binary");
	public static final QName XSD_BOOLEAN = new QName(URI_2001_SCHEMA_XSD, "boolean");
	public static final QName XSD_BYTE = new QName(URI_2001_SCHEMA_XSD, "byte");
	public static final QName XSD_DATE = new QName(URI_2001_SCHEMA_XSD, "date");
	public static final QName XSD_DATETIME = new QName(URI_2001_SCHEMA_XSD, "dateTime");
	public static final QName XSD_DECIMAL = new QName(URI_2001_SCHEMA_XSD, "decimal");
	public static final QName XSD_DOUBLE = new QName(URI_2001_SCHEMA_XSD, "double");
	public static final QName XSD_FLOAT = new QName(URI_2001_SCHEMA_XSD, "float");
	public static final QName XSD_HEXBIN = new QName(URI_2001_SCHEMA_XSD, "hexBinary");
	public static final QName XSD_INT = new QName(URI_2001_SCHEMA_XSD, "int");
	public static final QName XSD_INTEGER = new QName(URI_2001_SCHEMA_XSD, "integer");
	public static final QName XSD_LONG = new QName(URI_2001_SCHEMA_XSD, "long");
	public static final QName XSD_NEGATIVEINTEGER = new QName(URI_2001_SCHEMA_XSD, "negativeInteger");
	public static final QName XSD_NONNEGATIVEINTEGER = new QName(URI_2001_SCHEMA_XSD, "nonNegativeInteger");
	public static final QName XSD_NONPOSITIVEINTEGER = new QName(URI_2001_SCHEMA_XSD, "nonPositiveInteger");
	public static final QName XSD_NORMALIZEDSTRING = new QName(URI_2001_SCHEMA_XSD, "normalizedString");
	public static final QName XSD_POSITIVEINTEGER = new QName(URI_2001_SCHEMA_XSD, "positiveInteger");
	public static final QName XSD_SHORT = new QName(URI_2001_SCHEMA_XSD, "short");
	public static final QName XSD_STRING = new QName(URI_2001_SCHEMA_XSD, "string");
	public static final QName XSD_UNSIGNEDBYTE = new QName(URI_2001_SCHEMA_XSD, "unsignedByte");
	public static final QName XSD_UNSIGNEDINT = new QName(URI_2001_SCHEMA_XSD, "unsignedInt");
	public static final QName XSD_UNSIGNEDLONG = new QName(URI_2001_SCHEMA_XSD, "unsignedLong");
	public static final QName XSD_UNSIGNEDSHORT = new QName(URI_2001_SCHEMA_XSD, "unsignedShort");

}
