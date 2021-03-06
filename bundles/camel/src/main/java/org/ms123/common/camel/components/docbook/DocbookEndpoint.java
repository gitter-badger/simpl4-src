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
import flexjson.*;

@SuppressWarnings("unchecked")
public class DocbookEndpoint extends ResourceEndpoint {

	private JSONDeserializer ds = new JSONDeserializer();
	private String namespace = null ;
	private String output = null ;
	private String source = null ;
	private String destination = null ;
	private String input = "docbook" ;
	private Map<String,String> parameters;
	private String headerFields;
	private List<Map<String,String>> assignments;

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
	public void setAssignments(String a) {
		if (a != null) {
			this.assignments = (List)ds.deserialize(a);
		}
	}

	public List<Map<String,String>> getAssignments() {
		return this.assignments;
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
		Map<String, Object> variableMap = ExchangeUtils.getAssignments(exchange, this.getAssignments());
		if( variableMap == null || variableMap.size() == 0){
			List<String> _headerList=null;
			if( this.headerFields!=null){
				_headerList = Arrays.asList(this.headerFields.split(","));
			}else{
				_headerList = new ArrayList();
			}
			Map<String,String> modMap = new HashMap<String,String>();
			List<String> headerList = new ArrayList<String>();
			for( String h : _headerList){
				String[]  _tmp = h.split(":");
				String key = _tmp[0];
				String mod = _tmp.length>1 ? _tmp[1] : "";
				headerList.add(key);
				modMap.put(key,mod);
			}
		
			variableMap = exchange.getIn().getHeader(DocbookConstants.DOCBOOK_DATA, Map.class);
			if (variableMap == null) {
				variableMap = new HashMap();
				for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
					if( headerList.size()==0 || headerList.contains( header.getKey())){
						if( header.getValue() instanceof Map && !"asMap".equals(modMap.get(header.getKey()) )){
							variableMap.putAll((Map)header.getValue());
						}else{
							variableMap.put(header.getKey(), header.getValue());
						}
					}
				}
			}
		}
		info(this,"variableMap:"+variableMap);
		return variableMap;
	}

	public DocbookService getDocbookService() {
		return getByType(DocbookService.class);
	}

	private <T> T getByType(Class<T> kls) {
		return kls.cast(getCamelContext().getRegistry().lookupByName(kls.getName()));
	}
}
