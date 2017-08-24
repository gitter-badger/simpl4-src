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
package org.ms123.common.process.listener;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.jcabi.log.Logger.info;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class BaseListener  {

	protected void fillDictionary(Object o,java.util.Map<String,Object> properties, boolean isTask) {
		Class clazz = o.getClass();
		if( isTask ){
			initializeFormKey(o,clazz);
		}
		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			if(m.getParameterTypes().length > 0){
				continue;
			}
			Class returnType = m.getReturnType();
			String getter = m.getName();
			String baseName = getBaseName(getter);
			String prefix = getGetterPrefix(getter);
			if (!Modifier.isStatic(m.getModifiers()) && prefix != null && isPrimitiveOrPrimitiveWrapperOrString(returnType)) {
				try{
					Method method = clazz.getMethod(getter);
					Object value = method.invoke(o);
					if (value != null ) {
						properties.put( baseName, value);
					}
				}catch(Exception e){
					info(this,"Exception("+baseName+"):"+e);
				}
			}
		}
	}
	private boolean isPrimitiveOrPrimitiveWrapperOrString(Class<?> type) {
		return (type.isPrimitive() && type != void.class) || type == Object.class || type == Double.class || type == Float.class || type == Long.class || type == Integer.class || type == Short.class || type == Character.class || type == Byte.class || type == Boolean.class || type == String.class || type == java.util.Date.class || type == byte[].class;
	}

	private void initializeFormKey(Object o, Class clazz){
		try{
			Method method = clazz.getMethod("initializeFormKey");
			method.invoke(o);
		}catch(Exception e){
			info(this,"Exception(initializeFormKey):"+e);
		}
	}

	private String[] getterPrefixes = new String[] { "is", "has", "get" };
	private String getGetterPrefix(String mName) {
		for (String pre : getterPrefixes) {
			if (mName.startsWith(pre)) {
				return pre;
			}
		}
		return null;
	}
	private String getBaseName(String methodName) {
		if (methodName.startsWith("get")) {
			return firstToLower(methodName.substring(3));
		}
		if (methodName.startsWith("has")) {
			return firstToLower(methodName.substring(3));
		}
		if (methodName.startsWith("is")) {
			return firstToLower(methodName.substring(2));
		}
		return firstToLower(methodName);
	}
	private String firstToLower(String s) {
		char c[] = s.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}

}

