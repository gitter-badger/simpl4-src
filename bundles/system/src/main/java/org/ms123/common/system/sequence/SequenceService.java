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
