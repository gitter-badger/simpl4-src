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
package org.ms123.common.utils;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

/**
 * A MultiHashMap is a HashMap capable of holding multiple objects per key. 
 * If a key has only one object mapped to it, get returns that object; 
 * if the key has multiple objects mapped to it, get returns a list. 
 * The getArray method returns an array in all cases; 
 * if the key has only object mapped to it, the length of the array will be 1.
 */

@SuppressWarnings("unchecked")
public class MultiHashMap extends HashMap {

	//
	// constructors
	//
	public MultiHashMap() {
		super();
	}

	public MultiHashMap(int nInitialCapacity) {
		super(nInitialCapacity);
	}

	public MultiHashMap(int nInitialCapacity, float fLoadFactor) {
		super(nInitialCapacity, fLoadFactor);
	}

	public MultiHashMap(Map oMap) {
		super(oMap);
	}

	//
	// methods
	//

	/**
	 * Puts a new element into the HashMap.
	 * If the given key already exists in the HashMap
	 * the new element will be assign to that key as well.
	 * A linked list is used to store all elements
	 * assign to a key.
	 * If the new element is an instance of a collection
	 * class all elements of the collection will be assigned
	 * to the key.
	 */
	public Object put(Object oKey, Object oNewElement) {
		LinkedList oElements;

		if (containsKey(oKey) == true) {
			oElements = (LinkedList) super.get(oKey);
		} else {
			oElements = new LinkedList();
		}

		if (oNewElement instanceof Collection) {
			oElements.addAll((Collection) oNewElement);
		} else {
			oElements.add(oNewElement);
		}

		super.put(oKey, oElements);

		return null;
	}

	/**
	 * Returns the value or values to which this map maps the specified key.
	 */
	public Object get(Object key) {
		List list = (List) super.get(key);
		if (list == null)
			return null;

		if (list.size() == 1) {
			return list.get(0);
		}
		return list;
	}

	/**
	 * Returns the values to which this map maps the specified key.
	 */
	public Object getList(Object key) {
		List list = (List) super.get(key);
		if (list == null)
			return new java.util.ArrayList();
		return list;
	}

	/**
	 * Returns the number of values mapped to a key.
	 */
	public int getValueCount(Object key) {
		List list = (List) super.get(key);
		if (list == null)
			return 0;
		return list.size();
	}

	/**
	 * Returns the value or values to which this map maps the specified key.
	 */
	public Object[] getArray(Object key, Object[] a) {
		List list = (List) super.get(key);
		if (list == null) {
			return (Object[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), 0);
		}
		return list.toArray(a);
	}

	/**
	 * Returns the value or values to which this map maps the specified key.
	 */
	public Object[] getArray(Object key) {
		List list = (List) super.get(key);
		if (list == null)
			return new Object[0];
		return list.toArray();
	}
}

