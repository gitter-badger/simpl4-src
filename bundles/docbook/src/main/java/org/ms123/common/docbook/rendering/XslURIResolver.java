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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.*;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.osgi.framework.BundleContext;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParserFactory;
import com.ctc.wstx.sax.*;
import com.ctc.wstx.api.ReaderConfig;

public class XslURIResolver implements URIResolver, javax.xml.stream.XMLResolver {

	protected BundleContext m_bundleContect;

	protected SAXParserFactory m_saxParserFactory;

	private static final String defaultXslStylesheetBase = "/xsl/docbook/fo/";

	private static final String defaultXslStylesheetBase2 = "/xsl/docbook/common/";

	private static final String defaultXslStylesheetBase3 = "/xsl/";

	public XslURIResolver(BundleContext bc, SAXParserFactory f) {
		m_bundleContect = bc;
		m_saxParserFactory = f;
	}

	private static final Logger log = LoggerFactory.getLogger(XslURIResolver.class);

	private String docbookXslBase;

	public Source resolve(String href, String base) throws TransformerException {
		if (href == null || href.trim().length() == 0) {
			throw new TransformerException("href is null");
		}
		try {
			String aname = defaultXslStylesheetBase + href;
			if (aname.indexOf("..") != -1) {
				aname = new File(aname).getCanonicalPath().toString();
			}
			if (m_bundleContect.getBundle().getEntry(aname) == null) {
				aname = defaultXslStylesheetBase2 + href;
			}
			if (m_bundleContect.getBundle().getEntry(aname) == null) {
				aname = defaultXslStylesheetBase3 + href;
			}
			SAXSource ss = new SAXSource(new InputSource(m_bundleContect.getBundle().getEntry(aname).openStream()));
			XMLReader reader = m_saxParserFactory.newSAXParser().getXMLReader();
			ReaderConfig rc = ((WstxSAXParser) reader).getStaxConfig();
			rc.setXMLResolver(new XslURIResolver(m_bundleContect, m_saxParserFactory));
			ss.setXMLReader(reader);
			return ss;
		} catch (Exception e) {
			throw new RuntimeException("XslURIResolver.resolve", e);
		}
	}

	public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) {
		try {
			String aname = defaultXslStylesheetBase + systemID;
			if (aname.indexOf("..") != -1) {
				aname = new File(aname).getCanonicalPath().toString();
			}
			if (m_bundleContect.getBundle().getEntry(aname) == null) {
				aname = defaultXslStylesheetBase2 + systemID;
			}
			if (m_bundleContect.getBundle().getEntry(aname) == null) {
				aname = defaultXslStylesheetBase3 + systemID;
			}
			StreamSource ss = new StreamSource(m_bundleContect.getBundle().getEntry(aname).openStream());
			return ss;
		} catch (Exception e) {
			throw new RuntimeException("XslURIResolver.resolveEntity", e);
		}
	}
}
