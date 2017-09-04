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
package org.ms123.common.utils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;

public class PrintJNDIContext {

	public void print(Context context, String ct) {
		printJNDITree(context, ct);
	}

	private void printJNDITree(Context context, String ct) {
		try {
			printNE(context, context.list(ct), ct);
		} catch (Exception e) {
			System.out.println("printJNDITree.error:" + e);
			e.printStackTrace();
		}
	}

	private void printNE(Context context, NamingEnumeration ne, String parentctx) throws Exception {
		while (ne.hasMoreElements()) {
			NameClassPair next = (NameClassPair) ne.nextElement();
			printEntry(next);
			increaseIndent();
			printJNDITree(context, (parentctx.length() == 0) ? next.getName() : parentctx + "/" + next.getName());
			decreaseIndent();
		}
	}

	private void printEntry(NameClassPair next) {
		System.out.println(printIndent() + "-->" + next);
	}

	private int indentLevel = 0;

	private void increaseIndent() {
		indentLevel += 2;
	}

	private void decreaseIndent() {
		indentLevel -= 2;
	}

	private String printIndent() {
		StringBuffer buf = new StringBuffer(indentLevel);
		for (int i = 0; i < indentLevel; i++) {
			buf.append(" ");
		}
		return buf.toString();
	}
}
