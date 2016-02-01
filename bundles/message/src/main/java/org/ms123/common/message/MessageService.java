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
package org.ms123.common.message;
import java.util.Map;
import java.util.List;
import org.ms123.common.rpc.RpcException;

public interface MessageService {
	public Map<String, String> getMessage(String namespace, String lang, String id) throws Exception;
	public List<Map> getMessages(String namespace, String lang, Map filter) throws Exception;
	public void addMessages( String namespace, String lang, List<Map> msgs, Boolean overwrite) throws RpcException;
	public void deleteMessages( String namespace, String lang, String regex, List<String> msgIds) throws RpcException;
}
