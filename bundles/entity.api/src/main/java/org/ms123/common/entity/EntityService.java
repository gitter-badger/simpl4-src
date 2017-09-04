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
package org.ms123.common.entity.api;

import java.util.Map;
import java.util.List;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.rpc.RpcException;

public interface EntityService {
	/*Base*/
	public Map getPermittedFields(StoreDesc sdesc, String entityName, String actions);
	public Map getPermittedFields(StoreDesc sdesc, String entityName);
	public List<Map> getPrimaryKeyFields(StoreDesc sdesc, String entityName);
	public List getEntities(StoreDesc sdesc, Boolean withChilds, String mappingstr) throws Exception;
	public Map getEntityTree( StoreDesc sdesc, String mainEntity, int maxlevel, Boolean pathid, String type, Boolean listResolved) throws Exception;
	public List<Map> getFields( StoreDesc  sdesc, String entityName,Boolean withAutoGen) throws Exception;
	public List<Map> getFields( StoreDesc  sdesc, String entityName,Boolean withAutoGen,Boolean withRelations) throws Exception;
	public List<Map> getEntitytypeInfo( String storeId, List<String> names) throws RpcException;

	/*not needed in orientdb*/
	public List<Map> getDefaultFields();
	public List<Map> getTeamFields();
	public List<Map> getStateFields();

	/*gitMetaData*/
	public List<Map> getRelations(StoreDesc sdesc) throws Exception;
	public void saveRelations(StoreDesc sdesc, List<Map> relations) throws Exception;
	public void saveEntitytype( String storeId, String name, Map<String, Object> data) throws RpcException;
	public void deleteEntitytype( String storeId, String name) throws RpcException;
	public void deleteEntitytypes( String storeId) throws RpcException;
	public List<Map> getEntitytypes(String storeId) throws RpcException;
	public Map<String, Object> getEntitytype(String storeId,  String name) throws RpcException;

	/*GraphCreator*/
	public Map createEntitytypes( String storeId, String dataMapperConfigName, Map dataMapperConfig, List<Map> strategy, String side,Boolean infoOnly) throws RpcException;
}
