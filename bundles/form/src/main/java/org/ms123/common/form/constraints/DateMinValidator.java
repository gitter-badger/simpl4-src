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
package org.ms123.common.form.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;
import java.util.Calendar;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Hardy Ferentschik
 */
public class DateMinValidator implements ConstraintValidator<DateMin, Date> {

	private long minValue;
	private int tolerance = 0;

	public void initialize(DateMin minValue) {
		try {
			Date date = new Date(minValue.value());
			this.minValue = getMillis(date) + tolerance;
			System.out.println("DateMinValidator.initialize:" + new Date(this.minValue));
		} catch (Exception nfe) {
			nfe.printStackTrace();
		}
	}

	public boolean isValid(Date value, ConstraintValidatorContext constraintValidatorContext) {
		System.out.println("DateMinValidator.isValid.value:" + value);
		if (value == null) {
			return true;
		}
		long cmpValue = getMillis(value);
		System.out.println("DateMinValidator.isValid:" + (minValue<cmpValue)+"/"+new Date(cmpValue));
		return minValue<cmpValue;
	}
	private long getMillis(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.add(Calendar.MILLISECOND, -1);
		return cal.getTimeInMillis();
	}
}
