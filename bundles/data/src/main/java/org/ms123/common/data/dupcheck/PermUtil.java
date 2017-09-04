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
package org.ms123.common.data.dupcheck;

import java.util.*;

public class PermUtil {

	private String[] arr;

	private int[] permSwappings;

	private int MAX = 3;

	public PermUtil(String str) {
		String[] arr = PermUtil.splitString(str);
		if (arr.length > MAX) {
			String narr[] = new String[MAX];
			for (int i = 0; i < (MAX - 1); i++) {
				narr[i] = arr[i];
			}
			String carr[] = new String[arr.length - (MAX - 1)];
			for (int i = 0; i < arr.length - (MAX - 1); i++) {
				carr[i] = arr[i + (MAX - 1)];
			}
			narr[MAX - 1] = arrayToString(carr, " ");
			arr = narr;
		}
		this.arr = arr.clone();
		this.permSwappings = new int[arr.length];
		for (int i = 0; i < permSwappings.length; i++) {
			permSwappings[i] = i;
		}
	}

	public String next() {
		if (arr == null) {
			return null;
		}
		String[] res = Arrays.copyOf(arr, permSwappings.length);
		// Prepare next 
		int i = permSwappings.length - 1;
		while (i >= 0 && permSwappings[i] == arr.length - 1) {
			swap(i, permSwappings[i]);
			// Undo the swap represented by permSwappings[i] 
			permSwappings[i] = i;
			i--;
		}
		if (i < 0) {
			arr = null;
		} else {
			int prev = permSwappings[i];
			swap(i, prev);
			int next = prev + 1;
			permSwappings[i] = next;
			swap(i, next);
		}
		return arrayToString(res, " ");
	}

	private void swap(int i, int j) {
		String tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}

	private static int indexOfAny(String str, char[] searchChars) {
		if (str == null) {
			return -1;
		}
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			for (char searchChar : searchChars) {
				if (searchChar == ch) {
					return i;
				}
			}
		}
		return -1;
	}

	private static String arrayToString(String[] arr, String separator) {
		if (arr.length == 1)
			return arr[0];
		StringBuffer result = new StringBuffer();
		if (arr.length > 0) {
			result.append(arr[0]);
			for (int i = 1; i < arr.length; i++) {
				result.append(separator);
				result.append(arr[i]);
			}
		}
		return result.toString();
	}

	private static char[] delimeters = { ' ', '-', '+', ',', '/' };

	public static String[] splitString(String s) {
		if (indexOfAny(s, delimeters) == -1) {
			String[] sa = new String[1];
			sa[0] = s;
			return sa;
		}
		StringTokenizer st = new StringTokenizer(s, " +-,/");
		String[] sp = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			sp[i++] = st.nextToken();
		}
		return sp;
	}

	public static void main(String args[]) {
		PermUtil pu = new PermUtil("eins-zwei+drei vier fünf");
		while (true) {
			String p = pu.next();
			if (p == null) {
				break;
			}
			System.out.println(p);
		}
	}
}
