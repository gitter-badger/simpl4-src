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
import org.ms123.common.docbook.rendering.ExpressionEvaluatingXMLReader;
import org.ms123.common.docbook.rendering.XslURIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import com.ctc.wstx.sax.*;
import com.ctc.wstx.api.ReaderConfig;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.stream.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.ms123.common.git.GitService;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;

abstract class BaseRenderer<T extends BaseRenderer<T>> implements Renderer<T> {

	protected BundleContext m_bundleContect;

	protected GitService m_gitService;

	protected String m_namespace;

	protected static Templates m_templates;

	protected static int m_useConter = 0;

	private static final Logger log = LoggerFactory.getLogger(BaseRenderer.class);

	protected InputStream xmlResource;

	protected Map<String, String> params = new HashMap<String, String>();

	protected Map<String, Object> vars = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	public T xml(InputStream xmlResource) {
		this.xmlResource = xmlResource;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T parameter(String name, String value) {
		this.params.put(name, value);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T parameters(Map<String, String> parameters) {
		if (parameters != null)
			this.params.putAll(parameters);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T variable(String name, Object value) {
		this.vars.put(name, value);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T variables(Map<String, Object> values) {
		if (values != null)
			this.vars.putAll(values);
		return (T) this;
	}

	public void render(OutputStream os) throws Exception {
		assertNotNull(xmlResource, "Value of the xml source should be not null!");
		ByteArrayOutputStream xsltResult = new ByteArrayOutputStream();
		ByteArrayOutputStream headerFooterResult = new ByteArrayOutputStream();
		InputStream xmlInputStream = null;
		try {
			SAXParserFactory factory = createParserFactory();
			final XMLReader reader = factory.newSAXParser().getXMLReader();
			ReaderConfig rc = ((WstxSAXParser) reader).getStaxConfig();
			rc.setXMLResolver(new XslURIResolver(m_bundleContect, factory));
			// prepare xml sax source
			ExpressionEvaluatingXMLReader piReader = new ExpressionEvaluatingXMLReader(reader, vars);
			SAXSource source = new SAXSource(reader, new InputSource(xmlResource));
			// create transofrmer and do transformation
			final Transformer transformer = createTransformer(factory, xmlResource);
			transformer.transform(source, new StreamResult(xsltResult));
			headerFooterProcess(new ByteArrayInputStream(xsltResult.toByteArray()), headerFooterResult);
			// do post processing
			postProcess(xmlInputStream, new ByteArrayInputStream(headerFooterResult.toByteArray()), os);
			xsltResult.close();
		} catch (SAXException e) {
			throw new Docbook4JException("Error transofrming xml!", e);
		} catch (ParserConfigurationException e) {
			throw new Docbook4JException("Error transofrming xml!", e);
		} catch (TransformerException e) {
			throw new Docbook4JException("Error transofrming xml!", e);
		} catch (IOException e) {
			throw new Docbook4JException("Error transofrming xml !", e);
		} finally {
		}
	}

	protected void headerFooterProcess(InputStream xsltResult, OutputStream result) throws Exception {
	}

	public void fopRender(InputStream fo, OutputStream fopResult) throws Docbook4JException {
		postProcess(null,fo,fopResult);
	}

	protected void postProcess(InputStream xmlSource, InputStream xsltResult, OutputStream fopResult) throws Docbook4JException {
	}

	protected SAXParserFactory createParserFactory() {
		SAXParserFactory factory = SAXParserFactory.newInstance("com.ctc.wstx.sax.WstxSAXParserFactory", null);
		factory.setNamespaceAware(true);
		return factory;
	}

	protected TransformerFactory createTransformerFactory() {
		return TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
	}

	protected Transformer createTransformer(SAXParserFactory f, InputStream xmlSource) throws TransformerConfigurationException, Exception {
		long startTime = new java.util.Date().getTime();
		if (m_useConter > 50) {
			m_templates = null;
			m_useConter = 0;
		}
		m_useConter++;
		if (m_templates == null) {
			TransformerFactory transformerFactory = createTransformerFactory();
			transformerFactory.setURIResolver(new XslURIResolver(m_bundleContect, f));
			InputStream xsl = getDefaultXslStylesheet();
			Source source = new StreamSource(xsl);
			m_templates = transformerFactory.newTemplates(source);
		}
		long endTime = new java.util.Date().getTime();
		Transformer transformer = m_templates.newTransformer();
		//transformer.setParameter("use.extensions", "1");
		transformer.setParameter("callout.graphics", "0");
		transformer.setParameter("callout.unicode", "1");
		transformer.setParameter("fop1.extensions", "1");
		//@@@MS needs maybe more investigations
		transformer.setParameter("callouts.extension", "1");
		info(this,"createTransformer:" + params);

		if( params.size() == 0){
			params.put("body.font.master","8");
			params.put("body.margin.bottom","20mm");
			params.put("body.margin.bottom.even","20mm");
			params.put("body.margin.bottom.odd","20mm");
			params.put("body.margin.top","20mm");
			params.put("body.margin.top.even","20mm");
			params.put("body.margin.top.odd","20mm");
			params.put("page.margin.bottom","5mm");
			params.put("page.margin.bottom.even","5mm");
			params.put("page.margin.bottom.odd","5mm");
			params.put("page.margin.inner","20mm");
			params.put("page.margin.outer","20mm");
			params.put("page.margin.top","5mm");
			params.put("page.margin.top.even","5mm");
			params.put("page.margin.top.odd","5mm");
			params.put("page.orientation","portrait");
			params.put("paper.type","A4");
			params.put("region.after.extent","20mm");
			params.put("region.after.extent.even","20mm");
			params.put("region.after.extent.odd","20mm");
			params.put("region.before.extent","0mm");
			params.put("region.before.extent.even","0mm");
			params.put("region.before.extent.odd","0mm");
			params.put("table.cell.border.color","#777777");
			params.put("table.cell.border.style","solid");
			params.put("table.cell.border.thickness","0.4pt");
			params.put("table.frame.border.color","#777777");
			params.put("table.frame.border.style","solid");
			params.put("table.frame.border.thickness","0.4pt");
		}else{
			if( params.get("page.margin.bottom.even")==null){
				params.put("page.margin.bottom.even", params.get("page.margin.bottom") );
				params.put("page.margin.bottom.odd", params.get("page.margin.bottom") );
			}
			if( params.get("page.margin.bottom.even")==null){
				params.put("page.margin.top.even", params.get("page.margin.top") );
				params.put("page.margin.top.odd", params.get("page.margin.top") );
			}
			if( params.get("body.margin.bottom.even")==null){
				params.put("body.margin.bottom.even", params.get("body.margin.bottom") );
				params.put("body.margin.bottom.odd", params.get("body.margin.bottom") );
			}
			if( params.get("body.margin.top.even")==null){
				params.put("body.margin.top.even", params.get("body.margin.top") );
				params.put("body.margin.top.odd", params.get("body.margin.top") );
			}
			if( params.get("region.before.extent.even")==null){
				params.put("region.before.extent.even", params.get("region.before.extent") );
				params.put("region.before.extent.odd", params.get("region.before.extent") );
			}
			if( params.get("region.after.extent.even")==null){
				params.put("region.after.extent.even", params.get("region.after.extent") );
				params.put("region.after.extent.odd", params.get("region.after.extent") );
			}
		}


		for (Map.Entry<String, String> entry : this.params.entrySet()) {
			transformer.setParameter(entry.getKey(), entry.getValue());
		}
		return transformer;
	}

	protected abstract InputStream getDefaultXslStylesheet() throws Exception;

	protected InputStream resolveXslStylesheet(String location) {
		return null;
	}

	protected void assertNotNull(Object value, String message) {
		if (value == null)
			throw new IllegalArgumentException(message);
	}
}
