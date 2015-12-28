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
package org.ms123.common.camel.components.docbook;

import java.io.StringWriter;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.io.IOUtils;
import org.ms123.common.docbook.DocbookService;

@SuppressWarnings("unchecked")
public class DocbookEndpoint extends ResourceEndpoint {

	private String namespace = null ;
	private String output = null ;

	public DocbookEndpoint() {
	}

	public DocbookEndpoint(String endpointUri, Component component, String resourceUri) {
		super(endpointUri, component, resourceUri);
		info("DocbookEndpoint:endpointUri:" + endpointUri + "/resourceUri:" + resourceUri);
	}

	public void setNamespace(String ns){
		 this.namespace = ns;
	}

	public String getNamespace(){
		 return this.namespace;
	}

	public void setOutput(String ns){
		 this.output = ns;
	}

	public String getOutput(){
		 return this.output;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public ExchangePattern getExchangePattern() {
		return ExchangePattern.InOut;
	}

	@Override
	protected void onExchange(Exchange exchange) throws Exception {
		String text = exchange.getIn().getHeader(DocbookConstants.DOCBOOK_SRC, String.class);
		if (text != null) {
			exchange.getIn().removeHeader(DocbookConstants.DOCBOOK_SRC);
		}
		if( text == null){
			text = exchange.getIn().getBody(String.class);
		}

		text = "<article xmlns=\"http://docbook.org/ns/docbook\" xmlns:xl=\"http://www.w3.org/1999/xlink\">"+ text + "</article>";
		Map params = new HashMap();
		DocbookService ds = getDocbookService();
		Message mout = exchange.getOut();
		ByteArrayOutputStream  bos = new ByteArrayOutputStream();
		InputStream is = IOUtils.toInputStream(text, "UTF-8");
		try{
			ds.docbookToPdf( getNamespace(), is, params, bos );
		}finally{
			bos.close();
			is.close();
		}
		mout.setBody(bos.toByteArray());

		mout.setHeaders(exchange.getIn().getHeaders());
		mout.setAttachments(exchange.getIn().getAttachments());
	}

	public DocbookService getDocbookService() {
		return getByType(DocbookService.class);
	}

	private <T> T getByType(Class<T> kls) {
		return kls.cast(getCamelContext().getRegistry().lookupByName(kls.getName()));
	}

	private void debug(String msg) {
		System.out.println(msg);
		m_logger.debug(msg);
	}

	private void info(String msg) {
		System.out.println(msg);
		m_logger.info(msg);
	}

	private static final org.slf4j.Logger m_logger = org.slf4j.LoggerFactory.getLogger(DocbookEndpoint.class);
}
