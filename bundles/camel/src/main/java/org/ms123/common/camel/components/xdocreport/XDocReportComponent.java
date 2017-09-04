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

import java.util.Map;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.ResourceHelper;
import fr.opensagres.xdocreport.template.TemplateEngineKind;

/**
 * @version 
 */
public class XDocReportComponent extends DefaultComponent {

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		XDocReportEndpoint endpoint = new XDocReportEndpoint(uri, this, remaining);
		endpoint.setTemplateEngineKind(TemplateEngineKind.valueOf(remaining));
		endpoint.setHeaderfields((String) parameters.get("headerfields"));
		endpoint.setOutputformat((String) parameters.get("outputformat"));
		setProperties(endpoint, parameters);
		return endpoint;
	}
}
