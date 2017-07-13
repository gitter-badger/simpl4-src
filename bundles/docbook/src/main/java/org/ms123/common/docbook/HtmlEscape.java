/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014,2017] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
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

