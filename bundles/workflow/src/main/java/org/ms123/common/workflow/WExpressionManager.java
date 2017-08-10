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
package org.ms123.common.workflow;


import org.flowable.engine.delegate.VariableScope;
import org.flowable.engine.impl.el.ExpressionManager;
import org.flowable.engine.impl.el.DefaultExpressionManager;
import org.flowable.engine.impl.el.VariableScopeElResolver;
import org.flowable.engine.common.impl.javax.el.ArrayELResolver;
import org.flowable.engine.common.impl.javax.el.BeanELResolver;
import org.flowable.engine.common.impl.javax.el.CompositeELResolver;
import org.flowable.engine.common.impl.javax.el.ELResolver;
import org.flowable.engine.common.impl.javax.el.ListELResolver;
import org.flowable.engine.common.impl.javax.el.MapELResolver;


/**
 */
public class WExpressionManager extends DefaultExpressionManager {
  
	public WExpressionManager() {
	}

	@Override
	protected ELResolver createElResolver(VariableScope variableScope) {
		CompositeELResolver compositeElResolver = new CompositeELResolver();

		compositeElResolver.add(new VariableScopeElResolver(variableScope));
		compositeElResolver.add(new ImplicitContextElResolver());
		compositeElResolver.add(new ArrayELResolver());
		compositeElResolver.add(new ListELResolver());
		compositeElResolver.add(new MapELResolver());
		compositeElResolver.add(new BeanELResolver());
		return compositeElResolver;
	}

}
