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
package org.ms123.common.docbook.rendering;

import org.ms123.common.docbook.rendering.Docbook4JException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.XMLReader;
import nu.xom.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.isDebugEnabled;

//import org.apache.xmlgraphics.image.loader.spi.*;
@SuppressWarnings("unchecked")
abstract class FORenderer<T extends FORenderer<T>> extends BaseRenderer<T> {

	//private static final String defaultXslStylesheet = "/xsl/docbook/fo/docbook.xsl";
	private static final String defaultXslStylesheet = "/xsl/sw-fo.xsl";

	@Override
	protected InputStream getDefaultXslStylesheet() throws Exception {
		return m_bundleContect.getBundle().getEntry(defaultXslStylesheet).openStream();
	}

	@Override
	protected void headerFooterProcess(InputStream inputStream, OutputStream result) throws Exception {
		long startTime = new Date().getTime();
		SAXParserFactory factory = createParserFactory();
		final XMLReader reader = factory.newSAXParser().getXMLReader();
		Builder builder = new Builder(reader);
		Document doc = builder.build(inputStream);
		Serializer ser = new Serializer(result);
		ser.setIndent(2);
		ser.setLineSeparator("\n");
		XPathContext pc = new XPathContext();
		pc.addNamespace("fo", "http://www.w3.org/1999/XSL/Format");
		Nodes nodes = doc.query("//fo:block[starts-with(@id,'hf_')]", pc);
		int idc = 1;
		for (int i = 0; i < nodes.size(); i++) {
			Element newHeaderElement = (Element) nodes.get(i);
			newHeaderElement.getParent().removeChild(newHeaderElement);
			String id = newHeaderElement.getAttribute("id").getValue();
			boolean isHeader = getIsHeader(id);
			String pages = getPages(id);
			List<Element> regionNodes = getRegionsNodes(isHeader, pages, doc, pc);
			for (Element n : regionNodes) {
				n.removeChildren();
				Element ne = (Element) newHeaderElement.copy();
				uniqueIds(ne, idc++, pc);
				n.appendChild(ne);
			}
		}
		ser.write(doc);
		long endTime = new Date().getTime();

		if(isDebugEnabled(this)){
			ser = new Serializer(System.out);
			ser.setLineSeparator("\n");
			ser.setIndent(2);
			System.out.println("FOP:");
			ser.write(doc);
		}
	}

	private List<Element> getRegionsNodes(boolean isHeader, String pages, Document doc, XPathContext pc) {
		List<Element> nodeList = new ArrayList();
		String where = isHeader ? "before" : "after";
		if (("first".equals(pages) || "all".equals(pages))) {
			Nodes nodes = doc.query("//fo:static-content[@flow-name='xsl-region-" + where + "-first']", pc);
			nodeList.add((Element) nodes.get(0));
		}
		if (("odd".equals(pages) || "all".equals(pages) || "allbf".equals(pages))) {
			Nodes nodes = doc.query("//fo:static-content[@flow-name='xsl-region-" + where + "-odd']", pc);
			nodeList.add((Element) nodes.get(0));
		}
		if (("even".equals(pages) || "all".equals(pages) || "allbf".equals(pages))) {
			Nodes nodes = doc.query("//fo:static-content[@flow-name='xsl-region-" + where + "-even']", pc);
			nodeList.add((Element) nodes.get(0));
		}
		return nodeList;
	}

	private void uniqueIds(Element e, int gId, XPathContext pc) {
		e.getAttribute("id").setValue("SW" + gId + "_" + 0);
		Nodes nodes = e.query("//*[@id]", pc);
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			if (n instanceof Element) {
				((Element) n).getAttribute("id").setValue("SW" + gId + "_" + (i + 1));
			}
		}
	}

	private boolean getIsHeader(String id) {
		String s[] = id.split("_");
		return s[1].equals("header") ? true : false;
	}

	private String getPages(String id) {
		String s[] = id.split("_");
		return s[2];
	}

	@Override
	protected synchronized void postProcess(final InputStream xmlSource, final InputStream xsltResult, OutputStream fopResult) throws Docbook4JException {
		try {
			FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI(), new FOURIResolver(m_gitService, m_namespace));
			builder.setStrictFOValidation(false);
			FopFactory fopFactory = builder.build();

			final FOUserAgent userAgent = fopFactory.newFOUserAgent();
			enhanceFOUserAgent(userAgent);
			Fop fop = fopFactory.newFop(getMimeType(), userAgent, fopResult);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();

			transformer.setParameter("use.extensions", "1");
			transformer.setParameter("fop.extensions", "0");
			transformer.setParameter("fop1.extensions", "1");
			Source src = new StreamSource(xsltResult);
			Result res = new SAXResult(fop.getDefaultHandler());
			transformer.transform(src, res);
		} catch (TransformerException e) {
			throw new Docbook4JException("Error transforming fo to pdf!", e);
		} catch (FOPException e) {
			throw new Docbook4JException("Error transforming fo to pdf!", e);
		} finally {
		}
	}

	protected void enhanceFOUserAgent(FOUserAgent userAgent) {
	}

	protected abstract String getMimeType();
}
