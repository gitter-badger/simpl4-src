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
package org.ms123.common.docbook.wikimodel;

import java.util.*;
import java.io.*;
import nu.xom.*;
import org.wikimodel.wem.*;
import org.ms123.common.docbook.Context;
import org.ms123.common.docbook.Utils;
import org.ms123.common.docbook.xom.html5.*;
import flexjson.*;

@SuppressWarnings("unchecked")
public class TocMacroRenderer extends AbstractMacroRenderer {

	public TocMacroRenderer(Context ctx, WikiParameters params, String content) {
		super(ctx, params, content);
	}

	public String render() {
		WikiParameter p = m_params.getParameter("id");
		int start = getInteger(m_params.getParameter("start"), 2);
		int depth = getInteger(m_params.getParameter("depth"), 5);
		boolean isNumbered = getBoolean(m_params.getParameter("numbered"), false);
		System.out.println("generateTree:" + start + "/" + depth + "/" + isNumbered);

		Base b = generateTree((List<Map>) m_ctx.get("headerList"), start, depth, isNumbered);
		String s = Utils.xomToString(b.toXom());
		System.out.println("TOC:" + s);
		m_ctx.set("toc", removeHeaders((List<Map>) m_ctx.get("headerList"),start,depth));
		return s;
	}

	private List<Map> removeHeaders(List<Map> headers,int start, int depth){
		List<Map> newList = new ArrayList();
		int min = 1000;
		for (Map headerMap : headers) {
			int headerLevel = (Integer) headerMap.get("level");
			if (headerLevel >= start && headerLevel <= depth) {
				min = Math.min(min,headerLevel);	
				newList.add(headerMap);
			}
		}
		for (Map headerMap : newList) {
			int headerLevel = (Integer) headerMap.get("level");
			headerMap.put("level", headerLevel-min);
		}
		return newList;
	}

	private Base generateTree(List<Map> headers, int start, int depth, boolean numbered) {
		Base tocBlock = null;
		int currentLevel = start - 1;
		Base currentBlock = null;
		for (Map headerMap : headers) {
			int headerLevel = (Integer) headerMap.get("level");
			if (headerLevel >= start && headerLevel <= depth) {
				if (currentLevel < headerLevel) {
					while (currentLevel < headerLevel) {
						if (currentBlock instanceof Ul) {
							currentBlock = addLi(currentBlock, null);
						}
						currentBlock = createChildList(numbered, currentBlock);
						++currentLevel;
					}
				} else {
					while (currentLevel > headerLevel) {
						currentBlock = currentBlock.getParent().getParent();
						--currentLevel;
					}
					currentBlock = currentBlock.getParent();
				}
				currentBlock = addLi(currentBlock, headerMap);
			}
		}
		while (currentBlock != null) {
			tocBlock = currentBlock;
			currentBlock = currentBlock.getParent();
		}
		if( tocBlock == null){
			tocBlock = new Div();
		}
		tocBlock.setClass("toc");
		return tocBlock;
	}

	private Base addLi(Base currentBlock, Map headerMap) {
		Li li = headerMap == null ? createEmptyTocEntry() : createTocEntry(headerMap);
		currentBlock.add(li);
		return li;
	}

	private Li createEmptyTocEntry() {
		return new Li();
	}

	private Li createTocEntry(Map headerMap) {
		System.out.println("\tCreateTocEntry:" + headerMap);
		Element anchor = null;
		try {
			String label = (String) headerMap.get("label");
			Document doc = new Builder().build("<div><a href=\"#"+headerMap.get("id")+"\">" + label + "</a></div>", null);
			anchor = (Element) doc.getRootElement().getChild(0);
			Utils.setNamespace(anchor);
			anchor.detach();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Li li = new Li();
		String icon = (String) headerMap.get("icon");
		if (icon != null) {
			Img img = new Img(icon);
			li.add(img);
		}
		li.add(anchor);
		return li;
	}

	private Base createChildList(boolean numbered, Base parentBlock) {
		System.out.println("createChildList:" + parentBlock);
		Base child = numbered ? new Ol() : new Ul();
		if (parentBlock != null) {
			parentBlock.add(child);
		}
		return child;
	}

}
