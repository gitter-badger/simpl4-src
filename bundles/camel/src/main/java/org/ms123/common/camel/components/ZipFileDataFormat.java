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
package org.ms123.common.camel.components;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.AntPathMatcher;
import org.apache.camel.util.StringHelper;
import org.apache.camel.dataformat.zipfile.ZipIterator;
import static org.apache.camel.Exchange.FILE_NAME;

/**
 * Zip file data format.
 * See {@link org.apache.camel.model.dataformat.ZipDataFormat} for "deflate" compression.
 */
public class ZipFileDataFormat implements DataFormat {

	private boolean usingIterator;
	private boolean oneToBody;
	private String pattern;

	public void setUsingIterator(boolean usingIterator) {
		this.usingIterator = usingIterator;
	}
	public void setOneToBody(boolean one) {
		this.oneToBody = one;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
		String filename = exchange.getIn().getHeader(FILE_NAME, String.class);
		if (filename != null) {
			filename = new File(filename).getName();
		} else {
			// generate the file name as the camel file component would do
			filename = StringHelper.sanitize(exchange.getIn().getMessageId());
		}
		ZipOutputStream zos = new ZipOutputStream(stream);
		zos.putNextEntry(new ZipEntry(filename));
		InputStream is = exchange.getContext().getTypeConverter().mandatoryConvertTo(InputStream.class, graph);
		try {
			IOHelper.copy(is, zos);
		} finally {
			IOHelper.close(is, zos);
		}
		String newFilename = filename + ".zip";
		exchange.getOut().setHeader(FILE_NAME, newFilename);
	}

	@Override
	public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
		if (usingIterator) {
			return new ZipIterator(exchange.getIn());
		}else{
			AntPathMatcher pm = new AntPathMatcher();
			Map<String,ByteArrayOutputStream> entries = new HashMap<String,ByteArrayOutputStream>();
			InputStream is = exchange.getIn().getMandatoryBody(InputStream.class);
			ZipInputStream zis = new ZipInputStream(is);
			ByteArrayOutputStream last=null;
			try {
				while(true){
					ZipEntry entry = zis.getNextEntry();
					if (entry == null) {
						break;
					}
					String name = entry.getName();
					name = name.replaceAll("\\\\", "/");
					if (entry.isDirectory()) {
						continue;
					}
					if( pattern != null && !pm.match( pattern, name)){
						continue;
					}
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					last = baos;
					IOHelper.copy(zis, baos);
					baos.close();
					entries.put( entry.getName(), baos);
				}
			}finally{
				zis.close();
			}
			if( entries.size() == 1 && this.oneToBody){
				return last.toByteArray();
			}
			return entries;
		}
	}
}
