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
package org.ms123.common.camel.components.scpevent;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.Message;
import org.osgi.framework.ServiceRegistration;
import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.helpers.MessageFormatter;
import java.util.Map;
import org.ms123.common.system.ssh.SshService;
import org.ms123.common.system.ssh.SshFileEventListener;
import org.ms123.common.camel.api.ExchangeUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings({"unchecked","deprecation"})
public class ScpEventConsumer extends DefaultConsumer implements SshFileEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(ScpEventConsumer.class);
	private final ScpEventEndpoint endpoint;
	private ServiceRegistration m_register;

	public ScpEventConsumer(ScpEventEndpoint endpoint, Processor processor, SshService sshService) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		sshService.addFileEventListener( this);
	}

  public void fileCreated(String username, Path path, Path home, Map<String,Object> params) {
		String pattern = this.endpoint.getFilepattern();
		path = Paths.get( home.toString() ,path.toString());
		String pathDest = this.endpoint.getPathDestination();
		info(this,"fileCreated("+username+",home:"+home+",pattern:"+pattern+",dest:"+pathDest+"):"+path);
		final boolean reply = false;
		final Exchange exchange = endpoint.createExchange(reply ? ExchangePattern.InOut : ExchangePattern.InOnly);
		ExchangeUtils.setDestination(pathDest, path.toString(), exchange);
		try {
			getAsyncProcessor().process(exchange, new AsyncCallback() {

				@Override
				public void done(boolean doneSync) {
				}
			});
		} catch (Exception e) {
			getExceptionHandler().handleException("Error processing SSH event: " + path, exchange, e);
		}
	}

	protected void doStart() throws Exception {
		super.doStart();
	}

	protected void doStop() throws Exception {
		super.doStop();
	}

}
