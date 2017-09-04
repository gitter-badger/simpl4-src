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
package org.ms123.common.docbook.rendering;

import org.ms123.common.docbook.rendering.eval.ExpressionEvaluators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import java.util.Map;

public class ExpressionEvaluatingXMLReader extends XMLFilterImpl {

	private static final Logger log = LoggerFactory.getLogger(ExpressionEvaluatingXMLReader.class);

	private Map<String, Object> piContext;

	public ExpressionEvaluatingXMLReader(XMLReader parent, Map<String, Object> piContext) {
		super(parent);
		this.piContext = piContext;
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		ExpressionEvaluators e = ExpressionEvaluators.lookup(target);
		if (e != null) {
			System.out.println("Processing instruction for target = {}, expression = {}" + target + "|" + data);
			String result = e.getEvaluator().evaluate(data, piContext);
			if (result != null) {
				char[] resultArray = result.toString().toCharArray();
				this.characters(resultArray, 0, resultArray.length);
			}
			return;
		}
		super.processingInstruction(target, data);
	}
}
