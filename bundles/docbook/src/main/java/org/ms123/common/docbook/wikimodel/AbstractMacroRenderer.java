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
import org.wikimodel.wem.*;
import org.ms123.common.docbook.Context;
import org.wikimodel.wem.xwiki.XWikiParser;
import java.io.StringReader;
import org.wikimodel.wem.xhtml.*;

public abstract class AbstractMacroRenderer implements IWikiMacroRenderer{
	protected WikiParameters m_params;
	protected String m_content;
	protected Context m_ctx;
	public AbstractMacroRenderer(Context ctx, WikiParameters params, String content){
		m_ctx = ctx;
		m_params = params;
		m_content = content;
	}
	public abstract String render();
	protected static class WikiSink implements IWikiPrinter {
		private StringBuffer m_sb = new StringBuffer();
		public void print(String str) {
			m_sb.append(str);
		}
		public void println(String str) {
			m_sb.append(str + "\n");
		}
		public String toString() {
			return m_sb.toString();
		}
	}
	protected String  parse(){
		WikiSink ws = new WikiSink();
		IWikiParser parser = new XWikiParser();
		try{
			parser.parse(new StringReader(m_content), new WemPrintListener(m_ctx,ws){
				public void beginSection(int docLevel, int headerLevel, WikiParameters params) {
				}
				public void endSection(int docLevel, int headerLevel, WikiParameters params) {
				}
			});
		}catch(Exception e){
			e.printStackTrace();
			return "<div class=\"syntaxerror\">Parse1 Error:"+e.getMessage()+"</div>";
		}
		return ws.toString();
	}
	protected String  parse(String s){
		WikiSink ws = new WikiSink();
		IWikiParser parser = new XWikiParser();
		try{
			parser.parse(new StringReader(s), new WemPrintListener(m_ctx,ws){
				public void beginSection(int docLevel, int headerLevel, WikiParameters params) {
				}
				public void endSection(int docLevel, int headerLevel, WikiParameters params) {
				}
			});
		}catch(Exception e){
			e.printStackTrace();
			return "<div class=\"syntaxerror\">Parse2 Error:"+e.getMessage()+"</div>";
		}
		return ws.toString();
	}
	public String getJS(){
		return null;
	}
	protected boolean getBoolean(WikiParameter p, boolean def) {
		try {
			return new Boolean(p.getValue());
		} catch (Exception e) {
			return def;
		}
	}
	protected int getInteger(WikiParameter p, int def) {
		try {
			return new Integer(p.getValue());
		} catch (Exception e) {
			return def;
		}
	}
	protected String getString(WikiParameter p, String def) {
		try {
			return String.valueOf(p.getValue());
		} catch (Exception e) {
			return def;
		}
	}
}
