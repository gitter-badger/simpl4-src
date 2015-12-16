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
package org.ms123.common.camel.components.vfs;

import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;

/**
 */
public class VfsConsumer extends ScheduledPollConsumer {

	protected VfsEndpoint endpoint;

	protected VfsConfiguration configuration;

	public VfsConsumer(VfsEndpoint endpoint, Processor processor, VfsConfiguration configuration) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		this.configuration = configuration;
	}

	protected int poll() throws Exception {
		return -1;
	}
}
