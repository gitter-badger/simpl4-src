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
package org.ms123.common.data.query;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.beanutils.*;
import java.lang.reflect.*;
import java.lang.annotation.*;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.data.api.SessionContext;

@SuppressWarnings({"unchecked","deprecation"})
public class JPASelectBuilderH2 extends JPASelectBuilderPostgresql implements SelectBuilder {

	private static final Logger m_logger = LoggerFactory.getLogger(JPASelectBuilderH2.class);

	public JPASelectBuilderH2(QueryBuilder qb, StoreDesc sdesc, String entityName, List<String> joinFields, Map filters, Map fieldSets) {
		super(qb, sdesc, entityName, joinFields, filters, fieldSets);
	}

	protected String getContains(String f, String d, String dt) {
		if( "".equals(d)){
			return f +" is null or " +f + ".regexCI(\"" + d + "\")";
		}else{
			return f + ".regexCI(\"" + d + "\")";
		}
	}
}
