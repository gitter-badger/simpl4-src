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
import org.hibernate.validator.*;
import javax.validation.*;
import javax.validation.bootstrap.*;

import java.util.*;

@SuppressWarnings("unchecked")
public final class ValidatorFactoryBean {  

	/**
	 * Custom provider resolver is needed since the default provider resolver 
	 * relies on current thread context loader and doesn't find the default 
	 * META-INF/services/.... configuration file 
	 * 
	 */  
	private static class HibernateValidationProviderResolver implements ValidationProviderResolver {  
		@Override  
		public List getValidationProviders() {  
			List providers = new ArrayList(1);  
			providers.add(new HibernateValidator());  
			return providers;  
		}  
	}  

	private final static ValidatorFactory instance;  

	static {  
		ProviderSpecificBootstrap validationBootStrap = Validation.byProvider(HibernateValidator.class);  
		System.out.println("validationBootStrap:"+validationBootStrap);

		validationBootStrap.providerResolver(new HibernateValidationProviderResolver());  
		instance = validationBootStrap.configure().buildValidatorFactory();  
		System.out.println("instance:"+instance);
	}  

	public final static ValidatorFactory getInstance() {  
		return instance;  
	}  
}  

