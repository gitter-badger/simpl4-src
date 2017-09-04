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
package org.ms123.common.docbook.rendering.eval;

public enum ExpressionEvaluators {

    MVEL(MvelExpressionEvaluator.class), ;

    private Class<? extends ExpressionEvaluator> evaluatorClass;

    private ExpressionEvaluator evaluator;

    private ExpressionEvaluators(
            Class<? extends ExpressionEvaluator> evaluatorClass) {
        this.evaluatorClass = evaluatorClass;
    }

    public ExpressionEvaluator getEvaluator() {

        if (evaluator == null) {
            try {
                evaluator = evaluatorClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Error creating expression evaluator!", e);
            }
        }

        return this.evaluator;
    }

    public static final ExpressionEvaluators lookup(String type) {
        for (ExpressionEvaluators e : ExpressionEvaluators.values()) {
            if (e.name().equalsIgnoreCase(type))
                return e;
        }

        return null;
    }

}
