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

import java.util.Set;

public interface FileHolderApi {

	public int getInt(final String section, final String name, final int defaultValue);

	public int getInt(final String section, String subsection, final String name, final int defaultValue);

	public long getLong(String section, String name, long defaultValue);

	public long getLong(final String section, String subsection, final String name, final long defaultValue);

	public boolean getBoolean(final String section, final String name, final boolean defaultValue);

	public boolean getBoolean(final String section, String subsection, final String name, final boolean defaultValue);

	public String getString(final String section, String subsection, final String name);

	public Set<String> getNames(String section);

	public void setInt(final String section, final String subsection, final String name, final int value);

	public void setLong(final String section, final String subsection, final String name, final long value);

	public void setBoolean(final String section, final String subsection, final String name, final boolean value);

	public void setString(final String section, final String subsection, final String name, final String value);
	public String getContent() throws Exception;

	public void putContent(String content) throws Exception; 

	public void putContent(String type, String content) throws Exception;
	public String getType() throws Exception;
}
