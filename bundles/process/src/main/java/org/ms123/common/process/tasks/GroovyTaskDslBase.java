/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014,2017] [Manfred Sattler] <manfred@ms123.org>
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
package org.ms123.common.process.tasks;

import flexjson.*;
import groovy.lang.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.info;

@SuppressWarnings("unchecked")
public abstract class GroovyTaskDslBase extends Script {

	protected JSONSerializer m_js = new JSONSerializer();
	protected JSONDeserializer m_ds = new JSONDeserializer();

	public String dec2(Object o) {
		if (o instanceof Integer) {
			o = ((Integer) o).floatValue();
		}
		return String.format("%1$,.2f", o);
	}

	public String today(Object o) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formater = new SimpleDateFormat();
		return formater.format(cal.getTime());
	}

	public long toDays(long duration) {
		return TimeUnit.MILLISECONDS.toDays(duration);
	}

	public long toHours(long duration) {
		return TimeUnit.MILLISECONDS.toHours(duration);
	}

	public long toMicros(long duration) {
		return TimeUnit.MILLISECONDS.toMicros(duration);
	}

	public long toMillis(long duration) {
		return TimeUnit.MILLISECONDS.toMillis(duration);
	}

	public long toMinutes(long duration) {
		return TimeUnit.MILLISECONDS.toMinutes(duration);
	}

	public long toNanos(long duration) {
		return TimeUnit.MILLISECONDS.toNanos(duration);
	}

	public long toSeconds(long duration) {
		return TimeUnit.MILLISECONDS.toSeconds(duration);
	}

	private EventAdmin getEventAdmin() {
		return (EventAdmin) this.getBinding().getVariable("__eventAdmin");
	}

	private String getNamespace() {
		return (String) this.getBinding().getVariable("__namespace");
	}

	private String getProcessDefinitionKey() {
		return (String) this.getBinding().getVariable("__processDefinitionKey");
	}

	private String getPid() {
		return (String) this.getBinding().getVariable("__pid");
	}

	public boolean isListEmpty(List l1) {
		if (l1 != null && l1.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isListInList(Object o1, Object o2) {
		return isListInList(o1, o2, "id");
	}

	public boolean isListInList(Object o1, Object o2, String key) {
		System.out.println("contains:" + o1 + "/" + o2 + "/" + key);
		if (o1 instanceof List && o2 instanceof List) {
			List l1 = (List) o1;
			List l2 = (List) o2;
			if (l1.size() == 0) {
				return true;
			}
			if (l2.size() == 0) {
				return false;
			}
			if (l1.get(0) instanceof Map) {
				for (Map m1 : (List<Map>) l1) {
					Object val1 = (Object) m1.get(key);
					boolean ok = false;
					for (Map m2 : (List<Map>) l2) {
						Object val2 = (Object) m2.get(key);

						if (val1 == null && val2 == null) {
							ok = true;
						} else if (val1 == null || val2 == null) {
							ok = false;
						} else if (val1.equals(val2)) {
							ok = true;
						}
					}
					if (!ok) {
						System.out.println("l2:" + l2 + "contains not " + l1);
						return false;
					}
				}
			}
			if (l1.get(0) instanceof String) {
				for (String s1 : (List<String>) l1) {
					boolean ok = false;
					for (String s2 : (List<String>) l2) {
						if (s1.equals(s2)) {
							ok = true;
						}
					}
					if (!ok) {
						System.out.println("l2:" + l2 + "contains not " + l1);
						return false;
					}
				}
			}
			System.out.println("l2:" + l2 + "contains " + l1);
			return true;
		}
		return false;
	}

	public void log(String message) {
		info(this, message);
		System.out.println(message);
	}
}

