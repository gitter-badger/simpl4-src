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
package org.ms123.common.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * 
 */
public class DelimiterInputStream extends PushbackInputStream {

	static int MAX_DELIMITER_SIZE = 300;

	/**
	 * @param in
	 */
	public DelimiterInputStream(InputStream in) {
		super(in, MAX_DELIMITER_SIZE);
	}

	/**
	 * reads till delimiter is found
	 * 
	 * @param delimiter
	 * @return the bytes read till the beginning of delimiter
	 * @throws IOException
	 * @throws DelimiterNotFoundException
	 */
	public byte[] readTill(byte[] delimiter) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int posInDelimiter = 0;
		while (true) {
			int ch = read();
			if (ch == -1) {
				return baos.toByteArray();
			}
			if (delimiter != null && ch == delimiter[posInDelimiter]) {
				posInDelimiter++;
				if (posInDelimiter == delimiter.length) {
					return baos.toByteArray();
				}
			} else {
				if (posInDelimiter > 0) {
					unread(ch);
					unread(delimiter, 1, posInDelimiter - 1);
					posInDelimiter = 0;
					ch = delimiter[0];
				}
				baos.write(ch);
			}
		}
	}
}
