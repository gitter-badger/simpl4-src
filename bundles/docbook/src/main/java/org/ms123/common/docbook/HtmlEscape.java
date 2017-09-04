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
package org.ms123.common.docbook;

public class HtmlEscape {

	private static char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String escape(String original) {
		if (original == null){
			return "";
		}
		StringBuffer out = new StringBuffer("");
		char[] chars = original.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			boolean found = true;
			switch (chars[i]) {
				case 38:
					out.append("&amp;");
					break; //& 
				default:
					found = false;
					break;
			}
			if (!found) {
				if (chars[i] > 127) {
					char c = chars[i];
					int a4 = c % 16;
					c = (char) (c / 16);
					int a3 = c % 16;
					c = (char) (c / 16);
					int a2 = c % 16;
					c = (char) (c / 16);
					int a1 = c % 16;
					out.append("&#x" + hex[a1] + hex[a2] + hex[a3] + hex[a4] + ";");
				} else {
					out.append(chars[i]);
				}
			}
		}
		return out.toString();
	}

}

