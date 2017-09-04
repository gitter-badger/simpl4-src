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
package org.ms123.common.camel.components.xdocreport;

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
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import java.security.MessageDigest;
import static com.jcabi.log.Logger.info;
import flexjson.*;

@SuppressWarnings("unchecked")
public class XDocReportEndpoint extends ResourceEndpoint {


	private JSONDeserializer ds = new JSONDeserializer();
	private TemplateEngineKind templateEngineKind = TemplateEngineKind.Freemarker;
	private String headerFields;
	private List<Map<String,String>> assignments;
	private String outputformat;
	private String source = null ;
	private String destination = null ;

	public XDocReportEndpoint() {
	}

	public XDocReportEndpoint(String endpointUri, Component component, String resourceUri) {
		super(endpointUri, component, resourceUri);
		info(this,"XDocReportEndpoint:endpointUri:" + endpointUri + "/resourceUri:" + resourceUri);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public ExchangePattern getExchangePattern() {
		return ExchangePattern.InOut;
	}

	public TemplateEngineKind getTemplateEngineKind() {
		return this.templateEngineKind;
	}
	public void setTemplateEngineKind(TemplateEngineKind t) {
		this.templateEngineKind = t;
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

	public void setOutputformat(String t) {
		this.outputformat = t;
	}

	public String getOutputformat() {
		return this.outputformat;
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
		Map<String, Object> variableMap = null;//exchange.getIn().getHeader(XDocReportConstants.XDOCREPORT_DATA, Map.class);
		if (variableMap == null) {
			variableMap = ExchangeUtils.getAssignments(exchange, this.getAssignments());
		}
		if (variableMap == null) {
			//variableMap = ExchangeHelper.createVariableMap(exchange);
			variableMap = new HashMap();
			for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
				if( headerList.size()==0 || headerList.contains( header.getKey())){
					if( header.getValue() instanceof Map  && !"asMap".equals(modMap.get(header.getKey()) )){
						variableMap.putAll((Map)header.getValue());
					}else{
						variableMap.put(header.getKey(), header.getValue());
					}
				}
			}
		}
		info(this,"variableMap:"+ variableMap);
		byte[] bytes = ExchangeUtils.getSource(this.source, exchange, byte[].class);

		InputStream in = new ByteArrayInputStream(bytes);
		IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, this.templateEngineKind);
		IContext context = report.createContext();
		context.putMap(variableMap);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if( "pdf".equals(getOutputformat() )){
			Options options = Options.getTo(ConverterTypeTo.PDF);
			report.convert(context, options, out);
		}else{
			report.process(context, out);
		}
		ExchangeUtils.setDestination(this.destination,out.toByteArray() , exchange);
	}
}
