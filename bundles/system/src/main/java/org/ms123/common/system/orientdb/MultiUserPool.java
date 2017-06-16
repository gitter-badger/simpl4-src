/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014,2017] [Manfred Sattler] <manfred@ms123.org>
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
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

public class MultiUserPool {

	private int maxsize;
	private Map<String,OrientGraphFactory> map = new HashMap<String,OrientGraphFactory>();
	private List<String> list = new ArrayList<String>();
	public MultiUserPool(int size){
		maxsize=size;
	}

	public OrientGraphFactory push( String key, OrientGraphFactory factory ){
		OrientGraphFactory ret = null;
		if( list.size() == maxsize){
			String k = list.remove(maxsize-1);
			ret = map.remove( k );
		}
		list.add(0, key );
		map.put( key, factory );
		return ret;
	}

	public OrientGraphFactory get( String key ){
		OrientGraphFactory factory = map.get(key);
		if( factory != null){
			list.remove( key );
			list.add( 0, key );
		}
		return factory;
	}

	public String toString(){
		return list.toString();
	}
}
