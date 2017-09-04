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

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.beanutils.PropertyUtils;

public class RelatedCompare implements Compare, Constants {

	private String m_field = null;

	public RelatedCompare(String field) {
		m_field = field;
	}

	public void init() {
	}
	public void reset() {
	}

	public boolean isEquals(Object o1, Object o2) {
		String s1 = getId(getValue(o1));
		String s2 = getId(getValue(o2));
		if (s1.equals(s2)) {
			//debug("\tR(" + s1 + "," + s2 + "):true");
			return true;
		}
		return false;
	}

	private Object getValue(Object obj) {
		Object v = null;
		try {
			v = PropertyUtils.getProperty(obj, m_field);
		} catch (Exception e) {
		}
		return v;
	}

	private String getId(Object o) {
		if (o == null) {
			return "NULL";
		}
		try {
			return (String) PropertyUtils.getProperty(o, ID);
		} catch (Exception e) {
			info("RelatedCompare.getId:"+o+"/failed");
		}
		return "";
	}

	private void debug(String message) {
		m_logger.debug(message);
		System.out.println(message);
	}

	private void info(String message) {
		m_logger.info(message);
		System.out.println(message);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(RelatedCompare.class);
}
