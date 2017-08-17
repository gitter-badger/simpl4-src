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
