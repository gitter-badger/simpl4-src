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

package org.ms123.common.system.ftp;

import java.util.Map;
import java.util.HashMap;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.UserManagerFactory;

/**
 */
public class Simpl4UserManagerFactory implements UserManagerFactory {

	private Map<String, Map<String, String>> userMap = new HashMap<String, Map<String, String>>();

	public UserManager createUserManager() {
		return new Simpl4UserManager(this.userMap);
	}

	public Map<String, Map<String, String>> getUserMap() {
		return this.userMap;
	}

	public void setUserMap(Map<String, Map<String, String>> userMap) {
		this.userMap = userMap;
	}

}

