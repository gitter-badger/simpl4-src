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
package org.ms123.common.rpc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Util class to handle annotation {@link PName}.
 * 
 * @author Karel Hovorka
 * 
 */
public enum ParameterNameUtil {

	INSTANCE;

	private ParameterNameUtil() {
	}

	public static boolean isMethodAnotatedByPName(Method method) {
		if (method.getParameterTypes().length == 0) {
			return false;
		}
		for (Annotation a : method.getParameterAnnotations()[0]) {
			if (a instanceof PName) {
				return true;
			}
		}
		return false;
	}

	public static PName getPName(Method method, int index) {
		if (method.getParameterTypes().length == 0) {
			return null;
		}
		for (Annotation a : method.getParameterAnnotations()[index]) {
			if (a instanceof PName) {
				return (PName) a;
			}
		}
		return null;
	}

	public static boolean isOptional(Method method, int index) {
		if (method.getParameterTypes().length == 0) {
			return false;
		}
		for (Annotation a : method.getParameterAnnotations()[index]) {
			if (a instanceof POptional) {
				return true;
			}
		}
		return false;
	}

	public static Integer getDefaultInt(Method method, int index) {
		if (method.getParameterTypes().length == 0) {
			return null;
		}
		for (Annotation a : method.getParameterAnnotations()[index]) {
			if (a instanceof PDefaultInt) {
				return ((PDefaultInt) a).value();
			}
		}
		return null;
	}

	public static Long getDefaultLong(Method method, int index) {
		if (method.getParameterTypes().length == 0) {
			return null;
		}
		for (Annotation a : method.getParameterAnnotations()[index]) {
			if (a instanceof PDefaultLong) {
				return ((PDefaultLong) a).value();
			}
		}
		return null;
	}

	public static String getDefaultString(Method method, int index) {
		if (method.getParameterTypes().length == 0) {
			return null;
		}
		for (Annotation a : method.getParameterAnnotations()[index]) {
			if (a instanceof PDefaultString) {
				return ((PDefaultString) a).value();
			}
		}
		return null;
	}

	public static Boolean getDefaultBool(Method method, int index) {
		if (method.getParameterTypes().length == 0) {
			return null;
		}
		for (Annotation a : method.getParameterAnnotations()[index]) {
			if (a instanceof PDefaultBool) {
				return ((PDefaultBool) a).value();
			}
		}
		return null;
	}

	public static Double getDefaultFloat(Method method, int index) {
		if (method.getParameterTypes().length == 0) {
			return null;
		}
		for (Annotation a : method.getParameterAnnotations()[index]) {
			if (a instanceof PDefaultFloat) {
				return ((PDefaultFloat) a).value();
			}
		}
		return null;
	}
}
