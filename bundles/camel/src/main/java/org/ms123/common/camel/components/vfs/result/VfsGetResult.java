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
package org.ms123.common.camel.components.vfs.result;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.apache.camel.Exchange;

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
