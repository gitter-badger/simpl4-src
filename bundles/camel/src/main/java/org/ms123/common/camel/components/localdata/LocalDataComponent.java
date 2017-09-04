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
package org.ms123.common.camel.components.localdata;

import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.ms123.common.data.api.DataLayer;
import org.apache.camel.CamelContext;

/**
 * Represents the component that manages {@link LocalDataEndpoint}.
 */
public class LocalDataComponent extends DefaultComponent {

	private DataLayer m_dataLayer;
	private DataLayer m_dataLayerOrientDb;

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		Endpoint endpoint = new LocalDataEndpoint(uri, this);
		setProperties(endpoint, parameters);
		return endpoint;
	}

	@Override
	public void setCamelContext(CamelContext context) {
		super.setCamelContext(context);
		m_dataLayer = getByType(context, DataLayer.class);
		m_dataLayerOrientDb = getByName( context, DataLayer.class, "dataLayerOrientdb");
	}

	private <T> T getByType(CamelContext ctx, Class<T> kls) {
		return kls.cast(ctx.getRegistry().lookupByName(kls.getName()));
	}
	private <T> T getByName(CamelContext ctx, Class<T> kls, String name) {
		return kls.cast(ctx.getRegistry().lookupByName( name ));
	}

	protected DataLayer getDataLayer() {
		return m_dataLayer;
	}
	protected DataLayer getDataLayerOrientDB() {
		return m_dataLayerOrientDb;
	}
}
