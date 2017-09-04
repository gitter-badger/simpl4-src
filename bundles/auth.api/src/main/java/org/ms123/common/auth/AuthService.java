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
package org.ms123.common.auth.api;

import org.ms123.common.rpc.RpcException;
import java.util.Map;
import java.util.List;

public interface AuthService {
	public Map getUserProperties( String id);
	public String getAdminUser();
	public Map getUser(String id);
	public List<Map> getUserList();
	public Map getUserData(String d);
	public List<Map> getUserList(Map filter);
	public List<Map> getUserList(Map filter,int startIndex, int numResults);
	public Map createUser( String userid, Map data);
	public Map deleteUser( String userid);
	public Map getUserByEmail(String email) throws Exception;
	public Map getUserByUserid(String id) throws Exception;
//	public Map requestUser( String userid, Map data);
}
