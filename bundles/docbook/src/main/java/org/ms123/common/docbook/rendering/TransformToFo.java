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
package org.ms123.common.docbook.rendering;

import com.ctc.wstx.api.ReaderConfig;
import com.ctc.wstx.sax.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.osgi.framework.BundleContext;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.isDebugEnabled;

//import org.apache.xmlgraphics.image.loader.spi.*;
@SuppressWarnings("unchecked")
public class TransformToFo {
	private BundleContext bundleContect;

	private static final String defaultXslStylesheet = "/xsl/xhtml-to-xslfo.xsl";
	private TransformToFo(BundleContext bc) {
		this.bundleContect = bc;
	}

	public static final synchronized TransformToFo create(BundleContext bc) {
		return new TransformToFo(bc);
	}

	private InputStream getDefaultXslStylesheet() throws Exception {
		return this.bundleContect.getBundle().getEntry(defaultXslStylesheet).openStream();
	}

	public void transform(InputStream xmlResource, OutputStream os) throws Exception {
		try {
			SAXParserFactory factory = createParserFactory();
			final XMLReader reader = factory.newSAXParser().getXMLReader();
			ReaderConfig rc = ((WstxSAXParser) reader).getStaxConfig();
			rc.setXMLResolver(new XslURIResolver(this.bundleContect, factory));

			final Transformer transformer = createTransformer(factory);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			SAXSource source = new SAXSource(reader, new InputSource(xmlResource));
			transformer.transform(source, new StreamResult(os));
		} catch (Exception e) {
			throw new RuntimeException("TransformToFo.transform.error!", e);
		} finally {
		}
	}

	private static Templates templates;
	private Transformer createTransformer(SAXParserFactory f) throws TransformerConfigurationException, Exception {
		if (this.templates == null) {
			TransformerFactory transformerFactory = createTransformerFactory();
			transformerFactory.setURIResolver(new XslURIResolver(this.bundleContect, f));
			InputStream xsl = getDefaultXslStylesheet();
			Source source = new StreamSource(xsl);
			this.templates = transformerFactory.newTemplates(source);
		}
		return this.templates.newTransformer();
	}
	private SAXParserFactory createParserFactory() {
		SAXParserFactory factory = SAXParserFactory.newInstance("com.ctc.wstx.sax.WstxSAXParserFactory", null);
		factory.setNamespaceAware(true);
		return factory;
	}

	private TransformerFactory createTransformerFactory() {
		return TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
	}
}
