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
package org.ms123.common.process.camel;

import flexjson.*;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.Processor;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.warn;

@SuppressWarnings({"unchecked","deprecation"})
public class ProcessConsumer extends DefaultConsumer  {

	private JSONDeserializer ds = new JSONDeserializer();
	private JSONSerializer ser = new JSONSerializer();
	private ProcessEndpoint endpoint;

	public ProcessConsumer(ProcessEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
	}

	public boolean isFailOnException() {
		return false;
	}

  @Override
	protected void doStart() throws Exception {
	}

  @Override
	protected void doStop() throws Exception {
	}
	private boolean isEmpty(String s) {
		return (s == null || "".equals(s.trim()));
	}
	private String getVariableType(String s) {
		int lastIndexDot = s.lastIndexOf(".");
		int lastIndexAt = s.lastIndexOf("@");
		if( lastIndexAt == -1){
			lastIndexDot = s.length();
		}
		return s.substring(lastIndexDot+1,lastIndexAt);
	}
}
