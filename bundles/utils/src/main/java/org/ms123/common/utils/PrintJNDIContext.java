/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
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
