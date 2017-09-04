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
