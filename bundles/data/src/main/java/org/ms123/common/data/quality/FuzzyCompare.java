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
import com.wcohen.ss.*;
import com.wcohen.ss.lookup.*;
import com.wcohen.ss.api.*;
import com.wcohen.ss.tokens.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class FuzzyCompare implements Compare {

	private SoftTFIDF m_cmp = null;

	double m_ot;

	double m_lastScore;
	double m_maxScore;

	List m_corbus = new ArrayList();

	List m_fields;

	public FuzzyCompare(String field, double innerThreshold, double outerThreshhold) {
		this(toList(field), innerThreshold, outerThreshhold);
	}

	public FuzzyCompare(List fields, double innerThreshold, double outerThreshhold) {
		info("FuzzyCompare.create:"+fields+"/"+outerThreshhold+"/"+innerThreshold);
		m_fields = fields;
		double it = innerThreshold;
		m_ot = outerThreshhold;
		SimpleTokenizer tokenizer = new SimpleTokenizer(true, true);
		m_cmp = new SoftTFIDF(tokenizer, new JaroWinkler(), it);
	}

	public void addTrainValue(String value) {
		if (value == null || value.length() < 2) {
			return;
		}
		m_corbus.add(m_cmp.prepare(value.toLowerCase()));
	}
	public void reset() {
		m_maxScore=0.0;
	}

	public void init() {
		info(toString() + ":" + m_corbus.size());
		m_cmp.train(new BasicStringWrapperIterator(m_corbus.iterator()));
	}

	public boolean isEquals(Object o1, Object o2) {
		String s1 = getValue(o1);
		String s2 = getValue(o2);
		if (s1.equals(s2)) {
			debug("\tF(" + s1 + " | " + s2 + "):equal");
			m_lastScore = 1.0;
			m_maxScore = 1.0;
			return true;
		} else {
			double d = m_cmp.score(m_cmp.prepare(s1), m_cmp.prepare(s2));
			m_lastScore = d;
			if( d > m_maxScore){
				debug("\tF(" + s1 + " | " + s2 + "):" + d);
			}
			m_maxScore = Math.max( m_maxScore, d);
			if (d > m_ot) {
				return true;
			}
		}
		return false;
	}

	private static List toList(String s) {
		ArrayList list = new ArrayList();
		list.add(s);
		return list;
	}

	public String toString() {
		return "FuzzyCompare(" + m_fields + "," + m_ot + ")";
	}

	private String getValue(Object obj) {
		List<String> fields = m_fields;
		String value = "";
		String blank = "";
		for (String field : fields) {
			Object v = null;
			try {
				v = PropertyUtils.getProperty(obj, field);
			} catch (Exception e) {
				v = null;
			}
			if (v != null) {
				value += blank + v;
				blank = " ";
			}
		}
		return value.toLowerCase().trim();
	}

	public double getLastScore() {
		return m_lastScore;
	}

	private void debug(String message) {
		m_logger.debug(message);
		System.out.println(message);
	}

	private void info(String message) {
		m_logger.info(message);
		System.out.println(message);
	}

	private static final Logger m_logger = LoggerFactory.getLogger(FuzzyCompare.class);
}
