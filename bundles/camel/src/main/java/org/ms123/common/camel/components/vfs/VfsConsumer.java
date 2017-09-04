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
