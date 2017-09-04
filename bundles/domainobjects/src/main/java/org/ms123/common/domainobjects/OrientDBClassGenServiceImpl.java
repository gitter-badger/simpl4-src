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
package org.ms123.common.domainobjects;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.Map;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.system.orientdb.OrientDBService;
import org.osgi.framework.BundleContext;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.ms123.common.store.StoreDesc;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "kind=orient,name=classGen" })
public class OrientDBClassGenServiceImpl extends BaseOrientDBClassGenService implements org.ms123.common.domainobjects.api.ClassGenService {

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
	}

	@Reference
	public void setEntityService(EntityService paramEntityService) {
		m_entityService = paramEntityService;
		info(this,"OrientDBClassGenServiceImpl.setEntityService:" + paramEntityService);
	}

	@Reference
	public void setOrientDBService(OrientDBService paramEntityService) {
		m_orientdbService = paramEntityService;
		info(this,"OrientDBClassGenServiceImpl.setOrientDBService:" + paramEntityService);
	}

}

