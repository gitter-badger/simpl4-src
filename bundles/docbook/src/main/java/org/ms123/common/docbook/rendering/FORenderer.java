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

		ser = new Serializer(System.out);
		ser.setLineSeparator("\n");
		ser.setIndent(2);
//		System.out.println("FOP:");
//		ser.write(doc);
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
