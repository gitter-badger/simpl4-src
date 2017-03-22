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

