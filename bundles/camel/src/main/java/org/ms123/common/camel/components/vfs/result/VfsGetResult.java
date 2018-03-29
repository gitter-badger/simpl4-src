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
package org.ms123.common.camel.components.vfs.result;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.apache.camel.Exchange;

@SuppressWarnings({"unchecked","deprecation"})
public class VfsGetResult extends VfsResult {

	/**
     * Object payload contained in Exchange
     * In case of a single file Exchange Header is populated with the name of the remote path downloaded
     * In case of a multiple files Exchange Header is populated with the name of the remote paths downloaded
     * In case of a single file Exchange Body is populated with the ByteArrayOutputStream downloaded from dropbox.
     * In case of multiple files Exchange Body is populated with a map containing as key the remote path
     * and as value the linked ByteArrayOutputStream
     * @param exchange
     */
	@Override
	public void populateExchange(Exchange exchange) {
		Map<String, ByteArrayOutputStream> map = (Map<String, ByteArrayOutputStream>) resultEntries;
		if (map.size() == 1) {
			String pathExtracted = null;
			ByteArrayOutputStream baosExtracted = null;
			for (Map.Entry<String, ByteArrayOutputStream> entry : map.entrySet()) {
				pathExtracted = entry.getKey();
				baosExtracted = entry.getValue();
			}
			exchange.getIn().setHeader(VfsResultHeader.DOWNLOADED_FILE.name(), pathExtracted);
			exchange.getIn().setHeader("CamelFileName", pathExtracted);
			exchange.getIn().setBody(baosExtracted);
		} else {
			StringBuffer pathsExtracted = new StringBuffer();
			for (Map.Entry<String, ByteArrayOutputStream> entry : map.entrySet()) {
				pathsExtracted.append(entry.getKey() + "\n");
			}
			exchange.getIn().setHeader(VfsResultHeader.DOWNLOADED_FILES.name(), pathsExtracted.toString());
			exchange.getIn().setBody(map);
		}
	}
}
