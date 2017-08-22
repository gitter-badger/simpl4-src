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
 * @author Ronny Bräunlich
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class BaseListener  {

	protected void fillDictionary(Object o,Dictionary properties, boolean isTask) {
		Class clazz = o.getClass();
		if( isTask ){
			initializeFormKey(o,clazz);
		}
		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			Class returnType = m.getReturnType();
			String getter = m.getName();
			String baseName = getBaseName(getter);
			String prefix = getGetterPrefix(getter);
			if (!Modifier.isStatic(m.getModifiers()) && prefix != null && isPrimitiveOrPrimitiveWrapperOrString(returnType)) {
				Map<String, Object> map = new HashMap<String, Object>();
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
		return (type.isPrimitive() && type != void.class) || type == Double.class || type == Float.class || type == Long.class || type == Integer.class || type == Short.class || type == Character.class || type == Byte.class || type == Boolean.class || type == String.class || type == java.util.Date.class || type == byte[].class;
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

