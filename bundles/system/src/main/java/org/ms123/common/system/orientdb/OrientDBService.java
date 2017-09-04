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
package org.ms123.common.system.orientdb;
import java.util.List;
import java.util.Map;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public interface OrientDBService {
	public OrientGraphFactory getFactory( String db );
	public OrientGraphFactory getFactory( String db, boolean autoStart );
	public OrientGraphFactory getUserFactory(String db );
	public OrientGraph getOrientGraphRoot(String db);
	public OrientGraph getOrientGraph(String db);
	public List<Map<String,Object>> executeQuery(OrientGraph graph, String sql, Object ... args);
	public void executeUpdate(OrientGraph graph, String sql, Object ... args);
}
