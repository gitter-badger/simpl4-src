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
package org.ms123.common.data.quality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ms123.common.data.*;
import org.apache.commons.beanutils.*;
import flexjson.*;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.setting.api.SettingService;

@SuppressWarnings("unchecked")
public class BasicQualityService {

	private final String ENTITY = "entity";

	protected NucleusService m_nucleusService;

	protected SettingService m_settingService;

	protected DataLayer m_dataLayer;

	private Map<String, QualityBatch> m_batches = new HashMap();

	protected List<Map> _dupCheck(String namespace, String entityName, List<Map> candidateList, String state, String id, boolean dry) throws Exception {
		QualityBatch b = m_batches.get(namespace + "_" + entityName);
		if (b == null) {
			b = new QualityBatch(namespace, entityName, m_dataLayer, m_settingService, m_nucleusService);
			m_batches.put(namespace + "_" + entityName, b);
		}
		if (candidateList != null) {
			return b.doCheckFromData(candidateList);
		} else {
			return b.doCheckFromDb(state, id, dry);
		}
	}

	protected void debug(String message) {
		m_logger.debug(message);
	}

	protected void info(String message) {
		m_logger.info(message);
		System.out.println(message);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(QualityServiceImpl.class);
}
