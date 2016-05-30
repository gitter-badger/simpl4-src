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
import java.util.Collections;
import java.util.Arrays;
import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.io.IOUtils;
import org.ms123.common.docbook.DocbookService;
import org.ms123.common.camel.api.ExchangeUtils;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings("unchecked")
public class DocbookEndpoint extends ResourceEndpoint {

	private String namespace = null ;
	private String output = null ;
	private String source = null ;
	private String destination = null ;
	private String input = "docbook" ;
	private Map<String,String> parameters;
	private String headerFields;

	public DocbookEndpoint() {
	}

	public DocbookEndpoint(String endpointUri, Component component, String resourceUri) {
		super(endpointUri, component, resourceUri);
		info(this,"DocbookEndpoint:endpointUri:" + endpointUri + "/resourceUri:" + resourceUri);
	}

	public void setNamespace(String ns){
		 this.namespace = ns;
	}

	public String getNamespace(){
		 return this.namespace;
	}

	public void setOutput(String o){
		 this.output = o;
	}

	public String getOutput(){
		 return this.output;
	}

	public void setDestination(String o){
		 this.destination = o;
	}

	public String getDestination(){
		 return this.destination;
	}

	public void setInput(String in){
		 this.input = in;
	}

	public String getInput(){
		 return this.input;
	}

	public void setSource(String o){
		 this.source = o;
	}

	public String getSource(){
		 return this.source;
	}

	public void setHeaderfields(String t) {
		this.headerFields = t;
	}

	public String getHeaderfields() {
		return this.headerFields;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public boolean isLenientProperties() {
		return true;
	}

	@Override
	public ExchangePattern getExchangePattern() {
		return ExchangePattern.InOut;
	}

	protected void setParameter(Map<String,Object> p){
		this.parameters = new HashMap<String, String>();
		Map<String, Object> intermediate = (Map)Collections.checkedMap(this.parameters, String.class, String.class);
		intermediate.putAll(p);
	}

	@Override
	protected void onExchange(Exchange exchange) throws Exception {
		String text = ExchangeUtils.getSource(this.source, exchange, String.class);
		DocbookService ds = getDocbookService();
		ByteArrayOutputStream  bos = new ByteArrayOutputStream();
		InputStream is = null;
		try{
			if("docbook".equals(this.input)){
				if( !text.startsWith( "<?xml")  && !text.startsWith("<article")){
					text = "<article xmlns=\"http://docbook.org/ns/docbook\" xmlns:xl=\"http://www.w3.org/1999/xlink\">"+ text + "</article>";
				}
				is = IOUtils.toInputStream(text, "UTF-8");
				ds.docbookToPdf( getNamespace(), is, this.parameters, bos );
			}else{
				is = IOUtils.toInputStream(text, "UTF-8");
				ds.jsonToPdf( getNamespace(), is, getVariablenMap(exchange), bos );
			}
		}finally{
			bos.close();
			if( is != null){
				is.close();
			}
		}
		ExchangeUtils.setDestination(this.destination,bos.toByteArray() , exchange);
	}
	private Map<String,Object> getVariablenMap(Exchange exchange){
		List<String> headerList=null;
		if( this.headerFields!=null){
			headerList = Arrays.asList(this.headerFields.split(","));
		}else{
			headerList = new ArrayList();
		}
		Map<String, Object> variableMap = exchange.getIn().getHeader(DocbookConstants.DOCBOOK_DATA, Map.class);
		if (variableMap == null) {
			variableMap = new HashMap();
			for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
				if( headerList.size()==0 || headerList.contains( header.getKey())){
					if( header.getValue() instanceof Map){
						variableMap.putAll((Map)header.getValue());
					}else{
						variableMap.put(header.getKey(), header.getValue());
					}
				}
			}
		}
		debug(this,"variableMap:"+variableMap);
		return variableMap;
	}

	public DocbookService getDocbookService() {
		return getByType(DocbookService.class);
	}

	private <T> T getByType(Class<T> kls) {
		return kls.cast(getCamelContext().getRegistry().lookupByName(kls.getName()));
	}
}
