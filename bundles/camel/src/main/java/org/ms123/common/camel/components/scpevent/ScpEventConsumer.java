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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.ms123.common.camel.api.ExchangeUtils;
import org.ms123.common.system.ssh.SshFileEventListener;
import org.ms123.common.system.ssh.SshService;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@SuppressWarnings({ "unchecked", "deprecation" })
public class ScpEventConsumer extends DefaultConsumer implements SshFileEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(ScpEventConsumer.class);
	private final ScpEventEndpoint endpoint;
	private final SshService sshService;
	private ServiceRegistration m_register;
	private Pattern pattern;

	public ScpEventConsumer(ScpEventEndpoint endpoint, Processor processor, SshService sshService) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		this.sshService = sshService;
		String p = this.endpoint.getFilePattern();
		this.pattern = Pattern.compile(p);
	}

	public void fileCreated(String username, Path filePath, Path vfsRoot, Map<String, Object> params) {
		String fileDest = this.endpoint.getFileDestination();
		Matcher m = this.pattern.matcher(filePath.toString());
		if (!m.matches()) {
			info(this, "fileCreated(" + pattern + ",filePath:" + filePath + "):no match");
			return;
		}
		if (isEmpty(fileDest)) {
			fileDest = "p.filename";
		}
		String vfsRootDest = this.endpoint.getVfsRootDestination();
		if (isEmpty(vfsRootDest)) {
			vfsRootDest = "p.vfsroot";
		}
		info(this, "fileCreated(" + username + ",vfsRoot:" + vfsRoot + ",pattern:" + pattern + ",fileDest:" + fileDest + "):" + filePath);
		final boolean reply = false;
		final Exchange exchange = endpoint.createExchange(reply ? ExchangePattern.InOut : ExchangePattern.InOnly);
		ExchangeUtils.setDestination(fileDest, filePath.toString(), exchange);
		ExchangeUtils.setDestination(vfsRootDest, vfsRoot.toString(), exchange);
		try {
			getAsyncProcessor().process(exchange, new AsyncCallback() {

				@Override
				public void done(boolean doneSync) {
				}
			});
		} catch (Exception e) {
			getExceptionHandler().handleException("Error processing SSH event: " + filePath, exchange, e);
		}
	}

	protected void doStart() throws Exception {
		super.doStart();
		this.sshService.addFileEventListener(this);
	}

	protected void doStop() throws Exception {
		this.sshService.removeFileEventListener(this);
		super.doStop();
	}

}

