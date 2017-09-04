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

import java.io.IOException;
import org.apache.camel.Exchange;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 * The Vfs smb producer.
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class VfsSmbProducer extends VfsBaseProducer {

	public VfsSmbProducer(VfsEndpoint endpoint, VfsConfiguration conf) {
		super(endpoint, conf);
		info(this, "VfsSmbProducer create");
	}

	protected boolean isAppendSupported() {
		return true;
	}

	protected String buildConnectionUrl(Exchange exchange) {
		String host = getStringCheck(exchange, "host", this.configuration.getHost());
		String port = getString(exchange, "port", this.configuration.getPort());
		String share = getStringCheck(exchange, "share", this.configuration.getShare());
		if (port == null) {
			return String.format("smb://%s/s%", host, share);
		} else {
			return String.format("smb://%s:%s/%s", host, port, share);
		}
	}
}
