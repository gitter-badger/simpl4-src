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

import java.io.InputStream;
import org.osgi.framework.BundleContext;
import org.ms123.common.git.GitService;

public class PDFRenderer extends FORenderer<PDFRenderer> {

	@Override
	protected String getMimeType() {
		return "application/pdf";
	}

	public PDFRenderer(BundleContext bc, GitService gs, String namespace) {
		m_bundleContect = bc;
		m_gitService = gs;
		m_namespace = namespace;
	}

	public static final synchronized PDFRenderer create(BundleContext bc) {
		return new PDFRenderer(bc, null, null);
	}

	public static final synchronized PDFRenderer create(BundleContext bc, GitService gs, String namespace, InputStream xmlResource) {
		return new PDFRenderer(bc, gs, namespace).xml(xmlResource);
	}
}
