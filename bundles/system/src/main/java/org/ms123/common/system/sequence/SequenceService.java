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
package org.ms123.common.system.sequence;
import java.util.List;
import java.util.Map;
import org.ms123.common.rpc.RpcException;

public interface SequenceService {
	public final String REGISTRY_SERVICE = "sequenceService";
	public void  set(String key, String value,Map attributes) throws RpcException;
	public String  get(String key) throws RpcException;
	public List<Map>  getAll(Map attributes) throws RpcException;
	public List<String>  getKeys(Map attributes) throws RpcException;
	public void  delete(String key) throws RpcException;
}
