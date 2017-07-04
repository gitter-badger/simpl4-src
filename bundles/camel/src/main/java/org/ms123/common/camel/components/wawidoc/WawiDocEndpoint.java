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
package org.ms123.common.camel.components.wawidoc;

import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
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
import org.ms123.common.camel.api.ExchangeUtils;
import org.ms123.common.docbook.DocbookService;
import java.security.MessageDigest;
import static com.jcabi.log.Logger.info;
import flexjson.*;

@SuppressWarnings("unchecked")
public class WawiDocEndpoint extends ResourceEndpoint {

	private JSONDeserializer ds = new JSONDeserializer();
	private List<Map<String,String>> assignments;
	private String outputformat;
	private String namespace;
	private String source = null ;
	private String destination = null ;
	private DocbookService docbookService = null;

	public WawiDocEndpoint() {
	}

	public WawiDocEndpoint(String endpointUri, Component component, String resourceUri) {
		super(endpointUri, component, resourceUri);
		info(this,"WawiDocEndpoint:endpointUri:" + endpointUri + "/resourceUri:" + resourceUri);
		docbookService = getDocbookService();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public ExchangePattern getExchangePattern() {
		return ExchangePattern.InOut;
	}

	public void setAssignments(String a) {
		if (a != null) {
			this.assignments = (List)ds.deserialize(a);
		}
	}

	public List<Map<String,String>> getAssignments() {
		return this.assignments;
	}

	public void setOutputformat(String t) {
		this.outputformat = t;
	}

	public String getOutputformat() {
		return this.outputformat;
	}

	public void setNamespace(String t) {
		this.namespace = t;
	}

	public String getNamespace() {
		return this.namespace;
	}

	private String getNamespace(Exchange exchange) {
		String ns = this.namespace;
		if (ns == null || "".equals(ns) || "-".equals(ns)) {
			ns = exchange.getProperty("_namespace", String.class);
		} else {
			ns = ExchangeUtils.getParameter(this.namespace, exchange, String.class, "namespace");
		}
		return ns;
	}

	public void setDestination(String o){
		 this.destination = o;
	}

	public String getDestination(){
		 return this.destination;
	}

	public void setSource(String o){
		 this.source = o;
	}

	public String getSource(){
		 return this.source;
	}

	@Override
	protected void onExchange(Exchange exchange) throws Exception {
		Map<String, Object> variableMap = ExchangeUtils.getAssignments(exchange, this.getAssignments());
		info(this,"variableMap:"+ variableMap);
		String json = ExchangeUtils.getSource(this.source, exchange, String.class);

		if( "pdf".equals(getOutputformat() )){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			docbookService.wawiToPdf(getNamespace(exchange), json, variableMap, out);
			ExchangeUtils.setDestination(this.destination,out.toByteArray() , exchange);
		}else{
			String fo = docbookService.wawiToFo(getNamespace(exchange), json, variableMap);
			ExchangeUtils.setDestination(this.destination,fo , exchange);
		}
	}

	public DocbookService getDocbookService() {
		return getByType(DocbookService.class);
	}
	private <T> T getByType(Class<T> kls) {
		return kls.cast(getCamelContext().getRegistry().lookupByName(kls.getName()));
	}
}
